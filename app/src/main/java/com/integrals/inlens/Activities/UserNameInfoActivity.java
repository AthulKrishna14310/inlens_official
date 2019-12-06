package com.integrals.inlens.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserNameInfoActivity extends AppCompatActivity {

    private DatabaseReference Ref;
    private FirebaseAuth Auth;

    private EditText UserNameEdittext,UserEmailEdittext;
    private TextView UserNameTextview;
    private ImageButton UserNameDoneButton , MyToolbarBackButton ;
    private View MyToolbar;

    private String Name,Email;

    private Toast toast;
    private TextView  CustomToastTitle, CustomToastMessage;
    private ProgressBar CustomToastProgressbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name_info);

        FirebaseInit();
        CheckUserAuthentication();
        VariablesInit();
        InitCustomToast();

        Ref.child("Users").child(Auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("Name"))
                {
                    Name = dataSnapshot.child("Name").getValue().toString();
                    UserNameEdittext.setText(Name);
                }
                if(dataSnapshot.hasChild("Email"))
                {
                    Email = dataSnapshot.child("Email").getValue().toString();
                    UserEmailEdittext.setText(Email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UserNameDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Name = UserNameEdittext.getText().toString();
                Email = UserEmailEdittext.getText().toString();

                if(!TextUtils.isEmpty(Name) && !TextUtils.isEmpty(Email))
                {
                    PreOperationCheck check = new PreOperationCheck();
                    check.hideSoftKeyboard(UserNameInfoActivity.this,UserNameEdittext);


                    Ref.child("Users").child(Auth.getCurrentUser().getUid()).child("Name").setValue(Name).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {


                                Ref.child("Users").child(Auth.getCurrentUser().getUid()).child("Email").setValue(Email);
                                startActivity(new Intent(UserNameInfoActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK).putExtra("ShowTour",true));
                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                finish();
                            }
                            else
                            {
                                final String info = UserNameTextview.getText().toString();
                                UserNameTextview.setText(task.getResult().toString());
                                UserNameTextview.setTextColor(Color.parseColor("#ff3815"));
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        UserNameTextview.setText(info);
                                        UserNameTextview.setTextColor(Color.parseColor("#cdcdcd"));

                                    }
                                },5000);
                            }
                        }
                    });
                }
                else
                {
                    if(TextUtils.isEmpty(Name) && TextUtils.isEmpty(Email))
                    {
                        ShowCustomToast("Fields Missing","Please type in your username and email.",false,100);
                    }
                    else if(TextUtils.isEmpty(Name))
                    {
                        ShowCustomToast("Name Missing","Please type in your username",false,100);
                    }
                    else if(TextUtils.isEmpty(Email))
                    {
                        ShowCustomToast("Email Missing","Please type in your email",false,100);
                    }
                    else
                    {
                        ShowCustomToast("Fields Missing","Please type in your username and email.",false,100);

                    }
                }



            }
        });

    }


    private void ShowCustomToast(String Title, String Message, boolean isProgressbarShown, int duration) {

        CustomToastTitle.setText(Title);
        CustomToastMessage.setText(Message);
        toast.setDuration(duration);
        if(isProgressbarShown)
            CustomToastProgressbar.setVisibility(View.VISIBLE);
        else
            CustomToastProgressbar.setVisibility(View.GONE);

        toast.show();

    }

    private void InitCustomToast() {
        toast = new Toast(getApplicationContext());
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_toast_layout,null);
        toast.setView(view);
        toast.setGravity(Gravity.BOTTOM,0,100);

        CustomToastTitle = view.findViewById(R.id.custom_toast_title);
        CustomToastMessage= view.findViewById(R.id.custom_toast_message);
        CustomToastProgressbar = view.findViewById(R.id.custom_toast_progressbar);

    }

    private void VariablesInit() {

        MyToolbar = findViewById(R.id.user_name_toolbar);
        MyToolbarBackButton = MyToolbar.findViewById(R.id.mytoolbar_back_button);

        MyToolbarBackButton.setVisibility(View.GONE);

        UserNameEdittext = findViewById(R.id.user_name_activity_edittext);
        UserNameDoneButton = findViewById(R.id.user_name_activity_done_button);
        UserNameTextview = findViewById(R.id.user_name_activity_textview);
        UserEmailEdittext= findViewById(R.id.user_email_activity_edittext);
    }

    private void FirebaseInit() {

        Ref = FirebaseDatabase.getInstance().getReference();
        Auth = FirebaseAuth.getInstance();

    }

    private void CheckUserAuthentication() {

        if(Auth.getCurrentUser() == null)
        {
            startActivity(new Intent(UserNameInfoActivity.this, AuthActivity.class));
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            finish();
        }

    }
}
