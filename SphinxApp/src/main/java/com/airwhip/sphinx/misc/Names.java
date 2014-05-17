package com.airwhip.sphinx.misc;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.airwhip.sphinx.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Whiplash on 10.04.2014.
 */
public class Names {

    private static Set<String> maleNames;
    private static Set<String> femaleNames;

    private static Map<String, String> translitToRus;

    private static List<String> russianWords;

    private Names() {
    }

    public static void generate(Context context) {
        maleNames = new HashSet<>();
        femaleNames = new HashSet<>();

        translitToRus = new HashMap<>();

        try {
            parseXml(context.getResources().getXml(R.xml.names_m), maleNames);
            parseXml(context.getResources().getXml(R.xml.names_w), femaleNames);

            XmlResourceParser xrp = context.getResources().getXml(R.xml.translit);
            int eventType = xrp.getEventType();
            String currentTag = "";
            String ru = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = xrp.getName();
                        if (currentTag.equals("item")) {
                            ru = xrp.getAttributeValue(0);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (currentTag.equals("item")) {
                            translitToRus.put(xrp.getText(), ru);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        currentTag = "";
                        break;
                }
                eventType = xrp.next();
            }
        } catch (XmlPullParserException e) {
            Log.e(Constants.ERROR_TAG, "Parsing failed because of XmlPullParserException");
        } catch (IOException e) {
            Log.e(Constants.ERROR_TAG, "Parsing failed because of IOException");
        }
    }

    private static void parseXml(XmlResourceParser parser, Set<String> set) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        String currentTag = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    currentTag = parser.getName();
                    break;
                case XmlPullParser.TEXT:
                    if (currentTag.equals("key") || currentTag.equals("string")) {
                        set.add(parser.getText());
                    }
                    break;
                case XmlPullParser.END_TAG:
                    currentTag = "";
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

    public static List<String> getRussianWords(String word) {
        russianWords = new ArrayList<>();
        translitHelper("", word.toLowerCase());
        return russianWords;
    }

    private static void translitHelper(String pre, String word) {
        if (word.equals("")) {
            russianWords.add(pre);
            return;
        }
        if (word.charAt(0) >= 'а' && word.charAt(0) <= 'я') {
            translitHelper(pre + word.charAt(0), word.substring(1));
        } else if (word.charAt(0) >= 'a' && word.charAt(0) <= 'z') {
            for (int i = 1; i < 4 && word.length() >= i; i++) {
                String sub = word.substring(0, i);
                if (translitToRus.containsKey(sub)) {
                    translitHelper(pre + translitToRus.get(sub), word.substring(i));
                }
            }
        } else {
            return;
        }
    }

}
