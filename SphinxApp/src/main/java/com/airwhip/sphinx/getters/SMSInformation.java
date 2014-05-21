package com.airwhip.sphinx.getters;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import com.airwhip.sphinx.R;
import com.airwhip.sphinx.misc.Constants;
import com.airwhip.sphinx.misc.Names;
import com.airwhip.sphinx.parser.Characteristic;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by Whiplash on 10.04.2014.
 */
public class SMSInformation {
    private final static Uri SENT = Uri.parse("content://sms/sent");
    private final static Uri INBOX = Uri.parse("content://sms/inbox");

    private final static Map<String, Integer> wordToAge = new HashMap<>();
    private final static Map<String, Integer> wordToRelationship = new HashMap<>();

    private static final double MAX = 100.;
    private static final double SHIFT = MAX * 0.01;

    private static final String WEIGHT_ARRAY_TAG = "weight-array";
    private static final String ITEM_TAG = "item";

    private final static String PRONOUN = "я_ты_вы_он_она_оно_мы_вы_они";

    public static void get(Context context) {
        try {
            XmlResourceParser xrp = context.getResources().getXml(R.xml.age_sms);
            int eventType = xrp.getEventType();
            String currentTag = "";
            int currentWeight = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = xrp.getName();
                        if (currentTag.equals(WEIGHT_ARRAY_TAG)) {
                            currentWeight = xrp.getAttributeIntValue(0, 0);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (currentTag.equals(ITEM_TAG)) {
                            wordToAge.put(xrp.getText(), currentWeight);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        currentTag = "";
                        break;
                }
                eventType = xrp.next();
            }

            xrp = context.getResources().getXml(R.xml.relationship_sms);
            eventType = xrp.getEventType();
            currentTag = "";
            currentWeight = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = xrp.getName();
                        if (currentTag.equals(WEIGHT_ARRAY_TAG)) {
                            currentWeight = xrp.getAttributeIntValue(0, 0);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (currentTag.equals(ITEM_TAG)) {
                            wordToRelationship.put(xrp.getText(), currentWeight);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        currentTag = "";
                        break;
                }
                eventType = xrp.next();
            }
        } catch (XmlPullParserException | IOException | NullPointerException e) {
            Log.e(Constants.ERROR_TAG, e.getMessage());
        }

        Pair<Double, Double> sent = analyzeMessages(getSMS(context, SENT), "я");
        Pair<Double, Double> inbox = analyzeMessages(getSMS(context, INBOX), "ты");

        double maleCounter = (sent.first + inbox.first);
        double femaleCounter = (sent.second + inbox.second);

        if (maleCounter != 0. || femaleCounter != 0.) {
            Characteristic.addMale(maleCounter / (maleCounter + femaleCounter));
            Characteristic.addFemale(1. - maleCounter / (maleCounter + femaleCounter));
        }

        wordToAge.clear();
        wordToRelationship.clear();
    }

    private static List<String> getSMS(Context context, Uri uri) {
        List<String> list = new ArrayList<>();
        List<Integer> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        try {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    for (boolean hasData = cursor.moveToFirst(); hasData; hasData = cursor.moveToNext()) {
                        list.add(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                        if (uri.equals(SENT)) {
                            calendar.setTimeInMillis(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow("date"))));
                            dates.add(calendar.get(Calendar.YEAR));
                        }
                    }
                    cursor.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }

        if (uri.equals(SENT)) {
            int curYear = Calendar.getInstance().get(Calendar.YEAR);
            for (int i = 0; i < list.size(); i++) {
                String message = list.get(i);

                int messageAge = 0;
                int messageEntries = 0;
                for (String word : wordToAge.keySet()) {
                    if (message.contains(word)) {
                        messageAge += wordToAge.get(word);
                        messageEntries++;
                    }
                }

                if (messageEntries != 0 && messageAge / messageEntries > 0) {
                    Characteristic.addAges(messageAge / messageEntries + (curYear - dates.get(i)), 1);
                }
            }
        }

        return list;
    }

    private static Pair<Double, Double> analyzeMessages(List<String> messages, String pronoun) {
        double maleCount = 0., femaleCount = 0.;
        double messageRelationship = 0.;
        double maxRelationship = 0.;

        for (String message : messages) {
            message = message.toLowerCase();

            double weightRelationship = 0.;
            int relationshipEntries = 0;
            for (String word : wordToRelationship.keySet()) {
                if (message.contains(word)) {
                    weightRelationship += wordToRelationship.get(word);
                    relationshipEntries++;
                }
            }
            if (relationshipEntries != 0) {
                weightRelationship /= relationshipEntries;
            }
            messageRelationship += weightRelationship;
            maxRelationship += (weightRelationship != 0 ? weightRelationship : SHIFT);

            StringTokenizer messageParser = new StringTokenizer(message, ".!?");
            while (messageParser.hasMoreTokens()) {
                List<String[]> subSentences = new ArrayList<>();
                StringTokenizer sentenceParser = new StringTokenizer(messageParser.nextToken(), ",");
                while (sentenceParser.hasMoreTokens()) {
                    subSentences.add(sentenceParser.nextToken().split(" "));
                }

                SentenceState[] subjectPosition = new SentenceState[subSentences.size()];
                boolean hasMainPronoun = false;
                Arrays.fill(subjectPosition, SentenceState.NO_SUBJECT);

                for (int i = 0; i < subSentences.size(); i++) {
                    String[] subSentence = subSentences.get(i);
                    for (String word : subSentence) {
                        if (word.toLowerCase().equals(pronoun)) {
                            subjectPosition[i] = SentenceState.MAIN_SUBJECT;
                            hasMainPronoun = true;
                        }
                    }
                    for (String word : subSentence) {
                        if (!word.toLowerCase().equals(pronoun) && isSubject(word)) {
                            subjectPosition[i] = (subjectPosition[i] == SentenceState.NO_SUBJECT ? SentenceState.HAS_SUBJECT : SentenceState.MANY_SUBJECT);
                            break;
                        }
                    }
                }

                for (int i = 0; i < subSentences.size(); i++) {
                    String[] subSentence = subSentences.get(i);
                    String nextWord, prevWord = "";
                    for (int j = 0; j < subSentence.length; j++) {
                        nextWord = (j + 1 >= subSentence.length ? "" : subSentence[j + 1]);
                        double count = 0.;
                        switch (subjectPosition[i]) {
                            case NO_SUBJECT:
                                count = 0.2;
                                break;
                            case MAIN_SUBJECT:
                                if (prevWord.toLowerCase().equals(pronoun) || nextWord.toLowerCase().equals(pronoun)) {
                                    count = 0.8;
                                } else {
                                    count = 0.3;
                                }
                                break;
                            case MANY_SUBJECT:
                                if (prevWord.toLowerCase().equals(pronoun) || nextWord.toLowerCase().equals(pronoun)) {
                                    count = 0.8;
                                }
                                break;
                        }
                        if (hasMainPronoun) {
                            count += 0.2;
                        }

                        if (isMaleVerb(subSentence[j].toLowerCase())) {
                            maleCount += count;
                        }
                        if (isFemaleVerb(subSentence[j].toLowerCase())) {
                            femaleCount += count;
                        }
                        prevWord = subSentence[j];
                    }
                }
            }
        }

        Characteristic.addRelationship(messageRelationship, maxRelationship);
        return new Pair<>(maleCount, femaleCount);
    }

    private static boolean isMaleVerb(String word) {
        return word.endsWith("ал") || word.endsWith("ил") || word.endsWith("ел") || word.endsWith("ял");
    }

    private static boolean isFemaleVerb(String word) {
        return word.endsWith("ала") || word.endsWith("ила") || word.endsWith("ела") || word.endsWith("яла");
    }

    private static boolean isSubject(String word) {
        return !word.equals("") && (PRONOUN.contains(word.toLowerCase()) || Names.contains(word));
    }

    private enum SentenceState {
        NO_SUBJECT,
        HAS_SUBJECT,
        MAIN_SUBJECT,
        MANY_SUBJECT
    }
}
