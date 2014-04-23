package com.airwhip.sphinx;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.airwhip.sphinx.misc.Constants;
import com.airwhip.sphinx.parser.Characteristic;

public class ServerSender extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ServerSender() {
        super("SERVER_SENDER");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(Constants.DEBUG_TAG, "START_SERVICE");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(Constants.DEBUG_TAG, "" + Characteristic.getMale());
        stopSelf();
        Log.d(Constants.DEBUG_TAG, "STOP_SELF");
    }

    @Override
    public void onDestroy() {
        Log.d(Constants.DEBUG_TAG, "STOP_SERVICE");
        super.onDestroy();
    }
}
