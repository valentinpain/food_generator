package com.valen.food;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView webView;
    private MealWidgetProvider widgetProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript("updateAndroidWidget()", null);
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        widgetProvider = new MealWidgetProvider();
        webView.addJavascriptInterface(new WidgetBridge(), "AndroidWidget");
        webView.loadUrl("file:///android_asset/index.html");
        setContentView(webView);
    }

    private class WidgetBridge {
        @JavascriptInterface
        public void update(String payload) {
            try {
                getSharedPreferences(MealWidgetProvider.PREFERENCES, MODE_PRIVATE)
                        .edit()
                        .putString(MealWidgetProvider.PAYLOAD, new JSONObject(payload).toString())
                        .apply();
                MealWidgetProvider.updateAll(MainActivity.this);
            } catch (JSONException ignored) {
                // Ignore malformed data coming from the page.
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
