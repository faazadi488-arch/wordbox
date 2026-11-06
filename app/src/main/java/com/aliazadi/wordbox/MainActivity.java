package com.aliazadi.wordbox;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

public class MainActivity extends Activity {
  private WebView web;
  private TextToSpeech tts;
  private boolean ttsReady = false;
  private ValueCallback<Uri[]> chooser;
  private static final int PICK = 1;

  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    tts = new TextToSpeech(this, status -> ttsReady = (status == TextToSpeech.SUCCESS));
    web = new WebView(this);
    setContentView(web);
    WebSettings s = web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setMediaPlaybackRequiresUserGesture(false);
    s.setAllowFileAccess(false);
    final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
    web.setWebViewClient(new WebViewClient() {
      @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
        return loader.shouldInterceptRequest(r.getUrl());
      }
    });
    web.setWebChromeClient(new WebChromeClient() {
      @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
        if (chooser != null) chooser.onReceiveValue(null);
        chooser = cb;
        try { startActivityForResult(p.createIntent(), PICK); }
        catch (Exception e) { chooser = null; return false; }
        return true;
      }
    });
    web.addJavascriptInterface(new Bridge(), "WordboxNative");
    web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
  }

  @Override protected void onActivityResult(int req, int res, Intent data) {
    if (req == PICK && chooser != null) {
      chooser.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
      chooser = null;
    } else super.onActivityResult(req, res, data);
  }

  @Override public void onBackPressed() {
    if (web.canGoBack()) web.goBack(); else super.onBackPressed();
  }

  @Override protected void onDestroy() {
    if (tts != null) { tts.stop(); tts.shutdown(); }
    super.onDestroy();
  }

  class Bridge {
    @JavascriptInterface public void speak(String text, String lang) {
      if (!ttsReady) return;
      try { tts.setLanguage(Locale.forLanguageTag(lang)); } catch (Exception e) { }
      tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wordbox");
    }

    @JavascriptInterface public void saveFile(String name, String text) {
      try {
        File dir = new File(getCacheDir(), "share");
        dir.mkdirs();
        File f = new File(dir, name);
        try (FileOutputStream o = new FileOutputStream(f)) { o.write(text.getBytes("UTF-8")); }
        final Uri u = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".files", f);
        runOnUiThread(() -> {
          Intent i = new Intent(Intent.ACTION_SEND);
          i.setType("application/octet-stream");
          i.putExtra(Intent.EXTRA_STREAM, u);
          i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          startActivity(Intent.createChooser(i, "Save or share backup"));
        });
      } catch (Exception e) { }
    }
  }
}
