package com.integrals.inlens.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.ReadFirebaseData;
import com.integrals.inlens.Interface.FirebaseRead;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;

import java.util.ArrayList;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userRef;
    List<String> userCommunityIdList;
    String currentUserId;
    static final int DELAY_IN_MILLIS=1000;
    ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        userCommunityIdList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS);
        CheckUserAuthentication();


    }

    private void CheckUserAuthentication() {

        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(SplashScreenActivity.this, AuthActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {

            currentUserId  =  firebaseAuth.getCurrentUser().getUid();
            ReadFirebaseData readFirebaseData = new ReadFirebaseData();
            listener= readFirebaseData.readData(userRef.child(currentUserId), new FirebaseRead() {
                @Override
                public void onSuccess(DataSnapshot datasnapshot) {

                    if (datasnapshot.hasChild(FirebaseConstants.COMMUNITIES)) {
                        for (DataSnapshot snapshot : datasnapshot.child(FirebaseConstants.COMMUNITIES).getChildren()) {
                            userCommunityIdList.add(snapshot.getKey());
                        }
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent mainIntent =  new Intent(SplashScreenActivity.this, MainActivity.class);
                            mainIntent.putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) userCommunityIdList);
                            startActivity(mainIntent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        }
                    },DELAY_IN_MILLIS);

                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFailure(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener !=null)
        {
            userRef.child(currentUserId).removeEventListener(listener);
        }

    }
}
