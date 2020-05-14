package com.integrals.inlens.Helper;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integrals.inlens.Models.CommunityModel;
import com.integrals.inlens.R;

@SuppressLint("ValidFragment")

public class PhotographerFragementBottomSheetNA extends BottomSheetDialogFragment {

    private View view;

    public PhotographerFragementBottomSheetNA() {
        super();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.photographer_display_layout_not_admin, container, false);

        return view;
    }
}


