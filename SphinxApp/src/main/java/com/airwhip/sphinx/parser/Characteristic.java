package com.airwhip.sphinx.parser;

import android.util.Log;

import com.airwhip.sphinx.misc.Constants;

/**
 * Created by Whiplash on 17.03.14.
 */
public class Characteristic {

    private static final int DEFAULT_AGE = 25;

    private static StringBuilder xml = new StringBuilder();

    private static double[] weight = new double[Constants.xmls.length];
    private static double[] max = new double[Constants.xmls.length];

    private static double maleWeight = 0.;
    private static double femaleWeight = 0.;

    private static double age = 0;
    private static int ageIteration = 0;

    private Characteristic() {
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

    public static boolean isUFO() {
        // TODO check it
        return false;
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
}
