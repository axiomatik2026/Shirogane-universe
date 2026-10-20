package sn.amado.downloadguard;

import android.content.*;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;
import android.service.notification.NotificationListenerService;
import android.provider.Settings;
import java.util.*;

public final class State {
    public static final RiskSession CONTEXT=new RiskSession();
    public static volatile String status="En attente d’activation";
    public static volatile boolean accessibility=false, notifications=false;
    public static SharedPreferences prefs(Context c){return c.getSharedPreferences("downloads",0);}
    public static boolean enabled(Context c){return prefs(c).getBoolean("enabled",false);}
    public static Set<String> apps(Context c){return new HashSet<>(prefs(c).getStringSet("apps",DownloadPolicy.DEFAULT_APPS));}
    public static DownloadPolicy policy(Context c){return new DownloadPolicy(prefs(c).getString("terms",DownloadPolicy.DEFAULT_TERMS));}
    public static boolean strict(Context c){return prefs(c).getBoolean("strict",false);}
    public static boolean accessibilityGranted(Context c){
        AccessibilityManager am=(AccessibilityManager)c.getSystemService(Context.ACCESSIBILITY_SERVICE);
        for(AccessibilityServiceInfo s:am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)){
            if(c.getPackageName().equals(s.getResolveInfo().serviceInfo.packageName)&&DownloadGuardService.class.getName().equals(s.getResolveInfo().serviceInfo.name))return true;
        }return false;
    }
    public static boolean notificationGranted(Context c){
        String enabled=Settings.Secure.getString(c.getContentResolver(),"enabled_notification_listeners");
        if(enabled==null)return false;
        ComponentName wanted=new ComponentName(c,TransferListener.class);
        for(String raw:enabled.split(":")){ComponentName n=ComponentName.unflattenFromString(raw);if(wanted.equals(n))return true;}return false;
    }
    public static void refreshRuntime(Context c){
        if(!enabled(c)||!accessibilityGranted(c)){c.stopService(new Intent(c,MonitorService.class));return;}
        try{c.startForegroundService(new Intent(c,MonitorService.class));}catch(RuntimeException e){status="Veille non démarrée : ouvre l’application et vérifie l’arrière-plan.";}
    }
}
