package com.airwhip.sphinx.getters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Pair;

import com.airwhip.sphinx.WelcomeActivity;
import com.airwhip.sphinx.parser.Characteristic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by Whiplash on 10.04.2014.
 */
public class SMSInformation {
    private final static Uri SENT = Uri.parse("content://sms/sent");
    private final static Uri INBOX = Uri.parse("content://sms/inbox");

    private final static String PRONOUN = "я_ты_вы_он_она_оно_мы_вы_они";

    public static void get(Context context) {
        Pair<Double, Double> sent = analyzeMessages(getSMS(context, SENT), "я");
        Pair<Double, Double> inbox = analyzeMessages(getSMS(context, INBOX), "ты");

        double maleCounter = (sent.first + inbox.first);
        double femaleCounter = (sent.second + inbox.second);

        Characteristic.addMale(maleCounter / (maleCounter + femaleCounter));
        Characteristic.addFemale(1. - maleCounter / (maleCounter + femaleCounter));
    }

    private static List<String> getSMS(Context context, Uri uri) {
        List<String> list = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            for (boolean hasData = cursor.moveToFirst(); hasData; hasData = cursor.moveToNext()) {
                final String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                list.add(body);
            }
            cursor.close();
        }

        return list;
    }

    private static Pair<Double, Double> analyzeMessages(List<String> messages, String pronoun) {
        double maleCount = 0., femaleCount = 0.;

        for (String message : messages) {
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

        return new Pair<>(maleCount, femaleCount);
    }

    private static boolean isMaleVerb(String word) {
        return word.endsWith("ал") || word.endsWith("ил") || word.endsWith("ел") || word.endsWith("ял");
    }

    private static boolean isFemaleVerb(String word) {
        return word.endsWith("ала") || word.endsWith("ила") || word.endsWith("ела") || word.endsWith("яла");
    }

    private static boolean isSubject(String word) {
        return !word.equals("") && (PRONOUN.contains(word.toLowerCase()) || WelcomeActivity.isContainsName(word));
    }

    private enum SentenceState {
        NO_SUBJECT,
        HAS_SUBJECT,
        MAIN_SUBJECT,
        MANY_SUBJECT
    }
}
