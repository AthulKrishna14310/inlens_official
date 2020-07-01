package com.integrals.inlens;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.integrals.inlens.Activities.CreateCloudAlbum;
import com.integrals.inlens.Activities.InlensGalleryActivity;
import com.integrals.inlens.Activities.PhotoView;
import com.integrals.inlens.Activities.QRCodeReader;
import com.integrals.inlens.Activities.SplashScreenActivity;
import com.integrals.inlens.Activities.WebViewActivity;
import com.integrals.inlens.Database.UploadQueueDB;
import com.integrals.inlens.Helper.AlbumOptionsBottomSheetFragment;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.BottomSheetFragment;
import com.integrals.inlens.Helper.BottomSheetFragment_Inactive;
import com.integrals.inlens.Helper.CustomHorizontalRecyclerViewScrollListener;
import com.integrals.inlens.Helper.CustomToast;
import com.integrals.inlens.Helper.CustomVerticalRecyclerViewScrollListener;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.MainCommunityViewHolder;
import com.integrals.inlens.Helper.MainHorizontalLoadingViewHolder;
import com.integrals.inlens.Helper.ParticipantsAdapter;
import com.integrals.inlens.Helper.QRCodeBottomSheet;
import com.integrals.inlens.Helper.ReadFirebaseData;
import com.integrals.inlens.Interface.FirebaseRead;
import com.integrals.inlens.Models.CommunityModel;
import com.integrals.inlens.Models.PhotographerModel;
import com.integrals.inlens.Models.PostModel;
import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.WorkManager.AlbumScanWorker;
import com.integrals.inlens.WorkManager.UploadWorker;
import com.skyfishjy.library.RippleBackground;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static com.integrals.inlens.Helper.AppConstants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;
import static com.integrals.inlens.Helper.AppConstants.MY_PERMISSIONS_REQUEST_START_WORKMANAGER;


public class MainActivity extends AppCompatActivity implements AlbumOptionsBottomSheetFragment.IScanCallback, AlbumOptionsBottomSheetFragment.ICreateCallback, AlbumOptionsBottomSheetFragment.IDismissDialog {


    private String currentActiveCommunityID = AppConstants.NOT_AVALABLE;

    private String PostKeyForEdit;
    int position;
    private static final int GALLERY_PICK = 1;
    private static final int COVER_GALLERY_PICK = 78;
    private static boolean COVER_CHANGE = false, PROFILE_CHANGE = false;
    private NavigationView navigationView;
    private DrawerLayout rootForMainActivity;
    private boolean displayed = false;

    private RecyclerView MainHorizontalRecyclerview, MainVerticalRecyclerView;


    private BroadcastReceiver br;
    private RelativeLayout NoInternetView;
    private TextView NoInternetTextView;


    RecyclerView ParticipantsRecyclerView;
    LinearLayout expandableCardView;


    FloatingActionButton mainAddPhotosFab;

    DatabaseReference currentUserRef, communityRef, participantRef, postRef, linkRef;
    FirebaseAuth firebaseAuth;
    String currentUserId;
    ValueEventListener userRefListenerForActiveAlbum, communityRefListenerForActiveAlbum, coummunityUserAddListener, communitiesDataListener, postRefListener, participantRefListener, communitiesListener;
    ReadFirebaseData readFirebaseData;
    ArrayList<String> userCommunityIdList;
    MainHorizontalAdapter mainHorizontalAdapter;


    MainVerticalAdapter mainVerticalAdapter;

    // info : underscore tells that the second one is a copy of the first
    List<CommunityModel> communityDataList = new ArrayList<>(), _communityDataList = new ArrayList<>();


    List<PostModel> postImageList = new ArrayList<>(), _postImageList = new ArrayList<>();

    // info : for vertical recyclerviewScrolling
    int lastVisiblesItems, visibleItemCount, totalItemCount;
    boolean isLoading = true;


    ParticipantsAdapter participantsAdapter;
    List<PhotographerModel> photographerList = new ArrayList<>();

    // info : toolbar
    ImageButton mainSearchButton;

    // info onbackpressed
    AppBarLayout appBarLayout;

    boolean isAppbarOpen = true;
    int POST_IMAGE_LOAD_COUNT = 9;

    CFAlertDialog.Builder cfBuilder;
    Dialog cfDialogService, cfDialogAddPhotoFab;


    //PURPOSE OF USER DIRECT


    AlbumOptionsBottomSheetFragment optionsBottomSheetFragment;
    private RippleBackground rippleBackground, rippleBackground2;

    QRCodeBottomSheet qrCodeBottomSheet;

    String appTheme = "", mainSelectedKey = AppConstants.NOT_AVALABLE;

    public MainActivity() {
    }


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
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            appDataPrefEditor.commit();
            setTheme(R.style.AppTheme);

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // to calculate screen width
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        POST_IMAGE_LOAD_COUNT = ((int) Math.ceil((height / (width / 3)) + 3)) * 3;

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


        // Fab
        mainAddPhotosFab = findViewById(R.id.fabadd);
        mainAddPhotosFab.hide();

        //photographers cardview
        expandableCardView = findViewById(R.id.photographers);

        // navigation view and drawerLayout  (Root)
        rootForMainActivity = findViewById(R.id.root_for_main_activity);
        navigationView = (NavigationView) findViewById(R.id.nv);

        // dark theme options
        Menu menu = navigationView.getMenu();
        CheckBox checkBox = (CheckBox) menu.findItem(R.id.dark_theme).getActionView();
        if (appTheme.equals(AppConstants.themeLight)) {
            checkBox.setChecked(false);
        } else {
            checkBox.setChecked(true);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    appDataPrefEditor.putString(AppConstants.appDataPref_theme, AppConstants.themeDark);
                    appDataPrefEditor.commit();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    startActivity(new Intent(MainActivity.this, SplashScreenActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else {
                    appDataPrefEditor.putString(AppConstants.appDataPref_theme, AppConstants.themeLight);
                    appDataPrefEditor.commit();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    startActivity(new Intent(MainActivity.this, SplashScreenActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();

                }
            }
        });

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
        linkRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.INVITE_LINK);
        // custom function for waiting until firebase read is complete
        readFirebaseData = new ReadFirebaseData();

        //album options dialog
        optionsBottomSheetFragment = new AlbumOptionsBottomSheetFragment(MainActivity.this);

        //dialogBuilder
        cfBuilder = new CFAlertDialog.Builder(MainActivity.this);
        cfDialogAddPhotoFab = showAddPhotoFabPermissionDialog(cfBuilder);
        cfDialogService = showServicePermissionDialog(cfBuilder);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.profile_preference) {

                    // no need to check internet connection as if is given by default
                    setCoverChange(false);
                    setProfileChange(true);
                    GetStartedWithNewProfileImage();

                    return true;

                }
                if (item.getItemId() == R.id.terms_and_conditions) {


                    startActivity(new Intent(MainActivity.this, WebViewActivity.class)
                            .putExtra("MESSAGE", "TERMS_AND_CONDITIONS"));

                    return true;

                }
                if (item.getItemId() == R.id.privacy_policy) {


                    startActivity(new Intent(MainActivity.this, WebViewActivity.class)
                            .putExtra("MESSAGE", "PRIVACY_POLICY"));

                    return true;

                }
                if (item.getItemId() == R.id.contact_us) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"inlens.athulkrishna@gmail.com"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Your Response");
                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        showInfoMessage("No E-mail App found", "Please install an email app");
                    }
                    return true;
                }
                if (item.getItemId() == R.id.help) {


                    startActivity(new Intent(MainActivity.this, WebViewActivity.class)
                            .putExtra("MESSAGE", "HELP"));
                    return true;

                }

                if (item.getItemId() == R.id.rate_us) {
                    Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                    }
                    return true;

                }
                if (item.getItemId() == R.id.feedback) {
                    Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                    }

                    return true;
                }
                return true;
            }
        });

        findViewById(R.id.nav_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rootForMainActivity.openDrawer(Gravity.START);
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
                        Dialog dialog = cfBuilder.create();
                        if (!cfDialogAddPhotoFab.isShowing()) {
                            cfDialogAddPhotoFab.show();
                        }

                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);


                    }
                } else {
                    CommunityModel model = getCommunityModel(currentActiveCommunityID);
                    Intent intent = new Intent(getApplicationContext(), InlensGalleryActivity.class);
                    intent.putExtra("CommunityID", currentActiveCommunityID);
                    intent.putExtra("CommunityStartTime", model.getStartTime());
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
                        if (communityDataList.size() < _communityDataList.size() || communityDataList.get(communityDataList.size() - 1) == null) {
                            isLoading = true;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = communityDataList.size() - 1; i > -1; i--) {
                                        if (communityDataList.get(i) == null) {
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

        GridLayoutManager Gridmanager = new GridLayoutManager(getApplicationContext(), 5);
        ParticipantsRecyclerView.setLayoutManager(Gridmanager);

        MainVerticalRecyclerView = findViewById(R.id.main_recyclerview);
        MainVerticalRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        MainVerticalRecyclerView.setLayoutManager(gridLayoutManager);
        mainVerticalAdapter = new MainVerticalAdapter(MainVerticalRecyclerView, MainActivity.this, postImageList, currentUserId);
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

                GridLayoutManager manager = (GridLayoutManager) MainVerticalRecyclerView.getLayoutManager();

                if (manager.getSpanCount() > 1) {
                    visibleItemCount = manager.getChildCount();
                    totalItemCount = manager.getItemCount();
                    lastVisiblesItems = manager.findLastVisibleItemPosition();

                    if ((visibleItemCount + lastVisiblesItems) >= totalItemCount) {

                        if (postImageList.size() < _postImageList.size()) {


                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int index = postImageList.size();
                                    int end;
                                    if (index + POST_IMAGE_LOAD_COUNT > _postImageList.size()) {
                                        end = index + (Math.abs(index + POST_IMAGE_LOAD_COUNT - _postImageList.size()));
                                    } else {
                                        end = index + POST_IMAGE_LOAD_COUNT;
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

            }
        });

        rippleBackground = (RippleBackground) findViewById(R.id.content);
        rippleBackground2 = (RippleBackground) findViewById(R.id.content2);


        findViewById(R.id.plus_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (checkIfImagesAreQueued()) {
                    provideQueueOptions();
                } else {
                    optionsBottomSheetFragment.show(((FragmentActivity) MainActivity.this).getSupportFragmentManager(), optionsBottomSheetFragment.getTag());
                }


            }
        });
    }


    public void provideQueueOptions() {
        Snackbar uploadFromQueue = Snackbar.make(rootForMainActivity, "Upload all queued photos", BaseTransientBottomBar.LENGTH_INDEFINITE).setAction("Options", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder queuedOptionsDialog = new AlertDialog.Builder(MainActivity.this);
                queuedOptionsDialog.setMessage("Upload queued images to create or  join new album. Please select one the following options.")
                        .setPositiveButton("Upload now", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Constraints uploadConstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                                OneTimeWorkRequest galleryUploader = new OneTimeWorkRequest.Builder(UploadWorker.class).addTag("uploadWorker").setConstraints(uploadConstraints).build();
                                WorkManager.getInstance(MainActivity.this).cancelAllWorkByTag("uploadWorker");
                                WorkManager.getInstance(MainActivity.this).enqueueUniqueWork("uploadWorker", ExistingWorkPolicy.REPLACE, galleryUploader);
                                showSnackbarMessage("Upload initiated");

                            }
                        })
                        .setNegativeButton("Clear Queue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                UploadQueueDB queueDB = new UploadQueueDB(MainActivity.this);
                                queueDB.deleteAllData();
                                Cursor cursor = queueDB.getQueuedData();
                                if (cursor.getCount() == 0) {
                                    showSnackbarMessage("Upload queue cleared");

                                } else {
                                    showSnackbarMessage("Could not clear upload queue. Try again.");

                                }
                                queueDB.close();
                                cursor.close();

                            }
                        });

                if (!queuedOptionsDialog.create().isShowing()) {
                    queuedOptionsDialog.create().show();
                }
            }
        });

        if (!uploadFromQueue.isShown()) {
            uploadFromQueue.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    uploadFromQueue.dismiss();

                }
            }, 5000);
        }
    }

    public boolean checkIfImagesAreQueued() {
        Cursor cursor = new UploadQueueDB(MainActivity.this).getQueuedData();
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    private CFAlertDialog showAddPhotoFabPermissionDialog(CFAlertDialog.Builder builder) {

        int cf_bg_color, colorPrimary, red_inlens;
        if (appTheme.equals(AppConstants.themeLight)) {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens = getResources().getColor(R.color.Light_red_inlens);
        } else {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens = getResources().getColor(R.color.Dark_red_inlens);

        }

        return builder
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle("Storage Permission")
                .setDialogBackgroundColor(cf_bg_color)
                .setTextColor(colorPrimary)
                .setIcon(R.drawable.ic_info)
                .setMessage("InLens require storage permission to access your photos. Please enable it and try again.")
                .setCancelable(true)
                .addButton("OK", colorPrimary
                        , cf_bg_color,
                        CFAlertDialog.CFAlertActionStyle.DEFAULT,
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
                .addButton("CANCEL",
                        red_inlens,
                        cf_bg_color
                        , CFAlertDialog.CFAlertActionStyle.DEFAULT,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .create();


    }

    private CFAlertDialog showServicePermissionDialog(CFAlertDialog.Builder builder) {

        int cf_bg_color, colorPrimary, red_inlens;
        if (appTheme.equals(AppConstants.themeLight)) {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens = getResources().getColor(R.color.Light_red_inlens);
        } else {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens = getResources().getColor(R.color.Dark_red_inlens);
        }

        return builder
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle("Storage Permission")
                .setIcon(R.drawable.ic_info)
                .setDialogBackgroundColor(cf_bg_color)
                .setTextColor(colorPrimary)
                .setMessage("InLens require storage permission to access your photos. Please enable it and try again.")
                .setCancelable(true)
                .addButton("OK", colorPrimary, cf_bg_color,
                        CFAlertDialog.CFAlertActionStyle.DEFAULT,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_START_WORKMANAGER);
                                dialog.dismiss();
                            }
                        })
                .addButton("CANCEL", red_inlens, cf_bg_color,
                        CFAlertDialog.CFAlertActionStyle.DEFAULT,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .create();


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

    private void showSnackbarMessage(String message) {
        Snackbar.make(rootForMainActivity, message, Snackbar.LENGTH_LONG).show();
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


        communitiesListener = currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                userCommunityIdList = new ArrayList<>();
                if (dataSnapshot.hasChild(FirebaseConstants.COMMUNITIES)) {
                    for (DataSnapshot snapshot : dataSnapshot.child(FirebaseConstants.COMMUNITIES).getChildren()) {
                        userCommunityIdList.add(snapshot.getKey());
                    }
                    Collections.sort(userCommunityIdList, Collections.reverseOrder());
                    getCloudAlbumData(userCommunityIdList);
                }
                if (dataSnapshot.hasChild(FirebaseConstants.LIVECOMMUNITYID) && isConnectedToNet()) {
                    mainAddPhotosFab.show();
                } else {
                    mainAddPhotosFab.hide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

                    qrCodeBottomSheet = new QRCodeBottomSheet(MainActivity.this, currentActiveCommunityID, linkRef, false, MainActivity.this);

                    // make the add photo fab visible
                    if (isConnectedToNet()) {
                        mainAddPhotosFab.show();

                    }

                    try {
                        String qrIntent = getIntent().getStringExtra("CREATED");
                        String id = getIntent().getStringExtra("ID");
                        if ((!qrIntent.isEmpty()) && (!id.isEmpty())) {
                            if (qrIntent.contentEquals("YES")) {
                                if (displayed == false) {

                                    //PURPOSE OF USER DIRECT
                                    initialStart();

                                }
                            }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    // make the start and stop services in navigation drawer visible

                    communityRefListenerForActiveAlbum = readFirebaseData.readData(communityRef.child(currentActiveCommunityID), new FirebaseRead() {
                        @Override
                        public void onSuccess(DataSnapshot snapshot) {

                            // optimization 1 resulted in this error, everytime the album is quit even if the album is inactive
                            // so first check the album  has status status;
                            if (snapshot.hasChild(FirebaseConstants.COMMUNITYSTATUS)) {


                                String status = snapshot.child(FirebaseConstants.COMMUNITYSTATUS).getValue().toString();
                                if (status.equals("T")) {
                                    long endtime = Long.parseLong(snapshot.child(FirebaseConstants.COMMUNITYENDTIME).getValue().toString());
                                    if (System.currentTimeMillis() >= endtime) {
                                        quitCloudAlbum(true);

                                    } else {
                                        // start the necessary services
                                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                                                if (!cfDialogService.isShowing()) {
                                                    cfDialogService.show();
                                                }

                                            } else {
                                                ActivityCompat.requestPermissions(MainActivity.this,
                                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                        MY_PERMISSIONS_REQUEST_START_WORKMANAGER);

                                            }

                                        } else {

                                            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest
                                                    .Builder(AlbumScanWorker.class, 15, TimeUnit.MINUTES)
                                                    .addTag(AppConstants.PHOTO_SCAN_WORK)
                                                    .build();

                                            WorkManager.getInstance(MainActivity.this).enqueueUniquePeriodicWork(AppConstants.PHOTO_SCAN_WORK, ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
                                            SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                                            SharedPreferences.Editor ceditor = CurrentActiveCommunity.edit();
                                            ceditor.putString("scanWorkerId", String.valueOf(periodicWorkRequest.getId()));
                                            ceditor.commit();
                                        }
                                    }
                                } else {
                                    // stop the necessary services
                                    WorkManager.getInstance().cancelAllWorkByTag(AppConstants.PHOTO_SCAN_WORK);

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
                    currentActiveCommunityID = AppConstants.NOT_AVALABLE;
                }

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(DatabaseError databaseError) {

            }
        });
    }

    private void initialStart() {
        //PURPOSE OF USER DIRECT

        MainActivity.this.getIntent().putExtra("CREATED", "NO");
        MainActivity.this.getIntent().putExtra("ID", "NULL");
        qrCodeBottomSheet = new QRCodeBottomSheet(MainActivity.this, currentActiveCommunityID, linkRef, true, MainActivity.this);
        qrCodeBottomSheet.show(getSupportFragmentManager(), qrCodeBottomSheet.getTag());

        displayed = true;

    }


    private void getCloudAlbumData(ArrayList<String> userCommunityIdList) {


        try {
            if (userCommunityIdList.size() == 0) {
                expandableCardView.setVisibility(View.GONE);
            } else {
                expandableCardView.setVisibility(View.VISIBLE);
            }

            for (int i = 1; i < _communityDataList.size(); i++) {
                if (_communityDataList.get(i).getCommunityID().equals(AppConstants.MORE_OPTIONS)) {
                    userCommunityIdList.remove(i);
                }
            }

            communitiesDataListener = readFirebaseData.readData(communityRef, new FirebaseRead() {
                @Override
                public void onSuccess(DataSnapshot snapshot) {

                    if (userCommunityIdList.size() > 0) {
                        findViewById(R.id.photoText).setVisibility(View.VISIBLE);
                        findViewById(R.id.photographers).setVisibility(View.VISIBLE);
                        //  findViewById(R.id.linePhotographer).setVisibility(View.VISIBLE);

                        for (String communityId : userCommunityIdList) {
                            String admin = AppConstants.NOT_AVALABLE, coverimage = AppConstants.NOT_AVALABLE, description = AppConstants.NOT_AVALABLE, endtime = AppConstants.NOT_AVALABLE, starttime = AppConstants.NOT_AVALABLE, status = AppConstants.NOT_AVALABLE, title = AppConstants.NOT_AVALABLE, type = AppConstants.NOT_AVALABLE;
                            boolean isReported = false;

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
                            if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITY_REPORTED)) {
                                isReported = true;
                            }

                            CommunityModel model = new CommunityModel(title, description, status, starttime, endtime, type, coverimage, admin, communityId, isReported);
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
                        for (int i = communityDataList.size() - 1; i > -1; i--) {
                            if (communityDataList.get(i) == null) {
                                communityDataList.remove(i);
                            }
                        }
                        if (_communityDataList.size() > 5 && _communityDataList.size() != communityDataList.size()) {
                            communityDataList.add(null);
                        }

                        mainHorizontalAdapter.notifyDataSetChanged();
                    } else {
                        if (communityDataList.size() < 1) {
                            communityDataList.add(_communityDataList.get(0));
                        }
                        appBarLayout.setExpanded(true, true);
                        mainHorizontalAdapter.notifyDataSetChanged();

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
            Log.i("MainActivity", "getCloudAlbumData " + e);
        }
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


    private void decryptDeepLink() {

        int cf_bg_color, colorPrimary, red_inlens, cf_alert_dialogue_dim_bg;
        if (appTheme.equals(AppConstants.themeLight)) {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens = getResources().getColor(R.color.Light_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);
        } else {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens = getResources().getColor(R.color.Dark_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }

        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
            @Override
            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {

                if (pendingDynamicLinkData != null) {

                    if(checkIfImagesAreQueued())
                    {
                        provideQueueOptions();
                    }
                    else
                    {
                        Uri deeplink = pendingDynamicLinkData.getLink();
                        String communityRefLinkId = deeplink.toString().replace("https://inlens.com=", "");

                        linkRef.child(communityRefLinkId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                Log.i("linkref", " datasnapshot " + dataSnapshot);

                                if (dataSnapshot.hasChild("id") && dataSnapshot.hasChild("count")) {
                                    String communityId = dataSnapshot.child("id").getValue().toString();
                                    String count = dataSnapshot.child("count").getValue().toString();

                                    if (currentActiveCommunityID.equals(AppConstants.NOT_AVALABLE)) {

                                        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(MainActivity.this)
                                                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                                                .setTitle("New Community")
                                                .setIcon(R.mipmap.ic_launcher_foreground)
                                                .setDialogBackgroundColor(cf_bg_color)
                                                .setTextColor(colorPrimary)
                                                .setCancelable(false)
                                                .setMessage("You are about to join a new community.")
                                                .addButton("JOIN",
                                                        colorPrimary,
                                                        cf_alert_dialogue_dim_bg,
                                                        CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                addCommunityToUserRef(communityId, count, communityRefLinkId);
                                                                dialog.dismiss();
                                                            }
                                                        })
                                                .addButton("CANCEL",
                                                        red_inlens,
                                                        cf_alert_dialogue_dim_bg,
                                                        CFAlertDialog.CFAlertActionStyle.DEFAULT,
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
                                                    .setIcon(R.mipmap.ic_launcher_foreground)
                                                    .setMessage("Are you sure you want to join this new community? This means quitting the previous one.")
                                                    .setTextGravity(Gravity.START)
                                                    .setDialogBackgroundColor(cf_bg_color)
                                                    .setTextColor(colorPrimary)
                                                    .setCancelable(false)
                                                    .addButton("YES",
                                                            colorPrimary,
                                                            cf_alert_dialogue_dim_bg,
                                                            CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    addCommunityToUserRef(communityId, count, communityRefLinkId);
                                                                    dialog.dismiss();
                                                                }
                                                            })
                                                    .addButton("NO",
                                                            red_inlens,
                                                            cf_alert_dialogue_dim_bg,
                                                            CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                }
                                                            });
                                            builder.show();
                                        }

                                    }

                                } else {
                                    Log.i("linkref", communityRefLinkId);
                                    showSnackbarMessage("Invite-Link is corrupted. Get a new Invite-Link.");
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
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


    private void addCommunityToUserRef(final String communityId, String remainingCount, String communityRefLinkId) {

        communityRef.child(communityId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(FirebaseConstants.COMMUNITYSTATUS)) {

                    long endtime = Long.parseLong(dataSnapshot.child("endtime").getValue().toString());
                    long serverTimeInMillis = System.currentTimeMillis();
                    String titleValue = dataSnapshot.child(FirebaseConstants.COMMUNITYTITLE).getValue().toString();
                    final long dy = (TimeUnit.MILLISECONDS.toDays(endtime) - System.currentTimeMillis());
                    final long hr = (TimeUnit.MILLISECONDS.toHours(endtime) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(endtime)));
                    final long min = TimeUnit.MILLISECONDS.toMinutes(endtime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(endtime));

                    if (serverTimeInMillis < endtime) {

                        try {
                            int remainingCountInt = Integer.parseInt(remainingCount);
                            if (remainingCountInt > 0) {
                                remainingCountInt -= 1;

                                linkRef.child(communityRefLinkId).child("count").setValue(remainingCountInt);
                                currentActiveCommunityID = communityId;
                                currentUserRef.child(FirebaseConstants.COMMUNITIES).child(communityId).setValue(ServerValue.TIMESTAMP);
                                currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).setValue(communityId);
                                participantRef.child(communityId).child(currentUserId).setValue(ServerValue.TIMESTAMP);

                                if (!userCommunityIdList.contains(communityId)) {
                                    userCommunityIdList.add(0, communityId);
                                    communityRef.child(communityId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {


                                            String admin = AppConstants.NOT_AVALABLE, coverimage = AppConstants.NOT_AVALABLE, description = AppConstants.NOT_AVALABLE, endtime = AppConstants.NOT_AVALABLE, starttime = AppConstants.NOT_AVALABLE, status = AppConstants.NOT_AVALABLE, title = AppConstants.NOT_AVALABLE, type = AppConstants.NOT_AVALABLE;
                                            boolean isReported = false;
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
                                            if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITY_REPORTED)) {
                                                isReported = true;
                                            }

                                            CommunityModel model = new CommunityModel(title, description, status, starttime, endtime, type, coverimage, admin, communityId, isReported);
                                            communityDataList.add(1, model);
                                            mainHorizontalAdapter.notifyItemInserted(1);
                                            showSnackbarMessage("You have been added to " + title);


                                            SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                                            SharedPreferences.Editor ceditor = CurrentActiveCommunity.edit();
                                            ceditor.putString("id", communityId);
                                            ceditor.putString("time", String.valueOf(System.currentTimeMillis()));
                                            ceditor.putString("stopAt", endtime);
                                            ceditor.putString("startAt", String.valueOf(System.currentTimeMillis()));
                                            ceditor.putInt("notiCount", 0);
                                            ceditor.remove(AppConstants.IS_NOTIFIED);
                                            ceditor.commit();

                                            setParticipants(model);


                                            NotificationHelper helper = new NotificationHelper(getApplicationContext());
                                            String notificationStr = "";
                                            if (titleValue.length() > 15) {
                                                notificationStr += titleValue.substring(0, 15) + "...";
                                            } else {
                                                notificationStr += titleValue;
                                            }
                                            if (dy > 0) {
                                                notificationStr += ", " + (int) dy + " days";

                                            } else {
                                                notificationStr += ",";
                                            }
                                            if (hr > 0) {
                                                if (hr == 1) {
                                                    notificationStr += " " + (int) hr + " hr left";
                                                } else {
                                                    notificationStr += " " + (int) hr + " hrs left";
                                                }
                                            }
                                            if (hr < 1 && dy < 1) {
                                                if (min == 1) {
                                                    notificationStr += " " + (int) min + " minute left";
                                                } else {
                                                    notificationStr += " " + (int) min + " minutes left";
                                                }
                                            }
                                            long time = Long.parseLong(endtime);
                                            CharSequence Time = DateUtils.getRelativeDateTimeString(MainActivity.this, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
                                            String timesubstring = Time.toString().substring(Time.length() - 8);

                                            Date date = new Date(Long.parseLong(endtime));
                                            String dateformat = DateFormat.format("dd-MM-yyyy", date).toString();


                                            helper.displayAlbumStartNotification(notificationStr, "You are active in this Cloud-Album till " + dateformat + ", " + timesubstring);
                                            MainActivity.this.getIntent().putExtra("CREATED", "YES");
                                            MainActivity.this.getIntent().putExtra("ID", communityId);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                } else {

                                    communityRef.child(communityId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {


                                            String admin = AppConstants.NOT_AVALABLE, coverimage = AppConstants.NOT_AVALABLE, description = AppConstants.NOT_AVALABLE, endtime = AppConstants.NOT_AVALABLE, starttime = AppConstants.NOT_AVALABLE, status = AppConstants.NOT_AVALABLE, title = AppConstants.NOT_AVALABLE, type = AppConstants.NOT_AVALABLE;
                                            boolean isReported = false;
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
                                            if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITY_REPORTED)) {
                                                isReported = true;
                                            }


                                            SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                                            SharedPreferences.Editor ceditor = CurrentActiveCommunity.edit();
                                            ceditor.putString("id", communityId);
                                            ceditor.putString("time", String.valueOf(System.currentTimeMillis()));
                                            ceditor.putString("stopAt", endtime);
                                            ceditor.putString("startAt", String.valueOf(System.currentTimeMillis()));
                                            ceditor.putInt("notiCount", 0);
                                            ceditor.remove(AppConstants.IS_NOTIFIED);
                                            ceditor.commit();


                                            CommunityModel model = new CommunityModel(title, description, status, starttime, endtime, type, coverimage, admin, communityId, isReported);
                                            mainHorizontalAdapter.selectedAlbumKey = snapshot.getKey();
                                            mainHorizontalAdapter.notifyDataSetChanged();

                                            setParticipants(model);
                                            showSnackbarMessage("You have rejoined " + title);


                                            NotificationHelper helper = new NotificationHelper(getApplicationContext());
                                            String notificationStr = "";
                                            if (titleValue.length() > 15) {
                                                notificationStr += titleValue.substring(0, 15) + "...";
                                            } else {
                                                notificationStr += titleValue;
                                            }
                                            if (dy > 0) {
                                                notificationStr += ", " + (int) dy + " days";

                                            } else {
                                                notificationStr += ",";
                                            }
                                            if (hr > 0) {
                                                if (hr == 1) {
                                                    notificationStr += " " + (int) hr + " hr left";
                                                } else {
                                                    notificationStr += " " + (int) hr + " hrs left";
                                                }
                                            }
                                            if (hr < 1 && dy < 1) {
                                                if (min == 1) {
                                                    notificationStr += " " + (int) min + " minute left";
                                                } else {
                                                    notificationStr += " " + (int) min + " minutes left";
                                                }
                                            }
                                            long time = Long.parseLong(endtime);
                                            CharSequence Time = DateUtils.getRelativeDateTimeString(MainActivity.this, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
                                            String timesubstring = Time.toString().substring(Time.length() - 8);

                                            Date date = new Date(Long.parseLong(endtime));
                                            String dateformat = DateFormat.format("dd-MM-yyyy", date).toString();

                                            helper.displayAlbumStartNotification(notificationStr, "You are active in this Cloud-Album till " + dateformat + ", " + timesubstring);
                                            //MainActivity.this.getIntent().putExtra("CREATED", "YES");
                                            //MainActivity.this.getIntent().putExtra("ID", communityId);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                            } else {
                                //linkRef.child(communityRefLinkId).removeValue();
                                Log.i("linkref", communityRefLinkId);
                                showSnackbarMessage("Invite-Link expired. Get a new Invite-Link.");
                            }
                        } catch (NumberFormatException e) {
                            if (remainingCount.equals("inf")) {
                                currentActiveCommunityID = communityId;
                                currentUserRef.child(FirebaseConstants.COMMUNITIES).child(communityId).setValue(ServerValue.TIMESTAMP);
                                currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).setValue(communityId);
                                participantRef.child(communityId).child(currentUserId).setValue(ServerValue.TIMESTAMP);
                                userCommunityIdList.add(0, communityId);
                                communityRef.child(communityId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {


                                        String admin = AppConstants.NOT_AVALABLE, coverimage = AppConstants.NOT_AVALABLE, description = AppConstants.NOT_AVALABLE, endtime = AppConstants.NOT_AVALABLE, starttime = AppConstants.NOT_AVALABLE, status = AppConstants.NOT_AVALABLE, title = AppConstants.NOT_AVALABLE, type = AppConstants.NOT_AVALABLE;
                                        boolean isReported = false;

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
                                        if (snapshot.child(communityId).hasChild(FirebaseConstants.COMMUNITY_REPORTED)) {
                                            isReported = true;
                                        }

                                        CommunityModel model = new CommunityModel(title, description, status, starttime, endtime, type, coverimage, admin, communityId, isReported);
                                        communityDataList.add(1, model);
                                        mainHorizontalAdapter.notifyItemInserted(1);
                                        showSnackbarMessage("You have been added to " + title);

                                        photographerList.add(0, new PhotographerModel("add", "add", "add", "add"));


                                        SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor ceditor = CurrentActiveCommunity.edit();
                                        ceditor.putString("id", communityId);
                                        ceditor.putString("time", String.valueOf(System.currentTimeMillis()));
                                        ceditor.putString("stopAt", endtime);
                                        ceditor.putString("startAt", String.valueOf(System.currentTimeMillis()));
                                        ceditor.putInt("notiCount", 0);
                                        ceditor.remove(AppConstants.IS_NOTIFIED);
                                        ceditor.commit();

                                        NotificationHelper helper = new NotificationHelper(getApplicationContext());
                                        String notificationStr = "";
                                        if (titleValue.length() > 15) {
                                            notificationStr += titleValue.substring(0, 15) + "...";
                                        } else {
                                            notificationStr += titleValue;
                                        }
                                        if (dy > 0) {
                                            notificationStr += ", " + (int) dy + " days";

                                        } else {
                                            notificationStr += ",";
                                        }
                                        if (hr > 0) {
                                            notificationStr += " " + (int) hr + " hrs left";
                                        }
                                        if (hr < 1 && dy < 1) {
                                            notificationStr += " " + (int) min + " minutes left";
                                        }
                                        long time = Long.parseLong(endtime);
                                        CharSequence Time = DateUtils.getRelativeDateTimeString(MainActivity.this, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
                                        String timesubstring = Time.toString().substring(Time.length() - 8);

                                        Date date = new Date(time);
                                        String dateformat = DateFormat.format("dd-MM-yyyy", date).toString();

                                        helper.displayAlbumStartNotification(notificationStr, "You are active in this Cloud-Album till " + dateformat + ", " + timesubstring);
                                        MainActivity.this.getIntent().putExtra("CREATED", "YES");
                                        MainActivity.this.getIntent().putExtra("ID", communityId);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }


                    } else {
                        //Log.i("ClickTime","S : "+serverTimeInMillis+" E : "+endtime);
                        showDialogMessage("Album Inactive", "The album has expired or admin has made the album inactive.");
                        linkRef.child(communityRefLinkId).removeValue();
                    }

                } else {
                    linkRef.child(communityRefLinkId).removeValue();
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

        int cf_bg_color, colorPrimary, red_inlens, cf_alert_dialogue_dim_bg;
        if (appTheme.equals(AppConstants.themeLight)) {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens = getResources().getColor(R.color.Light_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);
        } else {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens = getResources().getColor(R.color.Dark_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }


        if (checkIfImagesAreQueued()) {
            provideQueueOptions();
        } else {
            if (currentActiveCommunityID.equals(AppConstants.NOT_AVALABLE)) {
                startActivity(new Intent(MainActivity.this, CreateCloudAlbum.class).putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) userCommunityIdList));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            } else {

                CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                        .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                        .setTitle("Album Active")
                        .setIcon(R.drawable.ic_info)
                        .setDialogBackgroundColor(cf_bg_color)
                        .setTextColor(colorPrimary)
                        .setMessage("You have to leave the currently active album before creating a new album.")
                        .setCancelable(true)
                        .addButton("EXIT PARTICIPATION",
                                red_inlens,
                                cf_alert_dialogue_dim_bg,
                                CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        quitCloudAlbum(false);
                                    }
                                })

                        .addButton("CANCEL",
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
        }


    }

    public void scanQR() {

        int cf_bg_color, colorPrimary, red_inlens, cf_alert_dialogue_dim_bg;
        if (appTheme.equals(AppConstants.themeLight)) {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens = getResources().getColor(R.color.Light_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);
        } else {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens = getResources().getColor(R.color.Dark_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }


        if (checkIfImagesAreQueued()) {
            provideQueueOptions();
        } else {
            if (currentActiveCommunityID.equals(AppConstants.NOT_AVALABLE)) {
                startActivity(new Intent(MainActivity.this, QRCodeReader.class).putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) userCommunityIdList));
                finish();

            } else {
                CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                        .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                        .setTitle("Album Active")
                        .setDialogBackgroundColor(cf_bg_color)
                        .setTextColor(colorPrimary)
                        .setIcon(R.drawable.ic_info)
                        .setMessage("You have to leave the currently active album before creating a new album.")
                        .setCancelable(true)

                        .addButton("QUIT CLOUD-ALBUM",
                                red_inlens,
                                cf_alert_dialogue_dim_bg,
                                CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        quitCloudAlbum(false);
                                    }
                                })

                        .addButton("CANCEL",
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

        }

    }


    public void setVerticalRecyclerView(CommunityModel communityModel) {

        String communityID = communityModel.getCommunityID();

        try {

            setParticipants(communityModel);

            postRefListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

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

                    Log.i("galleryCount", "_postImageList" + _postImageList.size());

                    Collections.reverse(_postImageList);
                    for (int i = 0; i < POST_IMAGE_LOAD_COUNT && i < _postImageList.size(); i++) {
                        if (!getPostKeys(postImageList).contains(_postImageList.get(i).getPoskKey())) {
                            postImageList.add(_postImageList.get(i));
                        }

                    }
                    if (postImageList.size() > 0) {
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 3);
                        MainVerticalRecyclerView.setLayoutManager(gridLayoutManager);
                        mainVerticalAdapter.notifyDataSetChanged();

                    } else {
                        postImageList.add(new PostModel(AppConstants.NOT_AVALABLE, AppConstants.NOT_AVALABLE, AppConstants.NOT_AVALABLE, AppConstants.NOT_AVALABLE));
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 1);
                        MainVerticalRecyclerView.setLayoutManager(gridLayoutManager);
                        mainVerticalAdapter.notifyDataSetChanged();
                    }

                    Log.i("galleryCount", "postImageList" + postImageList.size());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            postRef.child(communityID).addValueEventListener(postRefListener);

        } catch (NullPointerException e) {
            Log.i("setVerticialRecyclervie", "Null Pointer");
            e.printStackTrace();
        }


    }


    private void setParticipants(CommunityModel communityModel) {

        String communityID = communityModel.getCommunityID();
        SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);


        participantRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("participant", "ref triggered");

                photographerList.clear();

                participantsAdapter = new ParticipantsAdapter(photographerList, MainActivity.this, qrCodeBottomSheet, communityModel.getAdmin(), FirebaseDatabase.getInstance().getReference(), communityID);
                ParticipantsRecyclerView.setAdapter(participantsAdapter);

                if (currentActiveCommunityID.equals(communityID) && !CurrentActiveCommunity.contains(AppConstants.IS_NOTIFIED)) {
                    photographerList.add(new PhotographerModel("add", "add", "add", "add"));
                }

                DatabaseReference photographerRef = FirebaseDatabase.getInstance().getReference().child("Users");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.i("participant", "userid " + snapshot.getKey());

                    photographerRef.child(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {

                            Log.i("participant", "getting user info " + snapshot.getKey());


                            String name = AppConstants.NOT_AVALABLE, imgurl = AppConstants.NOT_AVALABLE, email = AppConstants.NOT_AVALABLE;
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
                            if (userSnapshot.hasChild("Email")) {
                                email = userSnapshot.child("Email").getValue().toString();
                            }

                            if (!getPhotographerKeys(photographerList).contains(snapshot.getKey())) {

                                photographerList.add(new PhotographerModel(name, snapshot.getKey(), imgurl, email));
                                participantsAdapter.notifyDataSetChanged();
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }



                /*
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (!expandableCardView.isExpanded()) {
                            if (currentActiveCommunityID.equals(communityID)) {
                                if (photographerList.size() > 12) {
                                    // expandableCardView.setTitle( (photographerList.size() - 2) + " photographers");
                                    photographerList.add(new PhotographerModel("view", "view", "view", "view"));
                                } else {
                                    //expandableCardView.setTitle("Photographers : "+ (photographerList.size() - 1));
                                }
                            } else {
                                if (photographerList.size() > 13) {
                                    photographerList.add(new PhotographerModel("view", "view", "view","view"));
                                    //       expandableCardView.setTitle("Photographers : "+ (photographerList.size() - 1));
                                } else {
                                    //      expandableCardView.setTitle("Photographers : "+ (photographerList.size()));
                                }
                            }



                        }

                    }
                }, 500);
                 */


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        participantRef.child(communityID).addValueEventListener(participantRefListener);


       /*
        participantRefListener = readFirebaseData.readData(participantRef.child(communityID), new FirebaseRead() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(DatabaseError databaseError) {

            }
        });
        */


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

        //todo check if db is empty or not. If not empty prompt user to finish upload


        ProgressBar progressBar = findViewById(R.id.mainloadingpbar);

        if (forceQuit) {

            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            linkRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.hasChild("id") && snapshot.child("id").equals(currentActiveCommunityID)) {
                            linkRef.child(snapshot.getKey()).removeValue();

                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            communityRef.child(currentActiveCommunityID).child(FirebaseConstants.COMMUNITYSTATUS).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).removeValue();
                        currentActiveCommunityID = AppConstants.NOT_AVALABLE;

                        SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                        SharedPreferences.Editor ceditor = CurrentActiveCommunity.edit();
                        ceditor.remove("id");
                        ceditor.remove("time");
                        ceditor.remove("stopAt");
                        ceditor.remove("startAt");
                        ceditor.remove("notiCount");
                        ceditor.commit();

                        mainAddPhotosFab.hide();
                        String scanWorkId = CurrentActiveCommunity.getString("scanWorkerId", AppConstants.NOT_AVALABLE);
                        String albumEndWorkId = CurrentActiveCommunity.getString("albumendWorkerId", AppConstants.NOT_AVALABLE);
                        if (scanWorkId.equals(AppConstants.NOT_AVALABLE)) {
                            WorkManager.getInstance().cancelUniqueWork(AppConstants.PHOTO_SCAN_WORK);
                        } else {
                            WorkManager.getInstance().cancelWorkById(UUID.fromString(scanWorkId));
                        }
                        if (albumEndWorkId.equals(AppConstants.NOT_AVALABLE)) {
                            WorkManager.getInstance().cancelAllWork();
                        } else {
                            WorkManager.getInstance().cancelWorkById(UUID.fromString(albumEndWorkId));
                        }

                        progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        new CustomToast(MainActivity.this, MainActivity.this)
                                .showToast("Exited Participation");
                        Log.i("quit", "url" + photographerList.get(0).getImgUrl() + "getId" + photographerList.get(0).getId() + "getName" + photographerList.get(0).getName());

                        if (photographerList.get(0).getImgUrl().equals("add") && photographerList.get(0).getId().equals("add") && photographerList.get(0).getName().equals("add")) {
                            photographerList.remove(0);
                            participantsAdapter.notifyDataSetChanged();
                        }
                        //SetDefaultView();

                    } else {
                        //SetDefaultView();
                        progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        showDialogQuitUnsuccess();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    showDialogQuitUnsuccess();

                }
            });

        } else {
            showAlbumQuitPrompt("Exit Participation ?", "Are you sure you want to exit the participation in current Cloud-Album. You won't able to upload photos to this album again.",
                    "NO ",
                    "YES ");

        }


    }

    private void showAlbumQuitPrompt(String title,
                                     String message,
                                     String postiveButtonMessage,
                                     String negativeButtonMessage) {

        //fixme this dialog ui is not good

        int cf_bg_color, colorPrimary, red_inlens, cf_alert_dialogue_dim_bg;
        if (appTheme.equals(AppConstants.themeLight)) {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens = getResources().getColor(R.color.Light_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);
        } else {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens = getResources().getColor(R.color.Dark_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }

        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle(title)
                .setIcon(R.drawable.ic_info_red)
                .setMessage(message)
                .setDialogBackgroundColor(cf_bg_color)
                .setTextColor(colorPrimary)
                .setCancelable(false)
                .addButton(negativeButtonMessage,
                        red_inlens,
                        cf_alert_dialogue_dim_bg
                        , CFAlertDialog.CFAlertActionStyle.DEFAULT,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ProgressBar progressBar = findViewById(R.id.mainloadingpbar);
                                progressBar.setVisibility(View.VISIBLE);
                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                communityRef.child(currentActiveCommunityID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        String admin = dataSnapshot.child(FirebaseConstants.COMMUNITYADMIN).getValue().toString();
                                        if (admin.equals(currentUserId)) {

                                            linkRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        if (snapshot.hasChild("id") && snapshot.child("id").equals(currentActiveCommunityID)) {
                                                            linkRef.child(snapshot.getKey()).removeValue();

                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            communityRef.child(currentActiveCommunityID).child(FirebaseConstants.COMMUNITYSTATUS).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).removeValue();
                                                        currentActiveCommunityID = AppConstants.NOT_AVALABLE;
                                                        mainAddPhotosFab.hide();
                                                        SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                                                        SharedPreferences.Editor ceditor = CurrentActiveCommunity.edit();
                                                        ceditor.remove("id");
                                                        ceditor.remove("time");
                                                        ceditor.remove("stopAt");
                                                        ceditor.remove("startAt");
                                                        ceditor.remove("notiCount");
                                                        ceditor.commit();

                                                        String scanWorkId = CurrentActiveCommunity.getString("scanWorkerId", AppConstants.NOT_AVALABLE);
                                                        String albumEndWorkId = CurrentActiveCommunity.getString("albumendWorkerId", AppConstants.NOT_AVALABLE);
                                                        if (scanWorkId.equals(AppConstants.NOT_AVALABLE)) {
                                                            WorkManager.getInstance().cancelUniqueWork(AppConstants.PHOTO_SCAN_WORK);
                                                        } else {
                                                            WorkManager.getInstance().cancelWorkById(UUID.fromString(scanWorkId));
                                                        }
                                                        if (albumEndWorkId.equals(AppConstants.NOT_AVALABLE)) {
                                                            WorkManager.getInstance().cancelAllWork();
                                                        } else {
                                                            WorkManager.getInstance().cancelWorkById(UUID.fromString(albumEndWorkId));
                                                        }
                                                        progressBar.setVisibility(View.GONE);
                                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                        new CustomToast(MainActivity.this, MainActivity.this)
                                                                .showToast("Exited Participation");
                                                        if (photographerList.get(0).getImgUrl().equals("add") && photographerList.get(0).getId().equals("add") && photographerList.get(0).getName().equals("add")) {
                                                            photographerList.remove(0);
                                                            participantsAdapter.notifyDataSetChanged();
                                                        }
                                                        //SetDefaultView();

                                                    } else {
                                                        //SetDefaultView();
                                                        progressBar.setVisibility(View.GONE);
                                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                        showDialogQuitUnsuccess();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.GONE);
                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                    showDialogQuitUnsuccess();

                                                }
                                            });
                                        } else {
                                            currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {
                                                        currentActiveCommunityID = AppConstants.NOT_AVALABLE;
                                                        SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                                                        String scanWorkId = CurrentActiveCommunity.getString("scanWorkerId", AppConstants.NOT_AVALABLE);
                                                        String albumEndWorkId = CurrentActiveCommunity.getString("albumendWorkerId", AppConstants.NOT_AVALABLE);

                                                        if (scanWorkId.equals(AppConstants.NOT_AVALABLE)) {
                                                            WorkManager.getInstance().cancelUniqueWork(AppConstants.PHOTO_SCAN_WORK);
                                                        } else {
                                                            WorkManager.getInstance().cancelWorkById(UUID.fromString(scanWorkId));
                                                        }
                                                        if (albumEndWorkId.equals(AppConstants.NOT_AVALABLE)) {
                                                            WorkManager.getInstance().cancelAllWork();
                                                        } else {
                                                            WorkManager.getInstance().cancelWorkById(UUID.fromString(albumEndWorkId));
                                                        }
                                                        mainAddPhotosFab.hide();
                                                        progressBar.setVisibility(View.GONE);
                                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                        new CustomToast(MainActivity.this, MainActivity.this)
                                                                .showToast("Exited Participation");

                                                        if (photographerList.get(0).getImgUrl().equals("add") && photographerList.get(0).getId().equals("add") && photographerList.get(0).getName().equals("add")) {
                                                            photographerList.remove(0);
                                                            participantsAdapter.notifyDataSetChanged();
                                                        }

                                                    } else {
                                                        //SetDefaultView();
                                                        progressBar.setVisibility(View.GONE);
                                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                        showDialogQuitUnsuccess();
                                                    }

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.GONE);
                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
                .addButton(postiveButtonMessage,
                        colorPrimary,
                        cf_alert_dialogue_dim_bg,
                        CFAlertDialog.CFAlertActionStyle.DEFAULT,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });


        builder.show();

    }

    private void showDialogQuitUnsuccess() {

        int cf_bg_color, colorPrimary, red_inlens, cf_alert_dialogue_dim_bg;
        if (appTheme.equals(AppConstants.themeLight)) {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens = getResources().getColor(R.color.Light_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);
        } else {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens = getResources().getColor(R.color.Dark_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }

        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle("Unable to Exit")
                .setIcon(R.drawable.ic_info)
                .setDialogBackgroundColor(cf_bg_color)
                .setTextColor(colorPrimary)
                .setMessage("Unable to Quit Cloud-Album. Please check your internet connection or whether you are participating in a Cloud-Album")
                .setCancelable(false)
                .addButton("OK",
                        colorPrimary,
                        cf_alert_dialogue_dim_bg
                        , CFAlertDialog.CFAlertActionStyle.DEFAULT,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
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
                    finish();
                } else {
                    if (!cfDialogAddPhotoFab.isShowing()) {
                        cfDialogAddPhotoFab.show();
                    }
                }
            }
            break;
            case MY_PERMISSIONS_REQUEST_START_WORKMANAGER: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest
                            .Builder(AlbumScanWorker.class, 15, TimeUnit.MINUTES)
                            .addTag(AppConstants.PHOTO_SCAN_WORK)
                            .build();
                    WorkManager.getInstance().enqueueUniquePeriodicWork(AppConstants.PHOTO_SCAN_WORK, ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);

                    SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor ceditor = CurrentActiveCommunity.edit();
                    ceditor.putString("scanWorkerId", String.valueOf(periodicWorkRequest.getId()));
                    ceditor.commit();

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
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        currentUserRef.child("Profile_picture").setValue(uri.toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            {
                                                showSnackbarMessage("Successfully uploaded your profile picture.");
                                                for (int i = 0; i < photographerList.size(); i++) {
                                                    if (photographerList.get(i).getId().equals(currentUserId)) {
                                                        photographerList.get(i).setImgUrl(uri.toString());
                                                        participantsAdapter.notifyItemChanged(i);
                                                    }
                                                }

                                            }
                                        } else {
                                            showSnackbarMessage("Failed to upload picture. Please try again.");
                                        }

                                    }
                                });

//
//
//
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showSnackbarMessage("Failed to upload picture. Please try again.");

                    }
                });
            }

        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            showSnackbarMessage("Image cropping error. Please try again.");
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
                        FilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Communities")
                                        .child(PostKeyForEdit)
                                        .child("coverimage")
                                        .setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.INVISIBLE);
                                            showSnackbarMessage("Successfully uploaded the cover photo.");
                                            communityDataList.get(position).setCoverImage(uri.toString());
                                            mainHorizontalAdapter.notifyItemChanged(position);
                                        } else {
                                            // MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.INVISIBLE);
                                            showSnackbarMessage("Failed to uploaded the cover photo. Please try again.");

                                        }
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showSnackbarMessage("Failed to uploaded the cover photo. Please try again.");

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


        if (rootForMainActivity.isDrawerOpen(GravityCompat.START)) {
            rootForMainActivity.closeDrawer(GravityCompat.START);
        } else if (!isAppbarOpen) {
            appBarLayout.setExpanded(true, true);
            MainVerticalRecyclerView.smoothScrollToPosition(0);
        } else {
            super.onBackPressed();
        }

    }


    private void checkInternetConnection() {

        if (br == null) {

            br = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
                    if (isConnected) {

                        onResume();

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

                        mainAddPhotosFab.hide();

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

        int cf_bg_color, colorPrimary, red_inlens, cf_alert_dialogue_dim_bg;
        if (appTheme.equals(AppConstants.themeLight)) {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens = getResources().getColor(R.color.Light_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);
        } else {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens = getResources().getColor(R.color.Dark_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }

        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle(title)
                .setDialogBackgroundColor(cf_bg_color)
                .setTextColor(colorPrimary)
                .setIcon(R.drawable.ic_check_circle_black_24dp)
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
                                //For the purpose of Quit and cancelling notification
                                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancelAll();

                                dialog.dismiss();

                            }
                        });
        builder.show();
    }

    public void showInfoMessage(String title, String message) {

        int cf_bg_color, colorPrimary, red_inlens, cf_alert_dialogue_dim_bg;
        if (appTheme.equals(AppConstants.themeLight)) {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens = getResources().getColor(R.color.Light_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);
        } else {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens = getResources().getColor(R.color.Dark_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }

        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                .setTitle(title)
                .setIcon(R.drawable.ic_info)
                .setDialogBackgroundColor(cf_bg_color)
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

    @Override
    public void dismissDialog() {
        optionsBottomSheetFragment.dismiss();
    }

    public class PostGridViewHolder extends RecyclerView.ViewHolder {
        ImageView PostImageView;
        ProgressBar PostProgressbar;
        ImageButton postRefresButton;

        public PostGridViewHolder(View itemView) {
            super(itemView);

            PostImageView = itemView.findViewById(R.id.post_layout_imageview);
            PostProgressbar = itemView.findViewById(R.id.post_layout_progressbar);
            postRefresButton = itemView.findViewById(R.id.post_layout_refresh_button);

        }
    }

    public class EmptyGridViewHolder extends RecyclerView.ViewHolder {
        public EmptyGridViewHolder(View itemView) {
            super(itemView);

        }
    }

    public class MainVerticalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<PostModel> PostList;
        Picasso picasso;
        String comID;
        Activity activity;
        int VIEW_TYPE_PHOTO = 1, VIEW_TYPE_EMPTY = 0;

        public MainVerticalAdapter(RecyclerView recyclerView, Activity activity, List<PostModel> postList, String ID) {
            this.activity = activity;
            PostList = postList;
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


                    RequestOptions reqOpt = new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // It will cache your image after loaded for first time
                            .override(viewHolder.PostImageView.getWidth(), viewHolder.PostImageView.getHeight());// Overrides size of downloaded image and converts it's bitmaps to your desired image size;

//                    Log.i("Verticaladapter","resource "+PostList.get(position).getUri());
                    viewHolder.postRefresButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            viewHolder.postRefresButton.clearAnimation();
                            viewHolder.postRefresButton.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.rotate));
                            viewHolder.postRefresButton.getAnimation().start();
                            Glide.with(activity)
                                    .load(PostList.get(position).getUri())
                                    .apply(reqOpt)
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            viewHolder.postRefresButton.setVisibility(View.VISIBLE);
//                                            Log.i("Verticaladapter","exception "+e);
//                                            Log.i("Verticaladapter","resource "+PostList.get(position).getUri());
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            viewHolder.postRefresButton.setVisibility(View.GONE);
                                            return false;
                                        }

                                    })
                                    .into(viewHolder.PostImageView);

                        }
                    });

                    viewHolder.PostProgressbar.setVisibility(View.VISIBLE);
                    Glide.with(activity)
                            .load(PostList.get(position).getUri())
                            .apply(reqOpt)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    viewHolder.PostProgressbar.setVisibility(View.GONE);
                                    viewHolder.postRefresButton.setVisibility(View.VISIBLE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    viewHolder.PostProgressbar.setVisibility(View.GONE);
                                    viewHolder.postRefresButton.setVisibility(View.GONE);
                                    return false;
                                }

                            })
                            .into(viewHolder.PostImageView);

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
        private final int VIEW_TYPE_ALBUM = 0, VIEW_TYPE_LOADING = 1;
        Activity activity;
        int selectedAlbumPosition = 0;
        String selectedAlbumKey;

        int colorSecondary;

        public MainHorizontalAdapter(List<CommunityModel> communityDetails, Activity activity) {
            this.communityDetails = communityDetails;
            this.activity = activity;
            selectedAlbumKey = AppConstants.NOT_AVALABLE;
            if (appTheme.equals(AppConstants.themeLight)) {
                colorSecondary = getResources().getColor(R.color.colorLightSecondary);
            } else {
                colorSecondary = getResources().getColor(R.color.colorDarkSecondary);


            }
        }

        @Override
        public int getItemViewType(int position) {
            if (communityDetails.get(position) == null) {
                return VIEW_TYPE_LOADING;
            } else {
                return VIEW_TYPE_ALBUM;

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
                        setVerticalRecyclerView(communityDetails.get(selectedAlbumPosition));
                        mainSelectedKey = communityDetails.get(selectedAlbumPosition).getCommunityID();
                    }
                    selectedAlbumKey = communityDetails.get(selectedAlbumPosition).getCommunityID();
                    mainSelectedKey = selectedAlbumKey;
                } else {
                    viewHolder.Indicator.setVisibility(View.INVISIBLE);
                    viewHolder.itemView.setAlpha((float) 1);
                }

                if (!communityDetails.get(position).getCommunityID().equals(AppConstants.NOT_AVALABLE)) {
                    if (communityDetails.get(position).getType().contentEquals("Ceremony") ||
                            communityDetails.get(position).getType().contentEquals("Wedding")) {

                        Glide.with(activity)
                                .load(communityDetails.get(position)
                                        .getCoverImage()).placeholder(R.drawable.ic_ceremony_foreground)
                                .addListener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {


                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                }).into(viewHolder.AlbumCoverButton);
                    } else if (communityDetails.get(position).getType().contentEquals("Hangouts")) {

                        Glide.with(activity)
                                .load(communityDetails.get(position)
                                        .getCoverImage()).placeholder(R.drawable.ic_hangout_foreground)
                                .addListener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                }).into(viewHolder.AlbumCoverButton);
                    } else if (communityDetails.get(position).getType().contentEquals("Travel")) {

                        Glide.with(activity)
                                .load(communityDetails.get(position)
                                        .getCoverImage()).placeholder(R.drawable.ic_travel_foreground)
                                .addListener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                }).into(viewHolder.AlbumCoverButton);
                    } else if (communityDetails.get(position).getType().contentEquals("Others")) {

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


                    } else if (communityDetails.get(position).getType().contentEquals("Party")) {
                        Glide.with(activity)
                                .load(communityDetails.get(position)
                                        .getCoverImage()).placeholder(R.drawable.ic_party_foreground).addListener(new RequestListener<Drawable>() {
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


                }


                viewHolder.AlbumCoverButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (communityDetails.get(position).getCommunityID().equals(currentActiveCommunityID)) {
                            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(activity, communityDetails.get(position), position, FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseDatabase.getInstance().getReference());
                            bottomSheetFragment.show(((FragmentActivity) activity).getSupportFragmentManager(), bottomSheetFragment.getTag());
                        } else {
                            BottomSheetFragment_Inactive bottomSheetFragment_inactive = new BottomSheetFragment_Inactive(activity, communityDetails.get(position), position, FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseDatabase.getInstance().getReference());
                            bottomSheetFragment_inactive.show(((FragmentActivity) activity).getSupportFragmentManager(), bottomSheetFragment_inactive.getTag());
                        }
                        return false;
                    }
                });

                viewHolder.menuOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (communityDetails.get(position).getCommunityID().equals(currentActiveCommunityID)) {
                            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(activity, communityDetails.get(position), position, FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseDatabase.getInstance().getReference());
                            bottomSheetFragment.show(((FragmentActivity) activity).getSupportFragmentManager(), bottomSheetFragment.getTag());
                        } else {
                            BottomSheetFragment_Inactive bottomSheetFragment_inactive = new BottomSheetFragment_Inactive(activity, communityDetails.get(position), position, FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseDatabase.getInstance().getReference());
                            bottomSheetFragment_inactive.show(((FragmentActivity) activity).getSupportFragmentManager(), bottomSheetFragment_inactive.getTag());
                        }

                    }
                });

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (selectedAlbumPosition != viewHolder.getAdapterPosition()) {
                            notifyItemChanged(selectedAlbumPosition);
                            notifyItemChanged(viewHolder.getAdapterPosition());
                            selectedAlbumPosition = viewHolder.getAdapterPosition();
                            setVerticalRecyclerView(communityDetails.get(position));
                            if (currentActiveCommunityID.equals(communityDetails.get(position).getCommunityID()) && isConnectedToNet()) {
                                mainAddPhotosFab.show();
                            } else {
                                mainAddPhotosFab.hide();
                                try {
                                    rippleBackground.stopRippleAnimation();
                                    rippleBackground2.stopRippleAnimation();
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }

                            }

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
                            setVerticalRecyclerView(communityDetails.get(position));
                            if (currentActiveCommunityID.equals(communityDetails.get(position).getCommunityID()) && isConnectedToNet()) {
                                mainAddPhotosFab.show();
                            } else {
                                mainAddPhotosFab.hide();
                                try {
                                    rippleBackground.stopRippleAnimation();
                                    rippleBackground2.stopRippleAnimation();
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                    }
                });

                if (communityDetails.get(position).isReported()) {
                    viewHolder.AlbumNameTextView.setText(communityDetails.get(position).getTitle());
                    viewHolder.AlbumNameTextView.setTextColor(Color.RED);
                } else {
                    viewHolder.AlbumNameTextView.setText(communityDetails.get(position).getTitle());
                    viewHolder.AlbumNameTextView.setTextColor(colorSecondary);
                }


            } else if (holder instanceof MainHorizontalLoadingViewHolder) {
                MainHorizontalLoadingViewHolder viewHolder = (MainHorizontalLoadingViewHolder) holder;
                viewHolder.progressBar.setIndeterminate(true);
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

    public boolean isConnectedToNet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static void setCoverChange(boolean coverChange) {
        COVER_CHANGE = coverChange;
    }

    public static void setProfileChange(boolean profileChange) {
        PROFILE_CHANGE = profileChange;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }


}