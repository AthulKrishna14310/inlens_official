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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
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
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import com.integrals.inlens.Helper.ParticipantsAdapter;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.JobScheduler.Scheduler;
import com.integrals.inlens.Models.CommunityModel;
import com.integrals.inlens.Models.PostModel;
import com.integrals.inlens.Notification.AlarmManagerHelper;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vistrav.ask.Ask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import com.integrals.inlens.Activities.CreateCloudAlbum;
import com.integrals.inlens.Activities.QRCodeReader;
import com.integrals.inlens.Activities.SharedImageActivity;

import org.michaelbel.bottomsheet.BottomSheet;


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
    private static boolean COVER_CHANGE = false;
    private static boolean SEARCH_IN_PROGRESS = false;


    private RelativeLayout RootForMainActivity;

    private int INTID = 3939;

    private RecyclerView MainHorizontalRecyclerview, MainVerticalRecyclerView;
    private ImageButton MainNewAlbumButton, MainScanQrButton;
    private HorizontalScrollView MainHorizontalScrollView;
    private ScrollView MainScrollView;

    private TextView NoAlbumTextView;

    private Boolean SHOW_TOUR = false;


    private CircleImageView MainProfileImageview;
    private ImageButton MainSearchButton, MainBackButton;
    private EditText MainSearchEdittext;
    private RelativeLayout MainActionbar, MainSearchView;
    private CardView MainToolbar;

    private BroadcastReceiver br;
    private RelativeLayout NoInternetView;
    private TextView NoInternetTextView;

    private boolean GotoGallery = false;

    private int Position=0;

    private static final int JOB_ID = 465;
    private JobScheduler jobScheduler;
    private JobInfo jobInfo;


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


        GotoGallery = getIntent().getBooleanExtra("gallery", false);

        MainHorizontalScrollView = findViewById(R.id.main_horizontalscrollview);
        MainHorizontalScrollView.setHorizontalScrollBarEnabled(false);
        MainHorizontalScrollView.setVerticalScrollBarEnabled(false);
        MainScrollView = findViewById(R.id.main_scrollview);
        MainScrollView.setHorizontalScrollBarEnabled(false);
        MainScrollView.setVerticalScrollBarEnabled(false);



        MainNewAlbumButton = findViewById(R.id.main_horizontal_new_album_button);
        MainScanQrButton = findViewById(R.id.main_horizontal_scan_button);

        MainHorizontalScrollView.smoothScrollTo(0, 0);
        MainScrollView.smoothScrollTo(0, 0);

        NoAlbumTextView = findViewById(R.id.nocloudalbumtextview);

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


        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

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

                    if (!dataSnapshot.hasChild("Communities")) {
                        MainLoadingProgressBar.setVisibility(View.GONE);
                    } else {
                        AlarmManagerHelper managerHelper = new AlarmManagerHelper(MainActivity.this);
                        try {
                            managerHelper.deinitateAlarmManager();
                            managerHelper.initiateAlarmManager(5);
                        }
                        catch (Exception e)
                        {
                            managerHelper.initiateAlarmManager(5);

                        }
                        ShowAllAlbums();
                    }

                    if (dataSnapshot.hasChild("Profile_picture")) {
                        String image = dataSnapshot.child("Profile_picture").getValue().toString();
                        Glide.with(getApplicationContext()).load(image).into(MainProfileImageview);
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

                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
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

                        NoAlbumTextView.setText("No result found.");
                        NoAlbumTextView.setVisibility(View.GONE);
                        MainVerticalRecyclerView.setVisibility(View.VISIBLE);

                        if (!TextUtils.isEmpty(editable.toString())) {

                            CommunitySearchDetails.clear();
                            NoAlbumTextView.setVisibility(View.GONE);

                            for (int i = 0; i < MyCommunityDetails.size(); i++) {
                                if (MyCommunityDetails.get(i).getTitle().toLowerCase().contains(editable.toString().toLowerCase())) {
                                    CommunitySearchDetails.add(MyCommunityDetails.get(i));
                                }
                            }

                            if (CommunitySearchDetails.size() == 0) {
                                NoAlbumTextView.setVisibility(View.VISIBLE);
                                MainVerticalRecyclerView.setVisibility(View.GONE);

                            } else {
                                NoAlbumTextView.setVisibility(View.GONE);
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

                Snackbar.with(MainActivity.this,null)
                        .type(Type.CUSTOM)
                        .message("Loading QR-Code Scanner ..")
                        .duration(Duration.LONG)
                        .fillParent(true)
                        .textAlign(Align.LEFT)
                        .show();
                scanQR();
            }
        });


    }

    private void SetDefaultView() {

        SEARCH_IN_PROGRESS = false;
        MainSearchEdittext.setText("");

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(MainSearchEdittext.getWindowToken(), 0);
        }

        MainSearchView.clearAnimation();
        MainSearchView.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out));
        MainSearchView.getAnimation().start();
        MainSearchView.setVisibility(View.GONE);

        MainActionbar.clearAnimation();
        MainActionbar.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in));
        MainActionbar.getAnimation().start();
        MainActionbar.setVisibility(View.VISIBLE);


        if (CurrentActiveCommunityID.equals("Not Available")) {
            SetVerticalRecyclerView(CurrentDeadCommunityID);
        } else {
            SetVerticalRecyclerView(CurrentActiveCommunityID);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(MainScrollView.getScrollX() != 0)
                {
                    MainScrollView.smoothScrollTo(0, 0);
                }
            }
        }, 100);

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
                                Toast.makeText(getApplicationContext(), "You cannot participate in a new community before leaving the current one.", Toast.LENGTH_LONG).show();
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

                                    } else {

                                        Ref.child("Users").child(CurrentUserID).child("Communities").child(substring).setValue(ServerValue.TIMESTAMP);
                                        Ref.child("Users").child(CurrentUserID).child("live_community").setValue(substring);
                                        Ref.child("Communities").child(substring).child("participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(ServerValue.TIMESTAMP);

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

    private void inviteLink() {
        // Create Alert using Builder
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)

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
    }

    private void createAlbum() {

        if (CurrentActiveCommunityID.equals("Not Available")) {
            startActivity(new Intent(MainActivity.this, CreateCloudAlbum.class));
        } else {
            Toast.makeText(getApplicationContext(), "You cannot participate in a new community before leaving the current one.", Toast.LENGTH_LONG).show();
        }


    }

    private void scanQR() {

        if (CurrentActiveCommunityID.equals("Not Available")) {
            startActivity(new Intent(MainActivity.this, QRCodeReader.class));
        } else {
            Toast.makeText(getApplicationContext(), "You cannot participate in a new community before leaving the current one.", Toast.LENGTH_LONG).show();
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

        NoAlbumTextView.setVisibility(View.GONE);
        MainLoadingProgressBar.setVisibility(View.VISIBLE);


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
                    NoAlbumTextView.setVisibility(View.VISIBLE);
                    MainHorizontalRecyclerview.setVisibility(View.VISIBLE);

                } else {
                    NoAlbumTextView.setVisibility(View.GONE);
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


                /*
                if (GotoGallery) {

                    for (int i = 0; i < MyCommunityDetails.size(); i++) {
                        if (CurrentActiveCommunityID.equals(MyCommunityDetails.get(i).getCommunityID()) || CurrentDeadCommunityID.equals(MyCommunityDetails.get(i).getCommunityID())) {
                            GotoGallery = false;
                            startActivity(new Intent(MainActivity.this, CommunityActivity.class)
                                    .putExtra("CommunityID", MyCommunityDetails.get(i).getCommunityID())
                                    .putExtra("CommunityName", MyCommunityDetails.get(i).getTitle())
                                    .putExtra("CommunityStartTime", MyCommunityDetails.get(i).getStartTime())
                                    .putExtra("CommunityEndTime", MyCommunityDetails.get(i).getEndTime())
                                    .putExtra("select_fragment", 1));

                        }
                    }

                }
                 */

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (CurrentActiveCommunityID.equals("Not Available")) {
            SetVerticalRecyclerView(CurrentDeadCommunityID);
        } else {
            SetVerticalRecyclerView(CurrentActiveCommunityID);
        }


    }

    private void SetVerticalRecyclerView(String communityID) {

        CurrentKeyShowninVerticialRecyclerview = communityID;

        MyPostList = new ArrayList<>();
        NoAlbumTextView.setVisibility(View.GONE);

        Ref.child("Communities").child(communityID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                MyPostList.clear();
                MainVerticalRecyclerView.removeAllViews();

                if (dataSnapshot.hasChild("posts")) {
                    MainVerticalRecyclerView.setVisibility(View.VISIBLE);
                    NoAlbumTextView.setVisibility(View.GONE);


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
                        NoAlbumTextView.setText("Community has no photos");
                        NoAlbumTextView.setVisibility(View.VISIBLE);
                    }


                } else {
                    MainVerticalRecyclerView.setVisibility(View.GONE);
                    NoAlbumTextView.setText("Community has no photos");
                    NoAlbumTextView.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
                builder.setTitle("Quit community");
                builder.setMessage("Are you sure you want to quit the current community .");
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
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
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

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && COVER_CHANGE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri ImageUri = result.getUri();
                //MainBottomSheetAlbumCoverEditUserImage.setImageURI(ImageUri);
                //MainBottomSheetAlbumCoverEditprogressBar.setVisibility(View.VISIBLE);
                UploadCoverPhoto(ImageUri);
            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

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

    private boolean IsConnectedToNet() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

    }

    @Override
    public void onBackPressed() {

        if (SEARCH_IN_PROGRESS) {
            SetDefaultView();
        } else if (MainScrollView.getScrollY() != 0) {
            MainScrollView.smoothScrollTo(0, 0);
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
                Glide.with(getApplicationContext()).load(CommunityDetails.get(position).getCoverImage()).into(holder.AlbumCoverButton);
            } else {
                Glide.with(getApplicationContext()).load(R.drawable.ic_camera_shutter).into(holder.AlbumCoverButton);

            }

            if(holder.getLayoutPosition()==Position){
                holder.Indicator.setVisibility(View.VISIBLE);
                holder.itemView.setAlpha((float) 1);
                SetVerticalRecyclerView(CommunityDetails.get(Position).getCommunityID());


            }



            holder.AlbumCoverButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getCurrentCommunityinVerticialRecyclerview() != null && !getCurrentCommunityinVerticialRecyclerview().equals(CommunityDetails.get(position).getCommunityID()))
                    {
                        Position=holder.getLayoutPosition();
                        ShowAllAlbums();
                    }
                }
            });

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

            public MainCommunityViewHolder(View itemView) {
                super(itemView);
                AlbumOptions=itemView.findViewById(R.id.albumcard_options);
                AlbumCoverButton = itemView.findViewById(R.id.albumcard_image_view);
                AlbumNameTextView = itemView.findViewById(R.id.album_card_textview);
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


        PostDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                MainScrollView.requestDisallowInterceptTouchEvent(false);

            }
        });

    }

    public class MainVerticalAdapter extends RecyclerView.Adapter<MainVerticalAdapter.PostGridViewHolder> {

        Context context;
        List<PostModel> PostList;
        DatabaseReference UserRef;

        public MainVerticalAdapter(Context context, List<PostModel> postList, DatabaseReference userRef) {
            this.context = context;
            PostList = postList;
            UserRef = userRef;
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

        MainScrollView.requestDisallowInterceptTouchEvent(true);
    }

    public String getCurrentUserID() {
        return CurrentUserID;
    }

    public void setCurrentUserID(String currentUserID) {
        CurrentUserID = currentUserID;
    }

    public String getCurrentActiveCommunityID() {
        return CurrentActiveCommunityID;
    }

    public void setCurrentActiveCommunityID(String currentActiveCommunityID) {
        CurrentActiveCommunityID = currentActiveCommunityID;
    }

    public String getDummyCurrentActiveCommunityID() {
        return DummyCurrentActiveCommunityID;
    }

    public void setDummyCurrentActiveCommunityID(String dummyCurrentActiveCommunityID) {
        DummyCurrentActiveCommunityID = dummyCurrentActiveCommunityID;
    }

    public String getCurrentDeadCommunityID() {
        return CurrentDeadCommunityID;
    }

    public void setCurrentDeadCommunityID(String currentDeadCommunityID) {
        CurrentDeadCommunityID = currentDeadCommunityID;
    }

    public String getResultName() {
        return ResultName;
    }

    public void setResultName(String resultName) {
        ResultName = resultName;
    }

    public String getResultImage() {
        return ResultImage;
    }

    public void setResultImage(String resultImage) {
        ResultImage = resultImage;
    }

    public List<CommunityModel> getMyCommunityDetails() {
        return MyCommunityDetails;
    }

    public void setMyCommunityDetails(List<CommunityModel> myCommunityDetails) {
        MyCommunityDetails = myCommunityDetails;
    }

    public List<PostModel> getMyPostList() {
        return MyPostList;
    }

    public void setMyPostList(List<PostModel> myPostList) {
        MyPostList = myPostList;
    }

    public DatabaseReference getRef() {
        return Ref;
    }

    public void setRef(DatabaseReference ref) {
        Ref = ref;
    }

    public FirebaseAuth getInAuthentication() {
        return InAuthentication;
    }

    public void setInAuthentication(FirebaseAuth inAuthentication) {
        InAuthentication = inAuthentication;
    }

    public ProgressBar getMainLoadingProgressBar() {
        return MainLoadingProgressBar;
    }

    public void setMainLoadingProgressBar(ProgressBar mainLoadingProgressBar) {
        MainLoadingProgressBar = mainLoadingProgressBar;
    }

    public Dialog getQRCodeDialog() {
        return QRCodeDialog;
    }

    public void setQRCodeDialog(Dialog QRCodeDialog) {
        this.QRCodeDialog = QRCodeDialog;
    }

    public Dialog getPostDialog() {
        return PostDialog;
    }

    public void setPostDialog(Dialog postDialog) {
        PostDialog = postDialog;
    }

    public ImageView getPostDialogImageView() {
        return PostDialogImageView;
    }

    public void setPostDialogImageView(ImageView postDialogImageView) {
        PostDialogImageView = postDialogImageView;
    }

    public ProgressBar getPostDialogProgressbar() {
        return PostDialogProgressbar;
    }

    public void setPostDialogProgressbar(ProgressBar postDialogProgressbar) {
        PostDialogProgressbar = postDialogProgressbar;
    }

    public String getPostKeyForEdit() {
        return PostKeyForEdit;
    }

    public void setPostKeyForEdit(String postKeyForEdit) {
        PostKeyForEdit = postKeyForEdit;
    }

    public String getCurrentKeyShowninVerticialRecyclerview() {
        return CurrentKeyShowninVerticialRecyclerview;
    }

    public void setCurrentKeyShowninVerticialRecyclerview(String currentKeyShowninVerticialRecyclerview) {
        CurrentKeyShowninVerticialRecyclerview = currentKeyShowninVerticialRecyclerview;
    }

    public static int getGalleryPick() {
        return GALLERY_PICK;
    }

    public static int getCoverGalleryPick() {
        return COVER_GALLERY_PICK;
    }

    public static boolean isCoverChange() {
        return COVER_CHANGE;
    }

    public static void setCoverChange(boolean coverChange) {
        COVER_CHANGE = coverChange;
    }

    public static boolean isSearchInProgress() {
        return SEARCH_IN_PROGRESS;
    }

    public static void setSearchInProgress(boolean searchInProgress) {
        SEARCH_IN_PROGRESS = searchInProgress;
    }

    public RelativeLayout getRootForMainActivity() {
        return RootForMainActivity;
    }

    public void setRootForMainActivity(RelativeLayout rootForMainActivity) {
        RootForMainActivity = rootForMainActivity;
    }

    public int getINTID() {
        return INTID;
    }

    public void setINTID(int INTID) {
        this.INTID = INTID;
    }

    public RecyclerView getMainHorizontalRecyclerview() {
        return MainHorizontalRecyclerview;
    }

    public void setMainHorizontalRecyclerview(RecyclerView mainHorizontalRecyclerview) {
        MainHorizontalRecyclerview = mainHorizontalRecyclerview;
    }

    public RecyclerView getMainVerticalRecyclerView() {
        return MainVerticalRecyclerView;
    }

    public void setMainVerticalRecyclerView(RecyclerView mainVerticalRecyclerView) {
        MainVerticalRecyclerView = mainVerticalRecyclerView;
    }

    public ImageButton getMainNewAlbumButton() {
        return MainNewAlbumButton;
    }

    public void setMainNewAlbumButton(ImageButton mainNewAlbumButton) {
        MainNewAlbumButton = mainNewAlbumButton;
    }

    public ImageButton getMainScanQrButton() {
        return MainScanQrButton;
    }

    public void setMainScanQrButton(ImageButton mainScanQrButton) {
        MainScanQrButton = mainScanQrButton;
    }

    public HorizontalScrollView getMainHorizontalScrollView() {
        return MainHorizontalScrollView;
    }

    public void setMainHorizontalScrollView(HorizontalScrollView mainHorizontalScrollView) {
        MainHorizontalScrollView = mainHorizontalScrollView;
    }

    public ScrollView getMainScrollView() {
        return MainScrollView;
    }

    public void setMainScrollView(ScrollView mainScrollView) {
        MainScrollView = mainScrollView;
    }

    public TextView getNoAlbumTextView() {
        return NoAlbumTextView;
    }

    public void setNoAlbumTextView(TextView noAlbumTextView) {
        NoAlbumTextView = noAlbumTextView;
    }

    public Boolean getSHOW_TOUR() {
        return SHOW_TOUR;
    }

    public void setSHOW_TOUR(Boolean SHOW_TOUR) {
        this.SHOW_TOUR = SHOW_TOUR;
    }

    public CircleImageView getMainProfileImageview() {
        return MainProfileImageview;
    }

    public void setMainProfileImageview(CircleImageView mainProfileImageview) {
        MainProfileImageview = mainProfileImageview;
    }

    public ImageButton getMainSearchButton() {
        return MainSearchButton;
    }

    public void setMainSearchButton(ImageButton mainSearchButton) {
        MainSearchButton = mainSearchButton;
    }

    public ImageButton getMainBackButton() {
        return MainBackButton;
    }

    public void setMainBackButton(ImageButton mainBackButton) {
        MainBackButton = mainBackButton;
    }

    public EditText getMainSearchEdittext() {
        return MainSearchEdittext;
    }

    public void setMainSearchEdittext(EditText mainSearchEdittext) {
        MainSearchEdittext = mainSearchEdittext;
    }

    public RelativeLayout getMainActionbar() {
        return MainActionbar;
    }

    public void setMainActionbar(RelativeLayout mainActionbar) {
        MainActionbar = mainActionbar;
    }

    public RelativeLayout getMainSearchView() {
        return MainSearchView;
    }

    public void setMainSearchView(RelativeLayout mainSearchView) {
        MainSearchView = mainSearchView;
    }

    public CardView getMainToolbar() {
        return MainToolbar;
    }

    public void setMainToolbar(CardView mainToolbar) {
        MainToolbar = mainToolbar;
    }

    public BroadcastReceiver getBr() {
        return br;
    }

    public void setBr(BroadcastReceiver br) {
        this.br = br;
    }

    public RelativeLayout getNoInternetView() {
        return NoInternetView;
    }

    public void setNoInternetView(RelativeLayout noInternetView) {
        NoInternetView = noInternetView;
    }

    public boolean isGotoGallery() {
        return GotoGallery;
    }

    public void setGotoGallery(boolean gotoGallery) {
        GotoGallery = gotoGallery;
    }

    public int getPosition() {
        return Position;
    }

    public void setPosition(int position) {
        Position = position;
    }

    public List<String> getParticipantIDs() {
        return ParticipantIDs;
    }

    public void setParticipantIDs(List<String> participantIDs) {
        ParticipantIDs = participantIDs;
    }
}


