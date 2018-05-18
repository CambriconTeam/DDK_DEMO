package com.cambricon.productdisplay.caffenative;

import android.content.res.AssetManager;

/**
 * Created by dell on 18-5-9.
 */

public class OffLineCaffeClassification {
    static {
        System.loadLibrary("hiaiddk");
    }
    public static native int loadModelSync(AssetManager mgr);

    public static native int loadModelSyncFromSdcard();

    public static native String runModelSync(float[] buf);

    public static native int stopModelSync();

    public static native int loadModelAsync(AssetManager mgr);

    public static native String runModelAsync(float[] buf);

    public static native int stopModelAsync();

    public native void initLabels(byte[] words);

    public native void createModelClient(int isSync);

    public native void destroyModelClient(int isSync);
}
