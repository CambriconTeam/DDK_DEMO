package com.cambricon.productdisplay.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cambricon.productdisplay.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dell on 18-6-1.
 */

public class AssetUtil {
    public Context context;
    public AssetUtil(Context context){
        this.context=context;
    }
    public Bitmap getAssetImage(String imageName){
        try {
            InputStream is=context.getAssets().open(imageName);
            Bitmap bitmap= BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getArrayImage(){
        String[] imglist=context.getResources().getStringArray(R.array.asset_image_array);
        return imglist;
    }
}
