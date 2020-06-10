package com.integrals.inlens.Activities;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.R;

public class WebViewActivity extends AppCompatActivity {

    private static final String PRIVACY_POLICY="PRIVACY_POLICY";
    private static final String TERMS_AND_CONDITIONS="TERMS_AND_CONDITIONS";
    private static final String HELP="HELP";
    private static final String RATE_US="RATE_US";

    private static final String urlTermsAndConditions="https://sites.google.com/view/inlenstermsandconditions/terms-and-conditions";
    private static final String urlPrivacyPolicy="https://sites.google.com/view/inlens/privacy-policy";
    private static final String urlHelp="https://sites.google.com/view/inlenshelp/home";

    String appTheme="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences appDataPref = getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        final SharedPreferences.Editor appDataPrefEditor = appDataPref.edit();
        if(appDataPref.contains(AppConstants.appDataPref_theme))
        {
            appTheme = appDataPref.getString(AppConstants.appDataPref_theme,AppConstants.themeLight);
            if(appTheme.equals(AppConstants.themeLight))
            {
                setTheme(R.style.AppTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            else
            {
                setTheme(R.style.DarkTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        }
        else
        {
            appTheme = AppConstants.themeLight;
            appDataPrefEditor.putString(AppConstants.appDataPref_theme,AppConstants.themeLight);
            appDataPrefEditor.commit();
            setTheme(R.style.AppTheme);

        }
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
