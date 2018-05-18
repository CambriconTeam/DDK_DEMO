package com.cambricon.productdisplay.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by dell on 18-2-5.
 */

public class CommDB {

    public static final String DATABASE_NAME = "BenchMarkDB.db";
    public static final int DATABASE_VERSION = 10;
    //创建图片分类表
    private static final String CREATE_TABLE_Classification = "CREATE TABLE if not exists " + ClassificationDB.SQLITE_TABLE + " (" +
            ClassificationDB.KEY_ROWID + " integer PRIMARY KEY autoincrement," +
            ClassificationDB.KEY_NAME + "," +
            ClassificationDB.KEY_TIME + "," +
            ClassificationDB.KEY_FPS + "," +
            ClassificationDB.KEY_RESULT + "," +
            " UNIQUE (" + ClassificationDB.KEY_NAME + ")" + "ON CONFLICT REPLACE" + ");";

    private static final String CREATE_TABLE_IPU_Classification = "CREATE TABLE if not exists " + ClassificationDB.SQLITE_TABLE_IPU + " (" +
            ClassificationDB.KEY_ROWID_IPU + " integer PRIMARY KEY autoincrement," +
            ClassificationDB.KEY_NAME_IPU + "," +
            ClassificationDB.KEY_TIME_IPU + "," +
            ClassificationDB.KEY_FPS_IPU + "," +
            ClassificationDB.KEY_RESULT_IPU + "," +
            " UNIQUE (" + ClassificationDB.KEY_NAME_IPU + ")" + "ON CONFLICT REPLACE" + ");";

    private static final String CREATE_TABLE_OFFLINE_Classification = "CREATE TABLE if not exists " + ClassificationDB.SQLITE_TABLE_OFFLINE + " (" +
            ClassificationDB.KEY_ROWID_OFFLINE + " integer PRIMARY KEY autoincrement," +
            ClassificationDB.KEY_NAME_OFFLINE + "," +
            ClassificationDB.KEY_TIME_OFFLINE + "," +
            ClassificationDB.KEY_FPS_OFFLINE + "," +
            ClassificationDB.KEY_RESULT_OFFLINE + "," +
            " UNIQUE (" + ClassificationDB.KEY_NAME_OFFLINE + ")" + "ON CONFLICT REPLACE" + ");";
    //单层模型
    private static final String CREATE_TABLE_SIMPLE_CPU_CLASSIFICATION = "CREATE TABLE if not exists " + ClassificationDB.SQLITE_TABLE_SIMPLE_CPU + " (" +
            ClassificationDB.KEY_ROWID_SIMPLE_CPU + " integer PRIMARY KEY autoincrement," +
            ClassificationDB.KEY_NAME_SIMPLE_CPU + "," +
            ClassificationDB.KEY_TIME_SIMPLE_CPU + "," +
            ClassificationDB.KEY_FPS_SIMPLE_CPU + "," +
            ClassificationDB.KEY_RESULT_SIMPLE_CPU + "," +
            " UNIQUE (" + ClassificationDB.KEY_NAME_SIMPLE_CPU + ")" + "ON CONFLICT REPLACE" + ");";

    //单层IPU
    private static final String CREATE_TABLE_SIMPLE_IPU_CLASSIFICATION = "CREATE TABLE if not exists " + ClassificationDB.SQLITE_TABLE_SIMPLE_IPU + " (" +
            ClassificationDB.KEY_ROWID_SIMPLE_IPU + " integer PRIMARY KEY autoincrement," +
            ClassificationDB.KEY_NAME_SIMPLE_IPU + "," +
            ClassificationDB.KEY_TIME_SIMPLE_IPU + "," +
            ClassificationDB.KEY_FPS_SIMPLE_IPU + "," +
            ClassificationDB.KEY_RESULT_SIMPLE_IPU + "," +
            " UNIQUE (" + ClassificationDB.KEY_NAME_SIMPLE_IPU + ")" + "ON CONFLICT REPLACE" + ");";

    //创建目标检测分类表
    private static final String CREATE_TABLE_Detection = "CREATE TABLE if not exists " + DetectionDB.SQLITE_TABLE + " (" +
            DetectionDB.KEY_ROWID + " integer PRIMARY KEY autoincrement," +
            DetectionDB.KEY_NAME + "," +
            DetectionDB.KEY_TIME + "," +
            DetectionDB.KEY_FPS + "," +
            DetectionDB.KEY_NETTYPE + "," +
            " UNIQUE (" + DetectionDB.KEY_NAME + ")" + "ON CONFLICT REPLACE" + ");";

    private static final String CREATE_TABLE_IPU_Detection = "CREATE TABLE if not exists " + DetectionDB.SQLITE_TABLE_IPU + " (" +
            DetectionDB.KEY_ROWID_IPU + " integer PRIMARY KEY autoincrement," +
            DetectionDB.KEY_NAME_IPU + "," +
            DetectionDB.KEY_TIME_IPU + "," +
            DetectionDB.KEY_FPS_IPU + "," +
            DetectionDB.KEY_NETTYPE_IPU + "," +
            " UNIQUE (" + DetectionDB.KEY_NAME_IPU + ")" + "ON CONFLICT REPLACE" + ");";
    private static final String CREATE_TABLE_OFFLINE_Detection = "CREATE TABLE if not exists " + DetectionDB.SQLITE_TABLE_OFFLINE + " (" +
            DetectionDB.KEY_ROWID_OFFLINE + " integer PRIMARY KEY autoincrement," +
            DetectionDB.KEY_NAME_OFFLINE + "," +
            DetectionDB.KEY_TIME_OFFLINE + "," +
            DetectionDB.KEY_FPS_OFFLINE + "," +
            DetectionDB.KEY_NETTYPE_OFFLINE + "," +
            " UNIQUE (" + DetectionDB.KEY_NAME_OFFLINE + ")" + "ON CONFLICT REPLACE" + ");";

    //创建人脸检测分类表
    private static final String CREATE_TABLE_Face_Detection = "CREATE TABLE if not exists " + FaceDetectDB.SQLITE_TABLE + " (" +
            FaceDetectDB.KEY_ROWID + " integer PRIMARY KEY autoincrement," +
            FaceDetectDB.KEY_NAME + "," +
            FaceDetectDB.KEY_TIME + "," +
            FaceDetectDB.KEY_FPS + "," +
            " UNIQUE (" + FaceDetectDB.KEY_NAME + ")" + "ON CONFLICT REPLACE" + ");";

    private static final String CREATE_TABLE_IPU_Face_Detection = "CREATE TABLE if not exists " + FaceDetectDB.SQLITE_TABLE_IPU + " (" +
            FaceDetectDB.KEY_ROWID_IPU + " integer PRIMARY KEY autoincrement," +
            FaceDetectDB.KEY_NAME_IPU + "," +
            FaceDetectDB.KEY_TIME_IPU + "," +
            FaceDetectDB.KEY_FPS_IPU + "," +
            " UNIQUE (" + FaceDetectDB.KEY_NAME_IPU + ")" + "ON CONFLICT REPLACE" + ");";

    private final Context context;
    private DataBaseHelper dataBaseHelper;
    private SQLiteDatabase db;

    public CommDB(Context context) {
        this.context = context;
        this.dataBaseHelper = new DataBaseHelper(this.context);
    }

    private class DataBaseHelper extends SQLiteOpenHelper {
        private final String TAG = "DataBaseHelper";

        DataBaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_TABLE_Classification);
            sqLiteDatabase.execSQL(CREATE_TABLE_IPU_Classification);
            sqLiteDatabase.execSQL(CREATE_TABLE_Detection);
            sqLiteDatabase.execSQL(CREATE_TABLE_IPU_Detection);
            sqLiteDatabase.execSQL(CREATE_TABLE_Face_Detection);
            sqLiteDatabase.execSQL(CREATE_TABLE_IPU_Face_Detection);
            sqLiteDatabase.execSQL(CREATE_TABLE_OFFLINE_Classification);
            sqLiteDatabase.execSQL(CREATE_TABLE_SIMPLE_CPU_CLASSIFICATION);
			sqLiteDatabase.execSQL(CREATE_TABLE_OFFLINE_Detection);
			sqLiteDatabase.execSQL(CREATE_TABLE_SIMPLE_IPU_CLASSIFICATION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }

    /**
     * open db
     *
     * @return
     * @throws SQLException
     */

    public CommDB open() throws SQLException {
        this.db = this.dataBaseHelper.getWritableDatabase();
        return this;
    }

    /**
     * close db
     */
    public void close() {
        if (this.dataBaseHelper != null) {
            this.dataBaseHelper.close();
        }
    }
}
