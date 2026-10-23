package com.sheself.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Shell Android hors-ligne, sans publicité, analytique ni autorisation Internet. */
public final class MainActivity extends Activity {
    private static final int PICK_JSON = 101;
    private static final int SAVE_JSON = 102;
    private static final String START_URL = "file:///android_asset/index.html";
    private WebView webView;
    private ValueCallback<Uri[]> uploadCallback;
    private String pendingExport;

    @SuppressLint("SetJavaScriptEnabled")
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        webView.setBackgroundColor(0xFFFAF8F5);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true); // Nécessaire aux ressources de l'APK.
        webView.getSettings().setAllowFileAccessFromFileURLs(false);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        if (Build.VERSION.SDK_INT >= 26) webView.getSettings().setSafeBrowsingEnabled(true);
        // Pas de permission INTERNET dans AndroidManifest.xml.
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                return !("file".equals(u.getScheme()) && "/android_asset/index.html".equals(u.getPath()));
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                                         FileChooserParams params) {
                if (uploadCallback != null) uploadCallback.onReceiveValue(null);
                uploadCallback = callback;
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("application/json");
                try { startActivityForResult(i, PICK_JSON); }
                catch (ActivityNotFoundException e) {
                    uploadCallback.onReceiveValue(null); uploadCallback = null;
                    Toast.makeText(MainActivity.this, "Sélecteur de fichiers indisponible", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        webView.addJavascriptInterface(new ExportBridge(), "SheSelfAndroid");
        setContentView(webView);
        webView.loadUrl(START_URL);
    }

    private final class ExportBridge {
        @JavascriptInterface public void exportJson(String content) {
            if (content == null || content.length() > 2_000_000) return;
            runOnUiThread(() -> {
                pendingExport = content;
                Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("application/json");
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date());
                i.putExtra(Intent.EXTRA_TITLE, "SHESELF_mes_donnees_" + date + ".json");
                try { startActivityForResult(i, SAVE_JSON); }
                catch (ActivityNotFoundException e) {
                    pendingExport = null;
                    Toast.makeText(MainActivity.this, "Sauvegarde indisponible", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_JSON) {
            if (uploadCallback != null) {
                Uri[] result = (resultCode == RESULT_OK && data != null && data.getData() != null)
                    ? new Uri[]{data.getData()} : null;
                uploadCallback.onReceiveValue(result);
                uploadCallback = null;
            }
        } else if (requestCode == SAVE_JSON) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null && pendingExport != null) {
                try (OutputStream stream = getContentResolver().openOutputStream(data.getData())) {
                    if (stream == null) throw new IOException("Impossible d'ouvrir le fichier");
                    stream.write(pendingExport.getBytes(StandardCharsets.UTF_8));
                    Toast.makeText(this, "Données exportées", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Échec de l'export", Toast.LENGTH_LONG).show();
                }
            }
            pendingExport = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("SheSelfAndroid");
            webView.destroy();
        }
        super.onDestroy();
    }
}
