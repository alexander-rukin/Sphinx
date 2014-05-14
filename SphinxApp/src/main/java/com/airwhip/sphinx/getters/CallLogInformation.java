package com.airwhip.sphinx.getters;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import com.airwhip.sphinx.R;
import com.airwhip.sphinx.misc.Constants;
import com.airwhip.sphinx.parser.Characteristic;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Whiplash on 14.05.2014.
 */
public class CallLogInformation {

    public static void get(Context context) {
        Set<String> storage = new HashSet<>();
        try {
            XmlResourceParser xrp = context.getResources().getXml(R.xml.age_sms);
            int eventType = xrp.getEventType();
            String currentTag = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = xrp.getName();
                        break;
                    case XmlPullParser.TEXT:
                        if (currentTag.equals("item")) {
                            storage.add(xrp.getText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        currentTag = "";
                        break;
                }
                eventType = xrp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            for (boolean hasData = cursor.moveToFirst(); hasData; hasData = cursor.moveToNext()) {
                try {
                    if (storage.contains(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)).toLowerCase())) {
                        Characteristic.addRelationship(1000, 0);
                    }
                } catch (Exception e) {
                    Log.e(Constants.ERROR_TAG, "Call name is null");
                }
            }
            cursor.close();
        }
    }

}
