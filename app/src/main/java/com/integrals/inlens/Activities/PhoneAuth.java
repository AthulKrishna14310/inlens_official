package com.integrals.inlens.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.SnackShow;
import com.integrals.inlens.R;

public class PhoneAuth extends AppCompatActivity {
    private EditText authEditText;
    private ExtendedFloatingActionButton authNextButton;
    private CountryCodePicker countryCodePicker;
    private String countryCode;
    private String phoneNumber;
    private RelativeLayout relativeLayout;
    private String appTheme = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences appDataPref = getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        final SharedPreferences.Editor appDataPrefEditor = appDataPref.edit();
        if (appDataPref.contains(AppConstants.appDataPref_theme)) {
            appTheme = appDataPref.getString(AppConstants.appDataPref_theme, AppConstants.themeLight);
            if (appTheme.equals(AppConstants.themeLight)) {
                setTheme(R.style.AppTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                setTheme(R.style.DarkTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        } else {
            appTheme = AppConstants.themeLight;
            appDataPrefEditor.putString(AppConstants.appDataPref_theme, AppConstants.themeLight);
            appDataPrefEditor.commit();
            setTheme(R.style.AppTheme);

        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        authEditText =findViewById(R.id.editText_carrierNumber);
        authNextButton=findViewById(R.id.auth_next_button);
        countryCodePicker=findViewById(R.id.ccp);
        relativeLayout=findViewById(R.id.phoneAuth);
    }

    @Override
    protected void onStart() {
        super.onStart();
        authNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countryCode=countryCodePicker.getSelectedCountryCode();
                phoneNumber=authEditText.getText().toString();
                if(!countryCode.isEmpty()){
                    if(!phoneNumber.isEmpty()){
                        Intent verifyIntent=new Intent(PhoneAuth.this, VerificationActivity.class);
                        verifyIntent.putExtra("Phone",phoneNumber);
                        verifyIntent.putExtra("Country",countryCode);
                        startActivity(verifyIntent);

                    }else{
                        new SnackShow(relativeLayout,PhoneAuth.this)
                                .showErrorSnack("Please type your phone number.");
                    }
                }else{
                    new SnackShow(relativeLayout,PhoneAuth.this)
                            .showErrorSnack("Please select your country.");

                }
            }
        });
    }

}
