package com.cambricon.productdisplay.utils;

import android.util.Log;

/**
 * 日志工具类
 */

public class LogUtil {

    private static final int VERBOSE = 1;
    private static final int DEBUG = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;
    private static final int NOTHING = 6;

    //日志输出当前级别
    public static final int level = VERBOSE;

    private static final String TAG = "ProductDisPlay";

    private LogUtil() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 默认TAG方法
     * @param msg
     */
    public static void v(String msg)
    {
        if (level <= VERBOSE)
            Log.v(TAG, msg);
    }

    public static void d(String msg)
    {
        if (level <= DEBUG)
            Log.d(TAG, msg);
    }

    public static void i(String msg)
    {
        if (level <= INFO)
            Log.i(TAG, msg);
    }

    public static void w(String msg)
    {
        if (level <= WARN)
            Log.w(TAG, msg);
    }

    public static void e(String msg)
    {
        if (level <= ERROR)
            Log.e(TAG, msg);
    }

    /**
     * 传入自定义tag方法
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg)
    {
        if (level <= INFO)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg)
    {
        if (level <= DEBUG)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg)
    {
        if (level <= ERROR)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg)
    {
        if (level <= VERBOSE)
            Log.v(tag, msg);
    }

    public static void w(String tag, String msg)
    {
        if (level <= WARN)
            Log.w(tag, msg);
    }

}
