package com.integrals.inlens.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.integrals.inlens.Database.UploadQueueDB;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.Helper.SnackShow;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class CreateCloudAlbum extends AppCompatActivity {
    private EditText albumTitleEditText,albumDescEditText;
    private TextView submitButton;
    private StorageReference storageReference;
    private ProgressBar uploadProgressbar;
    private CheckBox dateofCompletionCheckbox;
    private String albumTime;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private CheckBox eventPickerCheckbox;
    private Dialog eventDialog;
    private String eventType = "";
    private String checkTimeTaken = "";
    private ImageButton createCloudAlbumBackButton;
    private Boolean eventTypeSet = false, albumDateSet = false;
    private String createdIntent="NO";

    FirebaseAuth firebaseAuth;
    DatabaseReference photographerRef, currentUserRef, communityRef;
    List<String> userCommunityIdList;
    String currentUserId;
    LinearLayout rootCreateCloudAlbum;
    private String globalID="";

    public CreateCloudAlbum() {
    }


    String appTheme="";
    int cf_bg_color,colorPrimary,red_inlens,cf_alert_dialogue_dim_bg;

    public interface ProvideOptionCallback
    {
        void provideQueueOption(View v);
    }

    ProvideOptionCallback provideOptionCallback;

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

        if(appTheme.equals(AppConstants.themeLight))
        {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            red_inlens =  getResources().getColor(R.color.Light_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);

        }
        else
        {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            red_inlens =  getResources().getColor(R.color.Dark_red_inlens);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_cloud_album_layout);

        provideOptionCallback = (ProvideOptionCallback) this;

        rootCreateCloudAlbum = findViewById(R.id.rootCreateCloudAlbum);
        userCommunityIdList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(currentUserId);
        photographerRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PARTICIPANTS);
        communityRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.COMMUNITIES);

        userCommunityIdList = getIntent().getExtras().getStringArrayList(AppConstants.USER_ID_LIST);
        EventDialogInit();

        eventPickerCheckbox = findViewById(R.id.EventTypeText);
        albumTitleEditText = (EditText) findViewById(R.id.AlbumTitleEditText);
        albumDescEditText = (EditText) findViewById(R.id.AlbumDescriptionEditText);
        submitButton = (TextView) findViewById(R.id.DoneButtonTextView);
        uploadProgressbar = (ProgressBar) findViewById(R.id.UploadProgress);
        createCloudAlbumBackButton = findViewById(R.id.create_cloud_album_backbutton);

        storageReference = FirebaseStorage.getInstance().getReference();

        Calendar calender = Calendar.getInstance();
        dateofCompletionCheckbox = findViewById(R.id.TimeEditText);


        int Month = calender.get(Calendar.MONTH);
        Month++;
        checkTimeTaken = calender.get(Calendar.DAY_OF_MONTH) + "-" + Month + "-" + calender.get(Calendar.YEAR);

        dateofCompletionCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        CreateCloudAlbum.this,
                        dateSetListener,
                        year, month, day
                );
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 4);
                dialog.setOnCancelListener(dialogInterface -> {
                    albumTime="";
                    dateofCompletionCheckbox.setChecked(false);
                    albumDateSet=false;
                    dateofCompletionCheckbox.setText("Expiry date");
                    TextView textView=findViewById(R.id.expiry_txt);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("Please select an album expiry date for your album.");

                });
                dialog.show();
            }
        });

        dateSetListener = (datePicker, year, month, day) -> {

            month = month + 1;
            if (month < 10) {
                albumTime = day + "-" + "0" + month + "-" + year;
                if (!checkNumberOfDays(checkTimeTaken, albumTime)) {
                    albumDateSet = true;
                    dateofCompletionCheckbox.setChecked(true);
                    dateofCompletionCheckbox.setText(albumTime);
                    TextView textView=findViewById(R.id.expiry_txt);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("You can only upload images and add participants to this album till " + albumTime);
                }

            } else {
                albumTime = day + "-" + month + "-" + year;
                if (!checkNumberOfDays(checkTimeTaken, albumTime)) {
                    albumDateSet = true;
                    dateofCompletionCheckbox.setChecked(true);

                    TextView textView=findViewById(R.id.expiry_txt);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("You can only upload images and add participants to this album till " + albumTime);


                }
            }


        };


        submitButton.setOnClickListener(v -> {

            Cursor cursor = new UploadQueueDB(CreateCloudAlbum.this).getQueuedData();
            if(cursor.getCount()>0)
            {
                provideOptionCallback.provideQueueOption(rootCreateCloudAlbum);
            }
            else
            {
                if (eventTypeSet && albumDateSet) {
                    if (!new PreOperationCheck().checkInternetConnectivity(getApplicationContext())) {
                        showDialogue("No Internet. Please check your internet connection and try again", false);

                    } else {
                        uploadNewAlbumData();
                    }
                } else {
                    SnackShow snackShow=new SnackShow(rootCreateCloudAlbum,this);
                    snackShow.showErrorSnack("Please fill all the fields to create your Cloud-Album");
                }
            }


        });

        eventPickerCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventPickerCheckbox.setChecked(false);
                eventDialog.show();

            }
        });


        createCloudAlbumBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });
        findViewById(R.id.date_range_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateofCompletionCheckbox.setChecked(false);
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        CreateCloudAlbum.this,
                        dateSetListener,
                        year, month, day
                );
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 4);
                dialog.show();
            }
        });
        findViewById(R.id.event_type_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventPickerCheckbox.setChecked(false);
                eventDialog.show();
            }
        });
    }

    private void EventDialogInit() {

        eventDialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar);
        eventDialog.setCancelable(true);
        eventDialog.setCanceledOnTouchOutside(false);
        eventDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        eventDialog.setContentView(R.layout.event_type_layout);
        eventDialog.getWindow().getAttributes().windowAnimations = R.style.BottomUpSlideDialogAnimation;

        Window EventDialogwindow = eventDialog.getWindow();
        EventDialogwindow.setGravity(Gravity.BOTTOM);
        EventDialogwindow.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        EventDialogwindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        EventDialogwindow.setDimAmount(0.75f);

        final Button EventWedding = eventDialog.findViewById(R.id.event_type_wedding_btn);
        final Button EventCeremony = eventDialog.findViewById(R.id.event_type_ceremony_btn);
        final Button EventOthers = eventDialog.findViewById(R.id.event_type_others_btn);
        final Button EventParty = eventDialog.findViewById(R.id.event_type_party_btn);
        final Button EventTravel = eventDialog.findViewById(R.id.event_type_travel_btn);
        final Button EventHangout = eventDialog.findViewById(R.id.event_type_hangouts_btn);
        final TextView SelectedEvent = eventDialog.findViewById(R.id.selected_event_type);

        SelectedEvent.setText("Selected Event Type : " + eventType);

        final ImageButton EventTypeDone = eventDialog.findViewById(R.id.event_done_btn);

        if (!TextUtils.isEmpty(eventType)) {
            EventTypeDone.setVisibility(View.VISIBLE);
        } else {
            EventTypeDone.setVisibility(View.INVISIBLE);

        }

        EventTypeDone.setOnClickListener(view -> {

            if (!TextUtils.isEmpty(eventType)) {
                eventDialog.dismiss();
            } else {
            SnackShow snackShow=new SnackShow(rootCreateCloudAlbum,this);
            snackShow.showErrorSnack("Please select an event type.");
            }

        });

        EventCeremony.setOnClickListener(view -> {

            SetCheckFalse(EventWedding, EventOthers, EventParty, EventTravel, EventHangout);
            EventCeremony.setBackgroundResource(R.drawable.radiobutton_pressed);
            EventCeremony.setTextColor(cf_bg_color);
            eventType = "Ceremony";
            SelectedEvent.setText("Selected Event Type : " + eventType);
            if (!TextUtils.isEmpty(eventType)) {
                EventTypeDone.setVisibility(View.VISIBLE);
                eventTypeSet = true;
                eventPickerCheckbox.setChecked(true);
                eventPickerCheckbox.setText(eventType);
                eventDialog.dismiss();
            } else {
                EventTypeDone.setVisibility(View.GONE);

            }
        });

        EventWedding.setOnClickListener(view -> {

            SetCheckFalse(EventCeremony, EventOthers, EventParty, EventTravel, EventHangout);
            EventWedding.setBackgroundResource(R.drawable.radiobutton_pressed);
            EventWedding.setTextColor(cf_bg_color);
            eventType = "Wedding";
            SelectedEvent.setText("Selected Event Type : " + eventType);
            if (!TextUtils.isEmpty(eventType)) {
                EventTypeDone.setVisibility(View.VISIBLE);
                eventTypeSet = true;
                eventPickerCheckbox.setChecked(true);
                eventPickerCheckbox.setText(eventType);
                eventDialog.dismiss();
            } else {
                EventTypeDone.setVisibility(View.GONE);

            }
        });

        EventOthers.setOnClickListener(view -> {

            SetCheckFalse(EventWedding, EventCeremony, EventParty, EventTravel, EventHangout);
            EventOthers.setBackgroundResource(R.drawable.radiobutton_pressed);
            EventOthers.setTextColor(cf_bg_color);
            eventType = "Others";
            SelectedEvent.setText("Selected Event Type : " + eventType);
            if (!TextUtils.isEmpty(eventType)) {
                EventTypeDone.setVisibility(View.VISIBLE);
                eventTypeSet = true;
                eventPickerCheckbox.setChecked(true);
                eventPickerCheckbox.setText(eventType);
                eventDialog.dismiss();
            } else {
                EventTypeDone.setVisibility(View.GONE);

            }
        });

        EventParty.setOnClickListener(view -> {

            SetCheckFalse(EventWedding, EventOthers, EventCeremony, EventTravel, EventHangout);
            EventParty.setBackgroundResource(R.drawable.radiobutton_pressed);
            EventParty.setTextColor(cf_bg_color);
            eventType = "Party";
            SelectedEvent.setText("Selected Event Type : " + eventType);
            if (!TextUtils.isEmpty(eventType)) {
                EventTypeDone.setVisibility(View.VISIBLE);
                eventTypeSet = true;
                eventPickerCheckbox.setChecked(true);
                eventPickerCheckbox.setText(eventType);
                eventDialog.dismiss();
            } else {
                EventTypeDone.setVisibility(View.GONE);

            }
        });

        EventTravel.setOnClickListener(view -> {

            SetCheckFalse(EventWedding, EventOthers, EventParty, EventCeremony, EventHangout);
            EventTravel.setBackgroundResource(R.drawable.radiobutton_pressed);
            EventTravel.setTextColor(cf_bg_color);
            eventType = "Travel";
            SelectedEvent.setText("Selected Event Type : " + eventType);
            if (!TextUtils.isEmpty(eventType)) {
                EventTypeDone.setVisibility(View.VISIBLE);
                eventTypeSet = true;
                eventPickerCheckbox.setChecked(true);
                eventPickerCheckbox.setText(eventType);
                eventDialog.dismiss();
            } else {
                EventTypeDone.setVisibility(View.GONE);

            }
        });

        EventHangout.setOnClickListener(view -> {

            SetCheckFalse(EventWedding, EventOthers, EventParty, EventCeremony, EventTravel);
            EventHangout.setBackgroundResource(R.drawable.radiobutton_pressed);
            EventHangout.setTextColor(cf_bg_color);
            eventType = "Hangouts";
            SelectedEvent.setText("Selected Event Type : " + eventType);
            if (!TextUtils.isEmpty(eventType)) {
                EventTypeDone.setVisibility(View.VISIBLE);
                eventTypeSet = true;
                eventPickerCheckbox.setChecked(true);
                eventPickerCheckbox.setText(eventType);
                eventDialog.dismiss();
            } else {
                EventTypeDone.setVisibility(View.GONE);

            }
        });


    }

    private void SetCheckFalse(Button btn1, Button btn2, Button btn3, Button btn4, Button btn5) {

        btn1.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn1.setTextColor(colorPrimary);
        btn2.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn2.setTextColor(colorPrimary);
        btn3.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn3.setTextColor(colorPrimary);
        btn4.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn4.setTextColor(colorPrimary);
        btn5.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn5.setTextColor(colorPrimary);

    }


    private void uploadNewAlbumData() {

        final String titleValue = albumTitleEditText.getText().toString().trim();
        final String descriptionValue = albumDescEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(titleValue) && !(TextUtils.isEmpty(eventType) && eventTypeSet && albumDateSet && (!TextUtils.isEmpty(albumTime)))) {


            final String newCommunityId = communityRef.push().getKey();

            Map communitymap =  new HashMap();
            submitButton.setEnabled(false);
            uploadProgressbar.setVisibility(View.VISIBLE);
            globalID=newCommunityId;
            communitymap.put(FirebaseConstants.COMMUNITYTITLE,titleValue);
            communitymap.put(FirebaseConstants.COMMUNITYDESC,descriptionValue);
            communitymap.put(FirebaseConstants.COMMUNITYSTATUS,"T");
            communitymap.put(FirebaseConstants.COMMUNITYTYPE,eventType);
            communitymap.put(FirebaseConstants.COMMUNITYENDTIME, getTimeStamp(albumTime));
            communitymap.put(FirebaseConstants.COMMUNITYSTARTTIME,ServerValue.TIMESTAMP);
            communitymap.put(FirebaseConstants.COMMUNITYADMIN,currentUserId);
            communityRef.child(newCommunityId).setValue(communitymap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        SharedPreferences CurrentActiveCommunity = getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF, Context.MODE_PRIVATE);
                        SharedPreferences.Editor ceditor = CurrentActiveCommunity.edit();
                        ceditor.putString("id", newCommunityId);
                        ceditor.putString("time", String.valueOf(System.currentTimeMillis()));
                        ceditor.putString("stopAt", getTimeStamp(albumTime));
                        ceditor.putString("startAt", String.valueOf(System.currentTimeMillis()));
                        ceditor.putInt("notiCount", 0);
                        ceditor.remove(AppConstants.IS_NOTIFIED);
                        ceditor.commit();

                        //drop table and create new one
                        new UploadQueueDB(CreateCloudAlbum.this).deleteAllData();

                        photographerRef.child(newCommunityId).child(currentUserId).setValue(ServerValue.TIMESTAMP);
                        currentUserRef.child(FirebaseConstants.COMMUNITIES).child(newCommunityId).setValue(String.valueOf(System.currentTimeMillis()));
                        currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).setValue(newCommunityId);
                        submitButton.setEnabled(false);
                        uploadProgressbar.setVisibility(View.GONE);
                        userCommunityIdList.add(newCommunityId);

                        final long dy = TimeUnit.MILLISECONDS.toDays(Long.parseLong(getTimeStamp(albumTime))-System.currentTimeMillis());
                        final long hr = TimeUnit.MILLISECONDS.toHours(Long.parseLong(getTimeStamp(albumTime))-System.currentTimeMillis())
                                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(Long.parseLong(getTimeStamp(albumTime))-System.currentTimeMillis()));
                        final long min = TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(getTimeStamp(albumTime))-System.currentTimeMillis())
                                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(Long.parseLong(getTimeStamp(albumTime))-System.currentTimeMillis()));

                        NotificationHelper helper = new NotificationHelper(getApplicationContext());
                        String notificationStr = "";
                        if (titleValue.length() >15)
                        {
                            notificationStr+=titleValue.substring(0,15)+"...";
                        }
                        else
                        {
                            notificationStr+=titleValue;
                        }
                        if(dy>0)
                        {
                            notificationStr+=", "+ (int) dy +" days";
                        }
                        else
                        {
                            notificationStr+=",";
                        }
                        if(hr>0)
                        {
                            notificationStr+=" "+(int)hr+" hrs left";
                        }
                        if(hr<1 && dy<1)
                        {
                            notificationStr+=" "+(int)min+" minutes left";
                        }
                        helper.displayAlbumStartNotification(notificationStr,"You are active in this Cloud-Album till "+ albumTime);

                        Handler handler = new Handler();
                        SnackShow snackShow=new SnackShow(rootCreateCloudAlbum,CreateCloudAlbum.this);
                        snackShow.showSuccessSnack("Your Cloud-Album created successfully. Enjoy your event by uploading moments together.");
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                createdIntent="YES";
                                onBackPressed();
                            }
                        }, 3000);

                    }
                    else
                    {
                        uploadProgressbar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);



                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    uploadProgressbar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    SnackShow snackShow=new SnackShow(rootCreateCloudAlbum,CreateCloudAlbum.this);
                    snackShow.showErrorSnack("Some error occurred, Please try again "+e.getMessage());

                }
            });

        } else {
            uploadProgressbar.setVisibility(View.GONE);
            submitButton.setEnabled(true);

        }
    }



    private String getTimeStamp(String albumTime) {

        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = (Date) formatter.parse(albumTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long output = date.getTime() / 1000L;
        String str = Long.toString(output);
        long timestamp = Long.parseLong(str) * 1000;

        timestamp += 86399000;
        return String.valueOf(timestamp);
    }

    private Boolean checkNumberOfDays(String DateStart, String DateEnd) {

        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");
        String inputString1 = DateStart;
        String inputString2 = DateEnd;

        try {
            Date date1 = myFormat.parse(inputString1);
            Date date2 = myFormat.parse(inputString2);
            long diff = date2.getTime() - date1.getTime();
            if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) >= 5) {
                return true;
            } else {
                return false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }


    }


    @Override
    public void onBackPressed() {

        if (uploadProgressbar.isShown()) {
            SnackShow snackShow=new SnackShow(rootCreateCloudAlbum,this);
            snackShow.showErrorSnack("Album creation on progress. Please wait...");
        } else {

            Intent mainIntent = new Intent(CreateCloudAlbum.this, MainActivity.class);
            mainIntent.putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>) userCommunityIdList);
            mainIntent.putExtra("CREATED",createdIntent);
            mainIntent.putExtra("ID",globalID);
            startActivity(mainIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }


    public void showDialogue(String dialogue, boolean positive) {

        // Create Alert using Builder
        if (positive) {
            SnackShow snackShow=new SnackShow(rootCreateCloudAlbum,this);
            snackShow.showSuccessSnack(dialogue);

        } else {
        SnackShow snackShow=new SnackShow(rootCreateCloudAlbum,this);
        snackShow.showErrorSnack(dialogue);
        }

    }

}