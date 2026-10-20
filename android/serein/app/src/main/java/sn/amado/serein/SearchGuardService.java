package sn.amado.serein;

import android.accessibilityservice.*;
import android.content.*;
import android.os.*;
import android.view.accessibility.*;
import android.widget.Toast;
import java.util.*;

/** User-enabled search guard; text is processed transiently on the device. */
public class SearchGuardService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static volatile boolean connected=false;
    private SharedPreferences prefs;
    private SearchPolicy policy;
    private final Handler handler=new Handler();
    private long lastBlock=0;
    private String pendingPackage="";
    private boolean scanning=false;
    private final Runnable inspect=()->{if(!scanning)inspectWindow(pendingPackage);};
    @Override public void onServiceConnected(){
        prefs=getSharedPreferences("serein",0);prefs.registerOnSharedPreferenceChangeListener(this);
        policy=new SearchPolicy(prefs.getString("search_terms",SearchPolicy.DEFAULT_TERMS));
        connected=true;configure();
    }
    private boolean enabled(){return prefs!=null && prefs.getBoolean("search_guard",false);}
    private Set<String> apps(){return new HashSet<>(prefs.getStringSet("search_apps",SearchPolicy.DEFAULT_APPS));}
    private void configure(){
        AccessibilityServiceInfo info=getServiceInfo();if(info==null)return;
        Set<String> selected=enabled()?apps():Collections.emptySet();
        // A non-matching own package prevents events when no app is selected.
        info.packageNames=selected.isEmpty()?new String[]{getPackageName()}:selected.toArray(new String[0]);
        info.eventTypes=AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED|AccessibilityEvent.TYPE_VIEW_FOCUSED|
            AccessibilityEvent.TYPE_VIEW_CLICKED|AccessibilityEvent.TYPE_VIEW_SCROLLED|
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED|AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.flags|=AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.notificationTimeout=80;setServiceInfo(info);
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences p,String key){
        if("search_terms".equals(key))policy=new SearchPolicy(prefs.getString("search_terms",SearchPolicy.DEFAULT_TERMS));
        if("search_guard".equals(key)||"search_apps".equals(key))configure();
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        if(!enabled() || event==null || event.isPassword())return;
        String pkg=event.getPackageName()==null?"":event.getPackageName().toString();
        if(pkg.equals(getPackageName()) || !apps().contains(pkg))return;
        pendingPackage=pkg;
        // Inspect immediately when editing; debounce busy content/scroll events.
        if(event.getEventType()==AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED){inspectWindow(pkg);}
        handler.removeCallbacks(inspect);handler.postDelayed(inspect,120);
    }
    private void inspectWindow(String pkg){
        if(!enabled() || scanning || SystemClock.uptimeMillis()-lastBlock<1500)return;
        AccessibilityNodeInfo root=getRootInActiveWindow();
        if(root==null)return;
        try{
            if(root.getPackageName()==null || !pkg.contentEquals(root.getPackageName()) || !apps().contains(pkg))return;
            scanning=true;
            boolean strict=prefs.getBoolean("search_strict",false);
            ArrayDeque<AccessibilityNodeInfo> nodes=new ArrayDeque<>();nodes.add(AccessibilityNodeInfo.obtain(root));
            int examined=0;
            try{
                while(!nodes.isEmpty() && examined++<700){
                    AccessibilityNodeInfo node=nodes.removeFirst();
                    try{
                        if(node.isPassword() || !node.isVisibleToUser())continue;
                        boolean search=searchNode(node);
                        // Pinterest may expose its collapsed search header as plain TextView.
                        if(!search && pkg.startsWith("com.pinterest") && !node.isEditable() && node.getText()!=null && node.getText().length()<160){
                            android.graphics.Rect bounds=new android.graphics.Rect(),screen=new android.graphics.Rect();
                            node.getBoundsInScreen(bounds);root.getBoundsInScreen(screen);
                            search=bounds.top>=screen.top && bounds.bottom<=screen.top+screen.height()/4 && bounds.width()>screen.width()/5;
                        }
                        String text=node.getText()==null?"":node.getText().toString();
                        // Content descriptions are checked only in an identified search field,
                        // or in explicitly enabled strict mode, never as an image classifier.
                        if(text.isEmpty() && (search||strict) && node.getContentDescription()!=null)text=node.getContentDescription().toString();
                        if(policy.shouldBlock(text,false,true,search,strict)){
                            lastBlock=SystemClock.uptimeMillis();
                            if(node.isEditable()){
                                Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,"");
                                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args);
                            }
                            // Home reliably leaves results, whereas Back may only dismiss IME.
                            boolean left=performGlobalAction(GLOBAL_ACTION_HOME);
                            Toast.makeText(this,left?"Serein : recherche sensible bloquée":"Serein : recherche détectée — ferme cette page",Toast.LENGTH_LONG).show();
                            if(!left){try{startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));}catch(Exception ignored){}}
                            return;
                        }
                        for(int i=0;i<node.getChildCount() && nodes.size()<700;i++){
                            AccessibilityNodeInfo child=node.getChild(i);if(child!=null)nodes.addLast(child);
                        }
                    }finally{node.recycle();}
                }
            }finally{while(!nodes.isEmpty())nodes.removeFirst().recycle();}
        }catch(RuntimeException ignored){
            // Nodes can disappear during navigation; fail safely without closing normal apps.
        }finally{scanning=false;root.recycle();}
    }
    private boolean searchNode(AccessibilityNodeInfo node){
        if(SearchPolicy.searchMetadata(node.getViewIdResourceName(),str(node.getHintText()),str(node.getContentDescription())))return true;
        AccessibilityNodeInfo parent=node.getParent();
        try{
            // SearchView containers often hold an EditText without its own hint/ID.
            if(parent!=null){
                String clazz=str(parent.getClassName());
                return (node.isEditable()||"android.widget.TextView".contentEquals(node.getClassName())) &&
                    (clazz.contains("SearchView")||SearchPolicy.searchMetadata(parent.getViewIdResourceName(),str(parent.getHintText()),str(parent.getContentDescription())));
            }
        }finally{if(parent!=null)parent.recycle();}
        return false;
    }
    private static String str(CharSequence c){return c==null?"":c.toString();}
    @Override public void onInterrupt(){handler.removeCallbacks(inspect);}
    @Override public void onDestroy(){
        connected=false;handler.removeCallbacksAndMessages(null);
        if(prefs!=null)prefs.unregisterOnSharedPreferenceChangeListener(this);super.onDestroy();
    }
}
