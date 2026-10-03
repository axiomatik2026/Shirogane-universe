package com.axiomatik.animaux;
import android.app.Activity;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;
import android.webkit.*;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.view.WindowInsets;
import java.util.Arrays;
public final class MainActivity extends Activity {
 private WebView web; private MediaPlayer sound;
 private static final java.util.List<String> IDS=Arrays.asList("loup","lion","chien","chat","grenouille","hibou","oiseau","vache","cheval","cochon","mouton","poule","coq","canard","chauve-souris");
 @Override public void onCreate(Bundle state){super.onCreate(state);LinearLayout root=new LinearLayout(this);root.setBackgroundColor(Color.rgb(16,25,22));web=new WebView(this);web.setBackgroundColor(Color.rgb(16,25,22));root.addView(web,new LinearLayout.LayoutParams(-1,-1));setContentView(root);getWindow().setStatusBarColor(Color.rgb(24,39,30));getWindow().setNavigationBarColor(Color.rgb(23,36,30));if(android.os.Build.VERSION.SDK_INT>=30){getWindow().setDecorFitsSystemWindows(false);root.setOnApplyWindowInsetsListener((v,i)->{android.graphics.Insets b=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(b.left,b.top,b.right,b.bottom);return WindowInsets.CONSUMED;});root.requestApplyInsets();}WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setAllowFileAccessFromFileURLs(false);s.setAllowUniversalAccessFromFileURLs(false);web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return true;}});web.addJavascriptInterface(new Sounds(),"AnimalAudio");web.loadUrl("file:///android_asset/index.html");}
 private void release(){if(sound!=null){sound.release();sound=null;}}
 private final class Sounds {
 @JavascriptInterface public void stop(){runOnUiThread(()->release());}
 @JavascriptInterface public void play(String id){if(!IDS.contains(id))return;runOnUiThread(()->{release();try{MediaPlayer m=new MediaPlayer();sound=m;try(AssetFileDescriptor fd=getAssets().openFd("audio/"+id+".mp3")){m.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());}m.setOnCompletionListener(p->{if(sound==p){release();web.evaluateJavascript("window.onAnimalEnded('"+id+"')",null);}});m.setOnErrorListener((p,w,e)->{if(sound==p){release();web.evaluateJavascript("window.onAnimalError('"+id+"')",null);}return true;});m.prepare();m.start();}catch(Exception e){release();web.evaluateJavascript("window.onAnimalError('"+id+"')",null);}});}
 }
 @Override protected void onPause(){release();if(web!=null)web.evaluateJavascript("window.onAnimalEnded()",null);super.onPause();}
 @Override protected void onDestroy(){release();if(web!=null){web.removeJavascriptInterface("AnimalAudio");web.destroy();}super.onDestroy();}
}
