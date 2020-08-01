package com.integrals.inlens.Helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.integrals.inlens.Activities.QRCodeReader;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class QRCodeBottomSheet extends BottomSheetDialogFragment {

    String currentActiveCommunityId;
    DatabaseReference linkRef;
    View qrCodeView;
    int themeId;
    boolean initialStart;
    MainActivity activity;
    public TextView cancelButton;
    View rootView;

    public QRCodeBottomSheet(View rootView,String id, DatabaseReference linkRef, boolean initialStart, MainActivity activity, boolean isAdmin) {
        this.rootView=rootView;
        currentActiveCommunityId = id;
        this.linkRef = linkRef;
        this.initialStart=initialStart;
        this.activity=activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        String appTheme;
        SharedPreferences themePref = activity.getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        if (themePref.contains(AppConstants.appDataPref_theme)) {
            if (themePref.getString(AppConstants.appDataPref_theme, AppConstants.themeLight).equals(AppConstants.themeLight)) {
                themeId = R.style.AppTheme;
                appTheme = AppConstants.themeLight;
                ContextWrapper contextWrapper = new ContextWrapper(activity);
                contextWrapper.setTheme(R.style.AppTheme);
                qrCodeView = inflater.cloneInContext(contextWrapper).inflate(R.layout.qrcode_generator_layout, container, false);

            } else {
                themeId = R.style.DarkTheme;
                appTheme = AppConstants.themeDark;
                ContextWrapper contextWrapper = new ContextWrapper(activity);
                contextWrapper.setTheme(R.style.DarkTheme);
                qrCodeView = inflater.cloneInContext(contextWrapper).inflate(R.layout.qrcode_generator_layout, container, false);

            }
        } else {
            themeId = R.style.AppTheme;
            appTheme = AppConstants.themeLight;
            ContextWrapper contextWrapper = new ContextWrapper(activity);
            contextWrapper.setTheme(R.style.AppTheme);
            qrCodeView = inflater.cloneInContext(contextWrapper).inflate(R.layout.qrcode_generator_layout, container, false);

        }


        Button InviteLinkButton = qrCodeView.findViewById(R.id.InviteLinkButton);
        TextView QRCodeCloseBtn = qrCodeView.findViewById(R.id.cancelButton);

        qrCodeView.findViewById(R.id.ScanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                startActivity(new Intent(activity, QRCodeReader.class).putStringArrayListExtra(AppConstants.USER_ID_LIST, (ArrayList<String>)activity.getUserCommunityIdList()));
            }
        });
        QRCodeCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        InviteLinkButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                dismiss();
                showSnackbarMessage(rootView,"Preparing community");
                shareInviteLink(currentActiveCommunityId);


            }
        });

        cancelButton=qrCodeView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             dismiss();
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
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }

    private void shareInviteLink(String CommunityID) {

        String url = "https://inlens.page.link/?link=https://inlens.com=" + CommunityID + "&apn=com.integrals.inlens";
        final Intent SharingIntent = new Intent(Intent.ACTION_SEND);
        SharingIntent.setType("text/plain");
        SharingIntent.putExtra(Intent.EXTRA_TEXT, "InLens Album \n" + url);
        activity.startActivity(SharingIntent);

    }


}
