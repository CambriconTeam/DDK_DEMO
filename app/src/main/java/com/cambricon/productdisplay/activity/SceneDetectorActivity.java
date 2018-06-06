package com.cambricon.productdisplay.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.image.detector.SceneDetector;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.detector.Scene;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xiaoxiao on 18-6-5.
 */

public class SceneDetectorActivity extends Activity {
    private static final String LOG_TAG = "Scene_demo";
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_SELECT = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MSG_SERIVCE_CONNECTED = 1;
    public static final int MSG_SERIVCE_DISCONNECTED = 2;
    public static final int MSG_SCENE = 1;
    public static final int MSG_SHOW_RESULT_SCENE = 11;

    private Button btnCamera;
    private Button btnSelect;
    private ImageView ivCaptured;
    private TextView tvLabel;
    private Uri fileUri;
    private Bitmap bmp;
    private Handler mMyHandler = null;
    private MyHandlerThread mMyHandlerThread = null;
    private Button btnscene;

    private static final String UNKNOWN = "Unknown";
    private static final String UNSUPPORT = "UnSupport";
    private static final String BEACH = "Beach";
    private static final String BLUESKY = "BlueSky";
    private static final String SUNSET = "Sunset";
    private static final String FOOD = "Food";
    private static final String FLOWER = "Flower";
    private static final String GREENPLANT = "GreenPlant";
    private static final String SNOW = "Snow";
    private static final String NIGHT = "Night";
    private static final String TEXT = "Text";
    private static final String STAGE = "Stage";
    private static final String CAT = "Cat";
    private static final String DOG = "Dog";
    private static final String FIREWORK = "Firework";
    private static final String OVERCAST = "Overcast";
    private static final String FALLEN = "Fallen";
    private static final String PANDA = "Panda";
    private static final String CAR = "Car";
    private static final String OLDBUILDINGS = "OldBuildings";
    private static final String BICYCLE = "Bicycle";
    private static final String WATERFALL = "Waterfall";
    private String sceneStringArr[] = {UNKNOWN, UNSUPPORT, BEACH, BLUESKY, SUNSET, FOOD, FLOWER, GREENPLANT, SNOW, NIGHT,
            TEXT, STAGE,
            CAT, DOG, FIREWORK, OVERCAST, FALLEN, PANDA, CAR, OLDBUILDINGS, BICYCLE, WATERFALL};
    String result;
    SceneDetector sceneDetector; //scene

    private String getSceneString(int type) {
        return sceneStringArr[type];
    }

    private void initPrediction() {
        btnCamera.setEnabled(false);
        btnSelect.setEnabled(false);
        tvLabel.setText("");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_detector);
        mMyHandlerThread = new MyHandlerThread();
        mMyHandlerThread.start();
        mMyHandler = new Handler(mMyHandlerThread.getLooper(), mMyHandlerThread);
        ivCaptured = (ImageView) findViewById(R.id.ivCaptured);
        tvLabel = (TextView) findViewById(R.id.tvLabel);

        btnCamera = (Button) findViewById(R.id.btn_take);
        btnCamera.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                initPrediction();
                //Log.d(LOG_TAG, "get uri");
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                Log.d(LOG_TAG, "end get uri = " + fileUri);
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
            }
        });

        btnSelect = (Button) findViewById(R.id.btn_select);
        btnSelect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                initPrediction();
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_IMAGE_SELECT);
            }
        });
        btnscene = (Button) findViewById(R.id.btn_scene);
        btnscene.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mMyHandler.sendEmptyMessage(MSG_SCENE);
            }
        });

        btnscene.setEnabled(false);

        VisionBase.init(getApplicationContext(), new ConnectionCallback() {
            @Override
            public void onServiceConnect() {

            }

            @Override
            public void onServiceDisconnect() {

            }
        });
        VisionBase.init(this, new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                mHandler.sendEmptyMessage(MSG_SERIVCE_CONNECTED);
            }

            @Override
            public void onServiceDisconnect() {
                mHandler.sendEmptyMessage(MSG_SERIVCE_DISCONNECTED);
            }
        });

        requestPermissions();
    }

    private class MyHandlerThread extends HandlerThread implements Handler.Callback {
        public MyHandlerThread() {
            super("MyHandler");
            // TODO Auto-generated constructor stub
        }

        public MyHandlerThread(String name) {
            super(name);
            // TODO Auto-generated constructor stub
        }

        @Override
        public boolean handleMessage(Message msg) {
            Frame frame = new Frame();
            frame.setBitmap(bmp);
            switch (msg.what) {
                case MSG_SCENE:
                    long startTime = System.currentTimeMillis();
                    JSONObject obj = sceneDetector.detect(frame, null);
                    if (obj == null) {
                        result = "error";
                    }

                    result = obj.toString();
                    Scene scene = sceneDetector.convertResult(obj);
                    if (scene == null) {
                        break;
                    }
                    result = "result:" + getSceneString(scene.getType());
                    long end = System.currentTimeMillis();
                    Log.e(LOG_TAG, "scene need time:" + (end - startTime));
                    mHandler.sendEmptyMessage(MSG_SHOW_RESULT_SCENE);
                    break;

                default:
                    break;
            }
            return false;
        }
    }

        Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_SERIVCE_CONNECTED:
                        Log.d(LOG_TAG, "bind ok ");
                        Toast.makeText(getApplicationContext(), "bind success", Toast.LENGTH_SHORT).show();
                        sceneDetector = new SceneDetector(getApplicationContext());
                        break;
                    case MSG_SERIVCE_DISCONNECTED:
                        Toast.makeText(getApplicationContext(), "disconnect", Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_SHOW_RESULT_SCENE:
                        ivCaptured.setImageBitmap(bmp);
                        tvLabel.setText(result);
                        btnCamera.setEnabled(true);
                        btnSelect.setEnabled(true);
                        break;
                }
            }

        };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_SELECT) && resultCode == RESULT_OK) {
            String imgPath;

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imgPath = Environment.getExternalStorageDirectory() + fileUri.getPath();
            } else {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getApplication().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
            }
            Log.d(LOG_TAG, "imgPath = " + imgPath);
            bmp = BitmapFactory.decodeFile(imgPath);
            btnscene.setEnabled(true);
        } else {
            btnCamera.setEnabled(true);
            btnSelect.setEnabled(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {
        //return Uri.fromFile(getOutputMediaFile(type));
        Log.d(LOG_TAG, "authority = " + getPackageName() + ".provider");
        Log.d(LOG_TAG, "getApplicationContext = " + getApplicationContext());
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", getOutputMediaFile(type));

    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                , "Scene-Demo");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(LOG_TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        Log.d(LOG_TAG, "mediaFile " + mediaFile);
        return mediaFile;
    }

    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission1 = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int permission2 = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);
                if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager
                        .PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroy() {
        sceneDetector.release();
        mMyHandlerThread.quit();
        super.onDestroy();
    }
}
