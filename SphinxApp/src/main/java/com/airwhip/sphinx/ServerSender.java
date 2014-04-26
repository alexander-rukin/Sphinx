package com.airwhip.sphinx;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.airwhip.sphinx.misc.Constants;
import com.airwhip.sphinx.parser.Characteristic;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

public class ServerSender extends IntentService {

    public ServerSender() {
        super("SERVER_SENDER");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(Constants.DEBUG_TAG, "START_SERVICE");

        postRequest();
        stopSelf();

        Log.d(Constants.DEBUG_TAG, "STOP_SELF");
    }

    private void postRequest() {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://sphinx-app.com/request/post.php");
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("text", Characteristic.getXml().toString()));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse resp = httpclient.execute(httppost);
            HttpEntity ent = resp.getEntity();
            String responseString = EntityUtils.toString(ent, "UTF-8");
            Log.d(Constants.DEBUG_TAG, responseString);
        } catch (Exception e) {
            Log.e(Constants.ERROR_TAG, "EXCEPTION " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.d(Constants.DEBUG_TAG, "STOP_SERVICE");
        super.onDestroy();
    }
}
