package sn.amado.serein;
import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.io.ByteArrayInputStream;
import java.net.URI;

/** Deliberately restrictive text browser: no image classification claims. */
public class BrowserActivity extends Activity {
    WebView web; EditText address; TextView info;
    String custom(){return getSharedPreferences("serein",0).getString("domains","");}
    WebResourceResponse deny(){return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);getWindow().setStatusBarColor(MainActivity.BG);getWindow().setNavigationBarColor(MainActivity.BG);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(MainActivity.BG);int p=MainActivity.dp(this,12);root.setPadding(p,p,p,0);
        LinearLayout bar=new LinearLayout(this);bar.setOrientation(0);address=new EditText(this);address.setSingleLine();address.setTextColor(MainActivity.INK);address.setHintTextColor(0xff9aafbf);address.setTextSize(14);address.setHint("Adresse HTTPS ou recherche");address.setInputType(17);bar.addView(address,new LinearLayout.LayoutParams(0,-2,1));
        Button go=new Button(this);go.setText("Aller");go.setOnClickListener(v->navigate());bar.addView(go);root.addView(bar);
        address.setOnEditorActionListener((v,id,event)->{navigate();return true;});
        info=new TextView(this);info.setTextColor(MainActivity.MINT);info.setTextSize(12);info.setPadding(0,p,0,p);info.setText("Serein • images et scripts désactivés • HTTPS uniquement");root.addView(info);
        LinearLayout nav=new LinearLayout(this);Button back=new Button(this);back.setText("Retour");back.setOnClickListener(v->{if(web.canGoBack())web.goBack();else finish();});nav.addView(back);
        Button home=new Button(this);home.setText("Accueil app");home.setOnClickListener(v->finish());nav.addView(home);root.addView(nav);
        web=new WebView(this);root.addView(web,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);root.setOnApplyWindowInsetsListener((v,i)->{root.setPadding(p,p+i.getSystemWindowInsetTop(),p,i.getSystemWindowInsetBottom());return i;});
        WebSettings s=web.getSettings();s.setJavaScriptEnabled(false);s.setDomStorageEnabled(false);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setBlockNetworkImage(true);s.setLoadsImagesAutomatically(false);s.setMediaPlaybackRequiresUserGesture(true);s.setSupportMultipleWindows(false);s.setJavaScriptCanOpenWindowsAutomatically(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);s.setSafeBrowsingEnabled(true);s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
        web.setDownloadListener((url,ua,cd,mime,len)->runOnUiThread(()->Toast.makeText(this,"Téléchargements désactivés dans Serein",Toast.LENGTH_SHORT).show()));
        web.setWebViewClient(new TextClient());
        address.setText("https://www.google.com/search?safe=active&q=");
    }
    class TextClient extends WebViewClient {
        @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){if(!Policy.allowedUrl(r.getUrl().toString(),custom())){blocked();return true;}return false;}
        @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){
            if(!Policy.allowedUrl(r.getUrl().toString(),custom())||!r.isForMainFrame())return deny();
            javax.net.ssl.HttpsURLConnection c=null;
            try {
                String url=r.getUrl().toString();
                // Manual redirects prevent automatic transport to a denied domain.
                for(int redirects=0;redirects<6;redirects++){
                    if(!Policy.allowedUrl(url,custom()))return deny();
                    c=(javax.net.ssl.HttpsURLConnection)new java.net.URL(url).openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(8000);c.setReadTimeout(8000);c.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 Serein/1.0");
                    int status=c.getResponseCode();
                    if(status>=300&&status<400){String target=c.getHeaderField("Location");if(target==null)return deny();url=new URI(url).resolve(target).toString();c.disconnect();c=null;continue;}
                    String type=c.getContentType();if(type==null||!type.toLowerCase(java.util.Locale.ROOT).startsWith("text/html"))return deny();
                    if(status<200||status>=300)return deny();
                    java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();
                    try(java.io.InputStream in=c.getInputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>3_000_000)return deny();out.write(b,0,n);}}
                    String charset="UTF-8";java.util.regex.Matcher match=java.util.regex.Pattern.compile("charset=([^;\\s]+)",java.util.regex.Pattern.CASE_INSENSITIVE).matcher(type);if(match.find())charset=match.group(1).replace("\"","");
                    String html=new String(out.toByteArray(),java.nio.charset.Charset.forName(charset));
                    String csp="default-src 'none'; img-src 'none'; media-src 'none'; script-src 'none'; style-src 'unsafe-inline'; frame-src 'none'; object-src 'none'; base-uri 'none'; form-action 'none'";
                    // Rebuild exclusively from escaped plain text and validated links.
                    // No remote HTML, inline SVG, image, CSS or form survives this step.
                    android.text.Spanned text=android.text.Html.fromHtml(html,android.text.Html.FROM_HTML_MODE_LEGACY);
                    android.text.style.URLSpan[] links=text.getSpans(0,text.length(),android.text.style.URLSpan.class);
                    java.util.Arrays.sort(links,java.util.Comparator.comparingInt(text::getSpanStart));
                    StringBuilder safe=new StringBuilder(); int cursor=0;
                    for(android.text.style.URLSpan link:links){
                        int a=text.getSpanStart(link),b=text.getSpanEnd(link);
                        if(a<cursor || b<=a)continue;
                        safe.append(android.text.TextUtils.htmlEncode(text.subSequence(cursor,a).toString()));
                        String target=new URI(url).resolve(link.getURL()).toString();
                        String label=android.text.TextUtils.htmlEncode(text.subSequence(a,b).toString());
                        if(Policy.allowedUrl(target,custom()))safe.append("<a href=\"").append(android.text.TextUtils.htmlEncode(target)).append("\">").append(label).append("</a>");else safe.append(label);
                        cursor=b;
                    }
                    safe.append(android.text.TextUtils.htmlEncode(text.subSequence(cursor,text.length()).toString()));
                    html="<!doctype html><html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><meta http-equiv=\"Content-Security-Policy\" content=\""+csp+"\"><style>body{background:#0d1721;color:#e9f2f8;font:17px sans-serif;padding:18px;line-height:1.6}a{color:#70e1bc}main{white-space:pre-wrap;overflow-wrap:anywhere}</style></head><body><main>"+safe+"</main></body></html>";
                    java.util.Map<String,String> headers=new java.util.HashMap<>();headers.put("Content-Security-Policy",csp);headers.put("Cache-Control","no-store");headers.put("X-Content-Type-Options","nosniff");
                    return new WebResourceResponse("text/html","UTF-8",200,"OK",headers,new ByteArrayInputStream(html.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                }
            } catch(Exception ignored){} finally{if(c!=null)c.disconnect();}
            return deny();
        }
        @Override public void onPageFinished(WebView v,String url){address.setText(url);info.setText("Mode texte • aucune image • certains sites sont simplifiés");}
        @Override public void onReceivedSslError(WebView v,SslErrorHandler h,android.net.http.SslError e){h.cancel();}
    }
    void blocked(){runOnUiThread(()->{info.setText("Adresse ou recherche bloquée par Serein");Toast.makeText(this,"Contenu bloqué",Toast.LENGTH_SHORT).show();});}
    void navigate(){String raw=address.getText().toString().trim();if(raw.isEmpty())return;String url;
        if(raw.contains("://"))url=raw;else if(!raw.contains(" ")&&raw.contains("."))url="https://"+raw;else url="https://www.google.com/search?safe=active&q="+android.net.Uri.encode(raw);
        if(!Policy.allowedUrl(url,custom())){blocked();return;}web.loadUrl(url);
        ((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);
    }
    @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
    @Override protected void onPause(){web.onPause();super.onPause();}
    @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
    @Override protected void onDestroy(){web.stopLoading();web.destroy();super.onDestroy();}
}
