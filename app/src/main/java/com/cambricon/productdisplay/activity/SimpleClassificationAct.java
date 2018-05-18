package com.cambricon.productdisplay.activity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.bean.ClassificationImage;
import com.cambricon.productdisplay.db.ClassificationDB;
import com.cambricon.productdisplay.utils.Config;
import com.cambricon.productdisplay.utils.ConvertUtil;
import com.cambricon.productdisplay.utils.DialogUtil;
import com.cambricon.productdisplay.utils.FileUtils;
import com.cambricon.productdisplay.utils.RootUtil;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dell on 18-4-19.
 */

public class SimpleClassificationAct extends BaseActivity {
    private final String TAG="OffLineClassifiaction";
    private int sumTime = 0;
    private ClassificationDB simpleDB;
    private static boolean isRooted=false;
    private final int SHOW_OFFLINE_DATA = 0x01;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case SHOW_OFFLINE_DATA:
                    try {
                        SimpleProcess();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setListener();
    }

    private void init(){
        simpleDB=new ClassificationDB(getApplicationContext());
        simpleDB.open();
        if(RootUtil.getRoot(getPackageCodePath())){
            isRooted=true;
        }else{
            isRooted=false;
        }
    }

    private void setListener(){
        basebtn_begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Config.getIsCPUMode(getApplicationContext())){
                    if(isRooted){
                        basebtn_begin.setVisibility(View.GONE);
                        basebtn_end.setVisibility(View.VISIBLE);
                        runOffline();
                        basebtn_end.setText("检测中...");
                        basebtn_end.setClickable(false);
                    }else{
                        Toast.makeText(getApplicationContext(), "Get Root first.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    DialogUtil.showDialog(SimpleClassificationAct.this,"操作提醒","需要在主页面打开IPU模式","确定");
                }
            }
        });
        basebtn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SimpleClassificationAct.this,"test end",Toast.LENGTH_SHORT).show();
                basebtn_end.setVisibility(View.GONE);
                basebtn_begin.setVisibility(View.VISIBLE);
            }
        });
    }

    public void runOffline(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cmd = "su -s sh  -c /data/test/caffe_ipu/classification_simply.sh";
                    Log.e("huangyaling", "cmd");
                    Process proc = Runtime.getRuntime().exec(cmd);
                    //proc.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.what = SHOW_OFFLINE_DATA;
                handler.sendMessage(message);
            }
        }).start();
    }

    private void SimpleProcess() throws IOException {
        Log.i(TAG, "IPUProcess: ");
        //数据信息展示
        function_text.setVisibility(View.GONE);
        testPro.setText("图片分类数据显示...");
        testResult.setVisibility(View.VISIBLE);
        testTime.setVisibility(View.VISIBLE);
        textFps.setVisibility(View.VISIBLE);
        ipu_text_pro.setVisibility(View.VISIBLE);
        ipu_progress.setVisibility(View.VISIBLE);
        //modify for ipu
        try {
            final ArrayList<ClassificationImage> classificationIPUImages = FileUtils.readClassificationIPUTxt(Config.classify_ipu_simple);
            int i = 0;
            for (final ClassificationImage image : classificationIPUImages) {
                final String time = image.getTime();
                final int delay = Integer.valueOf(time);
                final String result = image.getResult();
                final int fps = ConvertUtil.getFps(image.getFps());
                sumTime += delay;
                final int finalI = i;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (ipu_progress != null) {
                            ipu_progress.setVisibility(View.GONE);
                            ipu_text_pro.setVisibility(View.GONE);
                        }
                        base_img.setImageBitmap(BitmapFactory.decodeFile(image.getName()));
                        testResult.setText(getResources().getString(R.string.test_result) + result);
                        testTime.setText(getResources().getString(R.string.test_time) + time + "ms");
                        textFps.setText(getResources().getString(R.string.test_fps) + fps + getResources().getString(R.string.test_fps_units));
                        if (finalI > 0) {
                            simpleDB.addIPUSimpleClassification(image.getName(), image.getTime(), image.getFps(), image.getResult());
                        }
                    }
                }, sumTime);

                if (i == classificationIPUImages.size() - 1) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "检测结束", Toast.LENGTH_SHORT).show();
                            testPro.setText(getString(R.string.detection_end_guide));
                            basebtn_end.setText("停止测试");
                            basebtn_begin.setVisibility(View.VISIBLE);
                            basebtn_end.setVisibility(View.GONE);
                        }
                    }, sumTime);
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
