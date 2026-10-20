package sn.amado.serein;

import android.app.*;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import android.view.accessibility.AccessibilityManager;

/** Visible foreground lifetime for the user's local search protection. No network or polling. */
public class GuardRuntimeService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static volatile boolean running=false;
    public static volatile String problem="";
    private static final String CHANNEL="local_guard";
    private static final int NOTIFICATION_ID=31;
    private SharedPreferences prefs;
    public static boolean accessibilityEnabled(Context c){
        AccessibilityManager am=(AccessibilityManager)c.getSystemService(ACCESSIBILITY_SERVICE);
        if(am==null)return false;
        for(AccessibilityServiceInfo i:am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)){
            ServiceInfo s=i.getResolveInfo().serviceInfo;
            if(c.getPackageName().equals(s.packageName)&&SearchGuardService.class.getName().equals(s.name))return true;
        }
        return false;
    }
    public static boolean eligible(Context c){
        SharedPreferences p=c.getSharedPreferences("serein",0);
        return LocalGuardState.shouldRun(p.getBoolean("search_guard",false),p.getBoolean("guard_background",true),accessibilityEnabled(c),
            p.getStringSet("search_apps",SearchPolicy.DEFAULT_APPS).size(),LocalGuardState.countTerms(p.getString("search_terms",SearchPolicy.DEFAULT_TERMS)));
    }
    public static void ensure(Context c){
        if(!eligible(c)){c.stopService(new Intent(c,GuardRuntimeService.class));return;}
        try{c.startForegroundService(new Intent(c,GuardRuntimeService.class));}
        catch(RuntimeException e){problem="Veille locale non démarrée : ouvre Serein puis réessaie. Le service d’accessibilité reste indépendant.";}
    }
    public static void refresh(Context c){
        if(running){try{c.startService(new Intent(c,GuardRuntimeService.class));}catch(RuntimeException ignored){}}
    }
    @Override public void onCreate(){
        super.onCreate();prefs=getSharedPreferences("serein",0);prefs.registerOnSharedPreferenceChangeListener(this);
        getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel(CHANNEL,"Protection locale en arrière-plan",NotificationManager.IMPORTANCE_LOW));
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null && "DISABLE".equals(intent.getAction())){
            prefs.edit().putBoolean("search_guard",false).apply();stopSelf();return START_NOT_STICKY;
        }
        if(!eligible(this)){stopSelf();return START_NOT_STICKY;}
        // Foreground promotion happens immediately, also after a sticky restart.
        try{startForeground(NOTIFICATION_ID,notification());running=true;problem="";}
        catch(RuntimeException e){problem="Impossible d’afficher la veille de protection. Ouvre Serein et vérifie les autorisations.";stopSelf();return START_NOT_STICKY;}
        return START_STICKY;
    }
    private Notification notification(){
        PendingIntent settings=PendingIntent.getActivity(this,31,new Intent(this,GuardSettingsActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pause=PendingIntent.getService(this,32,new Intent(this,GuardRuntimeService.class).setAction("DISABLE"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        String title=SearchGuardService.connected?"Serein • recherches protégées localement":"Serein • accessibilité en attente";
        String detail=SearchGuardService.connected?"Fonctionne hors connexion, même Serein fermé.":"Ouvre les réglages pour vérifier l’accès d’accessibilité.";
        return new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.shield).setContentTitle(title).setContentText(detail)
            .setStyle(new Notification.BigTextStyle().bigText(detail)).setContentIntent(settings).setOngoing(true).setOnlyAlertOnce(true)
            .addAction(new Notification.Action.Builder(null,"Réglages",settings).build())
            .addAction(new Notification.Action.Builder(null,"Désactiver recherches",pause).build()).build();
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences p,String key){
        if(key.equals("search_guard")||key.equals("guard_background")||key.equals("search_apps")||key.equals("search_terms")){
            if(!eligible(this))stopSelf();else if(running)getSystemService(NotificationManager.class).notify(NOTIFICATION_ID,notification());
        }
    }
    @Override public void onTaskRemoved(Intent rootIntent){
        // Removing the activity from Recents intentionally does not stop protection.
        super.onTaskRemoved(rootIntent);
    }
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public void onDestroy(){
        running=false;if(prefs!=null)prefs.unregisterOnSharedPreferenceChangeListener(this);
        stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();
    }
}
