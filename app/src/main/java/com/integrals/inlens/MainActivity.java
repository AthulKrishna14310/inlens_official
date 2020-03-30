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
import android.content.pm.PackageManager;
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
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.integrals.inlens.Activities.CreateCloudAlbum;
import com.integrals.inlens.Activities.InlensGalleryActivity;
import com.integrals.inlens.Activities.PhotoView;
import com.integrals.inlens.Activities.QRCodeReader;
import com.integrals.inlens.Helper.AlbumOptionsBottomSheetFragment;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.BottomSheetFragment;
import com.integrals.inlens.Helper.BottomSheetFragment_Inactive;
import com.integrals.inlens.Helper.CustomHorizontalRecyclerViewScrollListener;
import com.integrals.inlens.Helper.CustomVerticalRecyclerViewScrollListener;
import com.integrals.inlens.Helper.ExpandableCardView;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.MainCommunityViewHolder;
import com.integrals.inlens.Helper.MainHorizontalLoadingViewHolder;
import com.integrals.inlens.Helper.MainHorizontalOptionsViewHolder;
import com.integrals.inlens.Helper.ParticipantsAdapter;
import com.integrals.inlens.Helper.ReadFirebaseData;
import com.integrals.inlens.Interface.FirebaseRead;
import com.integrals.inlens.JobScheduler.Scheduler;
import com.integrals.inlens.Models.CommunityModel;
import com.integrals.inlens.Models.PhotographerModel;
import com.integrals.inlens.Models.PostModel;
import com.integrals.inlens.Notification.AlarmManagerHelper;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

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

import static com.integrals.inlens.Helper.AppConstants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity implements AlbumOptionsBottomSheetFragment.IScanCallback, AlbumOptionsBottomSheetFragment.ICreateCallback {


    private String currentActiveCommunityID = AppConstants.NOT_AVALABLE;

    private Dialog QRCodeDialog;

    private String PostKeyForEdit;
    int position;
    private static final int GALLERY_PICK = 1;
    private static final int COVER_GALLERY_PICK = 78;
    private static boolean COVER_CHANGE = false, PROFILE_CHANGE = false;
    private NavigationView navigationView;
    private DrawerLayout RootForMainActivity;


    private RecyclerView MainHorizontalRecyclerview, MainVerticalRecyclerView;

    private CircleImageView mainProfileImageview;

    private BroadcastReceiver br;
    private RelativeLayout NoInternetView;
    private TextView NoInternetTextView;


    private static final int JOB_ID = 465;
    private JobScheduler jobScheduler;
    private JobInfo jobInfo;


    RecyclerView ParticipantsRecyclerView;
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


    List<PostModel> postImageList = new ArrayList<>(), _postImageList = new ArrayList<>();

    // info : for vertical recyclerviewScrolling
    int lastVisiblesItems, visibleItemCount, totalItemCount;
    boolean loading = true;
    boolean isLoading = true;

    ParticipantsAdapter participantsAdapter;
    List<PhotographerModel> photographerList = new ArrayList<>();

    // info : toolbar
    ImageButton mainSearchButton;

    // info onbackpressed
    AppBarLayout appBarLayout;

    boolean isAppbarOpen = true;

    public MainActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // on backpressed
        appBarLayout = findViewById(R.id.main_appbarlayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    isAppbarOpen = false;
                } else isAppbarOpen = verticalOffset == 0;
            }
        });

        // main actionbar
        mainProfileImageview = findViewById(R.id.mainactivity_actionbar_profileimageview);
        mainSearchButton = findViewById(R.id.mainactivity_actionbar_searchbutton);

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

        // receiving all the community id under the user and sorting them in reverse order to get the latest first
        try {

            userCommunityIdList = getIntent().getExtras().getStringArrayList(AppConstants.USER_ID_LIST);
            //info removing all null values
            if (userCommunityIdList.contains(null)) {
                userCommunityIdList.removeAll(null);
            }
            Collections.sort(userCommunityIdList, Collections.reverseOrder());
        } catch (Exception e) {
            //
        }

        mainSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(RootForMainActivity, "Search is being done and will be completed soon. Thank you for your cooperation.", Snackbar.LENGTH_SHORT).show();

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




        mainAddPhotosFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showPermissionDialog();

                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);


                    }
                } else {
                    CommunityModel model = getCommunityModel(currentActiveCommunityID);
                    Intent intent = new Intent(getApplicationContext(), InlensGalleryActivity.class);
                    intent.putExtra("CommunityID", currentActiveCommunityID);
                    intent.putExtra("CommunityName", model.getTitle());
                    intent.putExtra("CommunityStartTime", model.getStartTime());
                    intent.putExtra("CommunityEndTime", model.getEndTime());
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }

            }
        });

        MainHorizontalRecyclerview = (RecyclerView) findViewById(R.id.main_horizontal_recyclerview);
        MainHorizontalRecyclerview.setHasFixedSize(true);
        MainHorizontalRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mainHorizontalAdapter = new MainHorizontalAdapter(communityDataList, MainActivity.this);
        MainHorizontalRecyclerview.setAdapter(mainHorizontalAdapter);


        MainHorizontalRecyclerview.addOnScrollListener(new CustomHorizontalRecyclerViewScrollListener() {
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
                                    for(int i=communityDataList.size()-1;i>-1;i--)
                                    {
                                        if(communityDataList.get(i)==null)
                                        {
                                            communityDataList.remove(i);
                                        }
                                    }
                                    int index = communityDataList.size();
                                    int end;
                                    if (index + 5 > _communityDataList.size()) {
                                        end = index + (Math.abs(index + 5 - _communityDataList.size()));
                                    } else {
                                        end = index + 5;
                                    }
                                    for (int i = index; i < end; i++) {
                                        try {
                                            // info to avoid repetition
                                            if (!getCommunityKeys(communityDataList).contains(_communityDataList.get(i).getCommunityID())) {
                                                communityDataList.add(_communityDataList.get(i));
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                        }

                                    }
                                    if (communityDataList.size() < _communityDataList.size()) {
                                        communityDataList.add(null);
                                    }
                                    mainHorizontalAdapter.notifyDataSetChanged();
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

        MainVerticalRecyclerView.addOnScrollListener(new CustomVerticalRecyclerViewScrollListener() {
            @Override
            public void show() {

                if (!currentActiveCommunityID.equals(AppConstants.NOT_AVALABLE)) {
                    mainAddPhotosFab.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2)).start();
                }
            }

            @Override
            public void hide() {

                if (!currentActiveCommunityID.equals(AppConstants.NOT_AVALABLE)) {
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

    private void showPermissionDialog() {

        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(MainActivity.this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle("Storage Permission")
                .setIcon(R.drawable.ic_info)
                .setMessage("InLens require storage permission to access your photos. Please enable it and try again.")
                .setCancelable(false)
                .addButton("OK", -1, getResources().getColor(R.color.colorAccent), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                                dialog.dismiss();
                            }
                        })
                .addButton("CANCEl", -1, getResources().getColor(R.color.deep_orange_A400), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        builder.show();
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
            showSnackbarMessage("Please enable or disable background tasks for your phone  manually");
        }
    }

    private void showSnackbarMessage(String message) {
        Snackbar.make(RootForMainActivity, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences LastShownNotificationInfo = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
        if (!LastShownNotificationInfo.contains("time")) {
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

                SharedPreferences LastShownNotificationInfo = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);

                if (snapshot.hasChild(FirebaseConstants.LIVECOMMUNITYID)) {

                    currentActiveCommunityID = snapshot.child(FirebaseConstants.LIVECOMMUNITYID).getValue().toString();
                    if (!LastShownNotificationInfo.contains("id")) {
                        SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
                        editor.putString("id", currentActiveCommunityID);
                        editor.commit();
                    }

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

                                    if (!LastShownNotificationInfo.contains("stopAt")) {
                                        SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
                                        editor.putString("stopAt", String.valueOf(endtime));
                                        editor.commit();
                                    }

                                    TimeZone timeZone = TimeZone.getDefault();
                                    long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
                                    long serverTimeInMillis = (System.currentTimeMillis() - offsetInMillis);
                                    Log.i("timeQuit","Server : "+serverTimeInMillis+" End : "+endtime+" Systemmillis : "+System.currentTimeMillis());
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


    private void getCloudAlbumData(ArrayList<String> userCommunityIdList) {

        if(communityDataList.size()==0)
        {
            communityDataList.add(new CommunityModel(
                AppConstants.NOT_AVALABLE,
                AppConstants.NOT_AVALABLE,
                AppConstants.NOT_AVALABLE,
                AppConstants.NOT_AVALABLE,
                AppConstants.NOT_AVALABLE,
                AppConstants.NOT_AVALABLE,
                AppConstants.NOT_AVALABLE,
                AppConstants.NOT_AVALABLE,
                AppConstants.MORE_OPTIONS
            ));
        }
        else if(!communityDataList.get(0).getCommunityID().equals(AppConstants.MORE_OPTIONS) )
        {
            communityDataList.add(0,new CommunityModel(
                    AppConstants.NOT_AVALABLE,
                    AppConstants.NOT_AVALABLE,
                    AppConstants.NOT_AVALABLE,
                    AppConstants.NOT_AVALABLE,
                    AppConstants.NOT_AVALABLE,
                    AppConstants.NOT_AVALABLE,
                    AppConstants.NOT_AVALABLE,
                    AppConstants.NOT_AVALABLE,
                    AppConstants.MORE_OPTIONS
            ));
        }

        communitiesDataListener = readFirebaseData.readData(communityRef, new FirebaseRead() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {

                for (String communityId : userCommunityIdList) {
                    String admin = AppConstants.NOT_AVALABLE, coverimage = AppConstants.NOT_AVALABLE, description = AppConstants.NOT_AVALABLE, endtime = AppConstants.NOT_AVALABLE, starttime = AppConstants.NOT_AVALABLE, status = AppConstants.NOT_AVALABLE, title = AppConstants.NOT_AVALABLE, type = AppConstants.NOT_AVALABLE;
                    if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITYADMIN)) {
                        admin = snapshot.child(communityId).child(FirebaseConstants.COMMUNITYADMIN).getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITYCOVERIMAGE)) {
                        coverimage = snapshot.child(communityId).child(FirebaseConstants.COMMUNITYCOVERIMAGE).getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITYDESC)) {
                        description = snapshot.child(communityId).child(FirebaseConstants.COMMUNITYDESC).getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITYENDTIME)) {
                        endtime = snapshot.child(communityId).child(FirebaseConstants.COMMUNITYENDTIME).getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITYSTARTTIME)) {
                        starttime = snapshot.child(communityId).child(FirebaseConstants.COMMUNITYSTARTTIME).getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITYSTATUS)) {
                        status = snapshot.child(communityId).child(FirebaseConstants.COMMUNITYSTATUS).getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITYTITLE)) {
                        title = snapshot.child(communityId).child(FirebaseConstants.COMMUNITYTITLE).getValue().toString();
                    }
                    if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITYTYPE)) {
                        type = snapshot.child(communityId).child(FirebaseConstants.COMMUNITYTYPE).getValue().toString();
                    }

                    CommunityModel model = new CommunityModel(title, description, status, starttime, endtime, type, coverimage, admin, communityId);
                    if (!getCommunityKeys(_communityDataList).contains(communityId)) {
                        _communityDataList.add(model);
                    }
                }
                // info sorting by endtime
                Collections.sort(_communityDataList, Collections.reverseOrder());
                for (int i = 0; i < _communityDataList.size() && i < 5; i++) {
                    if (!getCommunityKeys(communityDataList).contains(_communityDataList.get(i).getCommunityID())) {
                        communityDataList.add(_communityDataList.get(i));
                    }
                }
                for(int i=communityDataList.size()-1;i>-1;i--)
                {
                    if(communityDataList.get(i)==null)
                    {
                        communityDataList.remove(i);
                    }
                }
                communityDataList.add(null);
                mainHorizontalAdapter.notifyDataSetChanged();


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
            if (communityDataList.get(i) != null) {
                keys.add(communityDataList.get(i).getCommunityID());
            }
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
                .setAspectRatio(1, 1)
                .start(MainActivity.this);
    }


    public void QRCodeInit(final String CommunityID) {

        QRCodeDialog = new Dialog(MainActivity.this, android.R.style.Theme_Light_NoTitleBar);
        QRCodeDialog.setCancelable(true);
        QRCodeDialog.setCanceledOnTouchOutside(true);
        QRCodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        QRCodeDialog.setContentView(R.layout.qrcode_generator_layout);
        QRCodeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
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


        if (!CommunityID.equals(AppConstants.NOT_AVALABLE)) {
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
                    if (currentActiveCommunityID.equals(AppConstants.NOT_AVALABLE)) {

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
                                                addCommunityToUserRef(communityId);
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
                                                    addCommunityToUserRef(communityId);
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

    private void addCommunityToUserRef(final String communityId) {

        communityRef.child(communityId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(FirebaseConstants.COMMUNITYSTATUS)) {
                    long endtime = Long.parseLong(dataSnapshot.child("endtime").getValue().toString());
                    TimeZone timeZone = TimeZone.getDefault();
                    long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
                    long serverTimeInMillis = (System.currentTimeMillis() - offsetInMillis);

                    if (serverTimeInMillis < endtime) {

                        currentActiveCommunityID = communityId;
                        currentUserRef.child(FirebaseConstants.COMMUNITIES).child(communityId).setValue(ServerValue.TIMESTAMP);
                        currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).setValue(communityId);
                        participantRef.child(communityId).child(currentUserId).setValue(ServerValue.TIMESTAMP);

                        userCommunityIdList.add(0, communityId);
                        communityRef.child(communityId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {

                                String admin = AppConstants.NOT_AVALABLE, coverimage = AppConstants.NOT_AVALABLE, description = AppConstants.NOT_AVALABLE, endtime = AppConstants.NOT_AVALABLE, starttime = AppConstants.NOT_AVALABLE, status = AppConstants.NOT_AVALABLE, title = AppConstants.NOT_AVALABLE, type = AppConstants.NOT_AVALABLE;
                                if (snapshot.hasChild(FirebaseConstants.COMMUNITYADMIN)) {
                                    admin = snapshot.child(FirebaseConstants.COMMUNITYADMIN).getValue().toString();
                                }
                                if (snapshot.hasChild(FirebaseConstants.COMMUNITYCOVERIMAGE)) {
                                    coverimage = snapshot.child(FirebaseConstants.COMMUNITYCOVERIMAGE).getValue().toString();
                                }
                                if (snapshot.hasChild(FirebaseConstants.COMMUNITYDESC)) {
                                    description = snapshot.child(FirebaseConstants.COMMUNITYDESC).getValue().toString();
                                }
                                if (snapshot.hasChild(FirebaseConstants.COMMUNITYENDTIME)) {
                                    endtime = snapshot.child(FirebaseConstants.COMMUNITYENDTIME).getValue().toString();
                                }
                                if (snapshot.hasChild(FirebaseConstants.COMMUNITYSTARTTIME)) {
                                    starttime = snapshot.child(FirebaseConstants.COMMUNITYSTARTTIME).getValue().toString();
                                }
                                if (snapshot.hasChild(FirebaseConstants.COMMUNITYSTATUS)) {
                                    status = snapshot.child(FirebaseConstants.COMMUNITYSTATUS).getValue().toString();
                                }
                                if (snapshot.hasChild(FirebaseConstants.COMMUNITYTITLE)) {
                                    title = snapshot.child(FirebaseConstants.COMMUNITYTITLE).getValue().toString();
                                }
                                if (snapshot.hasChild(FirebaseConstants.COMMUNITYTYPE)) {
                                    type = snapshot.child(FirebaseConstants.COMMUNITYTYPE).getValue().toString();
                                }

                                CommunityModel model = new CommunityModel(title, description, status, starttime, endtime, type, coverimage, admin, communityId);
                                communityDataList.add(0, model);
                                mainHorizontalAdapter.notifyItemInserted(1);
                                showSnackbarMessage("You have been added to " + title);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } else {
                        //Log.i("ClickTime","S : "+serverTimeInMillis+" E : "+endtime);
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


    public void createAlbum() {


        if (currentActiveCommunityID.equals(AppConstants.NOT_AVALABLE)) {
            startActivity(new Intent(MainActivity.this, CreateCloudAlbum.class).putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) userCommunityIdList));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {

            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                    .setTitle("Album Active")
                    .setIcon(R.drawable.ic_info)
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

    public void scanQR() {

        if (currentActiveCommunityID.equals(AppConstants.NOT_AVALABLE)) {
            startActivity(new Intent(MainActivity.this, QRCodeReader.class).putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) userCommunityIdList));
            finish();

        } else {
            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                    .setTitle("Album Active")
                    .setIcon(R.drawable.ic_info)
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
                        String by = AppConstants.NOT_AVALABLE, time = AppConstants.NOT_AVALABLE, uri = AppConstants.NOT_AVALABLE;

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
                    if (postImageList.size() > 0) {
                        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
                        MainVerticalRecyclerView.setLayoutManager(manager);
                        mainVerticalAdapter.notifyDataSetChanged();

                    } else {
                        postImageList.add(new PostModel(AppConstants.NOT_AVALABLE, AppConstants.NOT_AVALABLE, AppConstants.NOT_AVALABLE, AppConstants.NOT_AVALABLE));
                        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL);
                        MainVerticalRecyclerView.setLayoutManager(manager);
                        mainVerticalAdapter.notifyDataSetChanged();
                    }


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
                    photographerList.add(new PhotographerModel("add", "add", "add"));
                }

                DatabaseReference photographerRef = FirebaseDatabase.getInstance().getReference().child("Users");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    photographerRef.child(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {

                            String name = AppConstants.NOT_AVALABLE, imgurl = AppConstants.NOT_AVALABLE;
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
            try {
                keys.add(photographerList.get(i).getId());
            } catch (NullPointerException e) {
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
                        currentActiveCommunityID = AppConstants.NOT_AVALABLE;
                        mainAddPhotosFab.setVisibility(View.GONE);
                        AlarmManagerHelper alarmManagerHelper =
                                new AlarmManagerHelper(getApplicationContext());
                        alarmManagerHelper.deinitateAlarmManager();
                        showDialogMessage("Cloud-Album Quit", "Successfully left from the Cloud-Album");
                        if (photographerList.get(0).getImgUrl().equals("add") && photographerList.get(0).getId().equals("add") && photographerList.get(0).getName().equals("add")) {
                            photographerList.remove(0);
                            participantsAdapter.notifyItemRemoved(0);
                        }
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
                                                        currentActiveCommunityID = AppConstants.NOT_AVALABLE;
                                                        mainAddPhotosFab.setVisibility(View.GONE);
                                                        AlarmManagerHelper alarmManagerHelper =
                                                                new AlarmManagerHelper(getApplicationContext());
                                                        alarmManagerHelper.deinitateAlarmManager();
                                                        showDialogMessage("Cloud-Album Quit", "Successfully left from the Cloud-Album");
                                                        if (photographerList.get(0).getImgUrl().equals("add") && photographerList.get(0).getId().equals("add") && photographerList.get(0).getName().equals("add")) {
                                                            photographerList.remove(0);
                                                            participantsAdapter.notifyItemRemoved(0);
                                                        }
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
                .setIcon(R.drawable.ic_info)
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CommunityModel model = getCommunityModel(currentActiveCommunityID);
                    Intent intent = new Intent(getApplicationContext(), InlensGalleryActivity.class);
                    intent.putExtra("CommunityID", currentActiveCommunityID);
                    intent.putExtra("CommunityName", model.getTitle());
                    intent.putExtra("CommunityStartTime", model.getStartTime());
                    intent.putExtra("CommunityEndTime", model.getEndTime());
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    showPermissionDialog();
                }
            }
        }
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

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && COVER_CHANGE && !PROFILE_CHANGE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri ImageUri = result.getUri();
                //MainBottomSheetAlbumCoverEditUserImage.setImageURI(ImageUri);
                //MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.VISIBLE);
                uploadCoverPhoto(ImageUri);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && !COVER_CHANGE && PROFILE_CHANGE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                showSnackbarMessage("Profile picture is being uploaded. Please wait.");
                Uri resultUri = result.getUri();
                Bitmap bitmap = null;
                try {
                    InputStream stream = getContentResolver().openInputStream(resultUri);
                    bitmap = BitmapFactory.decodeStream(stream);
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


                            currentUserRef.child("Profile_picture").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        {
                                            showSnackbarMessage("Successfully uploaded your profile picture.");
                                            Glide.with(MainActivity.this).load(downloadUrl).into(mainProfileImageview);
                                            for (int i = 0; i < photographerList.size(); i++) {
                                                if (photographerList.get(i).getId().equals(currentUserId)) {
                                                    photographerList.get(i).setImgUrl(downloadUrl);
                                                    participantsAdapter.notifyItemChanged(i);
                                                }
                                            }

                                        }
                                    } else {
                                        showSnackbarMessage("Failed to upload picture. Please try again.");
                                    }

                                }
                            });
                        } else {
                            showSnackbarMessage("Failed to upload picture. Please try again.");

                        }
                    }


                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                showSnackbarMessage("Image cropping error. Please try again.");
            }

        }

    }

    private void uploadCoverPhoto(Uri imageUri) {

        // MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.VISIBLE);
        showSnackbarMessage("Uploading the cover photo. Please wait.");
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
                                    showSnackbarMessage("Successfully uploaded the cover photo.");
                                    communityDataList.get(position).setCoverImage(downloadUrl);
                                    mainHorizontalAdapter.notifyItemChanged(position);
                                } else {
                                    // MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.INVISIBLE);
                                    showSnackbarMessage("Failed to uploaded the cover photo. Please try again.");

                                }
                            }
                        });
                    } else {
                        //MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.INVISIBLE);
                        showSnackbarMessage("Failed to uploaded the cover photo. Please try again.");


                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.INVISIBLE);
                    showSnackbarMessage("Failed to uploaded the cover photo. Please try again.");
                }
            });

        } else {
            // MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.INVISIBLE);
            showSnackbarMessage("Failed to uploaded the cover photo. Please try again.");
        }


    }


    @Override
    public void onBackPressed() {


        if (RootForMainActivity.isDrawerOpen(GravityCompat.START)) {
            RootForMainActivity.closeDrawer(GravityCompat.START);
        } else if (!isAppbarOpen) {
            appBarLayout.setExpanded(true, true);
            MainVerticalRecyclerView.smoothScrollToPosition(0);
        } else {
            super.onBackPressed();
        }

    }


    private void shareInviteLink(String CommunityID) {

        String url = "https://inlens.page.link/?link=https://inlens.com=" + CommunityID + "&apn=com.integrals.inlens";
        final Intent SharingIntent = new Intent(Intent.ACTION_SEND);
        SharingIntent.setType("text/plain");
        SharingIntent.putExtra(Intent.EXTRA_TEXT, "Inlens Community Invite Link \n" + url);
        startActivity(SharingIntent);

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

                    } else if (state == NetworkInfo.State.DISCONNECTED) {

                        NoInternetTextView.setText("Internet connection lost.");
                        NoInternetView.setBackgroundColor(Color.parseColor("#ffc53929"));

                        NoInternetView.clearAnimation();
                        NoInternetView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up));
                        NoInternetView.getAnimation().start();
                        NoInternetView.setVisibility(View.VISIBLE);
                    } else {
                        NoInternetView.setVisibility(View.GONE);
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
                .setIcon(R.drawable.ic_info)
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

    public class EmptyGridViewHolder extends RecyclerView.ViewHolder {
        public EmptyGridViewHolder(View itemView) {
            super(itemView);

        }
    }

    public class MainVerticalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<PostModel> PostList;
        DatabaseReference UserRef;
        Picasso picasso;
        String comID;
        Activity activity;
        int VIEW_TYPE_PHOTO = 1, VIEW_TYPE_EMPTY = 0;

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
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            if (viewType == VIEW_TYPE_EMPTY) {
                View view = LayoutInflater.from(activity).inflate(R.layout.verticial_recyclerview_empty_layout, parent, false);
                return new EmptyGridViewHolder(view);
            } else {
                View view = LayoutInflater.from(activity).inflate(R.layout.post_layout, parent, false);
                return new PostGridViewHolder(view);
            }

        }


        @Override
        public int getItemViewType(int position) {

            return PostList.get(position).getPoskKey().equals(AppConstants.NOT_AVALABLE) &&
                    PostList.get(position).getPostBy().equals(AppConstants.NOT_AVALABLE) &&
                    PostList.get(position).getTime().equals(AppConstants.NOT_AVALABLE) &&
                    PostList.get(position).getUri().equals(AppConstants.NOT_AVALABLE) ? VIEW_TYPE_EMPTY : VIEW_TYPE_PHOTO;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

            if (holder instanceof PostGridViewHolder) {
                PostGridViewHolder viewHolder = (PostGridViewHolder) holder;
                if (PostList.get(position) != null) {
                    holder.itemView.clearAnimation();
                    holder.itemView.setAnimation(AnimationUtils.loadAnimation(activity, android.R.anim.fade_in));
                    holder.itemView.getAnimation().start();

                    RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_photo_camera);


                    Glide.with(activity)
                            .load(PostList.get(position).getUri())
                            .apply(requestOptions)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    viewHolder.PostProgressbar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    viewHolder.PostProgressbar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(viewHolder.PostImageView);

                    UserRef.child(PostList.get(position).getPostBy()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild("Name")) {
                                String name = dataSnapshot.child("Name").getValue().toString();
                                viewHolder.PostUploaderNameTextView.setText(name);

                            } else {
                                viewHolder.PostUploaderNameTextView.setText("Unknown");
                            }
                            if (dataSnapshot.hasChild("Profile_picture")) {
                                String UploaderImageUrl = dataSnapshot.child("Profile_picture").getValue().toString();
                                Glide.with(getApplicationContext()).load(UploaderImageUrl).into(viewHolder.PostUploaderImageView);

                            } else {
                                Glide.with(getApplicationContext()).load(R.drawable.ic_account_circle).into(viewHolder.PostUploaderImageView);

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent i = new Intent(MainActivity.this, PhotoView.class);
                            i.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) PostList);
                            i.putExtra("position", position);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(i);

                        }
                    });
                }
            }

        }


        @Override
        public int getItemCount() {
            return PostList.size();
        }

    }

    public class MainHorizontalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<CommunityModel> communityDetails;
        private final int VIEW_TYPE_ALBUM = 0, VIEW_TYPE_LOADING = 1,VIEW_TYPE_OPTIONS = -1;
        Activity activity;
        int selectedAlbumPosition = 1;
        String selectedAlbumKey;

        public MainHorizontalAdapter(List<CommunityModel> communityDetails, Activity activity) {
            this.communityDetails = communityDetails;
            this.activity = activity;
            selectedAlbumKey = AppConstants.NOT_AVALABLE;
        }

        @Override
        public int getItemViewType(int position) {
            if(communityDetails.get(position) == null)
            {
                return VIEW_TYPE_LOADING;
            }
            else
            {
                return communityDetails.get(position).getCommunityID().equals(AppConstants.MORE_OPTIONS) ? VIEW_TYPE_OPTIONS : VIEW_TYPE_ALBUM;

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ALBUM) {
                View view = LayoutInflater.from(activity).inflate(R.layout.album_card, parent, false);
                return new MainCommunityViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(activity).inflate(R.layout.item_loading_horizontal, parent, false);
                return new MainHorizontalLoadingViewHolder(view);
            }
            else if(viewType ==VIEW_TYPE_OPTIONS)
            {
                View view = LayoutInflater.from(activity).inflate(R.layout.album_options_card, parent, false);
                return new MainHorizontalOptionsViewHolder(view);
            }
            return null;
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

                } else {
                    viewHolder.Indicator.setVisibility(View.INVISIBLE);
                    viewHolder.itemView.setAlpha((float) 0.85);
                }

                if (!communityDetails.get(position).equals(AppConstants.NOT_AVALABLE)) {

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
                            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(activity, communityDetails.get(position), position);
                            bottomSheetFragment.show(((FragmentActivity) activity).getSupportFragmentManager(), bottomSheetFragment.getTag());
                        } else {
                            BottomSheetFragment_Inactive bottomSheetFragment_inactive = new BottomSheetFragment_Inactive(activity, communityDetails.get(position), position);
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


            } else if (holder instanceof MainHorizontalLoadingViewHolder) {
                MainHorizontalLoadingViewHolder viewHolder = (MainHorizontalLoadingViewHolder) holder;
                viewHolder.progressBar.setIndeterminate(true);
            }
            else if(holder instanceof MainHorizontalOptionsViewHolder)
            {
                MainHorizontalOptionsViewHolder viewHolder = (MainHorizontalOptionsViewHolder) holder;
                viewHolder.imageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlbumOptionsBottomSheetFragment optionsBottomSheetFragment = new AlbumOptionsBottomSheetFragment(activity);
                        optionsBottomSheetFragment.show(((FragmentActivity) activity).getSupportFragmentManager(), optionsBottomSheetFragment.getTag());
                    }
                });
            }
        }


        @Override
        public int getItemCount() {
            return communityDetails.size();
        }


    }


    public void setCommunityKeyForEdit(String postKeyForEdit, int pos) {
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