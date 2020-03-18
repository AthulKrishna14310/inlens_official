package com.integrals.inlens.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;

public class UserNameInfoActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private FirebaseAuth Auth;

    private EditText UserNameEdittext,UserEmailEdittext;
    private TextView UserNameTextview;
    private ImageButton UserNameDoneButton , MyToolbarBackButton ;
    private View MyToolbar;

    private String Name,Email;

    private TextView  CustomToastTitle, CustomToastMessage;
    private ProgressBar CustomToastProgressbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name_info);

        FirebaseInit();
        CheckUserAuthentication();
        VariablesInit();

        userRef.child("Users").child(Auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("Name"))
                {
                    Name = dataSnapshot.child("Name").getValue().toString();
                    UserNameEdittext.append(Name);

                }
                if(dataSnapshot.hasChild("Email"))
                {
                    Email = dataSnapshot.child("Email").getValue().toString();
                    UserEmailEdittext.append(Email);
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


                    userRef.child("Name").setValue(Name).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {


                                userRef.child("Email").setValue(Email);
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
                        showDialogMessage("Fields Missing","Please type in your username and email.");
                    }
                    else if(TextUtils.isEmpty(Name))
                    {
                        showDialogMessage("Name Missing","Please type in your username");
                    }
                    else if(TextUtils.isEmpty(Email))
                    {
                        showDialogMessage("Email Missing","Please type in your email");
                    }
                    else
                    {
                        showDialogMessage("Fields Missing","Please type in your username and email.");

                    }
                }



            }
        });

    }

    public void showDialogMessage(String title, String message) {
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle(title)
                .setIcon(R.drawable.ic_check_circle_black_24dp)
                .setMessage(message)
                .setCancelable(false)
                .addButton("OK", -1, getResources().getColor(R.color.colorAccent), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });
        builder.show();
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

        Auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(Auth.getCurrentUser().getUid());


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
