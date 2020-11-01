package com.integrals.inlens.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.Helper.SnackShow;
import com.integrals.inlens.MainActivity;

import com.integrals.inlens.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserNameInfoActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private FirebaseAuth Auth;

    private EditText UserNameEdittext,UserEmailEdittext;
    private TextView UserNameTextview;
    private ImageButton UserNameDoneButton  ;
    private ImageButton  MyToolbarBackButton;
    private View MyToolbar;
    private RelativeLayout relativeLayout;
    private String Name,Email;

    private ProgressBar userInfoProgressbar;

    String appTheme="";
    private DatabaseReference currentUserRef;
    private String ProfilePicUri;
    private ImageView profileImageView;
    private TextView heading;

    //todo if the user is active  in an album set pref values in this activity

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
        setContentView(R.layout.activity_user_name_info);

        FirebaseInit();
        CheckUserAuthentication();
        VariablesInit();
        relativeLayout=findViewById(R.id.root_user_name_info);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(FirebaseConstants.NAME))
                {
                    Name = dataSnapshot.child(FirebaseConstants.NAME).getValue().toString();
                    UserNameEdittext.setText(Name);

                }
                if(dataSnapshot.hasChild(FirebaseConstants.EMAIL))
                {
                    Email = dataSnapshot.child(FirebaseConstants.EMAIL).getValue().toString();
                    UserEmailEdittext.setText(Email);
                }

                if(dataSnapshot.hasChild(FirebaseConstants.PROFILEPICTURE))
                {
                    ProfilePicUri = dataSnapshot.child(FirebaseConstants.PROFILEPICTURE).getValue().toString();
                    Glide.with(getApplicationContext()).load(ProfilePicUri).into(profileImageView);

                }
                UserNameDoneButton.setVisibility(View.VISIBLE);
                userInfoProgressbar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                userInfoProgressbar.setVisibility(View.INVISIBLE);
                UserNameDoneButton.setVisibility(View.VISIBLE);

            }
        });

        heading.setText("User Profile");

        findViewById(R.id.user_image_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(UserNameInfoActivity.this);
            }
        });



        UserNameDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Name = UserNameEdittext.getText().toString();
                Email = UserEmailEdittext.getText().toString();

                if(!TextUtils.isEmpty(Name) && !TextUtils.isEmpty(Email) && isEmailValid(Email))
                {
                    PreOperationCheck check = new PreOperationCheck();
                    check.hideSoftKeyboard(UserNameInfoActivity.this,UserNameEdittext);


                    userRef.child("Name").setValue(Name).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {

                                userRef.child("Email").setValue(Email);
                                startActivity(new Intent(UserNameInfoActivity.this, SplashScreenActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK).putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) new ArrayList<String>()));
                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                finish();
                            }
                            else
                            {
                                final String info = UserNameTextview.getText().toString();
                                UserNameTextview.setText(task.getResult().toString());
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        UserNameTextview.setText(info);

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
                    else if(!isEmailValid(Email))
                    {
                        showDialogMessage("Invalid Email","Please type in your valid email again ");

                    }

                    else
                    {
                        showDialogMessage("Fields Missing","Please type in your username and email.");

                    }
                }



            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
                       CropImage.ActivityResult result = CropImage.getActivityResult(data);
               if (resultCode == RESULT_OK) {
                   //showDialogMessageInfo("Uploading profile picture. Please wait...");
                   Uri resultUri = result.getUri();
                   final String current_u_i_d = FirebaseAuth.getInstance().getCurrentUser().getUid();


                   final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profile_images").child(current_u_i_d + ".jpg");


                   filepath.putFile(resultUri)
                           .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                               @Override
                               public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                   filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                       @Override
                                       public void onSuccess(Uri uri) {
                                           currentUserRef
                                                   .child("Profile_picture")
                                                   .setValue(uri.toString())
                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {
                                                           if (task.isSuccessful()) {
                                                               {
                                                                   //showDialogMessageSuccess("Successfully uploaded your profile picture.");
                                                                   userInfoProgressbar.setVisibility(View.INVISIBLE);
                                                                   UserNameDoneButton.setVisibility(View.VISIBLE);
                                                               }
                                                           } else {
                                                               showDialogMessageError("DB:"+task.getException().getMessage());
                                                               userInfoProgressbar.setVisibility(View.INVISIBLE);
                                                               UserNameDoneButton.setVisibility(View.VISIBLE);

                                                           }

                                                       }
                                                   });
                                       }
                                   }).addOnFailureListener(new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                           showDialogMessageError(""+e.getMessage());
                                           userInfoProgressbar.setVisibility(View.INVISIBLE);
                                           UserNameDoneButton.setVisibility(View.VISIBLE);

                                       }
                                   });

                               }
                           }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                           UserNameDoneButton.setVisibility(View.INVISIBLE);
                           userInfoProgressbar.setVisibility(View.VISIBLE);
                           double progress = (taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                           userInfoProgressbar.setProgress((int)progress);
                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           showDialogMessageError(""+e.getMessage());
                           userInfoProgressbar.setVisibility(View.INVISIBLE);
                           UserNameDoneButton.setVisibility(View.VISIBLE);

                       }
                   });

               }








    }
    public void showDialogMessageError(String message) {
        SnackShow snackShow=new SnackShow(relativeLayout,UserNameInfoActivity.this);
        snackShow.showErrorSnack(message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if(getIntent().getStringExtra("Edit").contentEquals("yes")){
                UserNameDoneButton.setVisibility(View.VISIBLE);
                MyToolbarBackButton.setVisibility(View.VISIBLE);
                MyToolbarBackButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });

            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }


    }

    public void showDialogMessage(String title, String message) {
        SnackShow snackShow=new SnackShow(relativeLayout,UserNameInfoActivity.this);
        snackShow.showErrorSnack(message);
    }

    private void VariablesInit() {

        MyToolbar = findViewById(R.id.user_name_toolbar);
        MyToolbarBackButton = MyToolbar.findViewById(R.id.mytoolbar_back_button);

        MyToolbarBackButton.setVisibility(View.VISIBLE);
        MyToolbar.findViewById(R.id.mytoolbar_dir_options).setVisibility(View.GONE);

        UserNameEdittext = findViewById(R.id.user_name_activity_edittext);
        UserNameDoneButton = findViewById(R.id.user_name_activity_done_button);
        UserNameTextview = findViewById(R.id.user_name_activity_textview);
        UserEmailEdittext= findViewById(R.id.user_email_activity_edittext);
        userInfoProgressbar = findViewById(R.id.user_email_activity_progressbar);
        profileImageView = findViewById(R.id.profilepic_update);
        heading = findViewById(R.id.user_name_toolbar).findViewById(R.id.mytoolbar_textview);


    }

    private void FirebaseInit() {

        Auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(Auth.getCurrentUser().getUid());
        currentUserRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(Auth.getCurrentUser().getUid());


    }

    private void CheckUserAuthentication() {

        if(Auth.getCurrentUser() == null)
        {
            startActivity(new Intent(UserNameInfoActivity.this, PhoneAuth.class));
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            finish();
        }

    }
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
