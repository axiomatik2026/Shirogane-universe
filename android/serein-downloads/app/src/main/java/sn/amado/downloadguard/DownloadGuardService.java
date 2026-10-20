package sn.amado.downloadguard;

import android.accessibilityservice.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.accessibility.*;
import android.widget.*;
import java.util.*;

/** Covers recognized download controls before they can be pressed in risky context. */
public class DownloadGuardService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final Handler handler=new Handler();
    private final ArrayList<View> overlays=new ArrayList<>();
    private final ArrayList<Rect> currentRects=new ArrayList<>();
    private WindowManager wm;private DownloadPolicy policy;private boolean scanning=false;
    private final Runnable scan=()->inspect();
    private final BroadcastReceiver screenOff=new BroadcastReceiver(){public void onReceive(Context c,Intent i){clearOverlays();}};
    @Override public void onServiceConnected(){
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);State.accessibility=true;
        State.prefs(this).registerOnSharedPreferenceChangeListener(this);policy=State.policy(this);configure();
        if(Build.VERSION.SDK_INT>=33)registerReceiver(screenOff,new IntentFilter(Intent.ACTION_SCREEN_OFF),Context.RECEIVER_NOT_EXPORTED);else registerReceiver(screenOff,new IntentFilter(Intent.ACTION_SCREEN_OFF));
        State.refreshRuntime(this);
    }
    private void configure(){
        AccessibilityServiceInfo i=getServiceInfo();if(i==null)return;
        // Keep window-change events so old button overlays disappear on app switches.
        i.packageNames=null;i.eventTypes=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED|AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED|AccessibilityEvent.TYPE_VIEW_SCROLLED|AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED|AccessibilityEvent.TYPE_VIEW_CLICKED|AccessibilityEvent.TYPE_WINDOWS_CHANGED;
        i.flags|=AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS|AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        i.notificationTimeout=50;setServiceInfo(i);
        if(!State.enabled(this))clearOverlays();
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences p,String key){
        policy=State.policy(this);State.CONTEXT.clearAll();clearOverlays();configure();State.refreshRuntime(this);
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent e){
        if(!State.enabled(this)||e==null){clearOverlays();return;}
        String pkg=e.getPackageName()==null?"":e.getPackageName().toString();
        if(!pkg.isEmpty()&&!pkg.equals(getPackageName())&&!State.apps(this).contains(pkg)&&e.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)clearOverlays();
        handler.removeCallbacks(scan);handler.postDelayed(scan,e.getEventType()==AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED?0:55);
    }
    private static String str(CharSequence c){return c==null?"":c.toString();}
    private void inspect(){
        if(scanning||!State.enabled(this)){if(!State.enabled(this))clearOverlays();return;}
        AccessibilityNodeInfo active=getRootInActiveWindow();if(active==null){clearOverlays();return;}
        String pkg=str(active.getPackageName());
        if(!State.apps(this).contains(pkg)){active.recycle();clearOverlays();return;}
        scanning=true;
        ArrayList<AccessibilityNodeInfo> roots=new ArrayList<>();ArrayList<Rect> ime=new ArrayList<>();
        roots.add(active);
        try{
            for(AccessibilityWindowInfo window:getWindows()){try{
                if(window.getType()==AccessibilityWindowInfo.TYPE_INPUT_METHOD){Rect r=new Rect();window.getBoundsInScreen(r);ime.add(r);}
                else if(window.getType()==AccessibilityWindowInfo.TYPE_APPLICATION){AccessibilityNodeInfo r=window.getRoot();if(r!=null){if(pkg.equals(str(r.getPackageName())))roots.add(r);else r.recycle();}}
            }finally{window.recycle();}}
            ArrayList<Rect> buttons=new ArrayList<>();boolean riskyNow=false;boolean safeNewSearch=false;int seen=0;
            for(AccessibilityNodeInfo root:roots){
                ArrayDeque<AccessibilityNodeInfo> todo=new ArrayDeque<>();todo.add(AccessibilityNodeInfo.obtain(root));
                try{while(!todo.isEmpty()&&seen++<1100){
                    AccessibilityNodeInfo n=todo.removeFirst();
                    try{
                        if(!n.isVisibleToUser()||n.isPassword())continue;
                        String text=str(n.getText()),description=str(n.getContentDescription()),id=n.getViewIdResourceName();
                        String content=text+" "+description;
                        boolean sensitive=policy.sensitive(content);riskyNow|=sensitive;
                        if(n.isEditable()&&!text.trim().isEmpty()&&text.length()>2 && DownloadPolicy.normalize((id==null?"":id)+" "+str(n.getHintText())).matches(".*(?:search|query|recherch|url_bar|omnibox).*"))safeNewSearch|=!sensitive;
                        boolean download=DownloadPolicy.downloadAction(text.isEmpty()?description:text,id,n.isEditable());
                        if(download){
                            AccessibilityNodeInfo button=AccessibilityNodeInfo.obtain(n);
                            try{
                                for(int j=0;j<2&&!button.isClickable();j++){AccessibilityNodeInfo parent=button.getParent();if(parent==null)break;button.recycle();button=parent;}
                                Rect bounds=new Rect();button.getBoundsInScreen(bounds);
                                if(button.isClickable()&&bounds.width()>10&&bounds.height()>10){boolean keyboard=false;for(Rect key:ime)if(Rect.intersects(key,bounds))keyboard=true;if(!keyboard&&!buttons.contains(bounds))buttons.add(bounds);}
                            }finally{button.recycle();}
                        }
                        for(int j=0;j<n.getChildCount()&&todo.size()<700;j++){AccessibilityNodeInfo child=n.getChild(j);if(child!=null)todo.add(child);}
                    }finally{n.recycle();}
                }}finally{while(!todo.isEmpty())todo.removeFirst().recycle();}
            }
            long now=SystemClock.elapsedRealtime();if(safeNewSearch&&!riskyNow)State.CONTEXT.clear(pkg);if(riskyNow)State.CONTEXT.mark(pkg,now);
            boolean risk=riskyNow||State.CONTEXT.risky(pkg,now);
            if(policy.shouldBlock(!buttons.isEmpty(),risk,State.strict(this)))showOverlays(buttons);else clearOverlays();
            if(!riskyNow&&risk&&!State.strict(this)){handler.removeCallbacks(scan);handler.postDelayed(scan,State.CONTEXT.remaining(pkg,now)+1);}
            if(!buttons.isEmpty())State.status=risk||State.strict(this)?"Commande de téléchargement couverte":"Commande détectée, aucun indice sensible trouvé";
        }catch(RuntimeException ignored){clearOverlays();}
        finally{for(AccessibilityNodeInfo root:roots)root.recycle();scanning=false;}
    }
    private void showOverlays(ArrayList<Rect> rects){
        if(rects.size()>8)rects=new ArrayList<>(rects.subList(0,8));
        if(currentRects.equals(rects))return;clearOverlays();
        for(Rect r:rects){
            TextView cover=new TextView(this);cover.setText("⛔ Téléchargement filtré");cover.setTextSize(12);cover.setTextColor(Color.WHITE);cover.setGravity(Gravity.CENTER);cover.setBackgroundColor(0xff923d3d);cover.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            cover.setOnClickListener(v->Toast.makeText(this,"Enregistrement bloqué : indice sensible ou mode strict. Réglages dans Serein Downloads.",Toast.LENGTH_LONG).show());
            WindowManager.LayoutParams p=new WindowManager.LayoutParams(r.width(),r.height(),WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
            p.gravity=Gravity.TOP|Gravity.LEFT;p.x=r.left;p.y=r.top;
            if(Build.VERSION.SDK_INT>=30)p.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
            try{wm.addView(cover,p);overlays.add(cover);currentRects.add(new Rect(r));}catch(RuntimeException ignored){}
        }
    }
    private void clearOverlays(){for(View v:overlays)try{wm.removeViewImmediate(v);}catch(RuntimeException ignored){}overlays.clear();currentRects.clear();}
    @Override public void onInterrupt(){clearOverlays();}
    @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);clearOverlays();State.accessibility=false;State.prefs(this).unregisterOnSharedPreferenceChangeListener(this);try{unregisterReceiver(screenOff);}catch(Exception ignored){}super.onDestroy();}
}
