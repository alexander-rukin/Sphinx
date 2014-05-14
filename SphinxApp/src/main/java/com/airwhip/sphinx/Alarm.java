package com.airwhip.sphinx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.airwhip.sphinx.misc.Constants;

import java.io.File;

/**
 * Created by Whiplash on 27.04.2014.
 */
public class Alarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new File(Constants.FILE_PATH).delete();
        context.startService(new Intent(context, ServerSender.class));
    }
}
