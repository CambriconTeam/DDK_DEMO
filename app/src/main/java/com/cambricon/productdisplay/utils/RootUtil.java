package com.cambricon.productdisplay.utils;

import android.util.Log;

import java.io.DataOutputStream;

/**
 * Created by dell on 18-4-13.
 */

public class RootUtil {
    public static boolean getRoot(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        Log.e("huangyaling", "get root");
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }
}
