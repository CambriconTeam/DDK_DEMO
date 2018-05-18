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
import com.cambricon.productdisplay.bean.DetectionImage;
import com.cambricon.productdisplay.db.ClassificationDB;
import com.cambricon.productdisplay.db.DetectionDB;
import com.cambricon.productdisplay.utils.Config;
import com.cambricon.productdisplay.utils.ConvertUtil;
import com.cambricon.productdisplay.utils.FileUtils;
import com.cambricon.productdisplay.utils.RootUtil;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dell on 18-4-10.
 */

public class OffLineDetectionAct extends BaseActivity {
    private final String TAG = "OffLineDetection";
    private int sumTime = 0;
    private DetectionDB offlineDB;
    private static boolean isRooted = false;
    private final int SHOW_OFFLINE_DATA = 0x01;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_OFFLINE_DATA:
                    try {
                        offLineProcess();
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
        offlineDB = new DetectionDB(getApplicationContext());
        offlineDB.open();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        if (RootUtil.getRoot(getPackageCodePath())) {
            isRooted = true;
        } else {
            isRooted = false;
        }
    }

    private void setListener() {
        basebtn_begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRooted) {
                    basebtn_begin.setVisibility(View.GONE);
                    basebtn_end.setVisibility(View.VISIBLE);
                    runOffline();
                    basebtn_end.setText("检测中...");
                    basebtn_end.setClickable(false);
                } else {
                    Toast.makeText(getApplicationContext(), "Get Root first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        basebtn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(OffLineDetectionAct.this, "test end", Toast.LENGTH_SHORT).show();
                basebtn_end.setVisibility(View.GONE);
                basebtn_begin.setVisibility(View.VISIBLE);
            }
        });
    }

    public void runOffline() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cmd = "su -s sh  -c /data/test/offline/detection/offline_detect.sh";
                    Process proc = Runtime.getRuntime().exec(cmd);
                    proc.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.what = SHOW_OFFLINE_DATA;
                handler.sendMessage(message);
            }
        }).start();
    }

    private void offLineProcess() throws IOException {
        //数据信息展示
        function_text.setVisibility(View.GONE);
        testPro.setText("图片分类数据显示...");
        testTime.setVisibility(View.VISIBLE);
        textFps.setVisibility(View.VISIBLE);
        ipu_text_pro.setVisibility(View.VISIBLE);
        ipu_progress.setVisibility(View.VISIBLE);
        try {
            final ArrayList<DetectionImage> detectOfflineImages = FileUtils.readDetectionIPUTxt(Config.offline_detect_result);
            int i = 0;
            for (final DetectionImage image : detectOfflineImages) {
                final String time = image.getTime();
                final int delay = Integer.valueOf(time) / 1000;
                //offline状态下，测试时间单位是us,转换成fps的单位设为张/秒
                //final int fps = ConvertUtil.getFps(image.getFps())*1000/60;
                final int fps = 1000 * 1000 / Integer.parseInt(time) * 60;
                Log.e("huangyaling", "offline fps=" + fps + ";image" + image.getFps());
                sumTime += delay / 1000;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (ipu_progress != null) {
                            ipu_progress.setVisibility(View.GONE);
                            ipu_text_pro.setVisibility(View.GONE);
                        }
                        base_img.setImageBitmap(BitmapFactory.decodeFile(image.getName()));
                        testTime.setText(getResources().getString(R.string.test_time) + delay + "ms");
                        textFps.setText(getResources().getString(R.string.test_fps) + fps + getResources().getString(R.string.test_fps_units));
                        offlineDB.addOfflineDetection(image.getName(), image.getTime(), String.valueOf(fps), image.getNetType());
                    }
                }, sumTime);

                if (i == detectOfflineImages.size() - 1) {
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
