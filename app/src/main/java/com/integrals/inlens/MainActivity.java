package com.integrals.inlens;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chootdev.csnackbar.Align;
import com.chootdev.csnackbar.Duration;
import com.chootdev.csnackbar.Snackbar;
import com.chootdev.csnackbar.Type;
import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.integrals.inlens.Activities.AuthActivity;
import com.integrals.inlens.Activities.InlensGalleryActivity;
import com.integrals.inlens.Activities.PhotoView;
import com.integrals.inlens.Activities.ProfileActivity;
import com.integrals.inlens.Helper.BottomSheetFragment;
import com.integrals.inlens.Helper.BottomSheetFragment_Inactive;
import com.integrals.inlens.Helper.ExpandableCardView;
import com.integrals.inlens.Helper.ParticipantsAdapter;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.JobScheduler.Scheduler;
import com.integrals.inlens.Models.CommunityModel;
import com.integrals.inlens.Models.PostModel;
import com.integrals.inlens.Notification.AlarmManagerHelper;

import com.journeyapps.barcodescanner.BarcodeEncoder;

import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vistrav.ask.Ask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import com.integrals.inlens.Activities.CreateCloudAlbum;
import com.integrals.inlens.Activities.QRCodeReader;
import com.integrals.inlens.Activities.SharedImageActivity;


public class MainActivity extends AppCompatActivity {


    private String CurrentUserID = "Not Available", CurrentActiveCommunityID = "Not Available", DummyCurrentActiveCommunityID = "Not Available", CurrentDeadCommunityID = "Not Available";
    private String ResultName = "Unknown",ResultImage= "Unknown";
    private List<String> ParticipantIDs;

    private List<CommunityModel> MyCommunityDetails;
    private List<PostModel> MyPostList;
    private DatabaseReference Ref;
    private FirebaseAuth InAuthentication;
    private ProgressBar MainLoadingProgressBar;
    private Dialog QRCodeDialog;

    private Dialog PostDialog;
    private ImageView PostDialogImageView;
    private ProgressBar PostDialogProgressbar;

    private String PostKeyForEdit,CurrentKeyShowninVerticialRecyclerview=null;
    private static final int GALLERY_PICK = 1;
    private static final int COVER_GALLERY_PICK = 78;
    private static boolean COVER_CHANGE = false , PROFILE_CHANGE=false;
    private static boolean SEARCH_IN_PROGRESS = false;
    private NavigationView navigationView;

    private DrawerLayout RootForMainActivity;

    private int INTID = 3939;

    private RecyclerView MainHorizontalRecyclerview, MainVerticalRecyclerView;
    private ImageButton MainNewAlbumButton, MainScanQrButton;
    private HorizontalScrollView MainHorizontalScrollView;
    private Boolean SHOW_TOUR = false;


    private CircleImageView MainProfileImageview;
    private ImageButton MainSearchButton, MainBackButton;
    private EditText MainSearchEdittext;
    private RelativeLayout MainActionbar, MainSearchView;
    private CardView MainToolbar;

    private BroadcastReceiver br;
    private RelativeLayout NoInternetView;
    private TextView NoInternetTextView;


    private int Position=0;

    private static final int JOB_ID = 465;
    private JobScheduler jobScheduler;
    private JobInfo jobInfo;


    RecyclerView ParticipantsRecyclerView;
    List<String> ParticipantIdList;


    String name="";
    String imgurl = "";
    DatabaseReference getParticipantDatabaseReference;
    ExpandableCardView expandableCardView;
    public MainActivity() {
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
        {
            ComponentName componentName = new ComponentName(this, Scheduler.class);
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,componentName);
            builder.setPeriodic(5000);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setPersisted(true);
            jobInfo=builder.build();
            jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(jobInfo);
        }

        MyCommunityDetails = new ArrayList<>();
        ParticipantIDs = new ArrayList<>();
        MainHorizontalScrollView = findViewById(R.id.main_horizontalscrollview);
        MainHorizontalScrollView.setHorizontalScrollBarEnabled(false);
        MainHorizontalScrollView.setVerticalScrollBarEnabled(false);
        MainNewAlbumButton = findViewById(R.id.main_horizontal_new_album_button);
        MainScanQrButton = findViewById(R.id.main_horizontal_scan_button);
        expandableCardView=findViewById(R.id.photographers);

        ParticipantsRecyclerView = findViewById(R.id.main_bottomsheet_particpants_bottomsheet_recyclerview);
        ParticipantsRecyclerView.setHasFixedSize(true);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        GridLayoutManager Gridmanager = new GridLayoutManager(getApplicationContext(), (int) Math.floor(dpWidth / 85));
        ParticipantsRecyclerView.setLayoutManager(Gridmanager);


        navigationView = (NavigationView) findViewById(R.id.nv);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                 if(item.getItemId()==R.id.profile_preference_bg_service){
                    {

                        if(Build.BRAND.equalsIgnoreCase("xiaomi") ){

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                            startActivity(intent);


                        }else if(Build.BRAND.equalsIgnoreCase("Letv")){

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
                            startActivity(intent);

                        }
                        else if(Build.BRAND.equalsIgnoreCase("Honor")){

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                            startActivity(intent);

                        }
                        else if(Build.BRAND.equalsIgnoreCase("vivo"))
                        {
                            try {
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName("com.iqoo.secure",
                                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
                                startActivity(intent);
                            } catch (Exception e) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                                    startActivity(intent);
                                } catch (Exception ex) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setClassName("com.iqoo.secure",
                                                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager");
                                        startActivity(intent);
                                    } catch (Exception exx) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                        else  if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
                            try {
                                Intent intent = new Intent();
                                intent.setClassName("com.coloros.safecenter",
                                        "com.coloros.safecenter.permission.startup.StartupAppListActivity");
                                startActivity(intent);
                            } catch (Exception e) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setClassName("com.oppo.safe",
                                            "com.oppo.safe.permission.startup.StartupAppListActivity");
                                    startActivity(intent);

                                } catch (Exception ex) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setClassName("com.coloros.safecenter",
                                                "com.coloros.safecenter.startupapp.StartupAppListActivity");
                                        startActivity(intent);
                                    } catch (Exception exx) {

                                    }
                                }
                            }
                        }else{
                            // Set Content for Samsung
                            Toast.makeText(getApplicationContext(),"Please enable or disable background tasks for your phone  manually",Toast.LENGTH_SHORT).show();
                        }

                    }



                    return true;
                }

                 else if(item.getItemId()==R.id.profile_notification_stop){

                     AlarmManagerHelper helper = new AlarmManagerHelper(MainActivity.this);
                     helper.deinitateAlarmManager();

                 }

                 else if(item.getItemId()==R.id.profile_notification_start){

                     AlarmManagerHelper helper = new AlarmManagerHelper(MainActivity.this);
                     helper.initiateAlarmManager(5);
                     Toast.makeText(MainActivity.this, "Started inlens service", Toast.LENGTH_SHORT).show();
                 }

                else if(item.getItemId()==R.id.profile_preference_battery_optimization){

                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(),"Battery Optimisation has been disabled for this app, you can open Battery optimisation settings to enable it",Toast.LENGTH_LONG).show();

                    return true;
                }

                else if(item.getItemId()==R.id.profile_preference){
                     if (new PreOperationCheck().checkInternetConnectivity(getApplicationContext())) {
                          setCoverChange(false);
                          setProfileChange(true);
                          GetStartedWithNewProfileImage();
                     } else {
                         Toast.makeText(getApplicationContext(), "Unable to connect to internet. Try again.", Toast.LENGTH_SHORT).show();

                     }

                     return true;

                 }
                else if(item.getItemId()==R.id.profile_activity){
                     startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                     overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);

                    return true;
                 }
                return true;
            }
        });


        MainHorizontalScrollView.smoothScrollTo(0, 0);

        NoInternetView = findViewById(R.id.main_no_internet_relativelayout);
        NoInternetTextView = findViewById(R.id.main_no_internet_textview);


        MainToolbar = findViewById(R.id.mainactivity_toolbar);
        MainProfileImageview = findViewById(R.id.mainactivity_actionbar_profileimageview);
        MainSearchButton = findViewById(R.id.mainactivity_actionbar_searchbutton);
        MainActionbar = findViewById(R.id.mainactivity_actionbar_relativelayout);
        MainSearchView = findViewById(R.id.mainactivity_searchview_relativelayout);
        MainBackButton = findViewById(R.id.mainactivity_searchview_backbutton);
        MainSearchEdittext = findViewById(R.id.mainactivity_searchview_edittext);


        SHOW_TOUR = getIntent().getBooleanExtra("ShowTour", false);


        RootForMainActivity = findViewById(R.id.root_for_main_activity);

        MainHorizontalRecyclerview = (RecyclerView) findViewById(R.id.main_horizontal_recyclerview);
        MainHorizontalRecyclerview.setHasFixedSize(true);
        MainHorizontalRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));




        MainVerticalRecyclerView = findViewById(R.id.main_recyclerview);
        MainVerticalRecyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };


        MainVerticalRecyclerView.setLayoutManager(staggeredGridLayoutManager);

        MainLoadingProgressBar = findViewById(R.id.mainloadingpbar);

        FirebaseVariablesInit();
        CheckUserAuthentication();
        checkInternetConnection();
        InitPostDialog();


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    AlarmManagerHelper managerHelper = new AlarmManagerHelper(MainActivity.this);

                    if (!dataSnapshot.hasChild("Communities")) {
                        MainLoadingProgressBar.setVisibility(View.GONE);
                        try
                        {
                            managerHelper.deinitateAlarmManager();

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    } else {

                        ShowAllAlbums();
                    }
                    String name = dataSnapshot.child("Name").getValue().toString();
                    String email = dataSnapshot.child("Email").getValue().toString();


                    TextView tEmail=navigationView.getHeaderView(0).findViewById(R.id.headerEmailX);
                    TextView tName=navigationView.getHeaderView(0).findViewById(R.id.headerNameX);

                    tEmail.setText(email);
                    tName.setText(name);


                    if (dataSnapshot.hasChild("Profile_picture")) {

                        String image = dataSnapshot.child("Profile_picture").getValue().toString();

                        Glide.with(getApplicationContext()).load(image).addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                RequestOptions requestOptions=new RequestOptions()
                                        .centerCrop()
                                        ;
                                Glide.with(getApplicationContext())
                                        .load(image)
                                        .apply(requestOptions)
                                        .into((ImageView) navigationView.getHeaderView(0).findViewById(R.id.headerImageView))

                                ;

                                return false;
                            }
                        })
                                .into(MainProfileImageview);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        DecryptDeepLink();


        MainProfileImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//
//
                 RootForMainActivity.openDrawer(Gravity.START);
            }
        });


        MainSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SEARCH_IN_PROGRESS = true;

                final List<CommunityModel> CommunitySearchDetails = new ArrayList<>();

                MainActionbar.clearAnimation();
                MainActionbar.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out));
                MainActionbar.getAnimation().start();
                MainActionbar.setVisibility(View.GONE);

                MainSearchView.clearAnimation();
                MainSearchView.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in));
                MainSearchView.getAnimation().start();
                MainSearchView.setVisibility(View.VISIBLE);

                MainSearchEdittext.requestFocus();
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                MainSearchEdittext.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                        MainVerticalRecyclerView.setVisibility(View.VISIBLE);

                        if (!TextUtils.isEmpty(editable.toString())) {

                            CommunitySearchDetails.clear();

                            for (int i = 0; i < MyCommunityDetails.size(); i++) {
                                if (MyCommunityDetails.get(i).getTitle().toLowerCase().contains(editable.toString().toLowerCase())) {
                                    CommunitySearchDetails.add(MyCommunityDetails.get(i));
                                }
                            }

                            if (CommunitySearchDetails.size() == 0) {
                                MainVerticalRecyclerView.setVisibility(View.GONE);

                            } else {
                                MainVerticalRecyclerView.setVisibility(View.VISIBLE);

                            }

                            MainHorizontalAdapter adapter = new MainHorizontalAdapter(CommunitySearchDetails);
                            MainHorizontalRecyclerview.setAdapter(adapter);

                        } else {
                            MainHorizontalRecyclerview.removeAllViews();
                            ShowAllAlbums();
                        }

                    }
                });

            }
        });


        MainBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainSearchEdittext.setText("");
                onBackPressed();


            }
        });

        MainNewAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createAlbum();
            }
        });

        MainScanQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scanQR();
            }
        });



        getParticipantDatabaseReference = FirebaseDatabase.getInstance().getReference();
        
        findViewById(R.id.add_photographers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getMyCommunityDetails().get(getPosition()).getCommunityID().equals(getCurrentActiveCommunityID())
                        || getMyCommunityDetails().get(getPosition()).getCommunityID().equals(getCurrentActiveCommunityID())) {
                    QRCodeInit(getMyCommunityDetails().get(getPosition()).getCommunityID());

                } else {


                    CFAlertDialog.Builder builder = new CFAlertDialog.Builder(MainActivity.this)
                            .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                            .setTitle("Album expired.")
                            .setIcon(R.drawable.ic_warning_black_24dp)
                            .setMessage("You can't add photographers or add photos to this album since it's expired.")
                            .setCancelable(true)
                            .addButton("Ok , I understand", -1, Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                    CFAlertDialog.CFAlertActionAlignment.END,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();

                                        }
                                    });
                    builder.show();



                }
            }
        });
    }



    public void GetStartedWithNewProfileImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(MainActivity.this);
    }


    private void SetDefaultView() {

        SEARCH_IN_PROGRESS = false;
        MainSearchEdittext.setText("");

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(MainSearchEdittext.getWindowToken(), 0);
        }

        MainSearchView.setVisibility(View.GONE);

        MainActionbar.setVisibility(View.VISIBLE);


        if (CurrentActiveCommunityID.equals("Not Available")) {
            SetVerticalRecyclerView(CurrentDeadCommunityID);
        } else {
            SetVerticalRecyclerView(CurrentActiveCommunityID);
        }

    }


    private void CheckUserAuthentication() {

        if (InAuthentication.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
        } else {

            CurrentUserID = InAuthentication.getCurrentUser().getUid();

            Ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    if (dataSnapshot.child("Users").child(CurrentUserID).hasChild("live_community")) {
                        CurrentActiveCommunityID = dataSnapshot.child("Users").child(CurrentUserID).child("live_community").getValue().toString();
                        DummyCurrentActiveCommunityID = CurrentActiveCommunityID;
                        if (dataSnapshot.child("Communities").child(CurrentActiveCommunityID).hasChild("endtime")) {
                            long endtime = Long.parseLong(dataSnapshot.child("Communities").child(CurrentActiveCommunityID).child("endtime").getValue().toString());
                            if (System.currentTimeMillis() > endtime) {
                                quitCloudAlbum(1);
                            }
                        }

                    } else {
                        CurrentActiveCommunityID = "Not Available";
                        AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper(getApplicationContext());
                        alarmManagerHelper.deinitateAlarmManager();
                    }

                    if (dataSnapshot.child("Users").child(CurrentUserID).hasChild("dead_community")) {
                        CurrentDeadCommunityID = dataSnapshot.child("Users").child(CurrentUserID).child("dead_community").getValue().toString();

                    } else {
                        CurrentDeadCommunityID = "Not Available";
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            PermissionsInit();
            ShowAllAlbums();

        }
    }

    public void QRCodeInit(final String CommunityID) {

        QRCodeDialog = new Dialog(MainActivity.this, android.R.style.Theme_Light_NoTitleBar);
        QRCodeDialog.setCancelable(true);
        QRCodeDialog.setCanceledOnTouchOutside(true);
        QRCodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        QRCodeDialog.setContentView(R.layout.activity_qrcode_generator);
        QRCodeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        QRCodeDialog.getWindow().getAttributes().windowAnimations = R.style.BottomUpSlideDialogAnimation;

        Window QRCodewindow = QRCodeDialog.getWindow();
        QRCodewindow.setGravity(Gravity.BOTTOM);
        QRCodewindow.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        QRCodewindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        QRCodewindow.setDimAmount(0.75f);
        QRCodewindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Button InviteLinkButton = QRCodeDialog.findViewById(R.id.InviteLinkButton);

        ImageButton QRCodeCloseBtn = QRCodeDialog.findViewById(R.id.QR_dialog_closebtn);
        QRCodeCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                QRCodeDialog.dismiss();

            }
        });
        final TextView textView = QRCodeDialog.findViewById(R.id.textViewAlbumQR);
        final ImageView QRCodeImageView = QRCodeDialog.findViewById(R.id.QR_Display);

        final MultiFormatWriter multiFormatWriter = new MultiFormatWriter();


        if (!CommunityID.equals("Not Available")) {
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(CommunityID, BarcodeFormat.QR_CODE, 200, 200);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                QRCodeImageView.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                QRCodeImageView.setVisibility(View.INVISIBLE);
                textView.setText("You must be in an album to generate QR code");
            } catch (NullPointerException e) {
                QRCodeImageView.setVisibility(View.INVISIBLE);
                textView.setText("You must be in an album to generate QR code");

            }
        } else {

            QRCodeImageView.setVisibility(View.INVISIBLE);
            textView.setText("Some error was encountered. Please try again.");
        }

        InviteLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent SharingIntent = new Intent(Intent.ACTION_SEND);
                SharingIntent.setType("text/plain");

                SharingIntent.putExtra(Intent.EXTRA_TEXT, "InLens Cloud-Album Invite Link \n\n" + GenarateDeepLinkForInvite(CommunityID));
                startActivity(SharingIntent);

            }
        });

        QRCodeDialog.show();

    }

    private void FirebaseVariablesInit() {

        InAuthentication = FirebaseAuth.getInstance();
        Ref = FirebaseDatabase.getInstance().getReference();

    }


    private void DecryptDeepLink() {

        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
            @Override
            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {

                Uri DeepLink;
                if (pendingDynamicLinkData != null) {
                    DeepLink = pendingDynamicLinkData.getLink();
                    if (DeepLink != null) {


                        if (DeepLink.toString().contains("comid=")) {

                            final String UrlOrDComId = (DeepLink.toString().substring(DeepLink.toString().length() - 27)).substring(0, 26);


                            if (!CurrentActiveCommunityID.equals("Not Available")) {

                                CFAlertDialog.Builder builder = new CFAlertDialog.Builder(getApplicationContext())

                                        .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                                        .setTitle("Invite Link")
                                        .setIcon(R.drawable.inlens_logo_m)
                                        .setMessage("Just click on the Cloud-Album invite link that your friend had " +
                                                "shared" +
                                                " with you.")
                                        .addButton("OK, I UNDERSTAND", -1, Color.parseColor("#3E3D63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                                CFAlertDialog.CFAlertActionAlignment.END, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });

// Show the alert
                                builder.show();



                            } else {

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("New Community")
                                        .setMessage("Are you sure you want to join this new community? This means leaving the previous community by default.")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                AddCommunityToUserRef(UrlOrDComId.substring(6, 26));

                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .create()
                                        .show();

                            }

                        } else if (DeepLink.toString().contains("imagelink") && DeepLink.toString().contains("linkimage")) {

                            String first = DeepLink.toString().replace("https://integrals.inlens.in/", "");
                            String second = first.replace("imagelink", "https://firebasestorage.googleapis.com/v0/b/inlens-f0ce2.appspot.com/o/situations%2F");
                            String third = second.replace("linkimage", "media&token=");
                            String ImageUrl = third.substring(0, third.length() - 1);

                            startActivity(new Intent(MainActivity.this, SharedImageActivity.class).putExtra("url", ImageUrl));

                        }
                    }
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getApplicationContext(), "Invite Link Failed", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void AddCommunityToUserRef(final String substring) {

        Ref.child("Communities").child(substring).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("endtime")) {
                    Long endtime = Long.parseLong(dataSnapshot.child("endtime").getValue().toString());

                    if (endtime > System.currentTimeMillis()) {

                        if (CurrentActiveCommunityID.equals(substring)) {
                            Toast.makeText(getApplicationContext(), "Already a participant in this community.", Toast.LENGTH_SHORT).show();
                        } else {

                            Ref.child("Communities").child(substring).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        Toast.makeText(getApplicationContext(), "Rejoined this community.", Toast.LENGTH_SHORT).show();
                                        Ref.child("Users").child(CurrentUserID).child("live_community").setValue(substring);
                                        recreate();

                                    } else {

                                        Ref.child("Users").child(CurrentUserID).child("Communities").child(substring).setValue(ServerValue.TIMESTAMP);
                                        Ref.child("Users").child(CurrentUserID).child("live_community").setValue(substring);
                                        Ref.child("Communities").child(substring).child("participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(ServerValue.TIMESTAMP);
                                        recreate();
                                    }


                                }


                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Community not valid anymore.", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Community not valid anymore.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void createAlbum() {

        if (CurrentActiveCommunityID.equals("Not Available")) {
            startActivity(new Intent(MainActivity.this, CreateCloudAlbum.class));
        } else {

            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                    .setTitle("Please Quit.")
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setMessage("You have to quit this album before creating another one.")
                    .setCancelable(true)
                    .addButton("   Quit album  ", -1, Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                            CFAlertDialog.CFAlertActionAlignment.END,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    quitCloudAlbum(0);
                                }
                            });
            builder.show();

        }


    }

    private void scanQR() {

        if (CurrentActiveCommunityID.equals("Not Available")) {
            Snackbar.with(MainActivity.this,null)
                    .type(Type.CUSTOM)
                    .message("Loading QR-Code Scanner ..")
                    .duration(Duration.LONG)
                    .fillParent(true)
                    .textAlign(Align.LEFT)
                    .show();
            startActivity(new Intent(MainActivity.this, QRCodeReader.class));
        } else {
            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                    .setTitle("Please Quit.")
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setMessage("You have to quit this album before creating another one.")
                    .setCancelable(true)
                    .addButton("   Quit album  ", -1, Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                            CFAlertDialog.CFAlertActionAlignment.END,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    quitCloudAlbum(0);
                                }
                            });
            builder.show();
              }

    }


    private void PermissionsInit() {
        Ask.on(this)
                .id(INTID) // in case you are invoking multiple time Ask from same activity or fragment
                .forPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.INTERNET
                        , Manifest.permission.CAMERA
                        , Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.RECORD_AUDIO
                        , Manifest.permission.VIBRATE
                        , Manifest.permission.SYSTEM_ALERT_WINDOW
                )
                .go();
    }


    private void ShowAllAlbums() {

        final List<String> MyCommunities = new ArrayList<>();

        MainLoadingProgressBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(expandableCardView.isExpanded()){
                    expandableCardView.collapse();
                }


            }
        }, 500);


        Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                MyCommunityDetails.clear();
                MyCommunities.clear();
                MainHorizontalRecyclerview.removeAllViews();

                for (DataSnapshot snapshot : dataSnapshot.child("Users").child(CurrentUserID).child("Communities").getChildren()) {

                    String key = snapshot.getKey();
                    MyCommunities.add(key);

                }

                for (int i = 0; i < MyCommunities.size(); i++) {
                    String admin = "unknown", coverimage = "unknown", description = "unknown", endtime = "unknown", starttime = "unknown", status = "unknown", title = "unknown", type = "unknown";


                    if (dataSnapshot.child("Communities").child(MyCommunities.get(i)).hasChild("admin")) {
                        admin = dataSnapshot.child("Communities").child(MyCommunities.get(i)).child("admin").getValue().toString();

                    }
                    if (dataSnapshot.child("Communities").child(MyCommunities.get(i)).hasChild("coverimage")) {
                        coverimage = dataSnapshot.child("Communities").child(MyCommunities.get(i)).child("coverimage").getValue().toString();

                    }
                    if (dataSnapshot.child("Communities").child(MyCommunities.get(i)).hasChild("endtime")) {
                        endtime = dataSnapshot.child("Communities").child(MyCommunities.get(i)).child("endtime").getValue().toString();

                    }
                    if (dataSnapshot.child("Communities").child(MyCommunities.get(i)).hasChild("description")) {
                        description = dataSnapshot.child("Communities").child(MyCommunities.get(i)).child("description").getValue().toString();

                    }
                    if (dataSnapshot.child("Communities").child(MyCommunities.get(i)).hasChild("starttime")) {
                        starttime = dataSnapshot.child("Communities").child(MyCommunities.get(i)).child("starttime").getValue().toString();

                    }
                    if (dataSnapshot.child("Communities").child(MyCommunities.get(i)).hasChild("status")) {
                        status = dataSnapshot.child("Communities").child(MyCommunities.get(i)).child("status").getValue().toString();

                    }
                    if (dataSnapshot.child("Communities").child(MyCommunities.get(i)).hasChild("title")) {
                        title = dataSnapshot.child("Communities").child(MyCommunities.get(i)).child("title").getValue().toString();

                    }
                    if (dataSnapshot.child("Communities").child(MyCommunities.get(i)).hasChild("type")) {
                        type = dataSnapshot.child("Communities").child(MyCommunities.get(i)).child("type").getValue().toString();

                    }


                    CommunityModel model = new CommunityModel(title, description, status, starttime, endtime, dataSnapshot.child("Communities").child(MyCommunities.get(i)).child("participants").getRef(), type, coverimage, admin, MyCommunities.get(i));
                    MyCommunityDetails.add(model);
                }


                if (MyCommunityDetails.size() == 0) {
                    MainLoadingProgressBar.setVisibility(View.GONE);
                    MainHorizontalRecyclerview.setVisibility(View.VISIBLE);

                } else {
                    MainLoadingProgressBar.setVisibility(View.GONE);
                    MainHorizontalRecyclerview.setVisibility(View.VISIBLE);
                    SharedPreferences LastShownNotificationInfo = getSharedPreferences("LastNotification.pref",Context.MODE_PRIVATE);
                    if(LastShownNotificationInfo.getAll().size() != 1)
                    {
                        SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
                        editor.putString("time", String.valueOf(System.currentTimeMillis()));
                        editor.commit();
                    }

                }


                Collections.reverse(MyCommunityDetails);
                MainHorizontalAdapter adapter = new MainHorizontalAdapter(MyCommunityDetails);
                MainHorizontalRecyclerview.setAdapter(adapter);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (CurrentActiveCommunityID.equals("Not Available")) {
            ParticipantIDs.clear();
            Ref.child("Communities").child(CurrentDeadCommunityID).child("participants").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for(DataSnapshot snapshot : dataSnapshot.getChildren() )
                    {
                        if(!ParticipantIDs.contains(snapshot.getKey()))
                        {
                            ParticipantIDs.add(snapshot.getKey());
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            SetVerticalRecyclerView(CurrentDeadCommunityID);
        } else {
            ParticipantIDs.clear();
            Ref.child("Communities").child(CurrentActiveCommunityID).child("participants").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for(DataSnapshot snapshot : dataSnapshot.getChildren() )
                    {
                        if(!ParticipantIDs.contains(snapshot.getKey()))
                        {
                            ParticipantIDs.add(snapshot.getKey());
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            SetVerticalRecyclerView(CurrentActiveCommunityID);
        }


    }

    private void SetVerticalRecyclerView(String communityID) {


        CurrentKeyShowninVerticialRecyclerview = communityID;

        MyPostList = new ArrayList<>();
        setParticipants();
        Ref.child("Communities").child(communityID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                MyPostList.clear();
                MainVerticalRecyclerView.removeAllViews();

                if (dataSnapshot.hasChild("posts")) {
                    MainVerticalRecyclerView.setVisibility(View.VISIBLE);


                    for (DataSnapshot snapshot : dataSnapshot.child("posts").getChildren()) {
                        String key = snapshot.getKey();
                        String by = "unknown", time = "unknown", uri = "unknown";

                        if (snapshot.hasChild("by")) {
                            by = snapshot.child("by").getValue().toString();
                        }
                        if (snapshot.hasChild("time")) {
                            time = snapshot.child("time").getValue().toString();
                        }
                        if (snapshot.hasChild("uri")) {
                            uri = snapshot.child("uri").getValue().toString();
                        }

                        PostModel model = new PostModel(key, uri, time, by);
                        MyPostList.add(model);

                    }

                    if (MyPostList.size() != 0) {
                        Collections.reverse(MyPostList);
                        MainVerticalAdapter adapter = new MainVerticalAdapter(getApplicationContext(), MyPostList, FirebaseDatabase.getInstance().getReference().child("Users"));
                        MainVerticalRecyclerView.setAdapter(adapter);
                    } else {
                          MainVerticalRecyclerView.setVisibility(View.GONE);
                    }


                } else {
                    MainVerticalRecyclerView.setVisibility(View.GONE);
                 }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void setParticipants(){
        ParticipantIdList = ParticipantIDs;
        final List<String> MemberImageList = new ArrayList<>();
        final List<String> MemberNamesList = new ArrayList<>();
        ParticipantsRecyclerView.removeAllViews();
        for (String id : ParticipantIdList) {
            name= "NA";
            imgurl= "NA";

            getParticipantDatabaseReference.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild("Name")) {
                        name= dataSnapshot.child("Name").getValue().toString();
                        if (!MemberNamesList.contains(name) || !MemberNamesList.contains(id)) {
                            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(id))
                            {
                                MemberNamesList.add("You");

                            }
                            else
                            {
                                MemberNamesList.add(name);
                            }

                        }
                    } else {
                        MemberNamesList.add(id);
                    }
                    if (dataSnapshot.hasChild("Profile_picture")) {
                        imgurl = dataSnapshot.child("Profile_picture").getValue().toString();
                        if (!MemberImageList.contains(imgurl) || !MemberImageList.contains(id)) {
                            MemberImageList.add(imgurl);

                        }
                    } else {
                        MemberImageList.add(id);

                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        ParticipantsAdapter adapter = new ParticipantsAdapter(MemberImageList, MemberNamesList, getApplicationContext());
        ParticipantsRecyclerView.setAdapter(adapter);

    }

    public void quitCloudAlbum(int ForceQuit) {


        PreOperationCheck checker = new PreOperationCheck();

        if (ForceQuit == 1) {
            if (checker.checkInternetConnectivity(getApplicationContext())) {

                CurrentDeadCommunityID = CurrentActiveCommunityID;
                FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserID).child("live_community").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserID).child("dead_community").setValue(DummyCurrentActiveCommunityID);

                            AlarmManagerHelper alarmManagerHelper =
                                    new AlarmManagerHelper(getApplicationContext());
                            alarmManagerHelper.deinitateAlarmManager();
                            showDialogueQuit();
                            SetDefaultView();


                        } else {
                            SetDefaultView();
                            showDialogueQuitUnsuccess();
                        }
                    }
                });
            } else {
                SetDefaultView();
                showDialogueQuitUnsuccess();
            }

        } else {
            if (checker.checkInternetConnectivity(getApplicationContext()) && !CurrentActiveCommunityID.equals("Not Available")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Quit Cloud-Album");
                builder.setMessage("Are you sure you want to quit the current Cloud-Album. You won't able to upload photos to this album again.");
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                    }
                });
                builder.setPositiveButton(" Yes ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        CurrentDeadCommunityID = CurrentActiveCommunityID;
                        FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserID).child("live_community").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserID).child("dead_community").setValue(DummyCurrentActiveCommunityID);

                                    AlarmManagerHelper alarmManagerHelper =
                                            new AlarmManagerHelper(getApplicationContext());
                                    alarmManagerHelper.deinitateAlarmManager();
                                    showDialogueQuit();
                                    SetDefaultView();


                                } else {
                                    SetDefaultView();
                                    showDialogueQuitUnsuccess();
                                }

                            }
                        });
                    }

                });
                builder.create().show();

            } else {
                SetDefaultView();
                showDialogueQuitUnsuccess();
            }
        }


    }

    private void showDialogueQuitUnsuccess() {

        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle("Cloud-Album Quit")
                .setIcon(R.drawable.ic_cancel_black_24dp)
                .setMessage("Unable to Quit Cloud-Album. Please check your internet connection or" +
                        " whether you are participating in a Cloud-Album")
                .setCancelable(false)
                .addButton("    OK    ", -1, -1, CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                        CFAlertDialog.CFAlertActionAlignment.END, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });


        builder.show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == COVER_GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
            finish();
        } else if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
            finish();

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && COVER_CHANGE && !PROFILE_CHANGE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri ImageUri = result.getUri();
                //MainBottomSheetAlbumCoverEditUserImage.setImageURI(ImageUri);
                //MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.VISIBLE);
                UploadCoverPhoto(ImageUri);
            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && !COVER_CHANGE && PROFILE_CHANGE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {


                Uri resultUri = result.getUri();
                try {
                    InputStream stream = getContentResolver().openInputStream(resultUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


                File thumb_filePath = new File(resultUri.getPath());
                final String current_u_i_d = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(100)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profile_images").child(current_u_i_d + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();


                            FirebaseDatabase.getInstance().getReference().child("Users").child(current_u_i_d).child("Profile_picture").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        {
                                            Toast.makeText(MainActivity.this, "SUCCESSFULLY UPLOADED", Toast.LENGTH_LONG).show();


                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "FAILED TO SAVE TO DATABASE.MAKE SURE YOUR INTERNET IS CONNECTED AND TRY AGAIN.", Toast.LENGTH_LONG).show();

                                    }

                                }
                            });
                        } else {
                            Toast.makeText(MainActivity.this, "FAILED TO UPLOAD", Toast.LENGTH_LONG).show();

                        }
                    }


                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(),"Uploading your profile-picture please wait ",Toast.LENGTH_SHORT).show();
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(MainActivity.this, "FAILED TO UPLOAD", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void UploadCoverPhoto(Uri imageUri) {

        // MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(PostKeyForEdit) && imageUri != null) {

            StorageReference
                    FilePath = FirebaseStorage.getInstance().getReference()
                    .child("CommunityCoverPhoto")
                    .child(imageUri.getLastPathSegment());

            FilePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {
                        final String downloadUrl = task.getResult().getDownloadUrl().toString();
                        FirebaseDatabase.getInstance().getReference()
                                .child("Communities")
                                .child(PostKeyForEdit)
                                .child("coverimage")
                                .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(MainActivity.this, "Successfully changed the Cover-Photo.", Toast.LENGTH_LONG).show();
                                    ShowAllAlbums();

                                } else {
                                    // MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(MainActivity.this, "Unable to perform to change cover now.", Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                    } else {
                        //MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "Unable to perform to change cover now.", Toast.LENGTH_LONG).show();


                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "Unable to perform to change cover now.", Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress =
                            (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                    Toast toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
                    toast.setText("Uploading your cover photo  " +
                            (int) progress + "%  " +
                            "completed.");
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                }
            });

        } else {
            // MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(MainActivity.this, "Unable to perform to change cover now.", Toast.LENGTH_LONG).show();
        }


    }



    @Override
    public void onBackPressed() {

        if (SEARCH_IN_PROGRESS) {
            SetDefaultView();
        } else if (MainHorizontalScrollView.getScrollX() != 0) {
            MainHorizontalScrollView.smoothScrollTo(0, 0);
        } else {
            super.onBackPressed();
        }

    }




    public class MainHorizontalAdapter extends RecyclerView.Adapter<MainHorizontalAdapter.MainCommunityViewHolder> {

        List<CommunityModel> CommunityDetails;

        public MainHorizontalAdapter(List<CommunityModel> communityDetails) {
            CommunityDetails = communityDetails;
        }

        @NonNull
        @Override
        public MainCommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.album_card_, parent, false);
            return new MainHorizontalAdapter.MainCommunityViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MainCommunityViewHolder holder, final int position) {

            if (!CommunityDetails.get(position).equals("unknown")) {

                Glide.with(getApplicationContext())
                        .load(CommunityDetails.get(position)
                                .getCoverImage()).addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                        .into(holder.AlbumCoverButton);
            } else {
             }

            if(holder.getLayoutPosition()==Position){
                holder.Indicator.setVisibility(View.VISIBLE);
                holder.itemView.setAlpha((float) 1);
                SetVerticalRecyclerView(CommunityDetails.get(Position).getCommunityID());


            }

            holder.covePhotoChange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Position=holder.getLayoutPosition();
                    ParticipantIDs.clear();
                    Ref.child("Communities").child(CommunityDetails.get(position).getCommunityID()).child("participants").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot snapshot : dataSnapshot.getChildren() )
                            {
                                if(!ParticipantIDs.contains(snapshot.getKey()))
                                {
                                    ParticipantIDs.add(snapshot.getKey());
                                }
                            }
                            if(CurrentActiveCommunityID.contentEquals(CommunityDetails.get(position).getCommunityID()))
                            {
                                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(MainActivity.this,ParticipantIDs);
                                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                            }else{
                                BottomSheetFragment_Inactive bottomSheetFragment_inactive =new BottomSheetFragment_Inactive(MainActivity.this,ParticipantIDs);
                                bottomSheetFragment_inactive.show(getSupportFragmentManager(), bottomSheetFragment_inactive.getTag());

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            });

            holder.AlbumCoverButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getCurrentCommunityinVerticialRecyclerview() != null && !getCurrentCommunityinVerticialRecyclerview().equals(CommunityDetails.get(position).getCommunityID()))
                    {
                        Position=holder.getLayoutPosition();
                        ParticipantIDs.clear();
                        Ref.child("Communities").child(CommunityDetails.get(Position).getCommunityID()).child("participants").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for(DataSnapshot snapshot : dataSnapshot.getChildren() )
                                {
                                    if(!ParticipantIDs.contains(snapshot.getKey()))
                                    {
                                        ParticipantIDs.add(snapshot.getKey());
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        ShowAllAlbums();
                    }
                }
            });
            try {
                if(CurrentActiveCommunityID.contentEquals(CommunityDetails.get(Position).getCommunityID())){
                    findViewById(R.id.fabadd).setVisibility(View.VISIBLE);
                    findViewById(R.id.fabadd).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(new PreOperationCheck().checkInternetConnectivity(MainActivity.this))
                            {
                                Intent intent = new Intent(getApplicationContext(), InlensGalleryActivity.class);
                                intent.putExtra("CommunityID", getMyCommunityDetails().get(getPosition()).getCommunityID());
                                intent.putExtra("CommunityName", getMyCommunityDetails().get(getPosition()).getTitle());
                                intent.putExtra("CommunityStartTime", getMyCommunityDetails().get(getPosition()).getStartTime());
                                intent.putExtra("CommunityEndTime", getMyCommunityDetails().get(getPosition()).getEndTime());
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                            else
                            {
                                Snackbar.with(MainActivity.this,null)
                                        .type(Type.CUSTOM)
                                        .message("Please connect with internet and try again.")
                                        .duration(Duration.LONG)
                                        .fillParent(true)
                                        .textAlign(Align.LEFT)
                                        .show();
                            }


                        }
                    });

                } else{
                    findViewById(R.id.fabadd).setVisibility(View.GONE);

                }
            }catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }






            holder.AlbumNameTextView.setText(CommunityDetails.get(position).getTitle());
            holder.AlbumDescriptionTextView.setText(CommunityDetails.get(position).getDescription());

            holder.AlbumCoverButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Position=holder.getLayoutPosition();
                    ParticipantIDs.clear();
                    Ref.child("Communities").child(CommunityDetails.get(position).getCommunityID()).child("participants").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot snapshot : dataSnapshot.getChildren() )
                            {
                                if(!ParticipantIDs.contains(snapshot.getKey()))
                                {
                                    ParticipantIDs.add(snapshot.getKey());
                                }
                            }
                            if(CurrentActiveCommunityID.contentEquals(CommunityDetails.get(position).getCommunityID()))
                            {
                                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(MainActivity.this,ParticipantIDs);
                                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                            }else{
                                BottomSheetFragment_Inactive bottomSheetFragment_inactive =new BottomSheetFragment_Inactive(MainActivity.this,ParticipantIDs);
                                bottomSheetFragment_inactive.show(getSupportFragmentManager(), bottomSheetFragment_inactive.getTag());

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    return false;
                }
            });




        }

        private String getCurrentCommunityinVerticialRecyclerview() {

            return CurrentKeyShowninVerticialRecyclerview;
        }

        @Override
        public int getItemCount() {
            return CommunityDetails.size();
        }


        public class MainCommunityViewHolder extends RecyclerView.ViewHolder {


            ImageView AlbumCoverButton,AlbumOptions;
            TextView AlbumNameTextView;
            TextView AlbumDescriptionTextView;
            Button   Indicator;
            Button   covePhotoChange;
            CardView covePhotoChangeCard;

            public MainCommunityViewHolder(View itemView) {
                super(itemView);
                AlbumOptions=itemView.findViewById(R.id.albumcard_options);
                AlbumCoverButton = itemView.findViewById(R.id.albumcard_image_view);
                AlbumNameTextView = itemView.findViewById(R.id.album_card_textview);
                covePhotoChange=itemView.findViewById(R.id.coverphotochangebutton);
                covePhotoChangeCard=itemView.findViewById(R.id.cardcoverphotochange);

                AlbumDescriptionTextView=itemView.findViewById(R.id.albumcard_description);
                Indicator=itemView.findViewById(R.id.indication_button);
            }


        }


    }



    private static String GenarateDeepLinkForInvite(String CommunityID) {
        return "https://inlens.page.link/?link=https://integrals.inlens.in/comid=" + CommunityID + "/&apn=com.integrals.inlens";
    }


    private void checkInternetConnection() {

        if (br == null) {

            br = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    Bundle extras = intent.getExtras();
                    NetworkInfo info = (NetworkInfo) extras.getParcelable("networkInfo");

                    NetworkInfo.State state = info.getState();
                    if (state == NetworkInfo.State.CONNECTED) {


                        NoInternetTextView.setText("Back online.");
                        NoInternetView.setBackgroundColor(Color.parseColor("#ff0f9d58"));

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                NoInternetView.clearAnimation();
                                NoInternetView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_bottom));
                                NoInternetView.getAnimation().start();
                                NoInternetView.setVisibility(View.GONE);


                            }
                        },1000);

                    } else {

                        NoInternetTextView.setText("Internet connection lost.");
                        NoInternetView.setBackgroundColor(Color.parseColor("#ffc53929"));

                        NoInternetView.clearAnimation();
                        NoInternetView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up));
                        NoInternetView.getAnimation().start();
                        NoInternetView.setVisibility(View.VISIBLE);
                    }

                }
            };

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(br, intentFilter);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(br);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void showDialogueQuit() {
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle("Cloud-Album Quit")
                .setIcon(R.drawable.ic_check_circle_black_24dp)
                .setMessage("Successfully left from the Cloud-Album")
                .setCancelable(false)
                .addButton("    OK    ", -1, Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.END,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });
        builder.show();
    }



    private void InitPostDialog() {

        PostDialog = new Dialog(MainActivity.this);
        PostDialog.setCancelable(true);
        PostDialog.setContentView(R.layout.main_post_touch_dialog_layout);
        PostDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        PostDialogImageView = PostDialog.findViewById(R.id.main_postdialog_imageview);
        PostDialogProgressbar = PostDialog.findViewById(R.id.main_postdialog_progressbar);


    }

    public class MainVerticalAdapter extends RecyclerView.Adapter<MainVerticalAdapter.PostGridViewHolder> {

        Context context;
        List<PostModel> PostList;
        DatabaseReference UserRef;
        Picasso picasso;

        public MainVerticalAdapter(Context context, List<PostModel> postList, DatabaseReference userRef) {
            this.context = context;
            PostList = postList;
            UserRef = userRef;
            picasso = Picasso.get();
            picasso.setIndicatorsEnabled(false);
        }


        @NonNull
        @Override
        public PostGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(context).inflate(R.layout.post_layout, parent, false);
            return new MainVerticalAdapter.PostGridViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final PostGridViewHolder holder, final int position) {

            holder.itemView.clearAnimation();
            holder.itemView.setAnimation(AnimationUtils.loadAnimation(context,android.R.anim.fade_in));
            holder.itemView.getAnimation().start();

            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_photo_camera);


            /*
            picasso.load(PostList.get(position).getUri())
                    .resizeDimen(R.dimen.main_image_dimen200,R.dimen.main_image_dimen200)
                    .centerCrop()
                    .into(holder.PostImageView)
            ;
             */
            Glide.with(context)
                    .load(PostList.get(position).getUri())
                    .apply(requestOptions)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.PostProgressbar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.PostProgressbar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.PostImageView);

            UserRef.child(PostList.get(position).getPostBy()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild("Name")) {
                        String name = dataSnapshot.child("Name").getValue().toString();
                        holder.PostUploaderNameTextView.setText(name);

                    }
                    else
                    {
                        holder.PostUploaderNameTextView.setText("Unknown");
                    }
                    if (dataSnapshot.hasChild("Profile_picture")) {
                        String UploaderImageUrl = dataSnapshot.child("Profile_picture").getValue().toString();
                        Glide.with(context).load(UploaderImageUrl).into(holder.PostUploaderImageView);

                    }
                    else
                    {
                        Glide.with(context).load(R.drawable.ic_account_circle).into(holder.PostUploaderImageView);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP  && PostDialog.isShowing())
                    {
                        PostDialog.dismiss();
                    }
                    return false;
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ShowPostDialog(PostList.get(position));
                    PostDialog.show();
                    return false;
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainActivity.this , PhotoView.class);
                    i.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) PostList);
                    i.putExtra("position",position);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            });
        }


        @Override
        public int getItemCount() {
            return PostList.size();
        }

        public class PostGridViewHolder extends RecyclerView.ViewHolder {

            ImageView PostImageView;
            ProgressBar PostProgressbar;
            TextView PostUploaderNameTextView;
            CircleImageView PostUploaderImageView;

            public PostGridViewHolder(View itemView) {
                super(itemView);

                PostImageView = itemView.findViewById(R.id.post_layout_imageview);
                PostProgressbar = itemView.findViewById(R.id.post_layout_progressbar);
                PostUploaderImageView = itemView.findViewById(R.id.post_layout_userimageview);
                PostUploaderNameTextView = itemView.findViewById(R.id.post_layout_usernametextview);

            }
        }


    }

    private void ShowPostDialog(PostModel postModel) {


        Glide.with(getApplicationContext())
                .load(postModel.getUri())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        PostDialogProgressbar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        PostDialogProgressbar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(PostDialogImageView);

    }


    public String getCurrentActiveCommunityID() {
        return CurrentActiveCommunityID;
    }


    public List<CommunityModel> getMyCommunityDetails() {
        return MyCommunityDetails;
    }


    public void setPostKeyForEdit(String postKeyForEdit) {
        PostKeyForEdit = postKeyForEdit;
    }

    public static void setCoverChange(boolean coverChange) {
        COVER_CHANGE = coverChange;
    }

    public int getPosition() {
        return Position;
    }


    public static void setProfileChange(boolean profileChange) {
        PROFILE_CHANGE = profileChange;
    }
}


