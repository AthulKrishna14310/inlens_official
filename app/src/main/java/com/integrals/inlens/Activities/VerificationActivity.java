package com.integrals.inlens.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.SnackShow;
import com.integrals.inlens.R;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;
import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {
    private OtpView otpView;
    private String phoneNumber="";
    private String countryCode="";
    private TextView verifyCounter;
    private CountDownTimer countDownTimer;
    private RelativeLayout verifyLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks Callbacks;
    private String verificationID;
    private ProgressBar progressBar;
    private ImageButton forwardButton;
    private boolean verified=false;
    private String appTheme="";
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
        setContentView(R.layout.activity_verification);

        otpView=findViewById(R.id.verification_edittext);
        verifyCounter=findViewById(R.id.timeoutText);
        verifyLayout=findViewById(R.id.verify_layout);
        progressBar=findViewById(R.id.progress_circular);
        forwardButton=findViewById(R.id.forward);
        countryCode=getIntent().getStringExtra("Country");
        phoneNumber=getIntent().getStringExtra("Phone");
        progressBar.setVisibility(View.INVISIBLE);

    }


    @Override
    protected void onStart() {
        super.onStart();

        Callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                verified=true;
                SnackShow snackShow=new SnackShow(verifyLayout,VerificationActivity.this);
                snackShow.showSuccessSnack("Sign in successful");
                otpView.setText(phoneAuthCredential.getSmsCode());
                signInWithCredential(phoneAuthCredential);


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                new SnackShow(verifyLayout,VerificationActivity.this)
                        .showErrorSnack(e.getMessage());

                Log.i(AppConstants.AUTH,e.getMessage());
                progressBar.setVisibility(View.INVISIBLE);
                forwardButton.setVisibility(View.VISIBLE);
                forwardButton.setImageDrawable(getDrawable(R.drawable.ic_close_));
                verified=false;
            }

            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                verificationID = s;
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber("+"+countryCode + phoneNumber, 60, TimeUnit.SECONDS, VerificationActivity.this, Callbacks);
        countDownTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                verifyCounter.setText(String.format("Automatic Verification : %d s", millisUntilFinished / 1000));
                forwardButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

            }

            public void onFinish() {
                verifyCounter.setText("Please type your otp manually.");
                progressBar.setVisibility(View.INVISIBLE);
                forwardButton.setVisibility(View.VISIBLE);
            }

        }.start();


        otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                if(verified==false) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, otp);
                    signInWithCredential(credential);
                }
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verified==false){
                    finish();
                }
            }
        });

    }

    private void signInWithCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("number")
                            .setValue(countryCode + phoneNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                startActivity(new Intent(VerificationActivity.this, UserNameInfoActivity.class));
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finishAffinity();
                            }
                        }
                    });



                } else if (!task.isSuccessful()) {
                    countDownTimer.cancel();
                    progressBar.setVisibility(View.INVISIBLE);
                    forwardButton.setVisibility(View.VISIBLE);
                    forwardButton.setImageDrawable(getDrawable(R.drawable.ic_close_));
                    verified=false;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        new SnackShow(verifyLayout,VerificationActivity.this).showErrorSnack("Verification code is invalid or has expired.");
                       } catch (FirebaseAuthInvalidUserException e) {
                        new SnackShow(verifyLayout,VerificationActivity.this).showErrorSnack("Your account was blocked.");
                    } catch (RuntimeException e) {
                        new SnackShow(verifyLayout,VerificationActivity.this).showErrorSnack("Unknown error , Please check your network and try again later.");

                    } catch (Exception e) {
                        new SnackShow(verifyLayout,VerificationActivity.this).showErrorSnack("Unknown error , Please check your network and try again later.");
                    }
                } else {
                    new SnackShow(verifyLayout,VerificationActivity.this).showErrorSnack("Unknown error , Please check your network and try again later.");
                }
            }
        });

    }
}
