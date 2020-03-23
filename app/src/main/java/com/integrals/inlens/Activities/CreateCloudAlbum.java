package com.integrals.inlens.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.Helper.ReadFirebaseData;
import com.integrals.inlens.Interface.FirebaseRead;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.Notification.AlarmManagerHelper;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.integrals.inlens.R;


public class CreateCloudAlbum extends AppCompatActivity {
    private ImageView                           SetPostImage;
    private EditText                            CommunityAlbumTitle;
    private EditText                            CommunityAlbumDescription;
    private TextView                              SubmitButton;
    private Uri                                 ImageUri;
    private DatabaseReference                   PostDatabaseReference;
    private DatabaseReference                   CommunityDatabaseReference;
    private StorageReference                    PostStorageReference;
    private static final int                    GALLERY_REQUEST = 3;
    private FirebaseAuth                        InAuthentication;
    private FirebaseUser                        InUser;
    private DatabaseReference                   InUserReference;
    private String                              PostKey;
    private DatabaseReference                   photographerReference,databaseReference,ComNotyRef,participantRef;
    private String                              UserID;
    private ProgressBar                         UploadProgress;
    private static final int                    GALLERY_PICK=1 ;
    private CheckBox                            DateofCompletion;
    private String                              date;
    private String                              AlbumTime;
    private DatePickerDialog.OnDateSetListener  dateSetListener;
    private Calendar calendar;
    private CheckBox EventPicker ;
    private Dialog EventDialog;
    private String EventType = "";
    private String CheckTimeTaken="";
    private ImageButton CreateCloudAlbumBackButton;
    private Boolean EventTypeSet = false ,AlbumDateSet = false;


    FirebaseAuth firebaseAuth;
    DatabaseReference userRef;
    List<String> userCommunityIdList;
    String currentUserId;
    static final int DELAY_IN_MILLIS=1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_cloud_album_layout);

        // fixme update changes made in onbackpressed
        userCommunityIdList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS);

        ///////////////////////////////////////////////////////////////////////////////////////////
        EventDialogInit();

        InAuthentication = FirebaseAuth.getInstance();
        InUser = InAuthentication.getCurrentUser();
        UserID = InUser.getUid();

        CommunityDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Communities");
        InUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(InUser.getUid());
        participantRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PARTICIPANTS);
        EventPicker = findViewById(R.id.EventTypeText);
        CommunityAlbumTitle = (EditText) findViewById(R.id.AlbumTitleEditText);
        CommunityAlbumDescription = (EditText) findViewById(R.id.AlbumDescriptionEditText);
        SubmitButton = (TextView) findViewById(R.id.DoneButtonTextView);
        SetPostImage = (ImageView) findViewById(R.id.CoverPhoto);
        UploadProgress = (ProgressBar) findViewById(R.id.UploadProgress);
        CreateCloudAlbumBackButton=findViewById(R.id.create_cloud_album_backbutton);

        PostStorageReference = FirebaseStorage.getInstance().getReference();
        PostDatabaseReference = InUserReference.child("Communities");

        Calendar calender = Calendar.getInstance();
        DateofCompletion = findViewById(R.id.TimeEditText);


        int Month = calender.get(Calendar.MONTH);
        Month++;
        CheckTimeTaken=calender.get(Calendar.DAY_OF_MONTH) + "-"+ Month + "-"+calender.get(Calendar.YEAR);

        DateofCompletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateofCompletion.setChecked(false);
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        CreateCloudAlbum.this,
                        dateSetListener,
                        year,month,day
                );
                 dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                 dialog.getDatePicker().setMaxDate(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 4);
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                month=month+1;
                if(month<10)
                {
                    AlbumTime = day + "-" +"0"+ month + "-" + year;
                    if(!checkNumberOfDays(CheckTimeTaken,AlbumTime)){
                        AlbumDateSet = true;
                        DateofCompletion.setChecked(true);
                        DateofCompletion.setText(AlbumTime);
                        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(CreateCloudAlbum.this)
                                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                                .setTitle("Album expiry on "+AlbumTime)
                                .setIcon(R.drawable.ic_warning_black_24dp)
                                .setMessage("You can only upload images and add participants to this album till "+AlbumTime)
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














                    }else {
                        AlbumTime = "";
                        Toast.makeText(getApplicationContext(),"Album creation valid only for 5 days",Toast.LENGTH_SHORT).show();

                    }

                }
                else
                {    AlbumTime = day + "-" + month + "-" + year;
                     if(!checkNumberOfDays(CheckTimeTaken,AlbumTime)){
                            AlbumDateSet = true;
                            DateofCompletion.setChecked(true);

                            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(CreateCloudAlbum.this)
                                 .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                                 .setTitle("Album expiry on "+AlbumTime)
                                 .setIcon(R.drawable.ic_warning_black_24dp)
                                 .setMessage("You can only upload images and add participants to this album till "+AlbumTime)
                                 .setCancelable(false)
                                 .addButton("    OK , I understand   ", -1, Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                         CFAlertDialog.CFAlertActionAlignment.END,
                                         new DialogInterface.OnClickListener() {
                                             @Override
                                             public void onClick(DialogInterface dialog, int which) {
                                                 dialog.dismiss();

                                             }
                                         });
                         builder.show();




                     }else {
                         AlbumTime = "";
                         Toast.makeText(getApplicationContext(),"Album creation valid only for 5 days",Toast.LENGTH_LONG).show();
                     }

                }


            }
        };




        SetPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setAspectRatio((int) 390,285)
                        .setFixAspectRatio(true)
                        .start(CreateCloudAlbum.this);

            }
        });

        SubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(EventTypeSet && AlbumDateSet)
                {
                    if (!new PreOperationCheck().checkInternetConnectivity(getApplicationContext())){
                        showDialogue("No Internet. Please check your internet " +
                                "connection and try again",false);

                    }else {
                        PostingStarts();
                    }
                    }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please fill up all the provided " +
                            "fields and continue. ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        EventPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventPicker.setChecked(false);
                EventDialog.show();

            }
        });


        CreateCloudAlbumBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              onBackPressed();
            }
        });
        findViewById(R.id.date_range_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateofCompletion.setChecked(false);
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        CreateCloudAlbum.this,
                        dateSetListener,
                        year,month,day
                );
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 4);
                dialog.show();
            }
        });
        findViewById(R.id.event_type_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventPicker.setChecked(false);
                EventDialog.show();
            }
        });
    }

    private void EventDialogInit() {

        EventDialog = new Dialog(this,android.R.style.Theme_Light_NoTitleBar);
        EventDialog.setCancelable(true);
        EventDialog.setCanceledOnTouchOutside(false);
        EventDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        EventDialog.setContentView(R.layout.event_type_layout);
        EventDialog.getWindow().getAttributes().windowAnimations = R.style.BottomUpSlideDialogAnimation;

        Window EventDialogwindow = EventDialog.getWindow();
        EventDialogwindow.setGravity(Gravity.BOTTOM);
        EventDialogwindow.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        EventDialogwindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        EventDialogwindow.setDimAmount(0.75f);

        final Button EventWedding = EventDialog.findViewById(R.id.event_type_wedding_btn);
        final Button EventCeremony = EventDialog.findViewById(R.id.event_type_ceremony_btn);
        final Button EventOthers = EventDialog.findViewById(R.id.event_type_others_btn);
        final Button EventParty = EventDialog.findViewById(R.id.event_type_party_btn);
        final Button EventTravel = EventDialog.findViewById(R.id.event_type_travel_btn);
        final Button EventHangout = EventDialog.findViewById(R.id.event_type_hangouts_btn);
        final TextView SelectedEvent = EventDialog.findViewById(R.id.selected_event_type);

        SelectedEvent.setText("Selected Event Type : "+EventType );

        final ImageButton EventTypeDone  = EventDialog.findViewById(R.id.event_done_btn);

        if(!TextUtils.isEmpty(EventType))
        {
            EventTypeDone.setVisibility(View.VISIBLE);
        }
        else
        {
            EventTypeDone.setVisibility(View.INVISIBLE);

        }

        EventTypeDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!TextUtils.isEmpty(EventType))
                {
                    EventDialog.dismiss();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please select an event type.",Toast.LENGTH_SHORT).show();
                }

            }
        });

        EventCeremony.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding,EventOthers,EventParty,EventTravel,EventHangout);
                EventCeremony.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventCeremony.setTextColor(Color.parseColor("#ffffff"));
                EventType = "Ceremony";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                    EventPicker.setChecked(true);
                    EventPicker.setText(EventType);
                    EventDialog.dismiss();
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });

        EventWedding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventCeremony,EventOthers,EventParty,EventTravel,EventHangout);
                EventWedding.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventWedding.setTextColor(Color.parseColor("#ffffff"));
                EventType = "Wedding";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                    EventPicker.setChecked(true);
                    EventPicker.setText(EventType);
                    EventDialog.dismiss();
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });

        EventOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding,EventCeremony,EventParty,EventTravel,EventHangout);
                EventOthers.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventOthers.setTextColor(Color.parseColor("#ffffff"));
                EventType = "Others";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                    EventPicker.setChecked(true);
                    EventPicker.setText(EventType);
                    EventDialog.dismiss();
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });

        EventParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding,EventOthers,EventCeremony,EventTravel,EventHangout);
                EventParty.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventParty.setTextColor(Color.parseColor("#ffffff"));
                EventType = "Party";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                    EventPicker.setChecked(true);
                    EventPicker.setText(EventType);
                    EventDialog.dismiss();
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });

        EventTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding,EventOthers,EventParty,EventCeremony,EventHangout);
                EventTravel.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventTravel.setTextColor(Color.parseColor("#ffffff"));
                EventType = "Travel";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                    EventPicker.setChecked(true);
                    EventPicker.setText(EventType);
                    EventDialog.dismiss();
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });

        EventHangout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding,EventOthers,EventParty,EventCeremony,EventParty);
                EventHangout.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventHangout.setTextColor(Color.parseColor("#ffffff"));
                EventType = "Hangouts";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                    EventPicker.setChecked(true);
                    EventPicker.setText(EventType);
                    EventDialog.dismiss();
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });


    }

    private void SetCheckFalse(Button btn1,Button btn2,Button btn3,Button btn4,Button btn5) {

        btn1.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn1.setTextColor(Color.parseColor("#000000"));
        btn2.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn2.setTextColor(Color.parseColor("#000000"));
        btn3.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn3.setTextColor(Color.parseColor("#000000"));
        btn4.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn4.setTextColor(Color.parseColor("#000000"));
        btn5.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn5.setTextColor(Color.parseColor("#000000"));

    }


    private void PostingStarts() {

        final String TitleValue = CommunityAlbumTitle.getText().toString().trim();
        final String DescriptionValue = CommunityAlbumDescription.getText().toString().trim();

        if (!TextUtils.isEmpty(TitleValue) && !(TextUtils.isEmpty(EventType)&& EventTypeSet && AlbumDateSet && (!TextUtils.isEmpty(AlbumTime)))) {

            SubmitButton.setEnabled(false);
            SetPostImage.setEnabled(false);
            UploadProgress.setVisibility(View.VISIBLE);
            final String pushid = CommunityDatabaseReference.push().getKey();
            final DatabaseReference CommunityPost = CommunityDatabaseReference.child(pushid);
            final Uri DownloadUri = Uri.parse("default");
            PostKey = pushid;
            InUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    CommunityPost.child("title").setValue(TitleValue);
                    CommunityPost.child("description").setValue(DescriptionValue);
                    //CommunityPost.child("coverimage").setValue((DownloadUri).toString());
                    CommunityPost.child("status").setValue("T");
                    CommunityPost.child("type").setValue(EventType);
                    CommunityPost.child("endtime").setValue(GetTimeStamp(AlbumTime));
                    CommunityPost.child("starttime").setValue(ServerValue.TIMESTAMP);
                    CommunityPost.child("admin").setValue(UserID);

                    SharedPreferences CurrentActiveCommunity = getSharedPreferences("CurrentCommunity.pref",Context.MODE_PRIVATE);
                    SharedPreferences.Editor ceditor = CurrentActiveCommunity.edit();
                    ceditor.putString("id",pushid);
                    ceditor.putString("time", String.valueOf(System.currentTimeMillis()));
                    ceditor.commit();

                    participantRef.child(pushid).child(UserID).setValue(ServerValue.TIMESTAMP);

                    PostDatabaseReference.child(pushid).child("title").setValue(TitleValue);

                    InUserReference.child("live_community").setValue(pushid).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {

                                SubmitButton.setEnabled(false);
                                SetPostImage.setEnabled(false);
                                UploadProgress.setVisibility(View.GONE);
                                FirebaseDatabase.getInstance().getReference().child("Users").child(UserID).child("dead_community").removeValue();
                                showDialogue("Successfully created the Cloud-Album",true);

                            }
                            else
                            {
                                showDialogue("Error creating Cloud-Album. Please check your internet connection and try again",false);
                                UploadProgress.setVisibility(View.GONE);
                                SubmitButton.setEnabled(true);
                                SetPostImage.setEnabled(true);
                            }
                        }
                    });


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    showDialogue("Error creating Cloud-Album. Please check your internet " +
                            "connection and try again",false);
                    UploadProgress.setVisibility(View.GONE);
                    SubmitButton.setEnabled(true);
                    SetPostImage.setEnabled(true);

                    Toast.makeText(CreateCloudAlbum.this, "Sorry database error ...please try again", Toast.LENGTH_LONG).show();
                }
            });


           /*
            if(ImageUri==null)
            {


            }
            else
            {

                StorageReference
                        FilePath = PostStorageReference
                        .child("CommunityCoverPhoto")
                        .child(ImageUri.getLastPathSegment());
                FilePath
                        .putFile(ImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                                final Uri DownloadUri = taskSnapshot.getDownloadUrl();
                                final String pushid = CommunityDatabaseReference.push().getKey();
                                PostKey = pushid;
                                final DatabaseReference CommunityPost = CommunityDatabaseReference.child(pushid);
                                final DatabaseReference NewPost = PostDatabaseReference.child(pushid);
                                InUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        CommunityPost.child("title").setValue(TitleValue);
                                        CommunityPost.child("description").setValue(DescriptionValue);
                                        CommunityPost.child("coverimage").setValue((DownloadUri).toString());
                                        CommunityPost.child("status").setValue("T");
                                        CommunityPost.child("type").setValue(EventType);
                                        CommunityPost.child("endtime").setValue(GetTimeStamp(AlbumTime));
                                        CommunityPost.child("starttime").setValue(ServerValue.TIMESTAMP);
                                        CommunityPost.child("participants").child(UserID).setValue("admin");
                                        CommunityPost.child("admin").setValue(UserID);

                                        PostDatabaseReference.child(pushid).child("title").setValue(TitleValue);

                                        InUserReference.child("live_community").setValue(pushid).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful())
                                                {
                                                    SubmitButton.setEnabled(true);
                                                    SetPostImage.setEnabled(true);
                                                }
                                                else
                                                {
                                                    showDialogue("Error creating Cloud-Album. Please check your internet " +
                                                            "connection and try again",false);
                                                    UploadProgress.setVisibility(View.GONE);
                                                    SubmitButton.setEnabled(true);
                                                    SetPostImage.setEnabled(true);
                                                }
                                            }
                                        });



                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        showDialogue("Error creating Cloud-Album. Please check your internet " +
                                                "connection and try again",false);
                                        UploadProgress.setVisibility(View.GONE);
                                        SubmitButton.setEnabled(true);
                                        SetPostImage.setEnabled(true);
                                        Toast.makeText(CreateCloudAlbum.this, "Sorry database error ...please try again", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                     @Override
                                                     public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                                                     }
                                                 }
                ).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.isComplete()){
                                UploadProgress.setVisibility(View.GONE);
                                SubmitButton.setEnabled(true);
                                SetPostImage.setEnabled(true);
                                //CreateSituation();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        showDialogue("Error creating Cloud-Album. Please check your internet " +
                                "connection and try again",false);
                        UploadProgress.setVisibility(View.GONE);
                        SubmitButton.setEnabled(true);
                        SetPostImage.setEnabled(true);
                    }
                });

            }
            */


        }
        else
        {
            Toast.makeText(getApplicationContext(),"Please fill up all the provided fields and " +
                    "continue" +
                    " ", Toast.LENGTH_SHORT).show();
        }
    }

    private String GetTimeStamp(String albumTime) {

        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = (Date)formatter.parse(albumTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long output=date.getTime()/1000L;
        String str=Long.toString(output);
        long timestamp = Long.parseLong(str) * 1000;

        timestamp+=86399000;
        return String.valueOf(timestamp);
    }

    private Boolean checkNumberOfDays(String DateStart, String DateEnd){

        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");
        String inputString1 = DateStart;
        String inputString2 = DateEnd;

        try {
            Date date1 = myFormat.parse(inputString1);
            Date date2 = myFormat.parse(inputString2);
            long diff = date2.getTime() - date1.getTime();
            if(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)>=5){
                return true;
            }else {
                return false;
            }

            }
            catch (ParseException e) {
            e.printStackTrace();
            return false;
             }


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .start(this);
            finish();



        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                ImageUri = result.getUri();
                SetPostImage.setImageURI(ImageUri);
            }
        }
        else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Toast.makeText(getApplicationContext(),"Crop failed. ",Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onBackPressed() {

        if (UploadProgress.isShown())
        {
            Toast.makeText(getApplicationContext(), "Creating your Cloud-Album. Please wait.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            currentUserId  =  firebaseAuth.getCurrentUser().getUid();
            ReadFirebaseData readFirebaseData = new ReadFirebaseData();
            userRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild(FirebaseConstants.COMMUNITIES)) {
                        for (DataSnapshot snapshot : dataSnapshot.child(FirebaseConstants.COMMUNITIES).getChildren()) {
                            userCommunityIdList.add(snapshot.getKey());
                        }

                        Intent mainIntent =  new Intent(CreateCloudAlbum.this, MainActivity.class);
                        mainIntent.putStringArrayListExtra(AppConstants.USERIDLIST, (ArrayList<String>) userCommunityIdList);
                        startActivity(mainIntent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showDialogue(String dialogue,boolean positive){

        // Create Alert using Builder
        if(positive) {
            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                    .setTitle("Cloud-Album creation")
                    .setIcon(R.drawable.ic_check_circle_black_24dp)
                    .setMessage(dialogue)
                    .setCancelable(false)
                    .addButton("    OK    ", -1,  Color.parseColor("#3E3D63"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                            CFAlertDialog.CFAlertActionAlignment.END,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    onBackPressed();
                                }
                            });

            builder.show();
        }else{
            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                    .setTitle("Community creation")
                    .setIcon(R.drawable.ic_cancel_black_24dp)
                    .setMessage(dialogue)
                    .setCancelable(false)
                    .addButton("    OK    ", -1,  Color.parseColor("#3E3D63"), CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                            CFAlertDialog.CFAlertActionAlignment.END, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });


            builder.show();

        }

    }

    public void initiateNotificationService()
    {
        SharedPreferences LastShownNotificationInfo = getSharedPreferences("LastNotification.pref",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = LastShownNotificationInfo.edit();
        editor.putString("time", String.valueOf(System.currentTimeMillis()));
        editor.commit();

        AlarmManagerHelper alarmManagerHelper=new AlarmManagerHelper(getApplicationContext());
        alarmManagerHelper.initiateAlarmManager(5);

    }
}