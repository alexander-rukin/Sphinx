package com.airwhip.sphinx.parser;

import android.content.Context;
import android.util.Log;

import com.airwhip.sphinx.R;
import com.airwhip.sphinx.misc.Constants;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Whiplash on 17.03.14.
 */
public class Characteristic {

    private static final int DEFAULT_AGE = 25;
    public static int[] feedBackResult = new int[6];
    private static StringBuilder xml = new StringBuilder();
    private static String userID = "";
    private static String[] feedBackCategory;
    private static double[] weight = new double[Constants.xmls.length];
    private static double[] max = new double[Constants.xmls.length];

    private static double maleWeight = 0.;
    private static double femaleWeight = 0.;

    private static double age = 0;
    private static int ageIteration = 0;

    private Characteristic() {
    }

    public static void clear() {
        xml = new StringBuilder();
        weight = new double[Constants.xmls.length];
        max = new double[Constants.xmls.length];
        maleWeight = 0.;
        femaleWeight = 0.;
        age = 0.;
        ageIteration = 0;
    }

    public static void fillFeedBackCategory(String[] categoryes) {
        feedBackCategory = categoryes;
    }

    public static void setUserID(String id) {
        userID = id;
    }

    public static void addAge(int ageValue) {
        age += ageValue;
        ageIteration++;
    }

    public static void addAges(int ageValue, int iteration) {
        age += ageValue;
        ageIteration += iteration;
    }

    public static int getAge() {
        if (age == 0) {
            return DEFAULT_AGE;
        }
        return (int) Math.floor(age / ageIteration);
    }

    public static int getMale() {
        if (maleWeight == femaleWeight) {
            return 50;
        }
        return (int) (100 * maleWeight / (maleWeight + femaleWeight));
    }

    public static int getFemale() {
        if (maleWeight == femaleWeight) {
            return 50;
        }
        return 100 - (int) (100 * maleWeight / (maleWeight + femaleWeight));
    }

    public static boolean isMale() {
        return maleWeight > femaleWeight;
    }

    public static void addAll(double[] values, double[] maxValues) {
        if (values.length != weight.length) {
            Log.e(Constants.ERROR_TAG, "INCORRECT ARRAY SIZE");
        }
        for (int i = 0; i < values.length; i++) {
            weight[i] += values[i];
            max[i] += maxValues[i];
        }
    }

    public static void addMale(double value) {
        maleWeight += value;
    }

    public static void addFemale(double value) {
        femaleWeight += value;
    }

    public static void append(StringBuilder newXml) {
        xml.append(newXml);
    }

    public static void generate(Context context) {
        String not = context.getString(R.string.not);
        String[] ids = {context.getString(R.string.man),
                context.getString(R.string.woman),
                context.getString(R.string.in_relationship),
                context.getString(R.string.single),
                context.getString(R.string.studying),
                context.getString(R.string.not_studying),
                context.getResources().getStringArray(R.array.ages)[0],
                context.getResources().getStringArray(R.array.ages)[1],
                context.getResources().getStringArray(R.array.ages)[2],
                context.getResources().getStringArray(R.array.ages)[3],
                context.getResources().getStringArray(R.array.ages)[4],
                context.getResources().getStringArray(R.array.ages)[5],
                context.getResources().getStringArray(R.array.types)[0],
                not + " " + context.getResources().getStringArray(R.array.types)[0].toLowerCase(),
                context.getResources().getStringArray(R.array.types)[2],
                not + " " + context.getResources().getStringArray(R.array.types)[2].toLowerCase(),
                context.getResources().getStringArray(R.array.types)[4],
                not + " " + context.getResources().getStringArray(R.array.types)[4].toLowerCase(),
                context.getResources().getStringArray(R.array.types)[5],
                not + " " + context.getResources().getStringArray(R.array.types)[5].toLowerCase(),
                context.getResources().getStringArray(R.array.types)[6],
                not + " " + context.getResources().getStringArray(R.array.types)[6].toLowerCase(),
                context.getResources().getStringArray(R.array.types)[7],
                not + " " + context.getResources().getStringArray(R.array.types)[7].toLowerCase(),
                context.getResources().getStringArray(R.array.types)[8],
                not + " " + context.getResources().getStringArray(R.array.types)[8].toLowerCase(),
                context.getResources().getStringArray(R.array.types)[9],
                not + " " + context.getResources().getStringArray(R.array.types)[9].toLowerCase()};
        List<String> idList = Arrays.asList(ids);

        StringBuilder newXML = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        newXML.append("<user id=\"" + userID + "\">\n");

        newXML.append("<result>\n");
        for (int i = 0; i < feedBackCategory.length; i++) {
            newXML.append("\t<item name=\"" + idList.indexOf(feedBackCategory[i]) + "\" is_correct=\"" + (feedBackResult[i] ^ 1) + "\">\n");
        }
        newXML.append("</result>\n");

        newXML.append(xml);
        newXML.append(new StringBuilder("</user>"));
        xml = newXML;
    }

    public static boolean isUFO() {
        int sum = 0;
        for (int i = 0; i < weight.length; i++) {
            sum += get(i);
        }
        return sum < weight.length * 10;
    }

    public static int get(int i) {
        return max[i] != 0 ? Math.min((int) (100. * weight[i] / max[i]), 100) : 0;
    }

    public static StringBuilder getXml() {
        return xml;
    }

    public static int size() {
        return weight.length;
    }

    public static boolean containsPikabu() {
        int counter = 0;
        for (int i = 0; i != -1; ) {
            i = xml.indexOf("pikabu", i + 1);
            counter += (i != -1 ? 1 : 0);
        }
        return counter > 10;
    }

    public static int getAgeCategory() {
        int curAge = getAge();
        if (curAge < 18) return 0;
        if (curAge < 25) return 1;
        if (curAge < 34) return 2;
        if (curAge < 42) return 3;
        if (curAge < 56) return 4;
        return 5;
    }
}
