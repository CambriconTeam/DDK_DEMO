package com.cambricon.productdisplay.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.caffenative.FaceDetection;
import com.cambricon.productdisplay.db.FaceDetectDB;
import com.cambricon.productdisplay.task.CNNListener;
import com.cambricon.productdisplay.task.FaceDetectTask;
import com.cambricon.productdisplay.task.MMListener;
import com.cambricon.productdisplay.utils.AssetUtil;
import com.cambricon.productdisplay.utils.Config;
import com.cambricon.productdisplay.utils.ConvertUtil;
import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.visionkit.common.BoundingBox;
import com.huawei.hiai.vision.visionkit.face.Face;
import com.huawei.hiai.vision.visionkit.face.FaceLandmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by cambricon on 18-3-12.
 */

public class FaceDetectorActivity extends AppCompatActivity implements View.OnClickListener,
        CNNListener ,MMListener {


    private static final String TAG = FaceDetection.class.getSimpleName();

    private Button mBtn_face_detector_begin;
    private Button mBtn_face_detector_end;

    private Bitmap mBitmap;
    private Bitmap test;
    private Bitmap mResultFace;

    private ImageView mIv_face_detector;

    private Toolbar mToolbar;

    private TextView mTv_face_detect_time;
    private TextView mTv_face_detect_guide;
    private TextView mTv_face_detect_load_time;
    private TextView mTV_face_detect_fps_time;
    private TextView mTv_face_detect_function;

    public Thread mTestThread;

    private FaceDetectDB mFaceDetectDB;

    private double mDetectionTime;
    private long loadDTime;

    private boolean isExist = true;

    public static int index = 0;
    private final int START_LOAD_DETECT = 2;
    private final int LOED_DETECT_END = 3;
    private final int LOED_DETECT_101 = 4;
    private final int LOED_DETECT_101_END = 5;

    private AssetUtil assetUtil;
    private String[] imagepath;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detector);
        initUI();
        setActionBar();

        //数据库初始化并打开数据库
        mFaceDetectDB = new FaceDetectDB(this);
        mFaceDetectDB.open();
    }

    /**
     * 装载UI
     */
    private void initUI() {
        //装载UI组件
        mToolbar = findViewById(R.id.toolbar_face_detector);
        mIv_face_detector = findViewById(R.id.iv_face_detector_img);
        mTv_face_detect_guide = findViewById(R.id.tv_guide_face_detector);
        mTv_face_detect_time = findViewById(R.id.tv_face_detector_time);
        mTv_face_detect_function = findViewById(R.id.tv_face_detector_function_describe);
        mTv_face_detect_load_time = findViewById(R.id.tv_load_face_detector);
        mTV_face_detect_fps_time = findViewById(R.id.tv_face_detector_fps);
        mBtn_face_detector_begin = findViewById(R.id.btn_face_detector_begin);
        mBtn_face_detector_end = findViewById(R.id.btn_face_detector_end);

        mTv_face_detect_guide.setText("人脸识别检测结果显示");
        mTv_face_detect_function.setText(R.string.face_detector_function);

        mBtn_face_detector_begin.setOnClickListener(this);
        mBtn_face_detector_end.setOnClickListener(this);
        assetUtil=new AssetUtil(getApplicationContext());
        if(!Config.getIsCPUMode(getApplicationContext())){
            //To connect HiAi Engine service using VisionBase
            VisionBase.init(FaceDetectorActivity.this,new ConnectionCallback(){
                @Override
                public void onServiceConnect() {
                    //This callback method is called when the connection to the service is successful.
                    //Here you can initialize the detector class, mark the service connection status, and more.
                    Log.i(TAG, "onServiceConnect ");
                }

                @Override
                public void onServiceDisconnect() {
                    //This callback method is called when disconnected from the service.
                    //You can choose to reconnect here or to handle exceptions.
                    Log.i(TAG, "onServiceDisconnect");
                }
            });
        }

    }

    /**
     * 设置ActionBar样式
     */
    private void setActionBar() {
        String mode = Config.getIsCPUMode(FaceDetectorActivity.this) ? getString(R.string.cpu_mode) : getString(R.string.ipu_mode);
        mToolbar.setTitle(getString(R.string.faceDetecte_title) + "--" + mode);
        Drawable toolDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.toolbar_bg);
        toolDrawable.setAlpha(50);
        mToolbar.setBackground(toolDrawable);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_face_detector_begin:
                mTv_face_detect_function.setVisibility(View.GONE);
                mTv_face_detect_guide.setText(getString(R.string.face_detection_begin_guide));
                mTv_face_detect_time.setVisibility(View.VISIBLE);
                mTV_face_detect_fps_time.setVisibility(View.VISIBLE);
                isExist = true;
                startFaceDetector();
                mBtn_face_detector_begin.setVisibility(View.GONE);
                mBtn_face_detector_end.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_face_detector_end:
                mTv_face_detect_guide.setText(getString(R.string.face_detection_pasue_guide));
                isExist = false;
                mBtn_face_detector_begin.setVisibility(View.VISIBLE);
                mBtn_face_detector_end.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * 启动人脸检测
     */
    private void startFaceDetector() {
        mTestThread = new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                loadModel();
                executeImg();
            }
        });

        if (isExist) {
            mTestThread.start();
        }
    }

    private void executeImg() {
        File imgFile = new File(Config.faceImgDir, Config.faceImgArray[index]);
        mBitmap = BitmapFactory.decodeFile(imgFile.getPath());
        if(Config.getIsCPUMode(getApplicationContext())){
            MTCNNTask mtcnnTask = new MTCNNTask(FaceDetectorActivity.this);
            if (imgFile.exists()) {
                mtcnnTask.execute();
            } else {
                Log.e(TAG, "File is not exist");
            }
        }else{
            //test=BitmapFactory.decodeResource(getResources(), R.drawable.face);
            test=assetUtil.getAssetImage(imagepath[index]);
            FaceDetectTask cnnTask = new FaceDetectTask(FaceDetectorActivity.this);
            Log.e("huangyaling","test="+test);
            cnnTask.execute(test);
        }

    }

    private class MTCNNTask extends AsyncTask<Void, Void, Void> {

        private CNNListener mListener;
        private long startTime;

        public MTCNNTask(CNNListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            startTime = SystemClock.uptimeMillis();
            int w = mBitmap.getWidth();//图像宽度
            int h = mBitmap.getHeight();//图像高度
            int[] pix = new int[w * h];//设置一个int数组用来存放Bitmap图像从而传入JNI
            mBitmap.getPixels(pix, 0, w, 0, 0, w, h);
            Log.d(TAG, "start detect in JNI");
            int[] result = FaceDetection.doFaceDetector(Config.faceModelDir, w, h, pix);
            mResultFace = Bitmap.createBitmap(w, h, mBitmap.getConfig());
            mResultFace.setPixels(result, 0, w, 0, 0, w, h);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mDetectionTime = SystemClock.uptimeMillis() - startTime;
            mListener.onTaskCompleted(0);
            super.onPostExecute(aVoid);
        }
    }

    private String getFps(double classificationTime) {
        double fps = 60 * 1000 / classificationTime;
        Log.d(TAG, "fps:" + fps);
        return String.valueOf(fps);
    }

    @Override
    public void onTaskCompleted(int result) {
        if (isExist) {
            mIv_face_detector.setScaleType(ImageView.ScaleType.FIT_XY);
            mIv_face_detector.setImageBitmap(mResultFace);
            if (Config.getIsCPUMode(FaceDetectorActivity.this)) {
                Log.w(TAG, "onTaskCompleted: "+index);
                if(index>0){
                    mFaceDetectDB.addFaceDetection(Config.faceImgArray[index], String.valueOf((int) mDetectionTime), getFps(mDetectionTime));
                }
            }
            storeImage(mResultFace);
            index++;
            mTv_face_detect_time.setText(getResources().getString(R.string.test_time) +
                    String.valueOf(mDetectionTime) + "ms");
            mTV_face_detect_fps_time.setText(getResources().getString(R.string.test_fps)
                    + ConvertUtil.getFps(getFps(mDetectionTime))
                    + getResources().getString(R.string.test_fps_units));

            if (index < Config.faceImgArray.length) {
                executeImg();
            } else {
                Toast.makeText(this, "检测结束", Toast.LENGTH_LONG).show();
                mTv_face_detect_guide.setText(getString(R.string.face_detection_end_guide));
                isExist = false;
                mBtn_face_detector_begin.setVisibility(View.VISIBLE);
                mBtn_face_detector_end.setVisibility(View.GONE);
                Log.i(TAG, "检测完成");
            }
        } else {
            mTv_face_detect_guide.setText(getString(R.string.face_detection_end_guide));
        }
    }

    @Override
    public void onTaskCompleted(List<Face> faces) {
        if(isExist){
            Bitmap tempBmp= test.copy(Bitmap.Config.ARGB_8888, true);
            if (faces == null || tempBmp==null) {
                //tvFace.setText("not get face");
            } else {
                Canvas canvas = new Canvas(tempBmp);

                Paint paint = new Paint();
                paint.setColor(Color.GREEN);
                for (Face face : faces) {
                    BoundingBox faceRect = face.getFaceRect();
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth((float)(faceRect.getWidth())/100);
                    canvas.drawRect(faceRect.getLeft(), faceRect.getTop(),  faceRect.getLeft()+faceRect.getWidth(), faceRect.getTop()+faceRect.getHeight(), paint);
                    List<FaceLandmark> landmarks = face.getLandmarks();
                    for (FaceLandmark landmark : landmarks) {
                        canvas.drawPoint(landmark.getPosition().x, landmark.getPosition().y, paint);
                    }

                    int textHeight = faceRect.getWidth()/10;
                    paint.setTextSize(textHeight);
                    paint.setStyle(Paint.Style.FILL);
                    int textX = faceRect.getLeft();
                    int textY = faceRect.getTop()+faceRect.getHeight()+textHeight;
                    String strFace = "yaw: " + face.getYaw();
                    canvas.drawText(strFace, textX, textY, paint);
                    strFace = "pitch: " + face.getPitch();
                    textY += textHeight;
                    canvas.drawText(strFace, textX, textY, paint);
                    strFace = "roll: " + face.getRoll();
                    textY += textHeight;
                    canvas.drawText(strFace, textX, textY, paint);
                }

            }
            mIv_face_detector.setImageBitmap(tempBmp);
            mTv_face_detect_time.setText(getResources().getString(R.string.test_time) +
                    String.valueOf(FaceDetectTask.forwardTime) + "ms");
            if(FaceDetectTask.forwardTime!=0){
                mTV_face_detect_fps_time.setText(getResources().getString(R.string.test_fps)
                        + ConvertUtil.getFps(getFps(FaceDetectTask.forwardTime))
                        + getResources().getString(R.string.test_fps_units));
                if(index>0){
                    mFaceDetectDB.addIPUFaceDetection(imagepath[index], String.valueOf((int)FaceDetectTask.forwardTime), getFps(FaceDetectTask.forwardTime));
                }
            }
            index++;
            if (index < imagepath.length) {
                executeImg();
            } else {
                Toast.makeText(this, "检测结束", Toast.LENGTH_LONG).show();
                mTv_face_detect_guide.setText(getString(R.string.face_detection_end_guide));
                isExist = false;
                mBtn_face_detector_begin.setVisibility(View.VISIBLE);
                mBtn_face_detector_end.setVisibility(View.GONE);
                Log.i(TAG, "检测完成");
            }
        }else{
            Log.d(TAG,"finish");
        }

    }

    public void storeImage(Bitmap bitmap) {
        File file = new File(Config.faceDetectedImgDir, "faceDetected-" + index + ".jpg");
        Log.e(TAG, "storeImage: " + file.exists());
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
    protected void onDestroy() {
        super.onDestroy();
        isExist = false;
        index = 0;//在生命周期结束时将index清零,防止第二次开启时exception
    }

    public void loadModel() {
        Message msg = new Message();
        msg.what = START_LOAD_DETECT;
        handler.sendMessage(msg);
        Log.e(TAG, "loadModel: start");
        long startTime = SystemClock.uptimeMillis();

        Log.e(TAG, "loadModel: " + Config.getIsCPUMode(FaceDetectorActivity.this));
        int i = 0;
        if(Config.getIsCPUMode(FaceDetectorActivity.this)){
            while (i < Config.faceModelArray.length) {
                File file = new File(Config.faceModelDir, Config.faceModelArray[i]);
                if (file.exists()) {
                    i++;
                    try {
                        Thread.sleep(80);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            imagepath=assetUtil.getArrayImage();
            Log.i(TAG,"is ipu mode");
        }
        loadDTime = SystemClock.uptimeMillis() - startTime;
        Log.e(TAG, "loadModel: end");
        Message msg_end = new Message();
        msg_end.what = LOED_DETECT_END;
        handler.sendMessage(msg_end);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_LOAD_DETECT:
                    mTv_face_detect_load_time.setVisibility(View.VISIBLE);
                    mTv_face_detect_load_time.setText(R.string.load_data_detection);
                    break;
                case LOED_DETECT_END:
                    mTv_face_detect_load_time.setText(getResources().getString(R.string.detection_load_model) + loadDTime + "ms");
                    break;
                default:
                    break;
            }
        }
    };
}
