package com.integrals.inlens.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.integrals.inlens.Helper.AppConstants;
import com.integrals.inlens.Helper.FirebaseConstants;
import com.integrals.inlens.Helper.PreOperationCheck;
import com.integrals.inlens.MainActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.R;


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
            }
            else
            {
                setTheme(R.style.DarkTheme);

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

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                month = month + 1;
                if (month < 10) {
                    albumTime = day + "-" + "0" + month + "-" + year;
                    if (!checkNumberOfDays(checkTimeTaken, albumTime)) {
                        albumDateSet = true;
                        dateofCompletionCheckbox.setChecked(true);
                        dateofCompletionCheckbox.setText(albumTime);
                        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(CreateCloudAlbum.this)
                                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                                .setTitle("Expiry :" + albumTime)
                                .setDialogBackgroundColor(cf_bg_color)
                                .setTextColor(colorPrimary)
                                .setIcon(R.drawable.ic_access_time_black_24dp)
                                .setMessage("You can only upload images and add participants to this album till " + albumTime)
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

                } else {
                    albumTime = day + "-" + month + "-" + year;
                    if (!checkNumberOfDays(checkTimeTaken, albumTime)) {
                        albumDateSet = true;
                        dateofCompletionCheckbox.setChecked(true);

                        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(CreateCloudAlbum.this)
                                .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                                .setTitle("Album expiry on " + albumTime)
                                .setDialogBackgroundColor(cf_bg_color)
                                .setTextColor(colorPrimary)
                                .setIcon(R.drawable.ic_access_time_black_24dp)
                                .setMessage("You can only upload images and add participants to this album till " + albumTime)
                                .setCancelable(false)
                                .addButton("    OK , I understand   ",
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
        };


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventTypeSet && albumDateSet) {
                    if (!new PreOperationCheck().checkInternetConnectivity(getApplicationContext())) {
                        showDialogue("No Internet. Please check your internet " +
                                "connection and try again", false);

                    } else {
                        uploadNewAlbumData();
                    }
                } else {
                    showDialogue("Please fill up all the provided fields and continue.",false);
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

        EventTypeDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(eventType)) {
                    eventDialog.dismiss();
                } else {
                    Snackbar.make(rootCreateCloudAlbum,"Please select an event type.",Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        EventCeremony.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding, EventOthers, EventParty, EventTravel, EventHangout);
                EventCeremony.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventCeremony.setTextColor(Color.parseColor("#ffffff"));
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
            }
        });

        EventWedding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventCeremony, EventOthers, EventParty, EventTravel, EventHangout);
                EventWedding.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventWedding.setTextColor(Color.parseColor("#ffffff"));
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
            }
        });

        EventOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding, EventCeremony, EventParty, EventTravel, EventHangout);
                EventOthers.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventOthers.setTextColor(Color.parseColor("#ffffff"));
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
            }
        });

        EventParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding, EventOthers, EventCeremony, EventTravel, EventHangout);
                EventParty.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventParty.setTextColor(Color.parseColor("#ffffff"));
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
            }
        });

        EventTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding, EventOthers, EventParty, EventCeremony, EventHangout);
                EventTravel.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventTravel.setTextColor(Color.parseColor("#ffffff"));
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
            }
        });

        EventHangout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding, EventOthers, EventParty, EventCeremony, EventTravel);
                EventHangout.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventHangout.setTextColor(Color.parseColor("#ffffff"));
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
            }
        });


    }

    private void SetCheckFalse(Button btn1, Button btn2, Button btn3, Button btn4, Button btn5) {

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


    private void uploadNewAlbumData() {

        final String titleValue = albumTitleEditText.getText().toString().trim();
        final String descriptionValue = albumDescEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(titleValue) && !(TextUtils.isEmpty(eventType) && eventTypeSet && albumDateSet && (!TextUtils.isEmpty(albumTime)))) {

            submitButton.setEnabled(false);
            uploadProgressbar.setVisibility(View.VISIBLE);
            final String newCommunityId = communityRef.push().getKey();
            globalID=newCommunityId;

            Map communitymap =  new HashMap();
            communitymap.put(FirebaseConstants.COMMUNITYTITLE,titleValue);
            communitymap.put(FirebaseConstants.COMMUNITYDESC,descriptionValue);
            communitymap.put(FirebaseConstants.COMMUNITYSTATUS,"T");
            communitymap.put(FirebaseConstants.COMMUNITYTYPE,eventType);
            communitymap.put(FirebaseConstants.COMMUNITYENDTIME, getOffsetDeletedTime(getTimeStamp(albumTime)));
            communitymap.put(FirebaseConstants.COMMUNITYSTARTTIME,getOffsetDeletedTime(String.valueOf(System.currentTimeMillis())));
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
                        ceditor.putString("stopAt", getOffsetDeletedTime(getTimeStamp(albumTime)));
                        ceditor.putInt("notiCount", 0);
                        ceditor.remove(AppConstants.IS_NOTIFIED);
                        ceditor.commit();


                        photographerRef.child(newCommunityId).child(currentUserId).setValue(ServerValue.TIMESTAMP);
                        currentUserRef.child(FirebaseConstants.COMMUNITIES).child(newCommunityId).setValue(getOffsetDeletedTime(String.valueOf(System.currentTimeMillis())));
                        currentUserRef.child(FirebaseConstants.LIVECOMMUNITYID).setValue(newCommunityId);
                        submitButton.setEnabled(false);
                        uploadProgressbar.setVisibility(View.GONE);
                        userCommunityIdList.add(newCommunityId);
                        showDialogue("Successfully created the Cloud-Album", true);

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
                    }
                    else
                    {
                        showDialogue("Unable to connect and create new album right now. Please try again.", false);
                        uploadProgressbar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    showDialogue("Unable to connect and create new album right now. Please try again.", false);
                    uploadProgressbar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                }
            });



        } else {
            showDialogue("Please fill up all the provided fields and continue.",false);
        }
    }

    private String getOffsetDeletedTime(String timeStamp) {
        TimeZone timeZone = TimeZone.getDefault();
        long offsetInMillis = timeZone.getOffset(Calendar.ZONE_OFFSET);
        long givenTime = Long.parseLong(timeStamp);
        long offsetDeletedTime = givenTime-offsetInMillis;
        return String.valueOf(offsetDeletedTime);
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
            Snackbar.make(rootCreateCloudAlbum,"Creating your cloud  album. Please wait.",Snackbar.LENGTH_SHORT).show();
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
            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                    .setTitle("Cloud-Album creation")
                    .setIcon(R.drawable.ic_check_circle_black_24dp)
                    .setDialogBackgroundColor(cf_bg_color)
                    .setTextColor(colorPrimary)
                    .setMessage(dialogue)
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
                                    createdIntent="YES";
                                    onBackPressed();
                                }
                            });

            builder.show();
        } else {
            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                    .setTitle("Community creation")
                    .setIcon(R.drawable.ic_info)
                    .setDialogBackgroundColor(cf_bg_color)
                    .setTextColor(colorPrimary)
                    .setMessage(dialogue)
                    .setCancelable(false)
                    .addButton("OK", colorPrimary,
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

}