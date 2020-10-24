package com.integrals.inlens.Helper;

import android.app.Activity;
import android.view.View;

import com.google.android.material.snackbar.BaseTransientBottomBar;
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
        Snackbar.make(view,message, Snackbar.LENGTH_LONG).show();

    }
    public void showSuccessSnack(String message){
        Snackbar.make(view,message, Snackbar.LENGTH_LONG).show();

    }
    public void showInfoSnack(String message){
        Snackbar.make(view,message, Snackbar.LENGTH_LONG).show();

    }
}
