package com.ngengs.android.popularmovies.apps.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.ngengs.android.popularmovies.apps.DetailMovieActivity;
import com.ngengs.android.popularmovies.apps.MainActivity;

/**
 * Created by ngengs on 7/4/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Network connectivity change");

        if (intent.getExtras() != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null && ni.isConnectedOrConnecting()) {
                Log.i(TAG, "Network " + ni.getTypeName() + " connected");
                if (ni.isAvailable()) {
                    Log.i(TAG, "Network " + ni.getTypeName() + " available");
                    if (context instanceof MainActivity) {
                        Log.d(TAG, "onReceive: Context Main Activity");
                        ((MainActivity) context).connectionCennected();
                    } else if (context instanceof DetailMovieActivity) {
                        Log.d(TAG, "onReceive: Context Detail Activity");
                        ((DetailMovieActivity) context).connectionCennected();
                    }
                }
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d(TAG, "There's no network connectivity");
            }
        }
    }
}
