package com.airwhip.sphinx.parser;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.airwhip.sphinx.misc.Constants;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Whiplash on 17.03.14.
 */
public class InformationParser {

    private static final String WEIGHT_ARRAY_TAG = "weight-array";
    private static final String ITEM_TAG = "item";

    private static final double MAX = 100.;
    private static final double SHIFT = MAX * 0.2;
    private static final double PART = .1;
    private Context context;
    private ParserType type;
    private List<String> storage = new ArrayList<>();

    private double[] weight = new double[Constants.xmls.length];
    private double[] max = new double[Constants.xmls.length];

    public InformationParser(Context context, StringBuilder xml, ParserType type) {
        this.context = context;
        this.type = type;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(xml.toString()));
            int eventType = xpp.getEventType();

            boolean isCorrectTag = false;
            StringBuilder text = new StringBuilder();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (xpp.getName().equals(ITEM_TAG)) {
                            isCorrectTag = true;
                            text = new StringBuilder();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (xpp.getName().equals(ITEM_TAG)) {
                            isCorrectTag = false;
                            storage.add(text.toString());
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (isCorrectTag) {
                            text.append(xpp.getText().toLowerCase());
                        }
                        break;
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            Log.e(Constants.ERROR_TAG, e.getMessage() + " " + type.toString());
        }

        for (int i = 0; i < weight.length; i++) {
            calculateWeight(i);
        }
    }

    public double[] getAllMax() {
        double[] copyMax = new double[max.length];
        System.arraycopy(max, 0, copyMax, 0, max.length);
        return copyMax;
    }

    public double[] getAllWeight() {
        double[] copyWeight = new double[weight.length];
        System.arraycopy(weight, 0, copyWeight, 0, weight.length);
        return copyWeight;
    }

    private void calculateWeight(int index) {
        int xml = Constants.xmls[index];
        double resultWeight = 0;
        double resultMax = 0;
        boolean[] isUsed = new boolean[storage.size()];
        boolean hasCorrectTag = false;
        try {
            XmlResourceParser xrp = context.getResources().getXml(xml);

            int eventType = xrp.getEventType();

            String currentTag = "";
            double currentWeight = 0.;
            boolean isCorrectTag = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = xrp.getName();
                        if (currentTag.equals(type.toString())) {
                            isCorrectTag = true;
                            hasCorrectTag = true;
                        }
                        if (isCorrectTag && xrp.getName().equals(WEIGHT_ARRAY_TAG)) {
                            currentWeight = xrp.getAttributeIntValue(0, 0);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (isCorrectTag && currentTag.equals(ITEM_TAG)) {
                            int num = numberOfEntries(xrp.getText(), isUsed);
                            resultWeight += currentWeight * num;
                            resultMax += (MAX - SHIFT < currentWeight ? currentWeight * num : (currentWeight + SHIFT) * num);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        currentTag = "";
                        if (isCorrectTag && xrp.getName().equals(type.toString())) {
                            isCorrectTag = false;
                        }
                        break;
                }
                eventType = xrp.next();
            }

        } catch (XmlPullParserException | IOException | NullPointerException e) {
            Log.e(Constants.ERROR_TAG, e.getMessage() + " " + type.toString());
        }

        if (hasCorrectTag) {
            weight[index] = resultWeight;
            max[index] = (storage.size() * MAX * PART + resultMax) / 2.;
        }
    }

    private int numberOfEntries(String str, boolean[] isUsed) {
        str = str.toLowerCase();
        int result = 0;
        for (int i = 0; i < storage.size(); i++) {
            if (!isUsed[i] && storage.get(i).contains(str)) {
                isUsed[i] = true;
                result++;
            }
        }
        return result;
    }

    public enum ParserType {
        ACCOUNT, APPLICATION,
        HISTORY, BOOKMARKS,
        MUSIC;

        @Override
        public String toString() {
            switch (this) {
                case ACCOUNT:
                    return "account";
                case APPLICATION:
                    return "application";
                case HISTORY:
                    return "history";
                case BOOKMARKS:
                    return "history";
                case MUSIC:
                    return "music";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

}
