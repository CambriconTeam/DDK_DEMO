package com.cambricon.productdisplay.caffenative;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by dell on 18-1-31.
 */

public class CaffeClassification {
    // Used to load the 'native-lib' library on application startup.
    static {
        //System.loadLibrary("caffe-jni");
        System.loadLibrary("native-lib");
    }

    private static String TAG = "CaffeClassification";
    public native boolean loadModel(String modelPath, String weightPath,boolean mode);
    public native void setBlasThreadNum(int numThreads);
    public native int inputChannels();
    public native int inputWidth();
    //public native int hello();
    public native int inputHeight();
    private native float[] predict(byte[] bitmap, int channels, float[]mean);
    public native void hello();


    public float[] predictImage(String fileName, float[] mean) {
        CaffeImage image = readImage(fileName);
        float[] result = predict(image.pixels, image.channels, mean);
        return result;
    }

    class CaffeImage {
        int channels;
        int width;
        int height;
        byte[] pixels;
    };

    /**
     * @brief Read a image from file to BGR pixels buffer (OpenCV)
     * @param file_name
     * @param channels
     * @return
     */
    protected CaffeImage readImage(String file_name) {
        Log.i(TAG, "readImage: reading: " + file_name);
        // Read image file to bitmap (in ARGB format)
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inPremultiplied = false;
        //Bitmap bitmap = BitmapFactory.decodeFile(file_name, options);
        Bitmap bitmap_resize = BitmapFactory.decodeFile(file_name, options);
        Bitmap bitmap = getZoomImage(bitmap_resize,28,28);
        // Copy bitmap pixels to buffer
        ByteBuffer argb_buf = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(argb_buf);

        // Generate CaffeImage to classification
        CaffeImage image = new CaffeImage();
        image.width = bitmap.getWidth();
        image.height = bitmap.getHeight();
        image.channels = 4;
        Log.i(TAG, "readImage: image CxWxH: " + image.channels + "x" + image.width + "x" + image.height);
        // Get the underlying array containing the ARGB pixels
        image.pixels = argb_buf.array();
        Log.d(TAG, "readImage: bitmap(0,0)="
                + Integer.toHexString(bitmap.getPixel(0, 0))
                + ", rgba[0,0]="
                + Integer.toHexString((image.pixels[0] << 24 & 0xff000000) | (image.pixels[1] << 16 & 0xff0000)
                | (image.pixels[2] << 8 & 0xff00) | (image.pixels[3] & 0xff) ));
        return image;
    }

    /**
     * resize a bitmap to width:28xp height:28xp
     * @param orgBitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap getZoomImage(Bitmap orgBitmap,double newWidth,double newHeight){
        if(null==orgBitmap){
            return null;
        }
        if(newWidth<=0||newHeight<=0){
            return null;
        }

        float width=orgBitmap.getWidth();
        float height=orgBitmap.getHeight();
        Matrix matrix=new Matrix();
        float scaleWidth=((float) newWidth)/width;
        float scaleHeight=((float) newHeight)/height;
        matrix.postScale(scaleWidth,scaleHeight);
        Log.i(TAG,"width="+width+";height="+height+";scaleWidth="+scaleWidth+";scaleHeight="+scaleHeight);
        Bitmap bitmap =Bitmap.createBitmap(orgBitmap,0,0,(int)width,(int)height,matrix,true);
        return bitmap;
    }
}
