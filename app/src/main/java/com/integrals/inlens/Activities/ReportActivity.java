package com.integrals.inlens.Activities;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;


import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static android.view.View.VISIBLE;

public class ReportActivity extends AppCompatActivity {
    private TextInputEditText textInputEditText;
    private Button submitButton;
    ProgressDialog pDialog;
    private String albumName;
    private String albumId;
    private String albumCreatedBy;
    DatabaseReference reportRef, communityReportRef;

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
        setContentView(R.layout.activity_report);
        textInputEditText = findViewById(R.id.report);
        submitButton = findViewById(R.id.submit);
        pDialog = new ProgressDialog(ReportActivity.this);

        albumId = getIntent().getStringExtra("Album_ID");
        albumCreatedBy = getIntent().getStringExtra("Album_CreatedBy");
        albumName = getIntent().getStringExtra("Album_Name");

        reportRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.REPORTED).child(albumId);
        communityReportRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.COMMUNITIES).child(albumId).child(FirebaseConstants.COMMUNITY_REPORTED);

        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().isEmpty()) {
                    submitButton.setVisibility(View.INVISIBLE);
                } else {

                    submitButton.setVisibility(VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // to hide softkeyboard
                manager.hideSoftInputFromWindow(textInputEditText.getWindowToken(), 0);
                displayAlert();
            }
        });

        findViewById(R.id.bkButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private String getOffsetDeletedTime(String timeStamp) {
        TimeZone timeZone = TimeZone.getDefault();
        long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
        long givenTime = Long.parseLong(timeStamp);
        long offsetDeletedTime = givenTime - offsetInMillis;
        return String.valueOf(offsetDeletedTime);
    }

    public void displayAlert() {

        int cf_bg_color,colorPrimary,red_inlens,cf_alert_dialogue_dim_bg;
        if(appTheme.equals(AppConstants.themeLight))
        {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens =  getResources().getColor(R.color.Light_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);
        }
        else
        {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens =  getResources().getColor(R.color.Dark_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }

        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(ReportActivity.this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT)
                .setTitle("Album will be deleted")
                .setDialogBackgroundColor(cf_bg_color)
                .setTextColor(colorPrimary)
                .setIcon(R.drawable.ic_report_red)
                .setMessage("If you report this Cloud-Album it will be deleted permanently if found unfit. This action cannot be reverted and it may take 7 working days to complete the action.")
                .setCancelable(false)
                .addButton("OK , Report",
                        red_inlens,
                        cf_alert_dialogue_dim_bg,
                        CFAlertDialog.CFAlertActionStyle.DEFAULT,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pDialog.setMessage("Reporting.....");
                                pDialog.show();
                                Map reportMap = new HashMap();
                                reportMap.put("admin", albumCreatedBy);
                                reportMap.put("title", albumName);
                                reportMap.put("time", getOffsetDeletedTime(String.valueOf(System.currentTimeMillis())));
                                reportMap.put("statement", textInputEditText.getText().toString());

                                String reporterId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                reportRef.child(reporterId).setValue(reportMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            communityReportRef.setValue("T");
                                            pDialog.dismiss();
                                            startActivity(new Intent(ReportActivity.this,SplashScreenActivity.class));
                                            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                            finish();
                                        } else {
                                            pDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), "Attempt failed , please try again", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addButton("CANCEL",
                        colorPrimary,
                        cf_alert_dialogue_dim_bg,
                        CFAlertDialog.CFAlertActionStyle.DEFAULT,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });

        builder.show();
    }



    @Override
    public void onBackPressed() {
        startActivity(new Intent(ReportActivity.this,SplashScreenActivity.class));
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        finish();
    }
}
