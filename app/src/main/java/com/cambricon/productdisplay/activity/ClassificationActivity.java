package com.cambricon.productdisplay.activity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.bean.ClassificationImage;
import com.cambricon.productdisplay.caffenative.CaffeMobile;
import com.cambricon.productdisplay.caffenative.OffLineCaffeClassification;
import com.cambricon.productdisplay.db.ClassificationDB;
import com.cambricon.productdisplay.task.CNNListener;
import com.cambricon.productdisplay.utils.Config;
import com.cambricon.productdisplay.utils.ConvertUtil;
import com.cambricon.productdisplay.utils.RootUtil;
import com.cambricon.productdisplay.utils.StatusBarCompat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public class ClassificationActivity extends AppCompatActivity implements CNNListener {
    private static final String LOG_TAG = "ClassificationActivity";
    private final int START_LOADMODEL = 1;
    private final int END_LODEMODEL = 2;

    /**
     * 相关组件
     */
    private android.support.v7.widget.Toolbar toolbar;
    private Button classification_begin;
    private Button classification_end;
    private ImageView ivCaptured;
    private TextView textFps;
    private TextView testResult;
    private TextView loadCaffe;
    private TextView testTime;
    private TextView function_text;
    private TextView testPro;
    private CaffeMobile caffeMobile;
    private ProgressBar ipu_progress;
    private TextView ipu_text_pro;

    /**
     * 相关变量
     */
    private Bitmap bmp;
    private long end_time;
    private long start_time;
    public Thread testThread;
    public static int startIndex = 0;
    public static boolean isExist = true;
    private double classificationTime;
    private static String[] IMAGENET_CLASSES;
    private ClassificationDB classificationDB;
    private static float TARGET_WIDTH;
    private static float TARGET_HEIGHT;
    private File imageFile = new File(Config.sdcard, Config.imageName[0]);
    Timer timer = new Timer();

    //offline ipu
    private OffLineCaffeClassification offLineCaffeClassification;
    private AssetManager mgr;
    private static final int USING_SYNC = 1;
    private String predictedClass = "none";
    private static final int RESIZED_WIDTH = 227;
    private static final int RESIZED_HEIGHT = 227;
    private Bitmap rgba;
    private String[] ipuResult=new String[2];

    private ArrayList<ClassificationImage> arrayList = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_LOADMODEL:
                    loadCaffe.setText("开始加载分类网络...");
                    break;
                case END_LODEMODEL:
                    loadCaffe.setText(getString(R.string.load_model) + Config.loadClassifyTime + "ms");
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.colorPrimary));
        setContentView(R.layout.classification_layout);
        init();
        setListener();
        initLabels();
        setActionBar();
    }

    /**
     * 初始化组件
     */
    private void init() {
        ivCaptured = findViewById(R.id.classification_img);
        testResult = findViewById(R.id.test_result);
        testTime = findViewById(R.id.test_time);
        loadCaffe = findViewById(R.id.load_caffe);
        function_text = findViewById(R.id.function_describe);
        textFps = findViewById(R.id.test_fps);
        testPro = findViewById(R.id.test_guide);
        classification_begin = findViewById(R.id.classification_begin);
        classification_end = findViewById(R.id.classification_end);
        toolbar = findViewById(R.id.classification_toolbar);
        loadCaffe.setText("");
        classificationDB = new ClassificationDB(getApplicationContext());
        classificationDB.open();
        ipu_progress = findViewById(R.id.ipu_progress);
        ipu_text_pro = findViewById(R.id.ipu_pro_text);
        offLineCaffeClassification = new OffLineCaffeClassification();
        mgr = getResources().getAssets();
    }

    public void initLabels() {
        //读取类别文件
        AssetManager am = this.getAssets();
        byte[] labels;
        try {
            InputStream assetsInputStream = am.open("synset_words.txt");
            if (Config.getIsCPUMode(getApplicationContext())) {
                Scanner sc = new Scanner(assetsInputStream);
                List<String> lines = new ArrayList<String>();
                while (sc.hasNextLine()) {
                    final String temp = sc.nextLine();
                    lines.add(temp.substring(temp.indexOf(" ") + 1));
                }
                IMAGENET_CLASSES = lines.toArray(new String[0]);
            } else {
                int available = assetsInputStream.available();
                labels = new byte[available];
                assetsInputStream.read(labels);
                assetsInputStream.close();
                offLineCaffeClassification.initLabels(labels);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setListener() {
        classification_begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                function_text.setVisibility(View.GONE);
                testPro.setText(getString(R.string.classification_begin_guide));
                testResult.setVisibility(View.VISIBLE);
                testTime.setVisibility(View.VISIBLE);
                textFps.setVisibility(View.VISIBLE);
                startIndex = 0;
                isExist = true;
                startThread();
                classification_begin.setVisibility(View.GONE);
                classification_end.setVisibility(View.VISIBLE);
            }
        });
        classification_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testPro.setText(getString(R.string.classification_pasue_guide));
                isExist = false;
                classification_begin.setVisibility(View.VISIBLE);
                classification_end.setVisibility(View.GONE);
                if (!Config.getIsCPUMode(ClassificationActivity.this)) {

                }
                arrayList.clear();
                startIndex = 0;
                testResult.setVisibility(View.GONE);
                testTime.setVisibility(View.GONE);
                textFps.setVisibility(View.GONE);
                function_text.setVisibility(View.VISIBLE);
            }
        });

    }

    /**
     * 开始检测
     */
    public void startThread() {
        testThread = new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                load();
                executeImg();
            }
        });
        if (isExist) {
            testThread.start();
        }
    }

    /**
     * 加载模型
     */
    private void load() {
        Message msg = new Message();
        msg.what = START_LOADMODEL;
        handler.sendMessage(msg);
        if (Config.getIsCPUMode(getApplicationContext())) {
            //加载模型
            caffeMobile = new CaffeMobile();
            caffeMobile.setNumThreads(4);
            start_time = SystemClock.uptimeMillis();
            caffeMobile.loadModel(Config.modelProto, Config.modelBinary, Config.getIsCPUMode(ClassificationActivity.this));
            end_time = SystemClock.uptimeMillis() - start_time;
            float[] meanValues = {104, 117, 123};
            caffeMobile.setMean(meanValues);
            if (end_time > 100) {
                Config.loadClassifyTime = end_time;
            }
        } else {
            start_time = SystemClock.uptimeMillis();
            offLineCaffeClassification.createModelClient(USING_SYNC);
            end_time = SystemClock.uptimeMillis() - start_time;
            Config.loadClassifyTime = end_time;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int ret = offLineCaffeClassification.loadModelSyncFromSdcard();
                    if (0 == ret) {
                        //isModelSyncLoaded = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ClassificationActivity.this, "load model sync success.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ClassificationActivity.this, "load model sync fail.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        }
        Message msg_end = new Message();
        msg_end.what = END_LODEMODEL;
        handler.sendMessage(msg_end);

    }

    /**
     * 传入检测图片
     */
    private void executeImg() {
        imageFile = new File(Config.imagePath, Config.imageName[startIndex]);
        if (imageFile.exists()) {
            bmp = BitmapFactory.decodeFile(imageFile.getPath());
            if (!Config.getIsCPUMode(getApplicationContext())) {
                rgba = bmp.copy(Bitmap.Config.ARGB_8888, true);
            }
            CNNTask cnnTask = new CNNTask(ClassificationActivity.this);
            cnnTask.execute(imageFile.getPath());
        } else {
            Toast.makeText(getApplicationContext(), "Image file is not exists!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置ActionBar
     */
    private void setActionBar() {
        String mode = Config.getIsCPUMode(ClassificationActivity.this) ? getString(R.string.cpu_mode) : getString(R.string.ipu_mode);
        toolbar.setTitle(getString(R.string.gv_text_item1) + "--" + mode);
        setSupportActionBar(toolbar);
        Drawable toolDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.toolbar_bg);
        toolDrawable.setAlpha(50);
        toolbar.setBackground(toolDrawable);
        /*显示Home图标*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    public static Bitmap zoomBitmap(Bitmap target) {
        int width = target.getWidth();
        int height = target.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) TARGET_WIDTH) / width;
        float scaleHeight = ((float) TARGET_HEIGHT) / height;
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap result = Bitmap.createBitmap(target, 0, 0, width,
                height, matrix, true);
        return result;
    }

    /**
     * FPS格式转换
     *
     * @param classificationTime
     * @return
     */
    private String getFps(double classificationTime) {
        double fps = 60 * 1000 / classificationTime;
        Log.d(LOG_TAG, "fps:" + fps);
        return String.valueOf(fps);
    }

    /**
     * NPU data format is:NCHW
     * the demo code here use BGR causes of we use Caffe(caffe uses BGR)
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

    private class CNNTask extends AsyncTask<String, Void, Integer> {

        private CNNListener listener;
        private long startTime;

        public CNNTask(CNNListener listener) {
            this.listener = listener;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            if (Config.getIsCPUMode(getApplicationContext())) {
                startTime = SystemClock.uptimeMillis();
                return caffeMobile.predictImage(strings[0])[0];
            } else {
                final Bitmap initClassifiedImg = Bitmap.createScaledBitmap(rgba, RESIZED_WIDTH, RESIZED_HEIGHT, false);
                final float[] pixels = getPixel(initClassifiedImg, RESIZED_WIDTH, RESIZED_HEIGHT);
                predictedClass = offLineCaffeClassification.runModelSync(pixels);
                ipuResult=predictedClass.split("\n");
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            classificationTime = SystemClock.uptimeMillis() - startTime;
            listener.onTaskCompleted(integer);
            super.onPostExecute(integer);
        }
    }

    @Override
    public void onTaskCompleted(int result) {
        if (isExist) {
            TestProcess(result);
        } else {
            testPro.setText(getString(R.string.classification_end_guide));
        }
    }

    public void TestProcess(int result) {
        TARGET_WIDTH = ivCaptured.getWidth();
        TARGET_HEIGHT = ivCaptured.getHeight();
        ivCaptured.setImageBitmap(zoomBitmap(bmp));
        if (Config.getIsCPUMode(getApplicationContext())) {
            classificationDB.addClassification(Config.imagePath + "/" + Config.imageName[startIndex], String.valueOf((int) classificationTime), getFps(classificationTime), IMAGENET_CLASSES[result]);
            testPro.setText("图片分类进行中...(" + startIndex + "%)");
            testResult.setText(getResources().getString(R.string.test_result) + IMAGENET_CLASSES[result]);
            testTime.setText(getResources().getString(R.string.test_time) + String.valueOf((int) classificationTime) + "ms");
            textFps.setText(getResources().getString(R.string.test_fps) + ConvertUtil.getFps(getFps(classificationTime)) + getResources().getString(R.string.test_fps_units));
        } else {
            classificationDB.addIPUClassification(Config.imagePath+"/"+Config.imageName[startIndex],ipuResult[1],getFps(Double.parseDouble(ipuResult[1])),ipuResult[0]);
            testPro.setText("图片分类进行中...(" + startIndex + "%)");
            testResult.setText(getResources().getString(R.string.test_result) + ipuResult[0]);
            testTime.setText(getResources().getString(R.string.test_time) + ipuResult[1]+"ms");
            textFps.setText(getResources().getString(R.string.test_fps) + ConvertUtil.getFps(getFps(Double.parseDouble(ipuResult[1]))) + getResources().getString(R.string.test_fps_units));
        }
        startIndex++;
        if (startIndex < Config.imageName.length) {
            executeImg();
        } else {
            Toast.makeText(this, R.string.end_testing, Toast.LENGTH_SHORT).show();
            testPro.setText("图片分类检测结束");
            isExist = false;
            classification_begin.setVisibility(View.VISIBLE);
            classification_end.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isExist = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isExist = false;
    }
}
