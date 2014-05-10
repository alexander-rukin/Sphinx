package com.airwhip.sphinx.parser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.airwhip.sphinx.R;
import com.airwhip.sphinx.misc.Constants;
import com.airwhip.sphinx.misc.DBHelper;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Whiplash on 17.03.14.
 */
public class Characteristic {

    private static final int DEFAULT_AGE = 25;

    public static int[] feedBackResult = new int[6];
    public static int[] feedBackCategory = new int[6];

    private static DBHelper dbHelper;
    private static SQLiteDatabase sqLiteDatabase;

    private static double[] weight = new double[Constants.xmls.length];
    private static double[] max = new double[Constants.xmls.length];

    private static double relationshipWeight = 0.;
    private static double relationshipMaxWeight = 0.;

    private static double maleWeight = 0.;
    private static double femaleWeight = 0.;

    private static double age = 0;
    private static int ageIteration = 0;

    private Characteristic() {
    }

    public static void initDataBase(Context context) {
        dbHelper = new DBHelper(context);
        sqLiteDatabase = dbHelper.getWritableDatabase();
    }

    public static void updateDataBase(String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.TEXT_RESULT, value);

        Cursor cursor = sqLiteDatabase.query(DBHelper.TABLE_NAME, new String[]{DBHelper.TEXT_TYPE, DBHelper.TEXT_RESULT}, DBHelper.TEXT_TYPE + "=" + "\'" + key + "\'", null, null, null, null);
        if (cursor.moveToFirst()) {
            sqLiteDatabase.update(DBHelper.TABLE_NAME, cv, DBHelper.TEXT_TYPE + "=" + "\'" + key + "\'", null);
        } else {
            cv.put(DBHelper.TEXT_TYPE, key);
            sqLiteDatabase.insert(DBHelper.TABLE_NAME, null, cv);
        }
        cursor.close();
    }

    public static String getValueFromDataBase(String key) {
        Cursor cursor = sqLiteDatabase.query(DBHelper.TABLE_NAME, new String[]{DBHelper.TEXT_TYPE, DBHelper.TEXT_RESULT}, DBHelper.TEXT_TYPE + "=" + "\'" + key + "\'", null, null, null, null);
        cursor.moveToNext();
        String result = cursor.getString(cursor.getColumnIndex(DBHelper.TEXT_RESULT));
        cursor.close();
        return result;
    }

    public static void fillFeedBackCategory(Context context, String[] categories) {
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
                context.getResources().getStringArray(R.array.types)[2],
                context.getResources().getStringArray(R.array.types)[4],
                context.getResources().getStringArray(R.array.types)[5],
                context.getResources().getStringArray(R.array.types)[6],
                context.getResources().getStringArray(R.array.types)[7],
                context.getResources().getStringArray(R.array.types)[8],
                context.getResources().getStringArray(R.array.types)[9]};
        List<String> idList = Arrays.asList(ids);
        for (int i = 0; i < feedBackCategory.length; i++) {
            feedBackCategory[i] = idList.indexOf(categories[i]);
        }
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

    public static void addRelationship(double value, double maxValue) {
        relationshipWeight += value;
        relationshipMaxWeight += maxValue;
    }

    public static int getRelationship() {
        return relationshipWeight != 0. ? Math.min((int) (100. * relationshipWeight / relationshipMaxWeight), 100) : 0;
    }

    public static void generateResult(Context context) {
        StringBuilder xml = new StringBuilder();
        xml.append("<result>\n");
        for (int i = 0; i < feedBackCategory.length; i++) {
            xml.append("\t<item name=\"" + feedBackCategory[i] + "\" is_correct=\"" + (feedBackResult[i] ^ 1) + "\"/>\n");
        }
        xml.append("</result>\n");

        updateDataBase("RESULT", xml.toString());
    }

    public static StringBuilder generate() {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<user id=\"" + getValueFromDataBase("USER_ID") + "\">\n");
        xml.append(getValueFromDataBase("RESULT"));
        xml.append(getValueFromDataBase("ACCOUNT"));
        xml.append(getValueFromDataBase("APPLICATION"));
        xml.append(getValueFromDataBase("HISTORY"));
        xml.append(getValueFromDataBase("BOOKMARKS"));
        xml.append(getValueFromDataBase("MUSIC"));
        xml.append(new StringBuilder("</user>"));
        return xml;
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

    public static int size() {
        return weight.length;
    }

    public static boolean containsPikabu() {
        int counter = 0;
        StringBuilder xml = new StringBuilder(getValueFromDataBase("HISTORY"));
        xml.append(getValueFromDataBase("BOOKMARKS"));
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
