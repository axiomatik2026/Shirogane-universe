package sn.amado.serein;

import android.app.*;
import android.content.*;
import android.net.VpnService;
import android.os.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.*;

public class ShieldService extends VpnService {
    public static volatile boolean running=false;
    public static volatile String problem="";
    private ParcelFileDescriptor tun;
    private FileInputStream input;
    private FileOutputStream output;
    private Thread reader;
    private ThreadPoolExecutor workers;
    private final Set<Socket> sockets=Collections.synchronizedSet(new HashSet<>());
    private volatile boolean closing=false;
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        if(intent!=null && "STOP".equals(intent.getAction())) { stopSelf(); return START_NOT_STICKY; }
        if(running) return START_NOT_STICKY;
        problem=""; closing=false;
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("shield","Protection Serein",NotificationManager.IMPORTANCE_LOW));
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);
        Notification n=new Notification.Builder(this,"shield").setSmallIcon(sn.amado.serein.R.drawable.shield)
            .setContentTitle("Serein • filtrage DNS actif").setContentText("Domaines adultes et liste personnelle filtrés").setContentIntent(open).setOngoing(true).build();
        startForeground(1,n);
        try {
            tun=new Builder().setSession("Serein — DNS familial").addAddress("10.111.0.1",32)
                .addDnsServer("10.111.0.2").addRoute("10.111.0.2",32).allowFamily(android.system.OsConstants.AF_INET6).setMtu(1500).setBlocking(true).establish();
            if(tun==null) throw new IOException("Autorisation VPN absente");
            input=new FileInputStream(tun.getFileDescriptor()); output=new FileOutputStream(tun.getFileDescriptor());
            workers=new ThreadPoolExecutor(2,4,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(64));
            running=true;
            reader=new Thread(this::readLoop,"serein-dns"); reader.start();
        } catch(Exception e) { problem="Impossible d’activer le VPN. Réessaie après avoir arrêté l’autre VPN."; stopSelf(); }
        return START_NOT_STICKY;
    }
    private void readLoop() {
        try {
            byte[] b=new byte[32767]; int n;
            while(!closing && (n=input.read(b))>0) {
                byte[] p=Arrays.copyOf(b,n); int off=DnsPacket.offset(p,n);
                if(off<0) continue;
                byte[] q=Arrays.copyOfRange(p,off,n);
                if(DnsPacket.question(q).isEmpty()) { send(p,off,DnsPacket.error(q,1)); continue; }
                try { workers.execute(()->resolve(p,off,q)); }
                catch(RejectedExecutionException e) { send(p,off,DnsPacket.error(q,2)); }
            }
            if(!closing) throw new IOException("Interface fermée");
        } catch(Exception e) { if(!closing) { problem="Protection interrompue : réactive le VPN."; stopSelf(); } }
    }
    private void resolve(byte[] p,int off,byte[] q) {
        String host=DnsPacket.question(q);
        String custom=getSharedPreferences("serein",0).getString("domains","");
        if(Policy.blocked(host,custom)) { send(p,off,DnsPacket.error(q,3)); return; }
        for(String ip:new String[]{"185.228.168.168","185.228.169.168"}) {
            if(closing) return;
            Socket raw=new Socket(); sockets.add(raw);
            try {
                if(!protect(raw)) throw new IOException("Socket protection failed");
                raw.connect(new InetSocketAddress(ip,853),3500); raw.setSoTimeout(4500);
                try(SSLSocket tls=(SSLSocket)((SSLSocketFactory)SSLSocketFactory.getDefault()).createSocket(raw,"family-filter-dns.cleanbrowsing.org",853,true)) {
                    SSLParameters params=tls.getSSLParameters(); params.setEndpointIdentificationAlgorithm("HTTPS"); tls.setSSLParameters(params);
                    tls.startHandshake();
                    DataOutputStream out=new DataOutputStream(tls.getOutputStream()); out.writeShort(q.length); out.write(q); out.flush();
                    DataInputStream in=new DataInputStream(tls.getInputStream()); int len=in.readUnsignedShort();
                    if(len<12 || len>32000) throw new IOException("Invalid DNS length");
                    byte[] r=new byte[len]; in.readFully(r);
                    if(r[0]!=q[0] || r[1]!=q[1] || (r[2]&0x80)==0) throw new IOException("Invalid DNS response");
                    problem=""; send(p,off,r); return;
                }
            } catch(Exception ignored) { } finally { sockets.remove(raw); try { raw.close(); } catch(Exception ignored) {} }
        }
        if(!closing) { problem="DNS familial injoignable : nouvelles résolutions refusées. Vérifie le réseau (port 853)."; send(p,off,DnsPacket.error(q,2)); }
    }
    private void send(byte[] p,int off,byte[] r) {
        if(closing) return;
        try { synchronized(this) { if(output!=null) output.write(DnsPacket.reply(p,off,r)); } }
        catch(Exception ignored) { }
    }
    @Override public void onRevoke() { problem="Autorisation VPN retirée ou autre VPN activé."; stopSelf(); }
    @Override public void onDestroy() {
        closing=true; running=false;
        if(workers!=null) workers.shutdownNow();
        synchronized(sockets) { for(Socket s:sockets) try{s.close();}catch(Exception ignored){} sockets.clear(); }
        try { if(tun!=null) tun.close(); } catch(Exception ignored) {}
        try { if(input!=null) input.close(); } catch(Exception ignored) {}
        try { if(output!=null) output.close(); } catch(Exception ignored) {}
        if(reader!=null) reader.interrupt();
        stopForeground(STOP_FOREGROUND_REMOVE); super.onDestroy();
    }
}
