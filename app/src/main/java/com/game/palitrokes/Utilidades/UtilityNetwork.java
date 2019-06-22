package com.game.palitrokes.Utilidades;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;

public class UtilityNetwork {

    public static boolean isNetworkAvailable(Context context) {

        boolean br = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        br = (activeNetworkInfo != null && activeNetworkInfo.isConnected());

        return br;
    }

    public static boolean isWifiAvailable (Context context)
    {
        boolean bdev = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        bdev = ((activeNetwork != null)&& (activeNetwork.isConnected()) && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI));

        return bdev;
    }


}
