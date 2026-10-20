package sn.amado.downloadguard;

import android.app.*;
import android.content.*;
import android.os.*;

/** Visible lifetime of the local control guard; no network requests or periodic wakeups. */
public class MonitorService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static volatile boolean running=false;
    @Override public void onCreate(){super.onCreate();State.prefs(this).registerOnSharedPreferenceChangeListener(this);channels(this);}
    static void channels(Context c){NotificationManager m=c.getSystemService(NotificationManager.class);m.createNotificationChannel(new NotificationChannel("watch","Protection téléchargements",NotificationManager.IMPORTANCE_LOW));m.createNotificationChannel(new NotificationChannel("events","Transferts détectés",NotificationManager.IMPORTANCE_DEFAULT));}
    static PendingIntent settings(Context c){return PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);}
    public static void notice(Context c,String title,String text){channels(c);c.getSystemService(NotificationManager.class).notify(42,new Notification.Builder(c,"events").setSmallIcon(R.drawable.shield).setContentTitle(title).setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text)).setContentIntent(settings(c)).build());}
    @Override public int onStartCommand(Intent i,int f,int id){
        if(i!=null&&"STOP".equals(i.getAction())){State.prefs(this).edit().putBoolean("enabled",false).apply();State.CONTEXT.clearAll();stopSelf();return START_NOT_STICKY;}
        if(!State.enabled(this)||!State.accessibilityGranted(this)){stopSelf();return START_NOT_STICKY;}
        PendingIntent pause=PendingIntent.getService(this,1,new Intent(this,MonitorService.class).setAction("STOP"),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        startForeground(40,new Notification.Builder(this,"watch").setSmallIcon(R.drawable.shield).setContentTitle("Serein Downloads • protection locale")
            .setContentText("Commandes de téléchargement filtrées • ouvrir les réglages").setContentIntent(settings(this)).setOngoing(true)
            .addAction(new Notification.Action.Builder(null,"Désactiver",pause).build()).build());running=true;return START_STICKY;
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences p,String k){if(!State.enabled(this))stopSelf();}
    @Override public IBinder onBind(Intent i){return null;}
    @Override public void onDestroy(){running=false;State.prefs(this).unregisterOnSharedPreferenceChangeListener(this);stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();}
}
