package com.integrals.inlens.Activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.integrals.inlens.R;

public class WebViewActivity extends AppCompatActivity {

    private static final String PRIVACY_POLICY="PRIVACY_POLICY";
    private static final String TERMS_AND_CONDITIONS="TERMS_AND_CONDITIONS";
    private static final String HELP="HELP";
    private static final String RATE_US="RATE_US";

    private static final String urlTermsAndConditions="https://sites.google.com/view/inlenstermsandconditions/terms-and-conditions";
    private static final String urlPrivacyPolicy="https://sites.google.com/view/inlens/privacy-policy";
    private static final String urlHelp="https://sites.google.com/view/inlenshelp/home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        WebView webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);


        if(getIntent().getStringExtra("MESSAGE").contentEquals(TERMS_AND_CONDITIONS)) {


            webView.loadUrl(urlTermsAndConditions);
        }
        else if(getIntent().getStringExtra("MESSAGE").contentEquals(PRIVACY_POLICY)){


            webView.loadUrl(urlPrivacyPolicy);

        }
        else if(getIntent().getStringExtra("MESSAGE").contentEquals(HELP)){


            webView.loadUrl(urlHelp);

        }
    }
}
