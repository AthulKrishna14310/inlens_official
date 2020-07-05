package com.integrals.inlens.Helper;

import android.app.Activity;
import android.content.Context;
import android.drm.DrmStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.integrals.inlens.R;

public class CustomToast {
    private Context context;
    private Activity activity;

    public CustomToast(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public  void showToast(String message){
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                (ViewGroup) activity.findViewById(R.id.toast_layout_id));
        // get the reference of TextView and ImageVIew from inflated layout
        TextView toastTextView = (TextView) layout.findViewById(R.id.toastextView);
        ImageView toastImageView = (ImageView) layout.findViewById(R.id.toastImageView);
        // set the text in the TextView
        toastTextView.setText(message);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);// set the duration for the Toast
        toast.setView(layout); // set the inflated layout
        toast.show(); // display the custom Toast

    }
}
