package com.cambricon.productdisplay.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cambricon.productdisplay.bean.FaceDetectionImage;

import java.util.ArrayList;

/**
 * Created by cambricon on 18-3-13.
 */

public class FaceDetectDB {

    public static final String TAG = "FaceDetectDB";

    private final Context mContext;
    private FaceDetectDB.DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mSQLiteDatabase;

    //CPU mode
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TIME = "time";
    public static final String KEY_FPS = "fps";
    static final String SQLITE_TABLE = "FaceDetectTable";

    //IPU mode
    public static final String KEY_ROWID_IPU = "_id_ipu";
    public static final String KEY_NAME_IPU = "name_ipu";
    public static final String KEY_TIME_IPU = "time_ipu";
    public static final String KEY_FPS_IPU = "fps_ipu";
    static final String SQLITE_TABLE_IPU = "FaceDetectIPUTable";

    public FaceDetectDB(Context context) {
        mContext = context;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, CommDB.DATABASE_NAME, null, CommDB.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
            onCreate(db);
        }
    }

    public FaceDetectDB open() throws SQLException {
        mDatabaseHelper = new FaceDetectDB.DatabaseHelper(mContext);
        mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (mDatabaseHelper != null) {
            mDatabaseHelper.close();
        }
    }


    /**
     * CPU mode Face Detection
     * */

    public long addFaceDetection(String name, String time, String fps) {
        long createResult = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_FPS, fps);
        try {
            createResult = mSQLiteDatabase.insert(SQLITE_TABLE, null, initialValues);
        } catch (Exception e) {
            //handle exception
        }
        return createResult;
    }

    /**
     * 删除所有字段
     *
     * @return
     */

    public boolean deleteAllFaceDetection() {
        int doneDelete = 0;
        try {
            doneDelete = mSQLiteDatabase.delete(SQLITE_TABLE, null, null);
        } catch (Exception e) {

        }
        return doneDelete > 0;
    }

    /**
     * 获取表中所有
     *
     * @return
     */
    public ArrayList<FaceDetectionImage> fetchAll() {

        ArrayList<FaceDetectionImage> faceDetectionImageArrayList = new ArrayList<>();
        Cursor cursor = null;
        cursor = mSQLiteDatabase.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_NAME, KEY_FPS, KEY_TIME},
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                FaceDetectionImage faceDetectionImage = new FaceDetectionImage();
                faceDetectionImage.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
                faceDetectionImage.setFps(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FPS)));
                faceDetectionImage.setTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)));
                faceDetectionImageArrayList.add(faceDetectionImage);
            } while (cursor.moveToNext());
        }
        if (cursor != null || !cursor.isClosed()) {
            cursor.close();
        }
        return faceDetectionImageArrayList;
    }

    /**
     * IPU mode Face Detection
     */

    public long addIPUFaceDetection(String name, String time, String fps) {
        long createResult = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME_IPU, name);
        initialValues.put(KEY_TIME_IPU, time);
        initialValues.put(KEY_FPS_IPU, fps);
        try {
            createResult = mSQLiteDatabase.insert(SQLITE_TABLE_IPU, null, initialValues);
        } catch (Exception e) {
            //handle exception
        }
        return createResult;
    }

    /**
     * 删除所有字段
     *
     * @return
     */

    public boolean deleteAllIPUFaceDetection() {
        int doneDelete = 0;
        try {
            doneDelete = mSQLiteDatabase.delete(SQLITE_TABLE_IPU, null, null);
        } catch (Exception e) {

        }
        return doneDelete > 0;
    }

    /**
     * 获取表中所有
     *
     * @return
     */
    public ArrayList<FaceDetectionImage> fetchIPUAll() {

        ArrayList<FaceDetectionImage> faceDetectionImageArrayList = new ArrayList<>();
        Cursor cursor = null;
        cursor = mSQLiteDatabase.query(SQLITE_TABLE_IPU, new String[]{KEY_ROWID_IPU, KEY_NAME_IPU, KEY_FPS_IPU, KEY_TIME_IPU},
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                FaceDetectionImage faceDetectionImage = new FaceDetectionImage();
                faceDetectionImage.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME_IPU)));
                faceDetectionImage.setFps(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FPS_IPU)));
                faceDetectionImage.setTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME_IPU)));
                faceDetectionImageArrayList.add(faceDetectionImage);
            } while (cursor.moveToNext());
        }
        if (cursor != null || !cursor.isClosed()) {
            cursor.close();
        }
        return faceDetectionImageArrayList;
    }
}
