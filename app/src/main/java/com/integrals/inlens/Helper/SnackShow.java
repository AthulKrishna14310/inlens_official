package com.integrals.inlens.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.integrals.inlens.R;

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
