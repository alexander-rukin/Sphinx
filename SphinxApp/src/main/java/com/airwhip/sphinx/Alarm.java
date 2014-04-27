package com.airwhip.sphinx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.airwhip.sphinx.misc.Constants;

/**
 * Created by Whiplash on 27.04.2014.
 */
public class Alarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Constants.DEBUG_TAG, "WAKE UP!");
    }
}
