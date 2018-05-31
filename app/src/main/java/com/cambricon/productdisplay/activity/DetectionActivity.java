package com.cambricon.productdisplay.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.bean.DetectionImage;
import com.cambricon.productdisplay.caffenative.CaffeDetection;
import com.cambricon.productdisplay.caffenative.OfflineDetecte;
import com.cambricon.productdisplay.db.DetectionDB;
import com.cambricon.productdisplay.task.CNNListener;
import com.cambricon.productdisplay.utils.Config;
import com.cambricon.productdisplay.utils.ConvertUtil;
import com.cambricon.productdisplay.utils.FileUtils;
import com.cambricon.productdisplay.utils.RootUtil;
import com.cambricon.productdisplay.utils.StatusBarCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public class DetectionActivity extends AppCompatActivity implements View.OnClickListener, CNNListener {

    private final String TAG = "DetectionActivity";

    private Toolbar toolbar;
    private TextView testNet;
    private TextView loadCaffe;
    private TextView testTime;
    private TextView function_text;
    private TextView textFps;
    private TextView testPro;
    private Button detection_begin;
    private Button detection_end;
    private ImageView ivCaptured;
    private ProgressBar ipuProgress;
    private TextView ipu_pro_text;
    private boolean isExist = true;
    private boolean isRooted = false;
    public Thread testThread;
    private Bitmap resBitmap;
    private File imageFile;
    private Bitmap bitmap;
    public static int index = 0;
    private CaffeDetection caffeDetection;
    private double detectionTime;
    private DetectionDB detectionDB;
    private long loadDTime;
    private final int START_LOAD_DETECT = 2;
    private final int LOED_DETECT_END = 3;
    private final int IPU_DETECT_END = 4;
    private final int UPDATE_IMG = 5;
    private ArrayList<DetectionImage> mDetectionImageArrayList;
    private Bundle mBundle;

    private static final String BITMAP = "Bitmap";
    private static final String TIME = "Time";
    private static final String FPS = "Fps";
    private static final String NETTYPE = "NetType";

    private static final String FASTRCNN = "Fast-RCNN";
    private static final String RESNET50 = "ResNet50";

    //huangyaling add for offline ipu mode begin
    private OfflineDetecte offlineDetecte;
    private static final int USING_SYNC = 1;
    private boolean isModelSyncLoaded = false;
    //huangyaling add for offline ipu mode end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.colorPrimary));
        setContentView(R.layout.classification_layout);
        Log.i(TAG, "onCreate: ");
        init();
        setActionBar();
    }

    private void init() {
        toolbar = findViewById(R.id.classification_toolbar);
        ivCaptured = findViewById(R.id.classification_img);
        testNet = findViewById(R.id.test_result);
        testTime = findViewById(R.id.test_time);
        loadCaffe = findViewById(R.id.load_caffe);
        function_text = findViewById(R.id.function_describe);
        textFps = findViewById(R.id.test_fps);
        testPro = findViewById(R.id.test_guide);
        detection_begin = findViewById(R.id.classification_begin);
        detection_end = findViewById(R.id.classification_end);
        ipuProgress = findViewById(R.id.ipu_progress);
        ipu_pro_text = findViewById(R.id.ipu_pro_text);

        loadCaffe.setText("");
        testNet.setText(getString(R.string.decete_type) + String.valueOf(getIntent().getSerializableExtra("netType")));
        testPro.setText(R.string.detection_result);
        function_text.setText(R.string.detection_introduce);
        testNet.setText(R.string.decete_type);

        detection_begin.setOnClickListener(this);
        detection_end.setOnClickListener(this);

        detectionDB = new DetectionDB(getApplicationContext());
        detectionDB.open();

        caffeDetection = new CaffeDetection();
        offlineDetecte=new OfflineDetecte();
    }

    /**
     * 设置ActionBar
     */
    private void setActionBar() {
        String mode = Config.getIsCPUMode(DetectionActivity.this) ? getString(R.string.cpu_mode) : getString(R.string.ipu_mode);
        toolbar.setTitle(getString(R.string.detection_title) + "--" + mode);
        Drawable toolDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.toolbar_bg);
        toolDrawable.setAlpha(50);
        toolbar.setBackground(toolDrawable);
        setSupportActionBar(toolbar);
        /*显示Home图标*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.classification_begin:
                testDetect();
                break;
            case R.id.classification_end:
                testPro.setText(getString(R.string.detection_pasue_guide));
                isExist = false;
                detection_begin.setVisibility(View.VISIBLE);
                detection_end.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void testDetect() {
        Log.i("DetectionActivity", "CPU Detect");
        function_text.setVisibility(View.GONE);
        testPro.setText(getString(R.string.detection_begin_guide));
        testTime.setVisibility(View.VISIBLE);
        textFps.setVisibility(View.VISIBLE);
        testNet.setVisibility(View.VISIBLE);
        index = 0;
        Config.isFastRCNN = false;
        isExist = true;
        startDetect();
        detection_begin.setVisibility(View.GONE);
        detection_end.setVisibility(View.VISIBLE);

    }

    public void loadModel() {
        Message msg = new Message();
        msg.what = START_LOAD_DETECT;
        handler.sendMessage(msg);
        Log.i(TAG, "loadModel: "+Config.getIsCPUMode(DetectionActivity.this));
        if(Config.getIsCPUMode(DetectionActivity.this)){
            Log.i(TAG, "loadModel: CPU");
            long startTime = SystemClock.uptimeMillis();
            caffeDetection.setNumThreads(4);
            Log.e(TAG, "loadModel: " + Config.getIsCPUMode(DetectionActivity.this));
            caffeDetection.loadModel(Config.dModelProto_FRC, Config.dModelBinary_FRC, Config.getIsCPUMode(DetectionActivity.this));
            caffeDetection.setMean(Config.dModelMean);

            loadDTime = SystemClock.uptimeMillis() - startTime;

            Config.isFastRCNN = false;
            Config.isResNet50 = true;
            if (loadDTime > 100) {
                Config.loadDetecteTime = loadDTime;
            }
        }else{
            long start_time = SystemClock.uptimeMillis();
            if(!isModelSyncLoaded){
                isModelSyncLoaded=true;
                offlineDetecte.createModelClient(USING_SYNC);
                Log.e("huangyaling","create model client");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int ret = offlineDetecte.loadModelSyncFromSdcard();
                        if (0 == ret) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetectionActivity.this, "load model sync success.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetectionActivity.this, "load model sync fail.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
            long end_time = SystemClock.uptimeMillis() - start_time;
            Config.loadDetecteTime = end_time;
        }
        Message msg_end = new Message();
        msg_end.what = LOED_DETECT_END;
        handler.sendMessage(msg_end);
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_LOAD_DETECT:
                    loadCaffe.setText(R.string.load_data_detection);
                    testNet.setText(getString(R.string.decete_type) + RESNET50);
                    if(!Config.getIsCPUMode(DetectionActivity.this)){
                        testNet.setText(getString(R.string.decete_type) + "fast-rcnn");
                    }
                    break;
                case LOED_DETECT_END:
                    loadCaffe.setText(getResources().getString(R.string.detection_load_model) + Config.loadDetecteTime + "ms");
                    break;
                case IPU_DETECT_END:
                    Toast.makeText(getApplicationContext(), "检测结束", Toast.LENGTH_SHORT).show();
                    testPro.setText(getString(R.string.detection_end_guide));
                    isExist = false;
                    detection_begin.setVisibility(View.VISIBLE);
                    detection_end.setVisibility(View.GONE);
                    break;
                case UPDATE_IMG:
                    String netType = msg.getData().getString(NETTYPE);
                    detection_end.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(), "正在检测中，不能停止", Toast.LENGTH_SHORT).show();
                        }
                    });
                    if (ipuProgress != null) {
                        ipuProgress.setVisibility(View.GONE);
                        ipu_pro_text.setVisibility(View.GONE);
                    }
                    int time = msg.getData().getInt(TIME);
                    Double fps = msg.getData().getDouble(FPS);
                    Bitmap bitmapToShow = msg.getData().getParcelable(BITMAP);
                    ivCaptured.setImageBitmap(bitmapToShow);
                    testNet.setText(getString(R.string.decete_type) + netType);
                    testTime.setText(getResources().getString(R.string.test_time) + time + "ms");
                    textFps.setText(getResources().getString(R.string.test_fps) + fps + getResources().getString(R.string.test_fps_units));
                    break;
                default:
                    break;
            }
        }
    };

    private void startDetect() {
        testThread = new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                    loadModel();
                    executeImg();

            }
        });
        if (isExist) {
            testThread.start();
        }
    }

    public void executeImg() {
        imageFile = new File(Config.imagePath, Config.dImageArray[index]);
        bitmap = BitmapFactory.decodeFile(imageFile.getPath());
        CNNTask cnnTask = new CNNTask(DetectionActivity.this);
        if (imageFile.exists()) {
            cnnTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Log.e(TAG, "file is not exist");
        }
    }

    private class CNNTask extends AsyncTask<Void, Void, Void> {
        private CNNListener listener;
        private long startTime;

        public CNNTask(CNNListener listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            startTime = SystemClock.uptimeMillis();
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            if(Config.getIsCPUMode(DetectionActivity.this)){
                int[] pixels = new int[w * h];
                bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, w, h);
                int[] resultInt = caffeDetection.grayPoc(pixels, w, h);
                resBitmap = Bitmap.createBitmap(resultInt, w, h, bitmap.getConfig());
            }else{
                Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                final Bitmap initClassifiedImg = Bitmap.createScaledBitmap(rgba, w, h, false);
                final float[] pixels = getPixel(initClassifiedImg, w, h);
                int result = offlineDetecte.runModelSync(pixels);
                //结果
                resBitmap = bitmap;

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            detectionTime = SystemClock.uptimeMillis() - startTime;
            listener.onTaskCompleted(0);
            super.onPostExecute(aVoid);
        }
    }

    private String getFps(double classificationTime) {
        double fps = 60 * 1000 / classificationTime;
        return String.valueOf(fps);
    }

    @Override
    public void onTaskCompleted(int result) {
        if (isExist) {
            if(Config.getIsCPUMode(DetectionActivity.this)){
                Log.i(TAG, "CPUProcess");
                CPUProcess();
            }else{
                Log.i(TAG, "IPUProcess");
                IPUProcess();
            }

        } else {
            testPro.setText(getString(R.string.detection_end_guide));
        }
    }


    private void IPUProcess() {
        ivCaptured.setScaleType(ImageView.ScaleType.FIT_XY);
        ivCaptured.setImageBitmap(resBitmap);
        Log.i(TAG, "IPUProcess: "+index);

        detectionDB.addIPUClassification(Config.dImageArray[index], String.valueOf((int) detectionTime), getFps(detectionTime),"fast-rcnn");

        storeIPUImage(resBitmap);
        testTime.setText(getResources().getString(R.string.test_time) + String.valueOf(detectionTime) + "ms");
        textFps.setText(getResources().getString(R.string.test_fps) + ConvertUtil.getFps(getFps(detectionTime)) + getResources().getString(R.string.test_fps_units));
        if (index < Config.dImageArray.length-1) {
            executeImg();
        } else {
            Toast.makeText(this, "检测结束", Toast.LENGTH_SHORT).show();
            testPro.setText(getString(R.string.detection_end_guide));
            isExist = false;
            detection_begin.setVisibility(View.VISIBLE);
            detection_end.setVisibility(View.GONE);

            destroyModelClient();

        }
        index++;
    }

    private void CPUProcess() {
        ivCaptured.setScaleType(ImageView.ScaleType.FIT_XY);
        ivCaptured.setImageBitmap(resBitmap);
        String netType;
        if (index > (Config.dImageArray.length / 2) - 1) {
            netType = FASTRCNN;
        } else {
            netType = RESNET50;
        }

        if (index > 0) {
            detectionDB.addDetection(Config.dImageArray[index], String.valueOf((int) detectionTime), getFps(detectionTime), netType);
            storeImage(resBitmap);
        }

        testTime.setText(getResources().getString(R.string.test_time) + String.valueOf(detectionTime) + "ms");
        textFps.setText(getResources().getString(R.string.test_fps) + ConvertUtil.getFps(getFps(detectionTime)) + getResources().getString(R.string.test_fps_units));

        if ((index > (Config.dImageArray.length / 2) - 1) && !Config.isFastRCNN) {
            loadFastRCNN();
        }
        if (index < Config.dImageArray.length - 1) {
            executeImg();
        } else {
            Toast.makeText(this, "检测结束", Toast.LENGTH_SHORT).show();
            testPro.setText(getString(R.string.detection_end_guide));
            isExist = false;
            detection_begin.setVisibility(View.VISIBLE);
            detection_end.setVisibility(View.GONE);
        }
        index++;
    }

    protected void loadFastRCNN() {
        long startTime = SystemClock.uptimeMillis();
        caffeDetection.setNumThreads(4);
        caffeDetection.loadModel(Config.dModelProto_FRC, Config.dModelBinary_FRC, Config.getIsCPUMode(DetectionActivity.this));
        caffeDetection.setMean(Config.dModelMean_FRC);

        Config.isResNet50 = false;
        Config.isFastRCNN = true;
        loadDTime = SystemClock.uptimeMillis() - startTime;
        loadCaffe.setText(getResources().getString(R.string.detection_change_model) + loadDTime + "ms");
        testNet.setText(getString(R.string.decete_type) + FASTRCNN);
    }

    public void storeImage(Bitmap bitmap) {
        File file = new File(Config.dImagePath, "detec-" + index + ".jpg");
        Log.i(TAG, "storageImage: "+index);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeIPUImage(Bitmap bitmap) {
        File file = new File(Config.dImagePath, "detecIPU-" + index + ".jpg");
        Log.i(TAG, "storageipuImage: "+index);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        index = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isExist = false;
        destroyModelClient();
    }

    public void destroyModelClient(){
        if(isModelSyncLoaded){
            isModelSyncLoaded=false;
            int ret = offlineDetecte.stopModelSync();
            isModelSyncLoaded=false;
            if (0 == ret) {
                offlineDetecte.destroyModelClient(USING_SYNC);
                isModelSyncLoaded = false;
                Toast.makeText(DetectionActivity.this, "Sync unload model success.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetectionActivity.this, "Sync unload model fail.", Toast.LENGTH_SHORT).show();
            }
        }
    }

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
}