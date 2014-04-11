package com.airwhip.sphinx.misc;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.airwhip.sphinx.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Whiplash on 10.04.2014.
 */
public class Names {

    private Set<String> names;
    private Set<List<String>> derivativeOfName;

    public Names(Context context) {
        names = new HashSet<>();
        derivativeOfName = new HashSet<>();

        try {
            parseXml(context.getResources().getXml(R.xml.names));
        } catch (XmlPullParserException e) {
            Log.e(Constants.ERROR_TAG, "Parsing failed because of XmlPullParserException");
        } catch (IOException e) {
            Log.e(Constants.ERROR_TAG, "Parsing failed because of IOException");
        }
    }

    private void parseXml(XmlResourceParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        String curTag = "";
        List<String> derivativeOfNameList = new ArrayList<>();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    curTag = parser.getName();
                    break;
                case XmlPullParser.TEXT:
                    String tagValue = parser.getText();
                    if (curTag.equals("key")) {
                        curTag = "";
                        names.add(tagValue);
                        derivativeOfNameList = new ArrayList<>();
                        derivativeOfNameList.add(tagValue);
                        derivativeOfName.add(derivativeOfNameList);
                        break;
                    }
                    if (curTag.equals("string")) {
                        curTag = "";
                        names.add(tagValue);
                        derivativeOfNameList.add(tagValue);
                    }
                    break;
            }
            eventType = parser.next();
        }
    }

    public boolean contains(String name) {
        return names.contains(name.toLowerCase());
    }

    public List<String> getDerivativeOfName(String name) {
        if (contains(name)) {
            String nameLowerCase = name.toLowerCase();
            for (List<String> list : derivativeOfName) {
                if (list.contains(nameLowerCase)) {
                    return list;
                }
            }
        }
        return null;
    }

}
