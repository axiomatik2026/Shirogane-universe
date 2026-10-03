package com.axiomatik.shirogane;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.view.WindowInsets;
import android.content.Intent;
import android.net.Uri;
public final class MainActivity extends Activity {
 private WebView web;
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  LinearLayout root=new LinearLayout(this);root.setBackgroundColor(Color.rgb(12,16,24));
  web=new WebView(this);web.setBackgroundColor(Color.rgb(12,16,24));root.addView(web,new LinearLayout.LayoutParams(-1,-1));setContentView(root);
  getWindow().setStatusBarColor(Color.rgb(12,16,24));getWindow().setNavigationBarColor(Color.rgb(12,16,24));
  if(android.os.Build.VERSION.SDK_INT>=30){getWindow().setDecorFitsSystemWindows(false);root.setOnApplyWindowInsetsListener((v,i)->{android.graphics.Insets b=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(b.left,b.top,b.right,b.bottom);return WindowInsets.CONSUMED;});root.requestApplyInsets();}
  WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setAllowFileAccessFromFileURLs(false);s.setAllowUniversalAccessFromFileURLs(false);s.setDefaultTextEncodingName("UTF-8");
  web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest request){Uri uri=request.getUrl();if("file".equals(uri.getScheme()) && uri.getPath()!=null && uri.getPath().startsWith("/android_asset/"))return false;if("https".equals(uri.getScheme()) && "github.com".equals(uri.getHost())){try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(android.content.ActivityNotFoundException ignored){}}return true;}});
  if(state==null || web.restoreState(state)==null)web.loadUrl("file:///android_asset/index.html");
 }
 @Override public void onBackPressed(){web.evaluateJavascript("window.shiroganeBack ? window.shiroganeBack() : false",result->{if(!"true".equals(result))finish();});}
 @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);web.saveState(state);}
 @Override protected void onDestroy(){if(web!=null)web.destroy();super.onDestroy();}
}
