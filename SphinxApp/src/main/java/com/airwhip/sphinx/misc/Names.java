package com.airwhip.sphinx.misc;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.airwhip.sphinx.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Whiplash on 10.04.2014.
 */
public class Names {

    private static Set<String> maleNames;
    private static Set<String> femaleNames;

    private static Map<String, String> transliteToRus;

    private Names() {
    }

    public static void generate(Context context) {
        maleNames = new HashSet<>();
        femaleNames = new HashSet<>();

        try {
            parseXml(context.getResources().getXml(R.xml.names_m), maleNames);
            parseXml(context.getResources().getXml(R.xml.names_w), femaleNames);
        } catch (XmlPullParserException e) {
            Log.e(Constants.ERROR_TAG, "Parsing failed because of XmlPullParserException");
        } catch (IOException e) {
            Log.e(Constants.ERROR_TAG, "Parsing failed because of IOException");
        }
    }

    private static void parseXml(XmlResourceParser parser, Set<String> set) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        String curTag = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    curTag = parser.getName();
                    break;
                case XmlPullParser.TEXT:
                    if (curTag.equals("key") || curTag.equals("string")) {
                        set.add(parser.getText());
                    }
                    break;
                case XmlPullParser.END_TAG:
                    curTag = "";
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
    }

    public static boolean contains(String name) {
        return maleNames.contains(name.toLowerCase()) || femaleNames.contains(name.toLowerCase());
    }

    public static boolean isMale(String name) {
        return maleNames.contains(name.toLowerCase());
    }

    public static boolean isFemale(String name) {
        return femaleNames.contains(name.toLowerCase());
    }

}
