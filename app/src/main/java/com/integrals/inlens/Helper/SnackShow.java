package com.integrals.inlens.Helper;

import android.content.Context;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.integrals.inlens.R;

public class SnackShow {
    private View view;
    private Context context;
    private Snackbar snack;

    public Snackbar getSnack() {
        return snack;
    }

    public void setSnack(Snackbar snack) {
        this.snack = snack;
    }

    public SnackShow(View view, Context context){
        this.view=view;
        this.context=context;
    }
    public void showErrorSnack(String message){
         snack = Snackbar.make(
                view,
                " "+message,
                Snackbar.LENGTH_LONG
        );
        SnackbarHelper.configSnackbar(context, snack,R.drawable.error_snackbar);
        snack.show();
    }
    public void showSuccessSnack(String message){
         snack = Snackbar.make(
                view,
                " "+message,
                Snackbar.LENGTH_LONG
        );
        SnackbarHelper.configSnackbar(context, snack,R.drawable.success_snackbar);
        snack.show();
    }
}
