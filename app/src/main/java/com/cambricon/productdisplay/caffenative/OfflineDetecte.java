package com.cambricon.productdisplay.caffenative;

/**
 * Created by dell on 18-5-18.
 */

public class OfflineDetecte {
    static {
        System.loadLibrary("hiaidetecte");
    }
    public static native int loadModelSyncFromSdcard();
    public native void createModelClient(int isSync);
    public static native int stopModelSync();
    public native void destroyModelClient(int isSync);
}
