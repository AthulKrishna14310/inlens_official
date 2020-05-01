package com.integrals.inlens.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.integrals.inlens.R;

public class WebViewActivity extends AppCompatActivity {

    private static final String PRIVACY_POLICY="";
    private static final String TERMS_AND_CONDITIONS="";
    private static final String HELP="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        WebView webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl("https://www.journaldev.com");
    }
}
