package com.cambricon.productdisplay.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cambricon.productdisplay.bean.DetectionImage;

import java.util.ArrayList;

/**
 * Created by dell on 18-2-5.
 */

public class DetectionDB {
    public static final String TAG = "DetectionDB";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TIME = "time";
    public static final String KEY_FPS = "fps";
    public static final String KEY_NETTYPE = "netType";
//    public static final String KEY_RESULT = "result";
    static final String SQLITE_TABLE = "DetectionTable";
    private final Context mContext;

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;

    //ipu mode
    public static final String KEY_ROWID_IPU="_id_ipu";
    public static final String KEY_NAME_IPU = "name_ipu";
    public static final String KEY_TIME_IPU = "time_ipu";
    public static final String KEY_FPS_IPU = "fps_ipu";
    public static final String KEY_NETTYPE_IPU = "netType";
    public static final String SQLITE_TABLE_IPU = "DetectionIPUTable";

    //offline mode
    public static final String KEY_ROWID_OFFLINE="_id_offline";
    public static final String KEY_NAME_OFFLINE = "name_offline";
    public static final String KEY_TIME_OFFLINE = "time_offline";
    public static final String KEY_FPS_OFFLINE = "fps_offline";
    public static final String KEY_NETTYPE_OFFLINE = "netType";
    public static final String SQLITE_TABLE_OFFLINE = "DetectionOfflineTable";

    public DetectionDB(Context context) {
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

    public DetectionDB open() throws SQLException {
        databaseHelper = new DetectionDB.DatabaseHelper(mContext);
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
     * @return
     */

    public long addDetection(String name, String time, String fps, String netType) {
        long createResult = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_FPS, fps);
        initialValues.put(KEY_NETTYPE, netType);

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
        isDelete = db.delete(SQLITE_TABLE, KEY_FPS + "=?", tname);
        return isDelete > 0;
    }

    /**
     * 获取表中所有
     *
     * @return
     */
    public ArrayList<DetectionImage> fetchAll() {
        ArrayList<DetectionImage> allTicketsList = new ArrayList<>();
        Cursor mCursor = null;
        mCursor = db.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_NAME, KEY_FPS, KEY_TIME, KEY_NETTYPE},
                null, null, null, null, null);

        if (mCursor.moveToFirst()) {
            do {
                DetectionImage dtimage = new DetectionImage();
                dtimage.setName(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_NAME)));
                dtimage.setFps(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_FPS)));
                dtimage.setTime(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_TIME)));
                dtimage.setNetType(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_NETTYPE)));

                allTicketsList.add(dtimage);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null || !mCursor.isClosed()) {
            mCursor.close();
        }
        return allTicketsList;
    }


    /*
     * IPU DetectionDB
     */

    public long addIPUClassification(String name, String time, String fps, String netType) {
        long createResult = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME_IPU, name);
        initialValues.put(KEY_TIME_IPU, time);
        initialValues.put(KEY_FPS_IPU, fps);
        initialValues.put(KEY_NETTYPE_IPU, netType);
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
    public ArrayList<DetectionImage> fetchIPUAll() {
        ArrayList<DetectionImage> allTicketsList = new ArrayList<>();
        Cursor mCursor = null;
        mCursor = db.query(SQLITE_TABLE_IPU, new String[]{KEY_ROWID_IPU, KEY_NAME_IPU,KEY_TIME_IPU, KEY_FPS_IPU,KEY_NETTYPE_IPU},
                null, null, null, null, null);

        if (mCursor.moveToFirst()) {
            do {
                DetectionImage cfimage = new DetectionImage();
                cfimage.setName(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_NAME_IPU)));
                cfimage.setFps(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_FPS_IPU)));
                cfimage.setTime(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_TIME_IPU)));
                cfimage.setNetType(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_NETTYPE_IPU)));
                allTicketsList.add(cfimage);
            } while (mCursor.moveToNext());
        }

        if(mCursor!=null||!mCursor.isClosed()){
            mCursor.close();
        }
        for(int i=0;i<allTicketsList.size();i++){
            Log.d("huangyaling","allTicketsList="+allTicketsList.get(i).getName());
        }
        return allTicketsList;
    }

    //offline db
    public long addOfflineDetection(String name, String time, String fps, String netType) {
        long createResult = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME_OFFLINE, name);
        initialValues.put(KEY_TIME_OFFLINE, time);
        initialValues.put(KEY_FPS_OFFLINE, fps);
        initialValues.put(KEY_NETTYPE_OFFLINE, netType);
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
    public ArrayList<DetectionImage> fetchOfflineAll() {
        ArrayList<DetectionImage> allTicketsList = new ArrayList<>();
        Cursor mCursor = null;
        mCursor = db.query(SQLITE_TABLE_OFFLINE, new String[]{KEY_ROWID_OFFLINE, KEY_NAME_OFFLINE,KEY_TIME_OFFLINE, KEY_FPS_OFFLINE,KEY_NETTYPE_OFFLINE},
                null, null, null, null, null);

        if (mCursor.moveToFirst()) {
            do {
                DetectionImage cfimage = new DetectionImage();
                cfimage.setName(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_NAME_OFFLINE)));
                cfimage.setFps(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_FPS_OFFLINE)));
                cfimage.setTime(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_TIME_OFFLINE)));
                cfimage.setNetType(mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_NETTYPE_OFFLINE)));
                allTicketsList.add(cfimage);
            } while (mCursor.moveToNext());
        }

        if(mCursor!=null||!mCursor.isClosed()){
            mCursor.close();
        }
        for(int i=0;i<allTicketsList.size();i++){
            Log.d("huangyaling","allofflineTicketsList="+allTicketsList.get(i).getName());
        }
        return allTicketsList;
    }
}
