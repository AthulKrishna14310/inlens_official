package com.integrals.inlens.Helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.integrals.inlens.R;
import com.journeyapps.barcodescanner.BarcodeEncoder;

@SuppressLint("ValidFragment")
public class AlbumOptionsBottomSheetFragment extends BottomSheetDialogFragment {
    Button createLayout;
    RelativeLayout scanLayout;
    ImageButton scanImageButton,createImageButton;
    ImageView imageQR;
    Activity activity;
    FirebaseAuth firebaseAuth;

    public interface IScanCallback
    {
        void scanQR();
    }

    public interface  ICreateCallback
    {
        void createAlbum();
    }

    public interface  IDismissDialog
    {
        void dismissDialog();
    }

    IScanCallback scanCallback;
    ICreateCallback createCallback;
    IDismissDialog dismissDialog;

    public AlbumOptionsBottomSheetFragment(Activity activity) {
        this.activity = activity;
        scanCallback = (IScanCallback) activity;
        createCallback = (ICreateCallback) activity;
        dismissDialog = (IDismissDialog) activity;
        firebaseAuth=FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View  albumOptionsView;

        SharedPreferences themePref = activity.getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        if(themePref.contains(AppConstants.appDataPref_theme))
        {
            if(themePref.getString(AppConstants.appDataPref_theme,AppConstants.themeLight).equals(AppConstants.themeLight))
            {
                ContextWrapper contextWrapper = new ContextWrapper(activity);
                contextWrapper.setTheme(R.style.AppTheme);
                albumOptionsView = inflater.cloneInContext(contextWrapper).inflate(R.layout.album_options_layout, container, false);

            }
            else
            {
                ContextWrapper contextWrapper = new ContextWrapper(activity);
                contextWrapper.setTheme(R.style.DarkTheme);
                albumOptionsView = inflater.cloneInContext(contextWrapper).inflate(R.layout.album_options_layout, container, false);

            }
        }
        else
        {
            ContextWrapper contextWrapper = new ContextWrapper(activity);
            contextWrapper.setTheme(R.style.AppTheme);
            albumOptionsView = inflater.cloneInContext(contextWrapper).inflate(R.layout.album_options_layout, container, false);

        }

        scanLayout = albumOptionsView.findViewById(R.id.option_scan_layout);
        createLayout = albumOptionsView.findViewById(R.id.option_create_layout);
        scanImageButton = albumOptionsView.findViewById(R.id.main_horizontal_scan_button);
        imageQR=albumOptionsView.findViewById(R.id.QR_Display);

        scanLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //scanCallback.scanQR();
                //dismissDialog.dismissDialog();

            }
        });
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCallback.createAlbum();
                dismissDialog.dismissDialog();
            }
        });
        scanImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCallback.scanQR();
                dismissDialog.dismissDialog();
            }
        });

        final MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(firebaseAuth.getCurrentUser().getUid(), BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageQR.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }


        return albumOptionsView;

    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) d.findViewById(R.id.album_options_bottomsheet_wrapper);
                View bottomSheetInternal = d.findViewById(R.id.album_options_bottomsheet_cardview);
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetInternal);
                BottomSheetBehavior.from((View)coordinatorLayout.getParent()).setPeekHeight(bottomSheetInternal.getHeight());
                bottomSheetBehavior.setPeekHeight(bottomSheetInternal.getHeight());
                coordinatorLayout.getParent().requestLayout();
            }
        });
    }
}
