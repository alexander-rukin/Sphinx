package com.airwhip.sphinx.misc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Whiplash on 05.05.2014.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String TEXT_TYPE = "_id";
    public static final String TEXT_RESULT = "textresult";
    public static final String TABLE_NAME = "sphinx_result";
    private static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME + " (" + TEXT_TYPE + " VARCHAR(255) PRIMARY KEY,"
            + TEXT_RESULT + " VARCHAR(255));";
    private final static String DATABASE_NAME = "sphinx.db";
    private Context context;

    public DBHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}
