package com.cambricon.productdisplay.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cambricon.productdisplay.bean.ClassificationImage;

import java.util.ArrayList;

/**
 * Created by dell on 18-2-5.
 */

public class ClassificationDB {
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private final Context mContext;

    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TIME = "time";
    public static final String KEY_FPS = "fps";
    public static final String KEY_RESULT = "result";
    public static final String SQLITE_TABLE = "ClassificationTable";

    //ipu mode
    public static final String KEY_ROWID_IPU="_id_ipu";
    public static final String KEY_NAME_IPU = "name_ipu";
    public static final String KEY_TIME_IPU = "time_ipu";
    public static final String KEY_FPS_IPU = "fps_ipu";
    public static final String KEY_RESULT_IPU = "result_ipu";
    public static final String SQLITE_TABLE_IPU = "ClassificationIPUTable";

    //simple_cpu
    public static final String KEY_ROWID_SIMPLE_CPU="_id_simple_cpu";
    public static final String KEY_NAME_SIMPLE_CPU = "name_ipu";
    public static final String KEY_TIME_SIMPLE_CPU = "time_ipu";
    public static final String KEY_FPS_SIMPLE_CPU = "fps_ipu";
    public static final String KEY_RESULT_SIMPLE_CPU = "result_ipu";
    public static final String SQLITE_TABLE_SIMPLE_CPU = "ClassifySimpleCPUTable";

    //simple_ipu
    public static final String KEY_ROWID_SIMPLE_IPU="_id_simple_ipu";
    public static final String KEY_NAME_SIMPLE_IPU = "name_ipu";
    public static final String KEY_TIME_SIMPLE_IPU = "time_ipu";
    public static final String KEY_FPS_SIMPLE_IPU = "fps_ipu";
    public static final String KEY_RESULT_SIMPLE_IPU = "result_ipu";
    public static final String SQLITE_TABLE_SIMPLE_IPU = "ClassifySimpleIPUTable";

    //offline
    public static final String KEY_ROWID_OFFLINE="_id_offline";
    public static final String KEY_NAME_OFFLINE = "name_offline";
    public static final String KEY_TIME_OFFLINE = "time_offline";
    public static final String KEY_FPS_OFFLINE = "fps_offline";
    public static final String KEY_RESULT_OFFLINE = "result_offline";
    public static final String SQLITE_TABLE_OFFLINE = "ClassificationOfflineTable";



    public ClassificationDB(Context context) {
        this.mContext = context;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, CommDB.DATABASE_NAME, null, CommDB.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
            onCreate(sqLiteDatabase);
        }
    }

    public ClassificationDB open() throws SQLException {
        databaseHelper = new DatabaseHelper(mContext);
        db = databaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    /**
     * 创建分类表字段
     *
     * @param name
     * @param time
     * @param fps
     * @param result
     * @return
     */

    public long addClassification(String name, String time, String fps, String result) {
        long createResult = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_FPS, fps);
        initialValues.put(KEY_RESULT, result);
        try {
            createResult = db.insert(SQLITE_TABLE, null, initialValues);
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

    public boolean deleteAllClassification() {
        int doneDelete = 0;
        try {
            doneDelete = db.delete(SQLITE_TABLE, null, null);
        } catch (Exception e) {

        }
        return doneDelete > 0;
    }

    /**
     * 删除表中字段
     *
     * @param name
     * @return
     */

    public boolean deleteTicketByName(String name) {
        int isDelete;
        String[] tname;
        tname = new String[]{name};
        isDelete = db.delete(SQLITE_TABLE, KEY_NAME + "=?", tname);
        return isDelete > 0;
    }

    /**
     * 获取表中所有
     *
     * @return
     */
    public ArrayList<ClassificationImage> fetchAll() {
        ArrayList<ClassificationImage> allTicketsList = new ArrayList<>();
        Cursor mCursor = null;
        mCursor = db.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_NAME,KEY_TIME, KEY_FPS, KEY_RESULT},
                null, null, null, null, null);

        if (mCursor.moveToFirst()) {
            do {
                ClassificationImage cfimage = new ClassificationImage();
                cfimage.setName(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_NAME)));
                cfimage.setFps(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_FPS)));
                cfimage.setResult(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_RESULT)));
                cfimage.setTime(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_TIME)));
                allTicketsList.add(cfimage);
            } while (mCursor.moveToNext());
        }

        if(mCursor!=null||!mCursor.isClosed()){
            mCursor.close();
        }
        return allTicketsList;
    }

    //ipu table
    public long addIPUClassification(String name, String time, String fps, String result) {
        long createResult = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME_IPU, name);
        initialValues.put(KEY_TIME_IPU, time);
        initialValues.put(KEY_FPS_IPU, fps);
        initialValues.put(KEY_RESULT_IPU, result);
        try {
            createResult = db.insert(SQLITE_TABLE_IPU, null, initialValues);
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

    public boolean deleteAllIPUClassification() {
        int doneDelete = 0;
        try {
            doneDelete = db.delete(SQLITE_TABLE_IPU, null, null);
        } catch (Exception e) {

        }
        return doneDelete > 0;
    }

    /**
     * 删除表中字段
     *
     * @param name
     * @return
     */

    public boolean deleteTicketIPUByName(String name) {
        int isDelete;
        String[] tname;
        tname = new String[]{name};
        isDelete = db.delete(SQLITE_TABLE_IPU, KEY_NAME_IPU + "=?", tname);
        return isDelete > 0;
    }

    /**
     * 获取表中所有
     *
     * @return
     */
    public ArrayList<ClassificationImage> fetchIPUAll() {
        ArrayList<ClassificationImage> allTicketsList = new ArrayList<>();
        Cursor mCursor = null;
        mCursor = db.query(SQLITE_TABLE_IPU, new String[]{KEY_ROWID_IPU, KEY_NAME_IPU,KEY_TIME_IPU, KEY_FPS_IPU, KEY_RESULT_IPU},
                null, null, null, null, null);

        if (mCursor.moveToFirst()) {
            do {
                ClassificationImage cfimage = new ClassificationImage();
                cfimage.setName(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_NAME_IPU)));
                cfimage.setFps(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_FPS_IPU)));
                cfimage.setResult(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_RESULT_IPU)));
                cfimage.setTime(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_TIME_IPU)));
                allTicketsList.add(cfimage);
            } while (mCursor.moveToNext());
        }

        if(mCursor!=null||!mCursor.isClosed()){
            mCursor.close();
        }
        return allTicketsList;
    }

    //offline table
    /**
     * 添加offline数据信息
     * @param name
     * @param time
     * @param fps
     * @param result
     * @return
     */
    public long addOfflineClassification(String name, String time, String fps, String result) {
        long createResult = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME_OFFLINE, name);
        initialValues.put(KEY_TIME_OFFLINE, time);
        initialValues.put(KEY_FPS_OFFLINE, fps);
        initialValues.put(KEY_RESULT_OFFLINE, result);
        try {
            createResult = db.insert(SQLITE_TABLE_OFFLINE, null, initialValues);
        } catch (Exception e) {
            //handle exception
        }
        return createResult;
    }

    /**
     * 获取表中所有
     *
     * @return
     */
    public ArrayList<ClassificationImage> fetchOfflineAll() {
        ArrayList<ClassificationImage> allTicketsList = new ArrayList<>();
        Cursor mCursor = null;
        mCursor = db.query(SQLITE_TABLE_OFFLINE, new String[]{KEY_ROWID_OFFLINE, KEY_NAME_OFFLINE,KEY_TIME_OFFLINE, KEY_FPS_OFFLINE, KEY_RESULT_OFFLINE},
                null, null, null, null, null);

        if (mCursor.moveToFirst()) {
            do {
                ClassificationImage cfimage = new ClassificationImage();
                cfimage.setName(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_NAME_OFFLINE)));
                cfimage.setFps(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_FPS_OFFLINE)));
                cfimage.setResult(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_RESULT_OFFLINE)));
                cfimage.setTime(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_TIME_OFFLINE)));
                allTicketsList.add(cfimage);
            } while (mCursor.moveToNext());
        }

        if(mCursor!=null||!mCursor.isClosed()){
            mCursor.close();
        }
        return allTicketsList;
    }

    //simple_cpu_table
    /**
     * 添加offline数据信息
     * @param name
     * @param time
     * @param fps
     * @param result
     * @return
     */
    public long addCPUSimpleClassification(String name, String time, String fps, String result) {
        long createResult = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME_SIMPLE_CPU, name);
        initialValues.put(KEY_TIME_SIMPLE_CPU, time);
        initialValues.put(KEY_FPS_SIMPLE_CPU, fps);
        initialValues.put(KEY_RESULT_SIMPLE_CPU, result);
        try {
            createResult = db.insert(SQLITE_TABLE_SIMPLE_CPU, null, initialValues);
        } catch (Exception e) {
            //handle exception
        }
        return createResult;
    }

    /**
     * 获取表中所有
     *
     * @return
     */
    public ArrayList<ClassificationImage> fetchCPUSimplelineAll() {
        ArrayList<ClassificationImage> allTicketsList = new ArrayList<>();
        Cursor mCursor = null;
        mCursor = db.query(SQLITE_TABLE_SIMPLE_CPU, new String[]{KEY_ROWID_SIMPLE_CPU, KEY_NAME_SIMPLE_CPU,KEY_TIME_SIMPLE_CPU, KEY_FPS_SIMPLE_CPU, KEY_RESULT_SIMPLE_CPU},
                null, null, null, null, null);

        if (mCursor.moveToFirst()) {
            do {
                ClassificationImage cfimage = new ClassificationImage();
                cfimage.setName(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_NAME_SIMPLE_CPU)));
                cfimage.setFps(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_FPS_SIMPLE_CPU)));
                cfimage.setResult(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_RESULT_SIMPLE_CPU)));
                cfimage.setTime(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_TIME_SIMPLE_CPU)));
                allTicketsList.add(cfimage);
            } while (mCursor.moveToNext());
        }

        if(mCursor!=null||!mCursor.isClosed()){
            mCursor.close();
        }
        return allTicketsList;
    }

    //simple_ipu_table
    /**
     * 添加offline数据信息
     * @param name
     * @param time
     * @param fps
     * @param result
     * @return
     */
    public long addIPUSimpleClassification(String name, String time, String fps, String result) {
        long createResult = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME_SIMPLE_IPU, name);
        initialValues.put(KEY_TIME_SIMPLE_IPU, time);
        initialValues.put(KEY_FPS_SIMPLE_IPU, fps);
        initialValues.put(KEY_RESULT_SIMPLE_IPU, result);
        try {
            createResult = db.insert(SQLITE_TABLE_SIMPLE_IPU, null, initialValues);
        } catch (Exception e) {
            //handle exception
        }
        return createResult;
    }

    /**
     * 获取表中所有
     *
     * @return
     */
    public ArrayList<ClassificationImage> fetchIPUSimplelineAll() {
        ArrayList<ClassificationImage> allTicketsList = new ArrayList<>();
        Cursor mCursor = null;
        mCursor = db.query(SQLITE_TABLE_SIMPLE_IPU, new String[]{KEY_ROWID_SIMPLE_IPU, KEY_NAME_SIMPLE_IPU,KEY_TIME_SIMPLE_IPU, KEY_FPS_SIMPLE_IPU, KEY_RESULT_SIMPLE_IPU},
                null, null, null, null, null);

        if (mCursor.moveToFirst()) {
            do {
                ClassificationImage cfimage = new ClassificationImage();
                cfimage.setName(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_NAME_SIMPLE_IPU)));
                cfimage.setFps(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_FPS_SIMPLE_IPU)));
                cfimage.setResult(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_RESULT_SIMPLE_IPU)));
                cfimage.setTime(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_TIME_SIMPLE_IPU)));
                allTicketsList.add(cfimage);
            } while (mCursor.moveToNext());
        }

        if(mCursor!=null||!mCursor.isClosed()){
            mCursor.close();
        }
        return allTicketsList;
    }
}
