package com.integrals.inlens.Helper;

import android.app.Activity;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import de.mateware.snacky.Snacky;


public class SnackShow {
    private View view;
    private Activity context;

    public SnackShow(View view, Activity context){
        this.view=view;
        this.context=context;
    }
    public void showErrorSnack(String message){
        Snacky.builder()
                .setActivity(context)
                .error().setText(message)
                .show();
    }
    public void showSuccessSnack(String message){
        Snacky.builder()
                .setActivity(context)
                .setMaxLines(2)
                .success().setText(message)
                .show();

    }
    public void showInfoSnack(String message){
        Snacky.builder()
                .setActivity(context)
                .info().setText(message)
                .setDuration(Snackbar.LENGTH_LONG)
                .show();

    }
}
