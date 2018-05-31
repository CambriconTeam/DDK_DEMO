package com.cambricon.productdisplay.activity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.bean.ClassificationImage;
import com.cambricon.productdisplay.caffenative.OffLineCaffeClassification;
import com.cambricon.productdisplay.db.ClassificationDB;
import com.cambricon.productdisplay.task.CNNListener;
import com.cambricon.productdisplay.utils.Config;
import com.cambricon.productdisplay.utils.ConvertUtil;
import com.cambricon.productdisplay.utils.DialogUtil;
import com.cambricon.productdisplay.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

/**
 * Created by dell on 18-4-10.
 */

public class OffLineClassifiactionAct extends BaseActivity implements CNNListener{
    private final String TAG="OffLineClassifiaction";
    private int sumTime = 0;
    private ClassificationDB offlineDB;
    private static boolean isRooted=false;
    private final int SHOW_OFFLINE_DATA = 0x01;
    private OffLineCaffeClassification offLineCaffeClassification;
    private AssetManager mgr;
    private static final int USING_SYNC = 1;
    private File imageFile = new File(Config.sdcard, Config.imageName[0]);
    private Bitmap bmp;
    public static int startIndex = 0;
    private String predictedClass = "none";
    private static final int RESIZED_WIDTH = 227;
    private static final int RESIZED_HEIGHT = 227;
    private Bitmap rgba;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setListener();
        //initLabels();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = offLineCaffeClassification.loadModelSync(mgr);
                if (0 == ret) {
                    //isModelSyncLoaded = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OffLineClassifiactionAct.this, "load model sync success.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OffLineClassifiactionAct.this, "load model sync fail.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void init(){
        offlineDB=new ClassificationDB(getApplicationContext());
        offlineDB.open();
        offLineCaffeClassification=new OffLineCaffeClassification();
        //offLineCaffeClassification.createModelClient(USING_SYNC);
        //mgr = getResources().getAssets();
    }

    /**
     * 传入检测图片
     */
    private void executeImg() {
        imageFile = new File(Config.imagePath, Config.imageName[startIndex]);
        bmp = BitmapFactory.decodeFile(imageFile.getPath());
        rgba = bmp.copy(Bitmap.Config.ARGB_8888, true);
        ClassifyTask classifyTask=new ClassifyTask(OffLineClassifiactionAct.this);
        classifyTask.execute(imageFile.getPath());
    }

    private void setListener(){
        basebtn_begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Config.getIsCPUMode(getApplicationContext())){

                        basebtn_begin.setVisibility(View.GONE);
                        basebtn_end.setVisibility(View.VISIBLE);
                        //runOffline();
                        basebtn_end.setText("检测中...");
                        basebtn_end.setClickable(false);
                        executeImg();
                }else{
                    DialogUtil.showDialog(OffLineClassifiactionAct.this,"操作提醒","需要在主页面打开IPU模式","确定");
                }
            }
        });
        basebtn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(OffLineClassifiactionAct.this,"test end",Toast.LENGTH_SHORT).show();
                basebtn_end.setVisibility(View.GONE);
                basebtn_begin.setVisibility(View.VISIBLE);
            }
        });
    }

    //huangyaling add for ddk begin
    private void initLabels() {
        byte[] labels;
        try {
            InputStream assetsInputStream = getAssets().open("synset_words.txt");
            int available = assetsInputStream.available();
            labels = new byte[available];
            assetsInputStream.read(labels);
            assetsInputStream.close();
            offLineCaffeClassification.initLabels(labels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskCompleted(int result) {
        offLineProcess();
        testResult.setText(getResources().getString(R.string.test_result) + predictedClass);
    }

    private class ClassifyTask extends AsyncTask<String, Void, Integer> {

        private CNNListener listener;
        private long startTime;

        public ClassifyTask(CNNListener listener) {
            this.listener = listener;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            startTime = SystemClock.uptimeMillis();
            final Bitmap initClassifiedImg = Bitmap.createScaledBitmap(rgba, RESIZED_WIDTH, RESIZED_HEIGHT, false);
            final float[] pixels = getPixel(initClassifiedImg, RESIZED_WIDTH, RESIZED_HEIGHT);
            predictedClass =  offLineCaffeClassification.runModelSync(pixels);
            Log.e("huangyaling:",predictedClass);
            return 1;
            //return caffeMobile.predictImage(strings[0])[0];
        }

        @Override
        protected void onPostExecute(Integer integer) {
           // classificationTime = SystemClock.uptimeMillis() - startTime;
            listener.onTaskCompleted(integer);
            super.onPostExecute(integer);
        }
    }


    /**
     NPU data format is:NCHW
     the demo code here use BGR causes of we use Caffe(caffe uses BGR)
     */
    private float[] getPixel(Bitmap bitmap, int resizedWidth, int resizedHeight) {
        int channel = 3;
        float[] buff = new float[channel * resizedWidth * resizedHeight];

        int rIndex, gIndex, bIndex;
        for (int i = 0; i < resizedHeight; i++) {
            for (int j = 0; j < resizedWidth; j++) {
                bIndex = i * resizedWidth + j;
                gIndex = bIndex + resizedWidth * resizedHeight;
                rIndex = gIndex + resizedWidth * resizedHeight;

                int color = bitmap.getPixel(j, i);

                buff[bIndex] = (float) (red(color) - 123.68);
                buff[gIndex] = (float) (green(color) - 116.78);
                buff[rIndex] = (float) (blue(color) - 103.94);
            }
        }
        return buff;
    }

    private void offLineProcess(){
        Log.i(TAG, "IPUProcess: ");
        //数据信息展示
        function_text.setVisibility(View.GONE);
        testPro.setText("图片分类数据显示...");
        testResult.setVisibility(View.VISIBLE);
        testTime.setVisibility(View.VISIBLE);
        textFps.setVisibility(View.VISIBLE);
        base_img.setImageBitmap(bmp);
    }
}
