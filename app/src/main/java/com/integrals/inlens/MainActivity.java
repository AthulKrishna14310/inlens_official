package com.integrals.inlens;


import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
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
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.integrals.inlens.Activities.CreateCloudAlbum;
import com.integrals.inlens.Activities.InlensGalleryActivity;
import com.integrals.inlens.Activities.ProfileActivity;
import com.integrals.inlens.Activities.QRCodeReader;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.BottomSheetFragment;
import com.integrals.inlens.Helper.BottomSheetFragment_Inactive;
import com.integrals.inlens.Helper.CustomHorizontalRecyclerViewScrollListener;
import com.integrals.inlens.Helper.CustomVerticalRecyclerViewScrollListener;
import com.integrals.inlens.Helper.ExpandableCardView;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.MainCommunityViewHolder;
import com.integrals.inlens.Helper.ParticipantsAdapter;
import com.integrals.inlens.Helper.ReadFirebaseData;
import com.integrals.inlens.Interface.FirebaseRead;
import com.integrals.inlens.Interface.LoadMoreData;
import com.integrals.inlens.JobScheduler.Scheduler;
import com.integrals.inlens.Models.CommunityModel;
import com.integrals.inlens.Models.PhotographerModel;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class MainActivity extends AppCompatActivity {


    private String currentActiveCommunityID = AppConstants.NOTAVALABLE;
    private List<String> ParticipantIDs;

    private ProgressBar MainLoadingProgressBar;
    private Dialog QRCodeDialog;

    private Dialog PostDialog;
    private ImageView PostDialogImageView;
    private ProgressBar PostDialogProgressbar;

    private String PostKeyForEdit;
    int position;
    private static final int GALLERY_PICK = 1;
    private static final int COVER_GALLERY_PICK = 78;
    private static boolean COVER_CHANGE = false, PROFILE_CHANGE = false;
    private static boolean SEARCH_IN_PROGRESS = false;
    private NavigationView navigationView;
    private DrawerLayout RootForMainActivity;

    private int INTID = 3939;

    private RecyclerView MainHorizontalRecyclerview, MainVerticalRecyclerView;
    private Boolean SHOW_TOUR = false;


    private CircleImageView mainProfileImageview;
    private ImageButton MainSearchButton, MainBackButton;
    private EditText MainSearchEdittext;
    private RelativeLayout MainActionbar, MainSearchView;

    private BroadcastReceiver br;
    private RelativeLayout NoInternetView;
    private TextView NoInternetTextView;


    private static final int JOB_ID = 465;
    private JobScheduler jobScheduler;
    private JobInfo jobInfo;


    RecyclerView ParticipantsRecyclerView;


    String name = "";
    String imgurl = "";
    DatabaseReference getParticipantDatabaseReference;
    ExpandableCardView expandableCardView;



    FloatingActionButton mainAddPhotosFab;

    DatabaseReference currentUserRef, communityRef, participantRef, postRef;
    FirebaseAuth firebaseAuth;
    String currentUserId;
    ValueEventListener userRefListenerForActiveAlbum, communityRefListenerForActiveAlbum, coummunityUserAddListener, communitiesDataListener, postRefListener, participantRefListener;
    ReadFirebaseData readFirebaseData;
    ArrayList<String> userCommunityIdList;
    MainHorizontalAdapter mainHorizontalAdapter;
    MainVerticalAdapter mainVerticalAdapter;

    // info : underscore tells that the second one is a copy of the first
    List<CommunityModel> communityDataList = new ArrayList<>(), _communityDataList = new ArrayList<>();

    // info : linearlayout for scan and new album
    LinearLayout mainAlbumMenu;

    List<PostModel> postImageList = new ArrayList<>(), _postImageList = new ArrayList<>();


    private ImageButton mainNewAlbumButton, mainScanQrButton;

    // info : for vertical recyclerviewScrolling
    int lastVisiblesItems, visibleItemCount, totalItemCount;
    boolean loading = true;
    boolean isLoading = true;

    ParticipantsAdapter participantsAdapter;
    List<PhotographerModel> photographerList = new ArrayList<>();

    // info : toolbar
    ImageButton mainSearchButton;

    public MainActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // main actionbar
        mainProfileImageview = findViewById(R.id.mainactivity_actionbar_profileimageview);
        mainSearchButton = findViewById(R.id.mainactivity_actionbar_searchbutton);

        // scan and new albumbutton of main plus the menu
        mainNewAlbumButton = findViewById(R.id.main_horizontal_new_album_button);
        mainScanQrButton = findViewById(R.id.main_horizontal_scan_button);
        mainAlbumMenu = findViewById(R.id.mainAlbumMenu);

        // Fab
        mainAddPhotosFab = findViewById(R.id.fabadd);

        //photographers cardview
        expandableCardView = findViewById(R.id.photographers);


        // navigation view and drawerLayout  (Root)
        RootForMainActivity = findViewById(R.id.root_for_main_activity);
        navigationView = (NavigationView) findViewById(R.id.nv);

        //no internet views and function
        NoInternetView = findViewById(R.id.main_no_internet_relativelayout);
        NoInternetTextView = findViewById(R.id.main_no_internet_textview);
        checkInternetConnection();

        // firebase refs and auths
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        currentUserRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(currentUserId);
        communityRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.COMMUNITIES);
        participantRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PARTICIPANTS);
        postRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.POSTS);

        // custom function for waiting until firebase read is complete
        readFirebaseData = new ReadFirebaseData();

        // receiving all the community id under the user
        userCommunityIdList = getIntent().getExtras().getStringArrayList(AppConstants.USERIDLIST);
        Collections.reverse(userCommunityIdList);

        mainSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(RootForMainActivity,"Search is being done and will be completed soon. Thanks for your cooperation.",Snackbar.LENGTH_SHORT).show();

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.profile_preference_bg_service) {
                    enableBackgroundServices();
                    return true;
                } else if (item.getItemId() == R.id.profile_notification_stop) {

                    AlarmManagerHelper helper = new AlarmManagerHelper(MainActivity.this);
                    helper.deinitateAlarmManager();

                } else if (item.getItemId() == R.id.profile_notification_start) {

                    AlarmManagerHelper helper = new AlarmManagerHelper(MainActivity.this);
                    helper.initiateAlarmManager(5);

                } else if (item.getItemId() == R.id.profile_preference_battery_optimization) {

                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.profile_preference) {

                    // no need to check internet connection as if is given by default
                    setCoverChange(false);
                    setProfileChange(true);
                    GetStartedWithNewProfileImage();

                    return true;

                } else if (item.getItemId() == R.id.profile_activity) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
                    return true;
                }
                return true;
            }
        });

        mainProfileImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RootForMainActivity.openDrawer(Gravity.START);
            }
        });


        mainNewAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createAlbum();
            }
        });

        mainScanQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scanQR();
            }
        });

        mainAddPhotosFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CommunityModel model = getCommunityModel(currentActiveCommunityID);
                Intent intent = new Intent(getApplicationContext(), InlensGalleryActivity.class);
                intent.putExtra("CommunityID", currentActiveCommunityID);
                intent.putExtra("CommunityName", model.getTitle());
                intent.putExtra("CommunityStartTime", model.getStartTime());
                intent.putExtra("CommunityEndTime", model.getEndTime());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        MainHorizontalRecyclerview = (RecyclerView) findViewById(R.id.main_horizontal_recyclerview);
        MainHorizontalRecyclerview.setHasFixedSize(true);
        MainHorizontalRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mainHorizontalAdapter = new MainHorizontalAdapter(communityDataList, MainActivity.this);
        MainHorizontalRecyclerview.setAdapter(mainHorizontalAdapter);


        MainHorizontalRecyclerview.addOnScrollListener(new CustomHorizontalRecyclerViewScrollListener() {
            @Override
            public void show() {

                mainAlbumMenu.animate().translationX(0).setInterpolator(new DecelerateInterpolator(2));

            }

            @Override
            public void hide() {

                mainAlbumMenu.animate().translationX(mainAlbumMenu.getWidth()).setInterpolator(new AccelerateInterpolator(2));
            }

            @Override
            public void loadMore() {

                LinearLayoutManager manager = (LinearLayoutManager) MainHorizontalRecyclerview.getLayoutManager();
                int visibleItemCount = manager.getChildCount();
                int totalItemCount = manager.getItemCount();
                int lastVisiblesItems = manager.findLastVisibleItemPosition();

                if (isLoading) {

                    if ((visibleItemCount + lastVisiblesItems) >= totalItemCount) {
                        isLoading = false;
                        if (communityDataList.size() < _communityDataList.size()) {
                            isLoading = true;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int index = communityDataList.size();
                                    int end;
                                    if (index + 5 > _communityDataList.size()) {
                                        end = index + (Math.abs(index + 5 - _communityDataList.size()));
                                    } else {
                                        end = index + 5;
                                    }
                                    for (int i = index; i < end; i++) {
                                        try {
                                            communityDataList.add(_communityDataList.get(i));
                                            mainHorizontalAdapter.notifyItemInserted(i);
                                        } catch (IndexOutOfBoundsException e) {
                                        }

                                    }

                                }
                            }, 1000);
                        }


                    }

                }
            }
        });

        ParticipantsRecyclerView = findViewById(R.id.main_bottomsheet_particpants_bottomsheet_recyclerview);
        ParticipantsRecyclerView.setHasFixedSize(true);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        GridLayoutManager Gridmanager = new GridLayoutManager(getApplicationContext(), (int) Math.floor(dpWidth / 50));
        ParticipantsRecyclerView.setLayoutManager(Gridmanager);

        MainVerticalRecyclerView = findViewById(R.id.main_recyclerview);
        MainVerticalRecyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        MainVerticalRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mainVerticalAdapter = new MainVerticalAdapter(MainVerticalRecyclerView, MainActivity.this, postImageList, FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS), currentUserId);
        mainVerticalAdapter.setHasStableIds(true);
        MainVerticalRecyclerView.setAdapter(mainVerticalAdapter);

        MainVerticalRecyclerView.addOnScrollListener(new CustomVerticalRecyclerViewScrollListener()
        {
            @Override
            public void show() {

                if (!currentActiveCommunityID.equals(AppConstants.NOTAVALABLE)) {
                    mainAddPhotosFab.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2)).start();
                }
            }

            @Override
            public void hide() {

                if (!currentActiveCommunityID.equals(AppConstants.NOTAVALABLE)) {
                    mainAddPhotosFab.animate().translationY(mainAddPhotosFab.getHeight() + Math.round(TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()))).setInterpolator(new DecelerateInterpolator(2)).start();
                }
            }

            @Override
            public void loadMore() {

                visibleItemCount = staggeredGridLayoutManager.getChildCount();
                totalItemCount = staggeredGridLayoutManager.getItemCount();
                int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) Objects.requireNonNull(MainVerticalRecyclerView.getLayoutManager())).findLastVisibleItemPositions(null);
                lastVisiblesItems = getLastVisibleItem(lastVisibleItemPositions);


                if ((visibleItemCount + lastVisiblesItems) >= totalItemCount) {
                    if (postImageList.size() < _postImageList.size()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int index = postImageList.size();
                                int end;
                                if (index + 7 > _postImageList.size()) {
                                    end = index + (Math.abs(index + 7 - _postImageList.size()));
                                } else {
                                    end = index + 7;
                                }
                                for (int i = index; i < end; i++) {
                                    try {
                                        postImageList.add(_postImageList.get(i));
                                        MainVerticalRecyclerView.getAdapter().notifyItemInserted(i);
                                    } catch (IndexOutOfBoundsException e) {
                                    }

                                }
                            }
                        }, 1000);



                    }

                }

            }
        });




        /*


        MyCommunityDetails = new ArrayList<>();
        ParticipantIDs = new ArrayList<>();












        MainSearchButton = findViewById(R.id.mainactivity_actionbar_searchbutton);
        MainActionbar = findViewById(R.id.mainactivity_actionbar_relativelayout);
        MainSearchView = findViewById(R.id.mainactivity_searchview_relativelayout);
        MainBackButton = findViewById(R.id.mainactivity_searchview_backbutton);
        MainSearchEdittext = findViewById(R.id.mainactivity_searchview_edittext);



        MainLoadingProgressBar = findViewById(R.id.mainloadingpbar);

        FirebaseVariablesInit();
        CheckUserAuthentication();
        checkInternetConnection();
        InitPostDialog();




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

                            MainHorizontalAdapter adapter1 = new MainHorizontalAdapter(MainHorizontalRecyclerview, CommunitySearchDetails, MainActivity.this);
                            MainHorizontalRecyclerview.setAdapter(adapter1);

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




        getParticipantDatabaseReference = FirebaseDatabase.getInstance().getReference();


         */


    }

    public static void slideView(View view,
                                 int currentHeight,
                                 int newHeight) {

        ValueAnimator slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
                .setDuration(500);

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */

        slideAnimator.addUpdateListener(animation1 -> {
            Integer value = (Integer) animation1.getAnimatedValue();
            view.getLayoutParams().height = value.intValue();
            view.requestLayout();
        });

        /*  We use an animationSet to play the animation  */

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
    }

    public int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    private CommunityModel getCommunityModel(String currentActiveCommunityID) {
        CommunityModel model = new CommunityModel();
        for (int i = 0; i < _communityDataList.size(); i++) {
            if (currentActiveCommunityID.equals(_communityDataList.get(i).getCommunityID())) {
                model = _communityDataList.get(i);
                break;
            }
        }
        return model;
    }

    private void enableBackgroundServices() {


        if (Build.BRAND.equalsIgnoreCase("xiaomi")) {

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            startActivity(intent);


        } else if (Build.BRAND.equalsIgnoreCase("Letv")) {

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            startActivity(intent);

        } else if (Build.BRAND.equalsIgnoreCase("Honor")) {

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            startActivity(intent);

        } else if (Build.BRAND.equalsIgnoreCase("vivo")) {
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
        } else if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
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
        } else {
            // Set Content for Samsung
            Toast.makeText(getApplicationContext(), "Please enable or disable background tasks for your phone  manually", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences LastShownNotificationInfo = getSharedPreferences("LastNotification.pref", Context.MODE_PRIVATE);
        if (LastShownNotificationInfo.getAll().size() != 1) {
            SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
            editor.putString("time", String.valueOf(System.currentTimeMillis()));
            editor.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // FIXME has to update decryption and  encryption.
        decryptDeepLink();
        // get live community id and check if album  is active or the app should quit the user from the album
        // if the album status is true the we  can start the service from the getServerTime async task only if the end time has not been reached;
        userRefListenerForActiveAlbum = readFirebaseData.readData(currentUserRef, new FirebaseRead() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {

                // navigation drawer items should be updated.

                String name = snapshot.child("Name").getValue().toString();
                String email = snapshot.child("Email").getValue().toString();

                TextView navEmailTextView = navigationView.getHeaderView(0).findViewById(R.id.headerEmailX);
                TextView navNameTextView = navigationView.getHeaderView(0).findViewById(R.id.headerNameX);
                CircleImageView navProfileImageView = navigationView.getHeaderView(0).findViewById(R.id.headerImageView);

                navEmailTextView.setText(email);
                navNameTextView.setText(name);


                if (snapshot.hasChild("Profile_picture")) {

                    String imageUrl = snapshot.child("Profile_picture").getValue().toString();
                    Glide.with(getApplicationContext()).load(imageUrl).into(mainProfileImageview);
                    Glide.with(getApplicationContext()).load(imageUrl).into(navProfileImageView);

                }

                if (snapshot.hasChild(FirebaseConstants.LIVECOMMUNITYID)) {

                    currentActiveCommunityID = snapshot.child(FirebaseConstants.LIVECOMMUNITYID).getValue().toString();
                    QRCodeInit(currentActiveCommunityID);

                    // make the add photo fab visible
                    mainAddPhotosFab.setVisibility(View.VISIBLE);


                    // make the start and stop services in navigation drawer visible
                    navigationView.getMenu().findItem(R.id.profile_notification_start).setVisible(true);
                    navigationView.getMenu().findItem(R.id.profile_notification_stop).setVisible(true);

                    communityRefListenerForActiveAlbum = readFirebaseData.readData(communityRef.child(currentActiveCommunityID), new FirebaseRead() {
                        @Override
                        public void onSuccess(DataSnapshot snapshot) {

                            // optimization 1 resulted in this error, everytime the album is quit even if the album is inactive
                            // so first check the album  has status status;
                            if (snapshot.hasChild(FirebaseConstants.COMMUNITYSTATUS)) {

                                String status = snapshot.child(FirebaseConstants.COMMUNITYSTATUS).getValue().toString();
                                if (status.equals("T")) {
                                    long endtime = Long.parseLong(snapshot.child(FirebaseConstants.COMMUNITYENDTIME).getValue().toString());
                                    TimeZone timeZone = TimeZone.getDefault();
                                    long offsetInMillis =  timeZone.getOffset(Calendar.ZONE_OFFSET);
                                    long serverTimeInMillis = (System.currentTimeMillis()-offsetInMillis);
                                    //Log.i("time","Server : "+serverTimeInMillis+" End : "+endtime+" Systemmillis : "+System.currentTimeMillis());
                                    if (serverTimeInMillis >= endtime) {
                                        quitCloudAlbum(true);
                                    } else {
                                        // start the necessary services
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            ComponentName componentName = new ComponentName(MainActivity.this, Scheduler.class);
                                            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, componentName);
                                            builder.setPeriodic(5000);
                                            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                                            builder.setPersisted(true);
                                            jobInfo = builder.build();
                                            jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                                            jobScheduler.schedule(jobInfo);
                                        }
                                        AlarmManagerHelper helper = new AlarmManagerHelper(getApplicationContext());
                                        helper.initiateAlarmManager(5);

                                    }

                                } else {
                                    // stop the necessary services
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                                        jobScheduler.cancel(JOB_ID);
                                    }
                                    AlarmManagerHelper helper = new AlarmManagerHelper(getApplicationContext());
                                    helper.deinitateAlarmManager();


                                }
                            }

                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onFailure(DatabaseError databaseError) {


                        }
                    });
                } else {
                    navigationView.getMenu().findItem(R.id.profile_notification_start).setVisible(false);
                    navigationView.getMenu().findItem(R.id.profile_notification_stop).setVisible(false);


                }

                getCloudAlbumData(userCommunityIdList);

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(DatabaseError databaseError) {

            }
        });
    }

    // todo  delete this
    public String getDate(long time) {
        try {
            CharSequence Time = DateUtils.getRelativeDateTimeString(MainActivity.this, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
            return String.valueOf(Time);
        } catch (NumberFormatException e) {
            return "Nil";
        }
    }

    private void getCloudAlbumData(ArrayList<String> userCommunityIdList) {

        communitiesDataListener = readFirebaseData.readData(communityRef, new FirebaseRead() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {

                for (String communityId : userCommunityIdList) {
                    String admin = AppConstants.NOTAVALABLE, coverimage = AppConstants.NOTAVALABLE, description = AppConstants.NOTAVALABLE, endtime = AppConstants.NOTAVALABLE, starttime = AppConstants.NOTAVALABLE, status = AppConstants.NOTAVALABLE, title = AppConstants.NOTAVALABLE, type = AppConstants.NOTAVALABLE;
                    if (snapshot.child(communityId).hasChild("admin")) {
                        admin = snapshot.child(communityId).child("admin").getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild("coverimage")) {
                        coverimage = snapshot.child(communityId).child("coverimage").getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild("description")) {
                        description = snapshot.child(communityId).child("description").getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild("endtime")) {
                        endtime = snapshot.child(communityId).child("endtime").getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild("starttime")) {
                        starttime = snapshot.child(communityId).child("starttime").getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild("status")) {
                        status = snapshot.child(communityId).child("status").getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild("title")) {
                        title = snapshot.child(communityId).child("title").getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild("type")) {
                        type = snapshot.child(communityId).child("type").getValue().toString();
                    }

                    CommunityModel model = new CommunityModel(title, description, status, starttime, endtime, type, coverimage, admin, communityId);
                    if (!getCommunityKeys(_communityDataList).contains(communityId)) {
                        _communityDataList.add(model);
                    }
                }
                // info sorting by endtime
                Collections.sort(_communityDataList, Collections.reverseOrder());
                for (int i = 0; i < _communityDataList.size() && i < 5; i++) {
                    communityDataList.add(_communityDataList.get(i));
                }
                mainHorizontalAdapter.notifyDataSetChanged();
                MainHorizontalRecyclerview.scheduleLayoutAnimation();


            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(DatabaseError databaseError) {

            }
        });
    }

    private List<String> getCommunityKeys(List<CommunityModel> communityDataList) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < communityDataList.size(); i++) {
            keys.add(communityDataList.get(i).getCommunityID());
        }

        return keys;
    }

    private List<String> getPostKeys(List<PostModel> communityDataList) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < communityDataList.size(); i++) {
            keys.add(communityDataList.get(i).getPoskKey());
        }

        return keys;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (communityRefListenerForActiveAlbum != null) {
            communityRef.child(currentActiveCommunityID).removeEventListener(communityRefListenerForActiveAlbum);
        }
        if (userRefListenerForActiveAlbum != null) {
            currentUserRef.removeEventListener(userRefListenerForActiveAlbum);
        }
        if (coummunityUserAddListener != null) {
            currentUserRef.removeEventListener(coummunityUserAddListener);
        }
        if (communitiesDataListener != null) {
            communityRef.removeEventListener(communitiesDataListener);
        }
        if (postRefListener != null) {
            postRef.removeEventListener(postRefListener);
        }
        if (participantRefListener != null) {
            participantRef.removeEventListener(participantRefListener);
        }


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


    }


    public void QRCodeInit(final String CommunityID) {

        QRCodeDialog = new Dialog(MainActivity.this, android.R.style.Theme_Light_NoTitleBar);
        QRCodeDialog.setCancelable(true);
        QRCodeDialog.setCanceledOnTouchOutside(true);
        QRCodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        QRCodeDialog.setContentView(R.layout.qrcode_generator_layout);
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


        if (!CommunityID.equals(AppConstants.NOTAVALABLE)) {
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

                shareInviteLink(CommunityID);


            }
        });

        //FIXME dialog hidden by elson
        //QRCodeDialog.show();

    }

    private void decryptDeepLink() {

        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
            @Override
            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {

                if (pendingDynamicLinkData != null) {
                    Uri deeplink = pendingDynamicLinkData.getLink();
                    String communityId = deeplink.toString().replace("https://inlens.com=", "");
                    if (currentActiveCommunityID.equals(AppConstants.NOTAVALABLE)) {

                        // fixme add cancel option too
                        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(MainActivity.this)
                                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                                .setTitle("New Community")
                                .setIcon(R.drawable.inlens_logo)
                                .setCancelable(false)
                                .setMessage("You are about to join a new community.")
                                .addButton("Join", -1, getResources().getColor(R.color.colorAccent), CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                AddCommunityToUserRef(communityId);
                                                dialog.dismiss();
                                            }
                                        })
                                .addButton("Cancel", -1, getResources().getColor(R.color.deep_orange_A400), CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });

                        builder.show();


                    } else {

                        if (currentActiveCommunityID.equals(communityId)) {

                            showInfoMessage("Your Community", "You are currently part of this community.");
                        } else {
                            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(MainActivity.this)
                                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                                    .setTitle("New Community")
                                    .setIcon(R.drawable.inlens_logo)
                                    .setMessage("Are you sure you want to join this new community? This means quitting the previous one.")
                                    .setTextGravity(Gravity.START)
                                    .setCancelable(false)
                                    .addButton("YES", -1, getResources().getColor(R.color.colorAccent), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    AddCommunityToUserRef(communityId);
                                                    dialog.dismiss();
                                                }
                                            })
                                    .addButton("NO", -1, getResources().getColor(R.color.deep_orange_A400), CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                            builder.show();
                        }

                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                showInfoMessage("Data Fetch Failed", e.getMessage());

            }
        });
    }

    private void AddCommunityToUserRef(final String communityId) {

        communityRef.child(communityId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(FirebaseConstants.COMMUNITYSTATUS)) {
                    long endtime = Long.parseLong(dataSnapshot.child("endtime").getValue().toString());
                    TimeZone timeZone = TimeZone.getDefault();
                    long offsetInMillis =  timeZone.getOffset(Calendar.ZONE_OFFSET);
                    long serverTimeInMillis = (System.currentTimeMillis()-offsetInMillis);

                    if (serverTimeInMillis < endtime) {
                        currentUserRef.child(FirebaseConstants.COMMUNITIES).child(communityId).setValue(ServerValue.TIMESTAMP);
                        participantRef.child(communityId).child(currentUserId).setValue(ServerValue.TIMESTAMP);

                        List<String> newCommunities = new ArrayList<>();
                        newCommunities.add(communityId);
                        newCommunities.addAll(userCommunityIdList);
                        userCommunityIdList.clear();
                        userCommunityIdList.addAll(newCommunities);
                        getCloudAlbumData(userCommunityIdList);

                    } else {
                        showDialogMessage("Album Inactive", "The album has expired or admin has made the album inactive.");

                    }

                } else {
                    showDialogMessage("Album Inactive", "The album has expired or admin has made the album inactive.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showDialogMessage("Error Caught", databaseError.toString());

            }
        });


    }


    private void createAlbum() {


        if (currentActiveCommunityID.equals(AppConstants.NOTAVALABLE)) {
            startActivity(new Intent(MainActivity.this, CreateCloudAlbum.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {

            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                    .setTitle("Album Active")
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setMessage("You have to leave the currently active album before creating a new album.")
                    .setCancelable(true)
                    .addButton("   Quit album  ", -1, Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                            CFAlertDialog.CFAlertActionAlignment.END,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    quitCloudAlbum(false);
                                }
                            });
            builder.show();

        }


    }

    private void scanQR() {

        if (currentActiveCommunityID.equals("Not Available")) {
            startActivity(new Intent(MainActivity.this, QRCodeReader.class));
        } else {
            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                    .setTitle("Album Active")
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setMessage("You have to leave the currently active album before joining a new album.")
                    .setCancelable(true)
                    .addButton("   Quit album  ", -1, Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                            CFAlertDialog.CFAlertActionAlignment.END,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    quitCloudAlbum(false);
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


    public void setVerticalRecyclerView(String communityID) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (expandableCardView.isExpanded()) {
                    expandableCardView.collapse();
                }

            }
        }, 50);



        try {

            setParticipants(communityID);

            postRefListener = readFirebaseData.readData(postRef.child(communityID), new FirebaseRead() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    _postImageList.clear();
                    postImageList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        String by = AppConstants.NOTAVALABLE, time = AppConstants.NOTAVALABLE, uri = AppConstants.NOTAVALABLE;

                        if (snapshot.hasChild("by")) {
                            by = snapshot.child("by").getValue().toString();
                        }
                        if (snapshot.hasChild("time")) {
                            time = snapshot.child("time").getValue().toString();
                        }
                        if (snapshot.hasChild("uri")) {
                            uri = snapshot.child("uri").getValue().toString();
                        }
                        if (!getPostKeys(_postImageList).contains(key)) {
                            _postImageList.add(new PostModel(key, uri, time, by));
                        }

                    }

                    Collections.reverse(_postImageList);
                    for (int i = 0; i < 7 && i < _postImageList.size(); i++) {
                        if (!getPostKeys(postImageList).contains(_postImageList.get(i).getPoskKey())) {
                            postImageList.add(_postImageList.get(i));
                        }

                    }
                    mainVerticalAdapter.notifyDataSetChanged();
                    MainVerticalRecyclerView.scheduleLayoutAnimation();


                }

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {
            Log.i("setVerticialRecyclervie", "Null Pointer");
            e.printStackTrace();
        }


    }


    private void setParticipants(String communityID) {


        participantRefListener = readFirebaseData.readData(participantRef.child(communityID), new FirebaseRead() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                photographerList.clear();
                if (currentActiveCommunityID.equals(communityID)) {
                    photographerList.add(new PhotographerModel("add","add","add"));

                }
                DatabaseReference photographerRef = FirebaseDatabase.getInstance().getReference().child("Users");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    photographerRef.child(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {

                            String name = AppConstants.NOTAVALABLE, imgurl = AppConstants.NOTAVALABLE;
                            if (userSnapshot.hasChild("Name")) {
                                if (currentUserId.equals(snapshot.getKey())) {
                                    name = "You";

                                } else {
                                    name = userSnapshot.child("Name").getValue().toString();
                                }

                            }
                            if (userSnapshot.hasChild("Profile_picture")) {
                                imgurl = userSnapshot.child("Profile_picture").getValue().toString();
                            }

                            if (!getPhotographerKeys(photographerList).contains(snapshot.getKey())) {

                                photographerList.add(new PhotographerModel(name, snapshot.getKey(), imgurl));

                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                participantsAdapter = new ParticipantsAdapter(photographerList, getApplicationContext(), QRCodeDialog);
                ParticipantsRecyclerView.setAdapter(participantsAdapter);

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(DatabaseError databaseError) {

            }
        });


    }

    private List<String> getPhotographerKeys(List<PhotographerModel> photographerList) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < photographerList.size(); i++) {
            try
            {
                keys.add(photographerList.get(i).getId());
            }
            catch (NullPointerException e)
            {
                continue;
            }
        }

        return keys;
    }

    public void quitCloudAlbum(boolean forceQuit) {

        if (forceQuit) {
            communityRef.child(currentActiveCommunityID).child(FirebaseConstants.COMMUNITYSTATUS).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).removeValue();
                        currentActiveCommunityID = AppConstants.NOTAVALABLE;
                        mainAddPhotosFab.setVisibility(View.GONE);
                        AlarmManagerHelper alarmManagerHelper =
                                new AlarmManagerHelper(getApplicationContext());
                        alarmManagerHelper.deinitateAlarmManager();
                        showDialogMessage("Cloud-Album Quit", "Successfully left from the Cloud-Album");
                        //SetDefaultView();

                    } else {
                        //SetDefaultView();
                        showDialogQuitUnsuccess();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    showDialogQuitUnsuccess();

                }
            });

        } else {
            showAlbumQuitPrompt("Leaving Community", "Are you sure you want to quit the current Cloud-Album. You won't able to upload photos to this album again.", "No", "Yes");

        }


    }

    private void showAlbumQuitPrompt(String title, String message, String postiveButtonMessage, String negativeButtonMessage) {

        //fixme this dialog ui is not good

        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle(title)
                .setIcon(R.drawable.ic_cancel_black_24dp)
                .setMessage(message)
                .setCancelable(false)
                .addButton(negativeButtonMessage, -1, getResources().getColor(R.color.deep_orange_A400), CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                communityRef.child(currentActiveCommunityID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        String admin = dataSnapshot.child(FirebaseConstants.COMMUNITYADMIN).getValue().toString();
                                        if (admin.equals(currentUserId)) {
                                            communityRef.child(currentActiveCommunityID).child(FirebaseConstants.COMMUNITYSTATUS).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).removeValue();
                                                        currentActiveCommunityID = AppConstants.NOTAVALABLE;
                                                        mainAddPhotosFab.setVisibility(View.GONE);
                                                        AlarmManagerHelper alarmManagerHelper =
                                                                new AlarmManagerHelper(getApplicationContext());
                                                        alarmManagerHelper.deinitateAlarmManager();
                                                        showDialogMessage("Cloud-Album Quit", "Successfully left from the Cloud-Album");
                                                        //SetDefaultView();

                                                    } else {
                                                        //SetDefaultView();
                                                        showDialogQuitUnsuccess();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    showDialogQuitUnsuccess();

                                                }
                                            });
                                        } else {
                                            currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {

                                                        AlarmManagerHelper alarmManagerHelper =
                                                                new AlarmManagerHelper(getApplicationContext());
                                                        alarmManagerHelper.deinitateAlarmManager();
                                                        showDialogMessage("Cloud-Album Quit", "Successfully left from the Cloud-Album");
                                                        //SetDefaultView();


                                                    } else {
                                                        //SetDefaultView();
                                                        showDialogQuitUnsuccess();
                                                    }

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    showDialogQuitUnsuccess();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                dialog.dismiss();

                            }
                        })
                .addButton(postiveButtonMessage, -1, getResources().getColor(R.color.colorAccent), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });


        builder.show();

    }

    private void showDialogQuitUnsuccess() {

        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle("Cloud-Album Quit")
                .setIcon(R.drawable.ic_cancel_black_24dp)
                .setMessage("Unable to Quit Cloud-Album. Please check your internet connection or whether you are participating in a Cloud-Album")
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
                        Toast.makeText(getApplicationContext(), "Uploading your profile-picture please wait ", Toast.LENGTH_SHORT).show();
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
                                    communityDataList.get(position).setCoverImage(downloadUrl);
                                    mainHorizontalAdapter.notifyItemChanged(position);
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
        } else if (RootForMainActivity.isDrawerOpen(GravityCompat.START)) {
            RootForMainActivity.closeDrawer(GravityCompat.START);
        }
       /*
        else if (toolbarCustomView.isShown()) {
            toolbarCustomView.clearAnimation();
            toolbarCustomView.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_back_up));
            toolbarCustomView.clearAnimation();
            toolbarCustomView.setVisibility(View.GONE);
            appBarLayout.setExpanded(true);

            //TODO scroll the recyclerview to the top

        }
        */
        else {
            super.onBackPressed();
        }
    }


    private void shareInviteLink(String CommunityID) {

        String url = "https://inlens.page.link/?link=https://inlens.com=" + CommunityID + "&apn=com.integrals.inlens";
        final Intent SharingIntent = new Intent(Intent.ACTION_SEND);
        SharingIntent.setType("text/plain");
        SharingIntent.putExtra(Intent.EXTRA_TEXT, "Inlens Community Invite Link \n" + url);
        MainActivity.this.startActivity(SharingIntent);

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
                        }, 1000);

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

    public void showInfoMessage(String title, String message) {
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle(title)
                .setIcon(R.drawable.ic_info_black_24dp)
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


    public class MainVerticalAdapter extends RecyclerView.Adapter<MainVerticalAdapter.PostGridViewHolder> {

        List<PostModel> PostList;
        DatabaseReference UserRef;
        Picasso picasso;
        String comID;
        Activity activity;

        public MainVerticalAdapter(RecyclerView recyclerView, Activity activity, List<PostModel> postList, DatabaseReference userRef, String ID) {
            this.activity = activity;
            PostList = postList;
            UserRef = userRef;
            picasso = Picasso.get();
            picasso.setIndicatorsEnabled(false);
            comID = ID;


        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public PostGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(activity).inflate(R.layout.post_layout, parent, false);
            return new MainVerticalAdapter.PostGridViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final PostGridViewHolder holder, final int position) {

            if (PostList.get(position) != null) {
                holder.itemView.clearAnimation();
                holder.itemView.setAnimation(AnimationUtils.loadAnimation(activity, android.R.anim.fade_in));
                holder.itemView.getAnimation().start();

                RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_photo_camera);


            /*
            picasso.load(PostList.get(position).getUri())
                    .resizeDimen(R.dimen.main_image_dimen200,R.dimen.main_image_dimen200)
                    .centerCrop()
                    .into(holder.PostImageView)
            ;
             */
                Glide.with(activity)
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

                        } else {
                            holder.PostUploaderNameTextView.setText("Unknown");
                        }
                        if (dataSnapshot.hasChild("Profile_picture")) {
                            String UploaderImageUrl = dataSnapshot.child("Profile_picture").getValue().toString();
                            Glide.with(activity).load(UploaderImageUrl).into(holder.PostUploaderImageView);

                        } else {
                            Glide.with(activity).load(R.drawable.ic_account_circle).into(holder.PostUploaderImageView);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        if (motionEvent.getAction() == MotionEvent.ACTION_UP && PostDialog.isShowing()) {
                            PostDialog.dismiss();
                        }
                        return false;
                    }
                });


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*
                        Intent i = new Intent(MainActivity.this, PhotoView.class);
                        i.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) PostList);
                        i.putExtra("position", position);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(i);
                         */
                    }
                });
            }


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

    public class MainHorizontalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<CommunityModel> communityDetails;
        private final int VIEW_TYPE_ALBUM = 0, VIEW_TYPE_LOADING = 1;
        LoadMoreData loadMoreData;
        Activity activity;
        int selectedAlbumPosition = 0;
        String selectedAlbumKey;

        public MainHorizontalAdapter(List<CommunityModel> communityDetails, Activity activity) {
            this.communityDetails = communityDetails;
            this.activity = activity;
            selectedAlbumKey = AppConstants.NOTAVALABLE;
        }

        @Override
        public int getItemViewType(int position) {
            return communityDetails.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ALBUM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.album_card, parent, false);
            return new MainCommunityViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof MainCommunityViewHolder) {
                MainCommunityViewHolder viewHolder = (MainCommunityViewHolder) holder;

                if (selectedAlbumPosition == viewHolder.getLayoutPosition()) {
                    viewHolder.Indicator.setVisibility(View.VISIBLE);
                    viewHolder.itemView.setAlpha((float) 1);
                    if (!selectedAlbumKey.equals(communityDetails.get(selectedAlbumPosition).getCommunityID())) {
                        setVerticalRecyclerView(communityDetails.get(selectedAlbumPosition).getCommunityID());

                    }
                    selectedAlbumKey = communityDetails.get(selectedAlbumPosition).getCommunityID();

                }else {
                    viewHolder.Indicator.setVisibility(View.INVISIBLE);
                    viewHolder.itemView.setAlpha((float) 0.85);
                }

                if (!communityDetails.get(position).equals(AppConstants.NOTAVALABLE)) {

                    Glide.with(activity)
                            .load(communityDetails.get(position)
                                    .getCoverImage()).addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    }).into(viewHolder.AlbumCoverButton);
                }


                viewHolder.AlbumCoverButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (communityDetails.get(position).getCommunityID().equals(currentActiveCommunityID)) {
                            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(activity, communityDetails.get(position),position);
                            bottomSheetFragment.show(((FragmentActivity) activity).getSupportFragmentManager(), bottomSheetFragment.getTag());
                        } else {
                            BottomSheetFragment_Inactive bottomSheetFragment_inactive = new BottomSheetFragment_Inactive(activity, communityDetails.get(position),position);
                            bottomSheetFragment_inactive.show(((FragmentActivity) activity).getSupportFragmentManager(), bottomSheetFragment_inactive.getTag());
                        }
                        return false;
                    }
                });

                viewHolder.menuOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (communityDetails.get(position).getStatus().equals("T")) {
                            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(activity, communityDetails.get(position), position);
                            bottomSheetFragment.show(((FragmentActivity) activity).getSupportFragmentManager(), bottomSheetFragment.getTag());
                        } else {
                            BottomSheetFragment_Inactive bottomSheetFragment_inactive = new BottomSheetFragment_Inactive(activity, communityDetails.get(position), position);
                            bottomSheetFragment_inactive.show(((FragmentActivity) activity).getSupportFragmentManager(), bottomSheetFragment_inactive.getTag());
                        }

                    }
                });

                viewHolder.AlbumCoverButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (selectedAlbumPosition != viewHolder.getAdapterPosition()) {
                            notifyItemChanged(selectedAlbumPosition);
                            notifyItemChanged(viewHolder.getAdapterPosition());
                            selectedAlbumPosition = viewHolder.getAdapterPosition();
                            setVerticalRecyclerView(communityDetails.get(position).getCommunityID());

                        }
                    }
                });

                viewHolder.AlbumNameTextView.setText(communityDetails.get(position).getTitle());
                viewHolder.AlbumDescriptionTextView.setText(communityDetails.get(position).getDescription());


            }
        }


        @Override
        public int getItemCount() {
            return communityDetails.size();
        }


    }


    public void setCommunityKeyForEdit(String postKeyForEdit,int pos) {
        PostKeyForEdit = postKeyForEdit;
        position = pos;
    }

    public static void setCoverChange(boolean coverChange) {
        COVER_CHANGE = coverChange;
    }

    public static void setProfileChange(boolean profileChange) {
        PROFILE_CHANGE = profileChange;
    }

}