package com.integrals.inlens.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.widget.EditText;

import com.integrals.inlens.R;

public class PhoneAuth extends AppCompatActivity {
    EditText phoneEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

    }
}
