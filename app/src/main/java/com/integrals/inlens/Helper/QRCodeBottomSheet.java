package com.integrals.inlens.Helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.integrals.inlens.Notification.NotificationHelper;
import com.integrals.inlens.R;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("ValidFragment")
public class QRCodeBottomSheet extends BottomSheetDialogFragment {

    Context context;
    String currentActiveCommunityId;
    DatabaseReference linkRef;
    View qrCodeView;
    int themeId;
    boolean initialStart;
    Activity activity;
    public TextView cancelButton;

    public QRCodeBottomSheet(Context context, String id, DatabaseReference linkRef , boolean initialStart, Activity activity) {
        this.context = context;
        currentActiveCommunityId = id;
        this.linkRef = linkRef;
        this.initialStart=initialStart;
        this.activity=activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        String appTheme;
        SharedPreferences themePref = context.getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        if (themePref.contains(AppConstants.appDataPref_theme)) {
            if (themePref.getString(AppConstants.appDataPref_theme, AppConstants.themeLight).equals(AppConstants.themeLight)) {
                themeId = R.style.AppTheme;
                appTheme = AppConstants.themeLight;
                ContextWrapper contextWrapper = new ContextWrapper(context);
                contextWrapper.setTheme(R.style.AppTheme);
                qrCodeView = inflater.cloneInContext(contextWrapper).inflate(R.layout.qrcode_generator_layout, container, false);

            } else {
                themeId = R.style.DarkTheme;
                appTheme = AppConstants.themeDark;
                ContextWrapper contextWrapper = new ContextWrapper(context);
                contextWrapper.setTheme(R.style.DarkTheme);
                qrCodeView = inflater.cloneInContext(contextWrapper).inflate(R.layout.qrcode_generator_layout, container, false);

            }
        } else {
            themeId = R.style.AppTheme;
            appTheme = AppConstants.themeLight;
            ContextWrapper contextWrapper = new ContextWrapper(context);
            contextWrapper.setTheme(R.style.AppTheme);
            qrCodeView = inflater.cloneInContext(contextWrapper).inflate(R.layout.qrcode_generator_layout, container, false);

        }


        Button InviteLinkButton = qrCodeView.findViewById(R.id.InviteLinkButton);
        TextView QRCodeCloseBtn = qrCodeView.findViewById(R.id.cancelButtonTextView);

        final TextView textView = qrCodeView.findViewById(R.id.textViewAlbumQR);
        final ImageView QRCodeImageView = qrCodeView.findViewById(R.id.QR_Display);


        QRCodeCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        final MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        if (!currentActiveCommunityId.equals(AppConstants.NOT_AVALABLE)) {
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(currentActiveCommunityId, BarcodeFormat.QR_CODE, 200, 200);
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

        int cf_bg_color, colorPrimary, cf_alert_dialogue_dim_bg;

        if (appTheme.equals(AppConstants.themeLight)) {
            cf_bg_color = getResources().getColor(R.color.Light_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorLightPrimary);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Light_cf_alert_dialogue_dim_bg);
        } else {
            cf_bg_color = getResources().getColor(R.color.Dark_cf_bg_color);
            colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            cf_alert_dialogue_dim_bg = getResources().getColor(R.color.Dark_cf_alert_dialogue_dim_bg);

        }

        InviteLinkButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {

                CFAlertDialog.Builder builder = new CFAlertDialog.Builder(context,themeId)
                        .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                        .setTitle("Participants Count ?")
                        .setIcon(R.drawable.ic_link)
                        .setDialogBackgroundColor(cf_bg_color)
                        .setTextColor(colorPrimary)
                        .setCancelable(false)
                        .setMessage("Select the number of participants to join via this link.")
                        .setMultiChoiceItems(new String[]{"Only 1", "5", "20", "20+"}, new boolean[]{false, false, false, false}, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int index, boolean b) {

                                Map linkMap = new HashMap();
                                linkMap.put("id", currentActiveCommunityId);

                                if (b) {
                                    if (index == 0) {
                                        dismiss();
                                        Toast.makeText(context, "Sending to 1", Toast.LENGTH_SHORT).show();
                                        dialogInterface.dismiss();
                                        linkMap.put("count", 1);
                                        String link_id = linkRef.push().getKey();
                                        linkRef.child(link_id).setValue(linkMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    shareInviteLink(link_id);
                                                } else {
                                                    showSnackbarMessage(qrCodeView, "Some error occurred. Please try again later.");
                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                showSnackbarMessage(qrCodeView, "Some error occurred. Please try again later.");

                                            }
                                        });

                                    } else if (index == 1) {
                                        dismiss();
                                        Toast.makeText(context, "Sending to 5", Toast.LENGTH_SHORT).show();
                                        dialogInterface.dismiss();
                                        linkMap.put("count", 5);
                                        String link_id = linkRef.push().getKey();
                                        linkRef.child(link_id).setValue(linkMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    shareInviteLink(link_id);
                                                } else {
                                                    showSnackbarMessage(qrCodeView, "Some error occurred. Please try again later.");
                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                showSnackbarMessage(qrCodeView, "Some error occurred. Please try again later.");

                                            }
                                        });

                                    } else if (index == 2) {
                                        dismiss();
                                        Toast.makeText(context, "Sending to 20", Toast.LENGTH_SHORT).show();
                                        dialogInterface.dismiss();
                                        linkMap.put("count", 10);
                                        String link_id = linkRef.push().getKey();
                                        linkRef.child(link_id).setValue(linkMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    shareInviteLink(link_id);
                                                } else {
                                                    showSnackbarMessage(qrCodeView, "Some error occurred. Please try again later.");
                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                showSnackbarMessage(qrCodeView, "Some error occurred. Please try again later.");

                                            }
                                        });
                                    } else {
                                        dismiss();
                                        Toast.makeText(context, "Sending to 20 or more", Toast.LENGTH_SHORT).show();
                                        dialogInterface.dismiss();
                                        linkMap.put("count", "inf");
                                        String link_id = linkRef.push().getKey();
                                        linkRef.child(link_id).setValue(linkMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    shareInviteLink(link_id);
                                                } else {
                                                    showSnackbarMessage(qrCodeView, "Some error occurred. Please try again later.");
                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                showSnackbarMessage(qrCodeView, "Some error occurred. Please try again later.");

                                            }
                                        });

                                    }

                                }
                            }
                        }).addButton("CANCEL",
                                colorPrimary,
                                cf_alert_dialogue_dim_bg,
                                CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        dismiss();
                                    }
                                }

                        );
                builder.show();
            }
        });

        cancelButton=qrCodeView.findViewById(R.id.cancelButtonTextView);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(initialStart){
                    initialStart=false;
                    dismiss();
                    int cf_bg_color,colorPrimary,red_inlens,cf_alert_dialogue_dim_bg;
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

                     CFAlertDialog.Builder builder = new CFAlertDialog.Builder(context)
                            .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                            .setTitle("Take photos now ?")
                            .setDialogBackgroundColor(cf_bg_color)
                            .setTextColor(colorPrimary)
                            .setIcon(R.drawable.ic_photo_camera)
                            .setMessage("Now you can proceed to upload photos to your album.")
                            .setCancelable(false)
                            .addButton("YES",colorPrimary,cf_alert_dialogue_dim_bg, CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                    CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            dialog.cancel();

                                            NotificationHelper notificationHelper = new NotificationHelper(context);
                                            notificationHelper.displayAlbumStartNotification("Open your Gallery", "After taking photos tap here to upload ");

                                            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(activity)
                                                    .setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT)
                                                    .setTitle("After taking photos, tap  on notification ")
                                                    .setDialogBackgroundColor(cf_bg_color)
                                                    .setTextColor(colorPrimary)
                                                    .setIcon(R.drawable.ic_touch_)
                                                    .setMessage("Whenever you take photos, just click the notification to upload it. You can use any camera application for taking photos")
                                                    .setCancelable(false)
                                                    .addButton("OK ,I Understand", colorPrimary,cf_alert_dialogue_dim_bg,
                                                            CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                    dialog.cancel();
                                                                    Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                                                                    activity.startActivity(cameraIntent);
                                                                    activity.finishAffinity();

                                                                }
                                                            }).addButton("CANCEL",red_inlens,cf_alert_dialogue_dim_bg,
                                                            CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                    dialog.cancel();

                                                                }
                                                            });
                                            builder.show();



                                        }
                                    }).addButton("NOT NOW",
                                     red_inlens,
                                     cf_alert_dialogue_dim_bg,
                                     CFAlertDialog.CFAlertActionStyle.DEFAULT,
                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                    dialog.cancel();


                                }
                            });;
                    builder.show();
                }else{
                    dismiss();
                }
            }
        });

        return qrCodeView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) d.findViewById(R.id.qrcode_bottomsheet_wrapper);
                View bottomSheetInternal = d.findViewById(R.id.qrcode_bottomsheet);
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetInternal);
                //bottomSheetBehavior.setHidable(false);
                BottomSheetBehavior.from((View)coordinatorLayout.getParent()).setPeekHeight(bottomSheetInternal.getHeight());
                bottomSheetBehavior.setPeekHeight(bottomSheetInternal.getHeight());
                coordinatorLayout.getParent().requestLayout();

            }
        });
    }

    private void showSnackbarMessage(View rootView, String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }

    private void shareInviteLink(String CommunityID) {

        String url = "https://inlens.page.link/?link=https://inlens.com=" + CommunityID + "&apn=com.integrals.inlens";
        final Intent SharingIntent = new Intent(Intent.ACTION_SEND);
        SharingIntent.setType("text/plain");
        SharingIntent.putExtra(Intent.EXTRA_TEXT, "InLens Community Invite Link \n" + url);
        context.startActivity(SharingIntent);

    }


}
