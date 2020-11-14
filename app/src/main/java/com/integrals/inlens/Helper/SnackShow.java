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
        String appTheme;

        SharedPreferences appDataPref = context.getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        if(appDataPref.contains(AppConstants.appDataPref_theme))
        {
            appTheme = appDataPref.getString(AppConstants.appDataPref_theme,AppConstants.themeLight);
            if(appTheme.equals(AppConstants.themeLight))
            {
                Snackbar.make(view,message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(context.getColor(R.color.Dark_image_bg_color))
                        .setText(message)
                        .setTextColor(context.getColor(R.color.Dark_red_cancel_icon))

                        .show();

            }
            else  if(appTheme.equals(AppConstants.themeDark))
            {
                Snackbar.make(view,message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(context.getColor(R.color.colorDarkPrimary))
                        .setText(message)
                        .setTextColor(context.getColor(R.color.quantum_googred700))
                        .show();

            }
        }




    }
    public void showSuccessSnack(String message){
        String appTheme;

        SharedPreferences appDataPref = context.getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        if(appDataPref.contains(AppConstants.appDataPref_theme))
        {
            appTheme = appDataPref.getString(AppConstants.appDataPref_theme,AppConstants.themeLight);
            if(appTheme.equals(AppConstants.themeLight))
            {
                Snackbar.make(view,message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(context.getColor(R.color.Dark_image_bg_color))
                        .setText(message)
                        .setTextColor(context.getColor(R.color.colorDarkPrimary))
                        .show();
            }
            else  if(appTheme.equals(AppConstants.themeDark))
            {
                Snackbar.make(view,message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(context.getColor(R.color.colorDarkPrimary))
                        .setText(message)
                        .setTextColor(context.getColor(R.color.colorLightPrimary))
                        .show();

            }
        }



    }
    public void showInfoSnack(String message){
        String appTheme;

        SharedPreferences appDataPref = context.getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        if(appDataPref.contains(AppConstants.appDataPref_theme))
        {
            appTheme = appDataPref.getString(AppConstants.appDataPref_theme,AppConstants.themeLight);
            if(appTheme.equals(AppConstants.themeLight))
            {
                Snackbar.make(view,message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(context.getColor(R.color.Dark_image_bg_color))
                        .setText(message)
                        .setTextColor(context.getColor(R.color.colorDarkPrimary))
                        .show();

            }
            else  if(appTheme.equals(AppConstants.themeDark))
            {
                Snackbar.make(view,message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(context.getColor(R.color.colorDarkPrimary))
                        .setText(message)
                        .setTextColor(context.getColor(R.color.colorLightPrimary))
                        .show();

            }
        }
    }
}
