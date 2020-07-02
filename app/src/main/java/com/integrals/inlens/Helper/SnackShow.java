package com.integrals.inlens.Helper;

import android.app.Activity;
import android.view.View;

import com.ankushyerwar.floatingsnackbar.SnackBar;
import com.google.android.material.snackbar.Snackbar;
import com.integrals.inlens.R;


public class SnackShow {
    private View view;
    private Activity context;
    private Snackbar snack;

    public Snackbar getSnack() {
        return snack;
    }

    public void setSnack(Snackbar snack) {
        this.snack = snack;
    }

    public SnackShow(View view, Activity context){
        this.view=view;
        this.context=context;
    }
    public void showErrorSnack(String message){
        SnackBar.error(view, message, SnackBar.LENGTH_LONG).show();
    }
    public void showSuccessSnack(String message){
        SnackBar.success(view, message, SnackBar.LENGTH_LONG).show();

    }
    public void showInfoSnack(String message){
        SnackBar.info(view,message, SnackBar.LENGTH_LONG).show();
    }
}
