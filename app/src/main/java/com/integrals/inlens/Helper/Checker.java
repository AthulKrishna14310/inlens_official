package com.integrals.inlens.Helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Checker {
    private Context context;

    public Checker(Context context) {
        this.context = context;
    }
    public boolean isConnectedToNet() {

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;


    }
    public boolean isAlbumActive(){

        return false;
    }

    public String getActiveAlbumID(){

        return null;
    }

}
