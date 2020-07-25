package com.integrals.inlens.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.integrals.inlens.Helper.CountryItem;
import com.integrals.inlens.Helper.HttpHandler;
import com.integrals.inlens.Helper.SnackShow;
import com.integrals.inlens.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {


    private EditText AuthEditText, AllCountryEditText, VerifyEditText;
    private ImageButton AuthNextButton, VerifyBackButton ;

    private TextView AuthCodeButton, CustomToastTitle, CustomToastMessage, VerifyCounter, VerifyTextView, VerifyTextViewNote;
    private InputMethodManager AuthIMM;
    private Animation FadeIn, FadeOut;
    private RelativeLayout AuthContainer;
    private ArrayList<CountryItem> CountryList, SearchList;
    private CountryAdapter AllAdapter, SearchAdapter;
    private RecyclerView AllCountryRecyclerView;
    private Dialog AllCountryDialog, VerificationDialog;
    private String ChoosenCode, VerificationID;
    private ProgressBar CustomToastProgressbar, VerifyProgressbar;
    private Button VerifyGoManualButton,VerifyManualButton;;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks Callbacks;
    private CountDownTimer countDownTimer;
    private RelativeLayout authRoot;

    String appTheme = "";
    int cf_bg_color, colorPrimary, default_bg_color, cf_alert_dialogue_dim_bg;


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

        if (appTheme.equals(AppConstants.themeLight)) {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            default_bg_color = getResources().getColor(R.color.Light_default_bg_color);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);
        } else {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            default_bg_color = getResources().getColor(R.color.Dark_default_bg_color);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        authRoot = findViewById(R.id.rootAuth);
        InitCountryNames();
        InitAllCountryDialog();
        VerifyDialogInit();

        AuthEditText = findViewById(R.id.auth_edittext);
        AuthCodeButton = findViewById(R.id.auth_countrycode_picker);
        AuthNextButton = findViewById(R.id.auth_next_button);
        AuthContainer = findViewById(R.id.auth_container);


        AuthIMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        FadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        FadeOut = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);

        AuthNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!AuthCodeButton.isShown() && !AuthEditText.isShown()) {
                    AuthContainer.clearAnimation();
                    AuthContainer.setAnimation(FadeOut);
                    AuthContainer.getAnimation().start();
                    AuthContainer.setVisibility(View.GONE);

                    AuthCodeButton.setVisibility(View.VISIBLE);
                    AuthEditText.setVisibility(View.VISIBLE);

                    AuthContainer.clearAnimation();
                    AuthContainer.setAnimation(FadeIn);
                    AuthContainer.getAnimation().start();
                    AuthContainer.setVisibility(View.VISIBLE);

                    findViewById(R.id.Name).setAnimation(FadeOut);
                    findViewById(R.id.appicon).setAnimation(FadeOut);

                    findViewById(R.id.Name).setVisibility(View.INVISIBLE);
                    findViewById(R.id.appicon).setVisibility(View.INVISIBLE);

                    getDefaultCountry();


                } else if (AuthCodeButton.isShown() && AuthEditText.isShown() && !TextUtils.isEmpty(AuthEditText.getText().toString())) {
                    if (!TextUtils.isEmpty(ChoosenCode)) {
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(ChoosenCode + AuthEditText.getText().toString(), 60, TimeUnit.SECONDS, AuthActivity.this, Callbacks);
                        countDownTimer = new CountDownTimer(60000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                VerifyCounter.setText(String.format("Automatic Verification : %d s", millisUntilFinished / 1000));
                            }

                            public void onFinish() {
                                VerifyCounter.setText("Please wait.");
                                EnableManualVerification();
                            }

                        }.start();
                        VerificationDialog.show();
                    } else {
                        SnackShow snackShow=new SnackShow(authRoot,AuthActivity.this);
                        snackShow.showErrorSnack("Please select your country code");
                    }
                } else {
                    SnackShow snackShow=new SnackShow(authRoot,AuthActivity.this);
                    snackShow.showErrorSnack("Phone Number missing...");
                }

            }
        });


        AuthCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AllCountryRecyclerView.removeAllViews();
                AllCountryRecyclerView.setAdapter(AllAdapter);
                AllCountryEditText.setText("");
                AllCountryDialog.show();

            }
        });

        Callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                SnackShow snackShow=new SnackShow(authRoot,AuthActivity.this);
                VerifyEditText.setText(phoneAuthCredential.getSmsCode());
                snackShow.showSuccessSnack("Sign in successful");
                SignInWithCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                /*
                VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
                VerifyTextViewNote.setText("*NOTE \nVerification failed. Please try after sometime.");
                VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
                VerifyTextViewNote.setVisibility(View.VISIBLE);
                 */
                if (VerificationDialog != null) {
                    VerificationDialog.dismiss();
                }
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                Log.i("authTag", "e " + e.toString());
                showDialogMessageFailed("Authentication Failed", e.getMessage());

                Log.i(AppConstants.AUTH, e.getMessage());
            }

            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                VerificationID = s;
            }
        };


    }





    private class getConfirmedList extends AsyncTask<Void, Void, Void> {

        String countryCode;

        @Override
        protected Void doInBackground(Void... voids) {

            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "http://ip-api.com/json";
            String jsonStr = "";
            jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null && !jsonStr.equals("")) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
//                    Log.i("json","data "+jsonObj);
                    countryCode = jsonObj.getString("countryCode");
//                    Log.i("json","data "+countryCode);

                } catch (final JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), " Failed to detect country.", Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!TextUtils.isEmpty(countryCode))
            {
                for (int i = 0; i < CountryList.size(); i++) {
                    if (CountryList.get(i).getCountryName().toLowerCase().equals(countryCode.toLowerCase())) {
                        CountryItem countryItem = CountryList.get(i);
                        AuthCodeButton.setText(countryItem.getCountryName().toUpperCase() + " " + String.format("+ %s", countryItem.getCountryCode()));
                        ChoosenCode = String.format("+%s", countryItem.getCountryCode());
//                            Log.i("authTag", " country "+ChoosenCode );

                        break;
                    }

                }
            }
        }
    }


    private void getDefaultCountry() {

        try {

            TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
            String countryCodeValue = tm.getNetworkCountryIso();
            if(TextUtils.isEmpty(countryCodeValue))
            {

                new getConfirmedList().execute();
            }

            for (int i = 0; i < CountryList.size(); i++) {
                if (CountryList.get(i).getCountryName().toLowerCase().equals(countryCodeValue)) {
                    CountryItem countryItem = CountryList.get(i);
                    AuthCodeButton.setText(countryItem.getCountryName().toUpperCase() + " " + String.format("+ %s", countryItem.getCountryCode()));
                    ChoosenCode = String.format("+%s", countryItem.getCountryCode());
                    Log.i("authTag", " country "+ChoosenCode );

                    break;
                }

            }


        } catch (Exception e) {
            Log.i("authTag", "" + e.toString());
        }

    }

    private void SignInWithCredential(PhoneAuthCredential phoneAuthCredential) {

        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("number").setValue(ChoosenCode + AuthEditText.getText().toString());
                    VerificationDialog.dismiss();
                    startActivity(new Intent(AuthActivity.this, UserNameInfoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (!task.isSuccessful()) {
                    countDownTimer.cancel();

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
                        VerifyTextViewNote.setText("*NOTE \nVerification code is invalid or has expired.");
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
                        VerifyTextViewNote.setVisibility(View.VISIBLE);
                    } catch (FirebaseAuthInvalidUserException e) {
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
                        VerifyTextViewNote.setText("*NOTE \nThe corresponding account has been blocked. Please contact FoodyGuide help-desk.");
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
                        VerifyTextViewNote.setVisibility(View.VISIBLE);
                    } catch (RuntimeException e) {
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
                        VerifyTextViewNote.setText("*NOTE \nVerification failed due to runtime exception. Please try after sometime.");
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
                        VerifyTextViewNote.setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
                        VerifyTextViewNote.setText("*NOTE \nVerification failed due to unknown exception. Please try after sometime.");
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
                        VerifyTextViewNote.setVisibility(View.VISIBLE);

                    }


                } else {
                    showDialogMessageFailed("Registration Failed", " Registration failed due to  some unknown reasons.Please try after sometime.");
                }
            }
        });

    }

    private void EnableManualVerification() {

        VerifyGoManualButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
        VerifyGoManualButton.setVisibility(View.GONE);
        VerifyCounter.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
        VerifyCounter.setVisibility(View.GONE);
        VerifyProgressbar.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
        VerifyProgressbar.setVisibility(View.GONE);
        VerifyEditText.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
        VerifyEditText.setVisibility(View.VISIBLE);

        VerifyTextView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
        VerifyTextView.setText("Manual Verification");
        VerifyTextView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
        VerifyTextViewNote.setVisibility(View.VISIBLE);
        VerifyManualButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
        VerifyManualButton.setVisibility(View.VISIBLE);
    }

    private void VerifyDialogInit() {

        VerificationDialog = new Dialog(this);
        VerificationDialog.setCancelable(false);
        VerificationDialog.setContentView(R.layout.verification_layout);
        VerifyProgressbar = VerificationDialog.findViewById(R.id.verification_progressbar);
        VerifyEditText = VerificationDialog.findViewById(R.id.verification_edittext);
        VerifyTextView = VerificationDialog.findViewById(R.id.verification_textview);
        VerifyTextViewNote = VerificationDialog.findViewById(R.id.verification_textviewnote);
        VerifyManualButton = VerificationDialog.findViewById(R.id.verification_done_button);
        VerifyBackButton = VerificationDialog.findViewById(R.id.verification_back_button);
        VerifyCounter = VerificationDialog.findViewById(R.id.verification_counter);
        VerifyGoManualButton = VerificationDialog.findViewById(R.id.verification_manual_verify_button);

        VerificationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //VerifyManualButton.setVisibility(View.GONE);
        VerifyTextView.setText("Verifying");
        //VerifyEditText.setVisibility(View.GONE);
        VerifyProgressbar.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        VerifyProgressbar.setVisibility(View.VISIBLE);
        //VerifyTextViewNote.setVisibility(View.INVISIBLE);

        VerifyBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                VerificationDialog.dismiss();
                countDownTimer.cancel();

            }
        });

        VerifyManualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(VerifyEditText.getText().toString())) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VerificationID, VerifyEditText.getText().toString());
                    SignInWithCredential(credential);
                } else {
                    VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
                    VerifyTextViewNote.setText("*NOTE \nVerification field is empty");
                    VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
                    VerifyTextViewNote.setVisibility(View.VISIBLE);
                }


            }
        });

        VerifyGoManualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EnableManualVerification();

            }
        });
    }


    private void InitAllCountryDialog() {

        AllCountryDialog = new Dialog(this);
        AllCountryDialog.setCancelable(false);
        AllCountryDialog.setCanceledOnTouchOutside(true);
        AllCountryDialog.setContentView(R.layout.all_country_layout);

        AllCountryEditText = AllCountryDialog.findViewById(R.id.all_country_edittext);
        AllCountryRecyclerView = AllCountryDialog.findViewById(R.id.all_country_recyclerview);
        AllCountryRecyclerView.setHasFixedSize(true);
        AllCountryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        AllAdapter = new CountryAdapter(this, CountryList);
        AllCountryRecyclerView.setAdapter(AllAdapter);
        SearchList = new ArrayList<>();
        AllCountryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (!TextUtils.isEmpty(editable.toString())) {
                    AllCountryRecyclerView.removeAllViews();
                    SearchList.clear();

                    for (int i = 0; i < CountryList.size(); i++) {
                        if (CountryList.get(i).getFullCountryName().toLowerCase().contains(editable.toString().toLowerCase())) {
                            CountryItem item = CountryList.get(i);
                            SearchList.add(item);
                        }
                    }

                    SearchAdapter = new CountryAdapter(AuthActivity.this, SearchList);
                    AllCountryRecyclerView.setAdapter(SearchAdapter);
                } else {
                    AllCountryRecyclerView.removeAllViews();
                    AllCountryRecyclerView.setAdapter(AllAdapter);
                }

            }
        });

    }

    public void showDialogMessageFailed(String title, String message) {
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(AuthActivity.this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle(title)
                .setIcon(R.drawable.ic_cancel_black_24dp)
                .setDialogBackgroundColor(default_bg_color)
                .setTextColor(colorPrimary)
                .setMessage(message)
                .setCancelable(false)
                .addButton("OK",
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
    private void InitCountryNames() {

        CountryList = new ArrayList<>();
        CountryList.add(new CountryItem("ad", "376", "Andorra"));
        CountryList.add(new CountryItem("ae", "971", "United Arab Emirates (UAE)"));
        CountryList.add(new CountryItem("af", "93", "Afghanistan"));
        CountryList.add(new CountryItem("ag", "1", "Antigua and Barbuda"));
        CountryList.add(new CountryItem("ai", "1", "Anguilla"));
        CountryList.add(new CountryItem("al", "355", "Albania"));
        CountryList.add(new CountryItem("am", "374", "Armenia"));
        CountryList.add(new CountryItem("ao", "244", "Angola"));
        CountryList.add(new CountryItem("aq", "672", "Antarctica"));
        CountryList.add(new CountryItem("ar", "54", "Argentina"));
        CountryList.add(new CountryItem("as", "1", "American Samoa"));
        CountryList.add(new CountryItem("at", "43", "Austria"));
        CountryList.add(new CountryItem("au", "61", "Australia"));
        CountryList.add(new CountryItem("aw", "297", "Aruba"));
        CountryList.add(new CountryItem("ax", "358", "Åland Islands"));
        CountryList.add(new CountryItem("az", "994", "Azerbaijan"));
        CountryList.add(new CountryItem("ba", "387", "Bosnia And Herzegovina"));
        CountryList.add(new CountryItem("bb", "1", "Barbados"));
        CountryList.add(new CountryItem("bd", "880", "Bangladesh"));
        CountryList.add(new CountryItem("be", "32", "Belgium"));
        CountryList.add(new CountryItem("bf", "226", "Burkina Faso"));
        CountryList.add(new CountryItem("bg", "359", "Bulgaria"));
        CountryList.add(new CountryItem("bh", "973", "Bahrain"));
        CountryList.add(new CountryItem("bi", "257", "Burundi"));
        CountryList.add(new CountryItem("bj", "229", "Benin"));
        CountryList.add(new CountryItem("bl", "590", "Saint Barthélemy"));
        CountryList.add(new CountryItem("bm", "1", "Bermuda"));
        CountryList.add(new CountryItem("bn", "673", "Brunei Darussalam"));
        CountryList.add(new CountryItem("bo", "591", "Bolivia, Plurinational State Of"));
        CountryList.add(new CountryItem("br", "55", "Brazil"));
        CountryList.add(new CountryItem("bs", "1", "Bahamas"));
        CountryList.add(new CountryItem("bt", "975", "Bhutan"));
        CountryList.add(new CountryItem("bw", "267", "Botswana"));
        CountryList.add(new CountryItem("by", "375", "Belarus"));
        CountryList.add(new CountryItem("bz", "501", "Belize"));
        CountryList.add(new CountryItem("ca", "1", "Canada"));
        CountryList.add(new CountryItem("cc", "61", "Cocos (keeling) Islands"));
        CountryList.add(new CountryItem("cd", "243", "Congo, The Democratic Republic Of The"));
        CountryList.add(new CountryItem("cf", "236", "Central African Republic"));
        CountryList.add(new CountryItem("cg", "242", "Congo"));
        CountryList.add(new CountryItem("ch", "41", "Switzerland"));
        CountryList.add(new CountryItem("ci", "225", "Côte D'ivoire"));
        CountryList.add(new CountryItem("ck", "682", "Cook Islands"));
        CountryList.add(new CountryItem("cl", "56", "Chile"));
        CountryList.add(new CountryItem("cm", "237", "Cameroon"));
        CountryList.add(new CountryItem("cn", "86", "China"));
        CountryList.add(new CountryItem("co", "57", "Colombia"));
        CountryList.add(new CountryItem("cr", "506", "Costa Rica"));
        CountryList.add(new CountryItem("cu", "53", "Cuba"));
        CountryList.add(new CountryItem("cv", "238", "Cape Verde"));
        CountryList.add(new CountryItem("cw", "599", "Curaçao"));
        CountryList.add(new CountryItem("cx", "61", "Christmas Island"));
        CountryList.add(new CountryItem("cy", "357", "Cyprus"));
        CountryList.add(new CountryItem("cz", "420", "Czech Republic"));
        CountryList.add(new CountryItem("de", "49", "Germany"));
        CountryList.add(new CountryItem("dj", "253", "Djibouti"));
        CountryList.add(new CountryItem("dk", "45", "Denmark"));
        CountryList.add(new CountryItem("dm", "1", "Dominica"));
        CountryList.add(new CountryItem("do", "1", "Dominican Republic"));
        CountryList.add(new CountryItem("dz", "213", "Algeria"));
        CountryList.add(new CountryItem("ec", "593", "Ecuador"));
        CountryList.add(new CountryItem("ee", "372", "Estonia"));
        CountryList.add(new CountryItem("eg", "20", "Egypt"));
        CountryList.add(new CountryItem("er", "291", "Eritrea"));
        CountryList.add(new CountryItem("es", "34", "Spain"));
        CountryList.add(new CountryItem("et", "251", "Ethiopia"));
        CountryList.add(new CountryItem("fi", "358", "Finland"));
        CountryList.add(new CountryItem("fj", "679", "Fiji"));
        CountryList.add(new CountryItem("fk", "500", "Falkland Islands (malvinas)"));
        CountryList.add(new CountryItem("fm", "691", "Micronesia, Federated States Of"));
        CountryList.add(new CountryItem("fo", "298", "Faroe Islands"));
        CountryList.add(new CountryItem("fr", "33", "France"));
        CountryList.add(new CountryItem("ga", "241", "Gabon"));
        CountryList.add(new CountryItem("gb", "44", "United Kingdom"));
        CountryList.add(new CountryItem("gd", "1", "Grenada"));
        CountryList.add(new CountryItem("ge", "995", "Georgia"));
        CountryList.add(new CountryItem("gf", "594", "French Guyana"));
        CountryList.add(new CountryItem("gh", "233", "Ghana"));
        CountryList.add(new CountryItem("gi", "350", "Gibraltar"));
        CountryList.add(new CountryItem("gl", "299", "Greenland"));
        CountryList.add(new CountryItem("gm", "220", "Gambia"));
        CountryList.add(new CountryItem("gn", "224", "Guinea"));
        CountryList.add(new CountryItem("gp", "450", "Guadeloupe"));
        CountryList.add(new CountryItem("gq", "240", "Equatorial Guinea"));
        CountryList.add(new CountryItem("gr", "30", "Greece"));
        CountryList.add(new CountryItem("gt", "502", "Guatemala"));
        CountryList.add(new CountryItem("gu", "1", "Guam"));
        CountryList.add(new CountryItem("gw", "245", "Guinea-bissau"));
        CountryList.add(new CountryItem("gy", "592", "Guyana"));
        CountryList.add(new CountryItem("hk", "852", "Hong Kong"));
        CountryList.add(new CountryItem("hn", "504", "Honduras"));
        CountryList.add(new CountryItem("hr", "385", "Croatia"));
        CountryList.add(new CountryItem("ht", "509", "Haiti"));
        CountryList.add(new CountryItem("hu", "36", "Hungary"));
        CountryList.add(new CountryItem("id", "62", "Indonesia"));
        CountryList.add(new CountryItem("ie", "353", "Ireland"));
        CountryList.add(new CountryItem("il", "972", "Israel"));
        CountryList.add(new CountryItem("im", "44", "Isle Of Man"));
        CountryList.add(new CountryItem("is", "354", "Iceland"));
        CountryList.add(new CountryItem("in", "91", "India"));
        CountryList.add(new CountryItem("io", "246", "British Indian Ocean Territory"));
        CountryList.add(new CountryItem("iq", "964", "Iraq"));
        CountryList.add(new CountryItem("ir", "98", "Iran, Islamic Republic Of"));
        CountryList.add(new CountryItem("it", "39", "Italy"));
        CountryList.add(new CountryItem("je", "44", "Jersey "));
        CountryList.add(new CountryItem("jm", "1", "Jamaica"));
        CountryList.add(new CountryItem("jo", "962", "Jordan"));
        CountryList.add(new CountryItem("jp", "81", "Japan"));
        CountryList.add(new CountryItem("ke", "254", "Kenya"));
        CountryList.add(new CountryItem("kg", "996", "Kyrgyzstan"));
        CountryList.add(new CountryItem("kh", "855", "Cambodia"));
        CountryList.add(new CountryItem("ki", "686", "Kiribati"));
        CountryList.add(new CountryItem("km", "269", "Comoros"));
        CountryList.add(new CountryItem("kn", "1", "Saint Kitts and Nevis"));
        CountryList.add(new CountryItem("kp", "850", "North Korea"));
        CountryList.add(new CountryItem("kr", "82", "South Korea"));
        CountryList.add(new CountryItem("kw", "965", "Kuwait"));
        CountryList.add(new CountryItem("ky", "1", "Cayman Islands"));
        CountryList.add(new CountryItem("kz", "7", "Kazakhstan"));
        CountryList.add(new CountryItem("la", "856", "Lao People's Democratic Republic"));
        CountryList.add(new CountryItem("lb", "961", "Lebanon"));
        CountryList.add(new CountryItem("lc", "1", "Saint Lucia"));
        CountryList.add(new CountryItem("li", "423", "Liechtenstein"));
        CountryList.add(new CountryItem("lk", "94", "Sri Lanka"));
        CountryList.add(new CountryItem("lr", "231", "Liberia"));
        CountryList.add(new CountryItem("ls", "266", "Lesotho"));
        CountryList.add(new CountryItem("lt", "370", "Lithuania"));
        CountryList.add(new CountryItem("lu", "352", "Luxembourg"));
        CountryList.add(new CountryItem("lv", "371", "Latvia"));
        CountryList.add(new CountryItem("ly", "218", "Libya"));
        CountryList.add(new CountryItem("ma", "212", "Morocco"));
        CountryList.add(new CountryItem("mc", "377", "Monaco"));
        CountryList.add(new CountryItem("md", "373", "Moldova, Republic Of"));
        CountryList.add(new CountryItem("me", "382", "Montenegro"));
        CountryList.add(new CountryItem("mf", "590", "Saint Martin"));
        CountryList.add(new CountryItem("mg", "261", "Madagascar"));
        CountryList.add(new CountryItem("mh", "692", "Marshall Islands"));
        CountryList.add(new CountryItem("mk", "389", "Macedonia (FYROM)"));
        CountryList.add(new CountryItem("ml", "223", "Mali"));
        CountryList.add(new CountryItem("mm", "95", "Myanmar"));
        CountryList.add(new CountryItem("mn", "976", "Mongolia"));
        CountryList.add(new CountryItem("mo", "853", "Macau"));
        CountryList.add(new CountryItem("mp", "1", "Northern Mariana Islands"));
        CountryList.add(new CountryItem("mq", "596", "Martinique"));
        CountryList.add(new CountryItem("mr", "222", "Mauritania"));
        CountryList.add(new CountryItem("ms", "1", "Montserrat"));
        CountryList.add(new CountryItem("mt", "356", "Malta"));
        CountryList.add(new CountryItem("mu", "230", "Mauritius"));
        CountryList.add(new CountryItem("mv", "960", "Maldives"));
        CountryList.add(new CountryItem("mw", "265", "Malawi"));
        CountryList.add(new CountryItem("mx", "52", "Mexico"));
        CountryList.add(new CountryItem("my", "60", "Malaysia"));
        CountryList.add(new CountryItem("mz", "258", "Mozambique"));
        CountryList.add(new CountryItem("na", "264", "Namibia"));
        CountryList.add(new CountryItem("nc", "687", "New Caledonia"));
        CountryList.add(new CountryItem("ne", "227", "Niger"));
        CountryList.add(new CountryItem("nf", "672", "Norfolk Islands"));
        CountryList.add(new CountryItem("ng", "234", "Nigeria"));
        CountryList.add(new CountryItem("ni", "505", "Nicaragua"));
        CountryList.add(new CountryItem("nl", "31", "Netherlands"));
        CountryList.add(new CountryItem("no", "47", "Norway"));
        CountryList.add(new CountryItem("np", "977", "Nepal"));
        CountryList.add(new CountryItem("nr", "674", "Nauru"));
        CountryList.add(new CountryItem("nu", "683", "Niue"));
        CountryList.add(new CountryItem("nz", "64", "New Zealand"));
        CountryList.add(new CountryItem("om", "968", "Oman"));
        CountryList.add(new CountryItem("pa", "507", "Panama"));
        CountryList.add(new CountryItem("pe", "51", "Peru"));
        CountryList.add(new CountryItem("pf", "689", "French Polynesia"));
        CountryList.add(new CountryItem("pg", "675", "Papua New Guinea"));
        CountryList.add(new CountryItem("ph", "63", "Philippines"));
        CountryList.add(new CountryItem("pk", "92", "Pakistan"));
        CountryList.add(new CountryItem("pl", "48", "Poland"));
        CountryList.add(new CountryItem("pm", "508", "Saint Pierre And Miquelon"));
        CountryList.add(new CountryItem("pn", "870", "Pitcairn Islands"));
        CountryList.add(new CountryItem("pr", "1", "Puerto Rico"));
        CountryList.add(new CountryItem("ps", "970", "Palestine"));
        CountryList.add(new CountryItem("pt", "351", "Portugal"));
        CountryList.add(new CountryItem("pw", "680", "Palau"));
        CountryList.add(new CountryItem("py", "595", "Paraguay"));
        CountryList.add(new CountryItem("qa", "974", "Qatar"));
        CountryList.add(new CountryItem("re", "262", "Réunion"));
        CountryList.add(new CountryItem("ro", "40", "Romania"));
        CountryList.add(new CountryItem("rs", "381", "Serbia"));
        CountryList.add(new CountryItem("ru", "7", "Russian Federation"));
        CountryList.add(new CountryItem("rw", "250", "Rwanda"));
        CountryList.add(new CountryItem("sa", "966", "Saudi Arabia"));
        CountryList.add(new CountryItem("sb", "677", "Solomon Islands"));
        CountryList.add(new CountryItem("sc", "248", "Seychelles"));
        CountryList.add(new CountryItem("sd", "249", "Sudan"));
        CountryList.add(new CountryItem("se", "46", "Sweden"));
        CountryList.add(new CountryItem("sg", "65", "Singapore"));
        CountryList.add(new CountryItem("sh", "290", "Saint Helena, Ascension And Tristan Da Cunha"));
        CountryList.add(new CountryItem("si", "386", "Slovenia"));
        CountryList.add(new CountryItem("sk", "421", "Slovakia"));
        CountryList.add(new CountryItem("sl", "232", "Sierra Leone"));
        CountryList.add(new CountryItem("sm", "378", "San Marino"));
        CountryList.add(new CountryItem("sn", "221", "Senegal"));
        CountryList.add(new CountryItem("so", "252", "Somalia"));
        CountryList.add(new CountryItem("sr", "597", "Suriname"));
        CountryList.add(new CountryItem("ss", "211", "South Sudan"));
        CountryList.add(new CountryItem("st", "239", "Sao Tome And Principe"));
        CountryList.add(new CountryItem("sv", "503", "El Salvador"));
        CountryList.add(new CountryItem("sx", "1", "Sint Maarten"));
        CountryList.add(new CountryItem("sy", "963", "Syrian Arab Republic"));
        CountryList.add(new CountryItem("sz", "268", "Swaziland"));
        CountryList.add(new CountryItem("tc", "1", "Turks and Caicos Islands"));
        CountryList.add(new CountryItem("td", "235", "Chad"));
        CountryList.add(new CountryItem("tg", "228", "Togo"));
        CountryList.add(new CountryItem("th", "66", "Thailand"));
        CountryList.add(new CountryItem("tj", "992", "Tajikistan"));
        CountryList.add(new CountryItem("tk", "690", "Tokelau"));
        CountryList.add(new CountryItem("tl", "670", "Timor-leste"));
        CountryList.add(new CountryItem("tm", "993", "Turkmenistan"));
        CountryList.add(new CountryItem("tn", "216", "Tunisia"));
        CountryList.add(new CountryItem("to", "676", "Tonga"));
        CountryList.add(new CountryItem("tr", "90", "Turkey"));
        CountryList.add(new CountryItem("tt", "1", "Trinidad &amp; Tobago"));
        CountryList.add(new CountryItem("tv", "688", "Tuvalu"));
        CountryList.add(new CountryItem("tw", "886", "Taiwan"));
        CountryList.add(new CountryItem("tz", "255", "Tanzania, United Republic Of"));
        CountryList.add(new CountryItem("ua", "380", "Ukraine"));
        CountryList.add(new CountryItem("ug", "256", "Uganda"));
        CountryList.add(new CountryItem("us", "1", "United States"));
        CountryList.add(new CountryItem("uy", "598", "Uruguay"));
        CountryList.add(new CountryItem("uz", "998", "Uzbekistan"));
        CountryList.add(new CountryItem("va", "379", "Holy See (vatican City State)"));
        CountryList.add(new CountryItem("vc", "1", "Saint Vincent &amp; The Grenadines"));
        CountryList.add(new CountryItem("ve", "58", "Venezuela, Bolivarian Republic Of"));
        CountryList.add(new CountryItem("vg", "1", "British Virgin Islands"));
        CountryList.add(new CountryItem("vi", "1", "US Virgin Islands"));
        CountryList.add(new CountryItem("vn", "84", "Vietnam"));
        CountryList.add(new CountryItem("vu", "678", "Vanuatu"));
        CountryList.add(new CountryItem("wf", "681", "Wallis And Futuna"));
        CountryList.add(new CountryItem("ws", "685", "Samoa"));
        CountryList.add(new CountryItem("xk", "383", "Kosovo"));
        CountryList.add(new CountryItem("ye", "967", "Yemen"));
        CountryList.add(new CountryItem("yt", "262", "Mayotte"));
        CountryList.add(new CountryItem("za", "27", "South Africa"));
        CountryList.add(new CountryItem("zm", "260", "Zambia"));
        CountryList.add(new CountryItem("zw", "263", "Zimbabwe"));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (AuthIMM.isAcceptingText()) {
            AuthIMM.hideSoftInputFromWindow(AuthEditText.getWindowToken(), 0);
        }

    }

    public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.CountryViewHolder> {

        Context context;
        List<CountryItem> CountryList;

        public CountryAdapter(Context context, List<CountryItem> countryList) {
            this.context = context;
            CountryList = countryList;
        }

        @NonNull
        @Override
        public CountryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CountryAdapter.CountryViewHolder(LayoutInflater.from(context).inflate(R.layout.country_spinner_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CountryViewHolder holder, int position) {

            final CountryItem countryItem = CountryList.get(position);
            holder.CountryName.setText(countryItem.getCountryName().toUpperCase());
            holder.CountryCode.setText(String.format("+ %s", countryItem.getCountryCode()));
            holder.CountryFullname.setText(countryItem.getFullCountryName().toUpperCase());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AllCountryDialog.dismiss();
                    AuthCodeButton.setText(countryItem.getCountryName().toUpperCase() + " " + String.format("+ %s", countryItem.getCountryCode()));
                    ChoosenCode = String.format("+%s", countryItem.getCountryCode());

                }
            });
        }

        @Override
        public int getItemCount() {
            return CountryList.size();
        }

        public class CountryViewHolder extends RecyclerView.ViewHolder {

            TextView CountryName, CountryCode, CountryFullname;


            public CountryViewHolder(View itemView) {
                super(itemView);

                CountryName = itemView.findViewById(R.id.spinner_country_name);
                CountryCode = itemView.findViewById(R.id.spinner_country_code);
                CountryFullname = itemView.findViewById(R.id.spinner_country_fullname);
            }
        }
    }

}
