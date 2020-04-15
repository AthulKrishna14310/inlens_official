package com.integrals.inlens.Helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;

@SuppressLint("ValidFragment")
public class AlbumOptionsBottomSheetFragment extends BottomSheetDialogFragment {
    RelativeLayout scanLayout,createLayout;
    ImageButton scanImageButton,createImageButton;


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
        scanCallback = (IScanCallback) activity;
        createCallback = (ICreateCallback) activity;
        dismissDialog = (IDismissDialog) activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View  albumOptionsView = inflater.inflate(R.layout.album_options_layout, container, false);
        scanLayout = albumOptionsView.findViewById(R.id.option_scan_layout);
        createLayout = albumOptionsView.findViewById(R.id.option_create_layout);
        scanImageButton = albumOptionsView.findViewById(R.id.main_horizontal_scan_button);
        createImageButton = albumOptionsView.findViewById(R.id.main_horizontal_new_album_button);

        scanLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scanCallback.scanQR();
                dismissDialog.dismissDialog();

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
        createImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCallback.createAlbum();
                dismissDialog.dismissDialog();
            }
        });
        return albumOptionsView;
    }
}
