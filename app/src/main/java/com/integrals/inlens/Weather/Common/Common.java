package com.integrals.inlens.Weather.Common;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Athul Krishna on 03/09/2017.
 */

public class Common {
    public static String API_KEY="d2cac21746726bce1da020a4beab9ad5";
    public static String API_LINK="http://api.openweathermap.org/data/2.5/weather";

    @NonNull
    public static String apiRequest(String Latitude,String Longitude){
        StringBuilder SB=new StringBuilder(API_LINK);
        SB.append(String.format("?lat=%s&lon=%s&APPID=%s&units=metric",Latitude,Longitude,API_KEY));
        return SB.toString();
    }
    public static String unixTimeStampToDateTime(double unixTimeStamp){
        DateFormat dateFormat=new SimpleDateFormat("HH:mm");
        Date date=new Date();
        date.setTime((long)unixTimeStamp*1000);
        return dateFormat.format(date);
    }
    public static String getImage(String icon){
        return String.format("http://openweathermap.org/img/w/%s.png",icon);
    }
    public static String getDataNow(){
        DateFormat dateFormat =new SimpleDateFormat("dd MMMM yyyy HH:mm");
        Date date=new Date();
        return dateFormat.format(date);

    }
}

