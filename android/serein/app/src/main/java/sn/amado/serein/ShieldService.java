package sn.amado.serein;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javax.net.ssl.HttpsURLConnection;

public class ShieldService extends VpnService {
    public static volatile boolean running=false, starting=false;
    public static volatile String problem="";
    private ParcelFileDescriptor tun;
    private FileInputStream input;
    private FileOutputStream output;
    private Thread reader, startup;
    private ThreadPoolExecutor workers;
    public static volatile boolean remoteReady=false;
    private ConnectivityManager.NetworkCallback networkWatch;
    private final Set<HttpsURLConnection> connections=Collections.synchronizedSet(new HashSet<>());
    private volatile boolean closing=false;
    private final Object lifecycle=new Object();
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        if(intent!=null && "STOP".equals(intent.getAction())){getSharedPreferences("serein",0).edit().putBoolean("vpn_enabled",false).apply();stopSelf();return START_NOT_STICKY;}
        if(intent==null && !getSharedPreferences("serein",0).getBoolean("vpn_enabled",false)){stopSelf();return START_NOT_STICKY;}
        getSharedPreferences("serein",0).edit().putBoolean("vpn_enabled",true).apply();
        if(running || starting)return START_STICKY;
        problem="";closing=false;starting=true;remoteReady=false;
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("shield","Protection Serein",NotificationManager.IMPORTANCE_LOW));
        nm.createNotificationChannel(new NotificationChannel("errors","État du filtre",NotificationManager.IMPORTANCE_DEFAULT));
        nm.cancel(2);
        startForeground(1,notification("Serein • activation locale","Chargement de la liste noire enregistrée.","shield"));
        startup=new Thread(()->{
            try {
                Network network=null;try{network=physicalNetwork();}catch(IOException offline){}
                // Establish locally even in airplane mode; never gate the blacklist on a server.
                synchronized(lifecycle){
                    if(closing)return;
                    tun=new Builder().setSession("Serein — filtre adulte").addAddress("10.111.0.1",32)
                        .addDnsServer("10.111.0.2").addRoute("10.111.0.2",32)
                        .allowFamily(android.system.OsConstants.AF_INET6).setMtu(1500).setBlocking(true)
                        .setUnderlyingNetworks(network==null?null:new Network[]{network}).establish();
                    if(tun==null)throw new IOException("VPN permission missing");
                    input=new FileInputStream(tun.getFileDescriptor());output=new FileOutputStream(tun.getFileDescriptor());
                    workers=new ThreadPoolExecutor(8,8,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(64));
                    running=true;starting=false;
                    reader=new Thread(this::readLoop,"serein-dns");reader.start();
                    updateMode(false);watchNetworks();
                }
            }catch(Exception e){ if(!closing)haltWithWarning("Activation du VPN impossible : protection DNS désactivée. Vérifie l’autorisation VPN."); }
            finally{starting=false;}
        },"serein-start");
        startup.start();return START_STICKY;
    }
    private void updateMode(boolean ready){
        if(closing)return;boolean changed=remoteReady!=ready;remoteReady=ready;
        problem="";
        if(changed||running)getSystemService(NotificationManager.class).notify(1,notification(ready?"Serein • filtrage DNS actif":"Serein • liste noire locale active",ready?"Liste personnelle + filtre adulte HTTPS":"Protection locale maintenue ; filtre distant en attente.","shield"));
    }
    private void watchNetworks(){
        networkWatch=new ConnectivityManager.NetworkCallback(){
            @Override public void onAvailable(Network n){if(!closing)try{setUnderlyingNetworks(new Network[]{physicalNetwork()});}catch(Exception ignored){}}
            @Override public void onLost(Network n){if(!closing){updateMode(false);try{setUnderlyingNetworks(new Network[]{physicalNetwork()});}catch(Exception e){setUnderlyingNetworks(null);}}}
        };
        getSystemService(ConnectivityManager.class).registerNetworkCallback(new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN).build(),networkWatch);
    }
    private Notification notification(String title,String body,String channel){
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this,channel).setSmallIcon(sn.amado.serein.R.drawable.shield).setContentTitle(title).setContentText(body).setStyle(new Notification.BigTextStyle().bigText(body)).setContentIntent(open).setOngoing("shield".equals(channel)).build();
    }
    private Network physicalNetwork()throws IOException{
        ConnectivityManager cm=getSystemService(ConnectivityManager.class);Network candidate=null;
        for(Network n:cm.getAllNetworks()){
            NetworkCapabilities c=cm.getNetworkCapabilities(n);
            if(c!=null && c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && c.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)){
                if(c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))return n;
                candidate=n;
            }
        }
        if(candidate==null)throw new IOException("No physical network");return candidate;
    }
    private byte[] query(byte[] q,Network network)throws IOException{
        // Network.openConnection binds HTTPS and its bootstrap DNS to Wi-Fi/mobile,
        // never to our own virtual resolver. It also uses the normal HTTPS port 443.
        final HttpsURLConnection[] current=new HttpsURLConnection[1];
        try{return DnsHttps.query(q,url->{
            HttpsURLConnection c=(HttpsURLConnection)network.openConnection(url);
            synchronized(connections){if(closing){c.disconnect();throw new IOException("Service stopped");}connections.add(c);current[0]=c;}
            return c;
        });}finally{if(current[0]!=null)connections.remove(current[0]);}
    }
    private void haltWithWarning(String message){
        synchronized(lifecycle){
            if(closing)return;
            problem=message;closing=true;running=false;starting=false;getSharedPreferences("serein",0).edit().putBoolean("vpn_enabled",false).apply();
            try{if(tun!=null)tun.close();}catch(Exception ignored){}
        }
        getSystemService(NotificationManager.class).notify(2,notification("Serein • protection désactivée",message,"errors"));stopSelf();
    }
    private void readLoop(){
        try{
            byte[] b=new byte[32767];int n;
            while(!closing && (n=input.read(b))>0){
                byte[] p=Arrays.copyOf(b,n);int off=DnsPacket.offset(p,n);if(off<0)continue;
                byte[] q=Arrays.copyOfRange(p,off,n);
                if(DnsPacket.question(q).isEmpty()){send(p,off,DnsPacket.error(q,1));continue;}
                try{workers.execute(()->resolve(p,off,q));}catch(RejectedExecutionException e){send(p,off,DnsPacket.error(q,2));}
            }
            if(!closing)throw new IOException("Tunnel closed");
        }catch(Exception e){if(!closing)haltWithWarning("VPN interrompu : protection désactivée. Réactive Serein pour rétablir le filtre.");}
    }
    private void resolve(byte[] p,int off,byte[] q){
        if(closing)return;
        if(Policy.blocked(DnsPacket.question(q),getSharedPreferences("serein",0).getString("domains",""))){send(p,off,DnsPacket.error(q,3));return;}
        try{
            Network network=physicalNetwork();byte[] reply=query(q,network);
            if(closing)return;
            setUnderlyingNetworks(new Network[]{network});if(!remoteReady)updateMode(true);
            // Recheck changes made while an upstream request was in flight.
            if(Policy.blocked(DnsPacket.question(q),getSharedPreferences("serein",0).getString("domains","")))send(p,off,DnsPacket.error(q,3));else send(p,off,reply);
        }catch(Exception e){
            send(p,off,DnsPacket.error(q,2));
            // A failed upstream never tears down the local blacklist or changes enabled state.
            if(!closing&&remoteReady)updateMode(false);
        }
    }
    private void send(byte[] p,int off,byte[] r){
        if(closing)return;
        try{synchronized(this){if(output!=null)output.write(DnsPacket.reply(p,off,r));}}catch(Exception ignored){}
    }
    @Override public void onRevoke(){getSharedPreferences("serein",0).edit().putBoolean("vpn_enabled",false).apply();problem="Autorisation VPN retirée ou autre VPN activé.";stopSelf();}
    @Override public void onDestroy(){
        synchronized(lifecycle){closing=true;running=false;starting=false;remoteReady=false;try{if(tun!=null)tun.close();}catch(Exception ignored){}}
        if(networkWatch!=null)try{getSystemService(ConnectivityManager.class).unregisterNetworkCallback(networkWatch);}catch(Exception ignored){}
        if(workers!=null)workers.shutdownNow();
        synchronized(connections){for(HttpsURLConnection c:connections)c.disconnect();connections.clear();}
        try{if(input!=null)input.close();}catch(Exception ignored){}
        try{if(output!=null)output.close();}catch(Exception ignored){}
        if(reader!=null)reader.interrupt();if(startup!=null)startup.interrupt();
        stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();
    }
}
