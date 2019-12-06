package com.integrals.inlens.Weather.Helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Athul Krishna on 03/09/2017.
 */

public class Helper {
    static String stream=null;

    public Helper() {
    }
    public String getHTTPData(String urlString){
        try {
              URL url=new URL(urlString);
              HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
              if(httpURLConnection.getResponseCode()==200){
                BufferedReader r=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder stringBuilder=new StringBuilder();
                String Line;
                while ((Line=r.readLine())!=null)stringBuilder.append(Line);
                stream=stringBuilder.toString();
                httpURLConnection.disconnect();
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream;
    }
}
