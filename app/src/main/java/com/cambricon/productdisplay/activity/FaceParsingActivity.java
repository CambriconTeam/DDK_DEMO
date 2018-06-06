package com.cambricon.productdisplay.activity;

/**
 * Created by xiaoxiao on 18-6-6.
 */


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
import com.huawei.hiai.vision.face.FaceParsing;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.ImageResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FaceParsingActivity extends Activity {
    private static final String LOG_TAG = "faceparsing";
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_SELECT = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MSG_SERIVCE_CONNECTED = 1;
    public static final int MSG_SERIVCE_DISCONNECTED = 2;
    public static final int MSG_FACE_PARSING = 1;
    public static final int MSG_SHOW_RESULT = 11;
    private Button btnCamera;
    private Button btnSelect;
    private ImageView ivCaptured;
    private TextView tvLabel;
    private Uri fileUri;
    private Bitmap bmp;
    private Handler mMyHandler = null;
    private MyHandlerThread mMyHandlerThread = null;
    private Button btnfp;
    private Bitmap newbmp;
    private ImageView dstView;
    String result;
    FaceParsing faceParsing;

    private void initPrediction() {
        btnCamera.setEnabled(false);
        btnSelect.setEnabled(false);
        tvLabel.setText("");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_parsing);
        mMyHandlerThread = new MyHandlerThread();
        mMyHandlerThread.start();
        mMyHandler = new Handler(mMyHandlerThread.getLooper(), mMyHandlerThread);
        ivCaptured = (ImageView) findViewById(R.id.iv_face_parsing);
        tvLabel = (TextView) findViewById(R.id.tv_face_parsing);
        dstView = (ImageView) findViewById(R.id.iv_face_parsing_dst);
        btnCamera = (Button) findViewById(R.id.btn_camera);
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

        btnSelect = (Button) findViewById(R.id.btn_storage);
        btnSelect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                initPrediction();
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_IMAGE_SELECT);
            }
        });
        btnfp = (Button) findViewById(R.id.btn_fp);
        btnfp.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mMyHandler.sendEmptyMessage(MSG_FACE_PARSING);
            }
        });

        btnfp.setEnabled(false);

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
                case MSG_FACE_PARSING: //scene detect

                    long s4 = System.currentTimeMillis();
                    ImageResult srt = faceParsing.doFaceParsing(frame, null);
                    if (srt == null) {
                        return false;
                    }
                    int code = srt.getResultCode();
                    Log.e(LOG_TAG, "resultcode:" + code);
                    if (newbmp != null) {
                        newbmp.recycle();
                    }
                    result = "result code :" + code;
                    newbmp = srt.getBitmap();
                    long end4= System.currentTimeMillis();
                    Log.e(LOG_TAG, "txtst need time:" + (end4 - s4));

                    mHandler.sendEmptyMessage(MSG_SHOW_RESULT);
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
                    faceParsing = new FaceParsing(getApplicationContext());
                    break;
                case MSG_SERIVCE_DISCONNECTED:
                    Toast.makeText(getApplicationContext(), "disconnect", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_SHOW_RESULT:
                    tvLabel.setText(result);
                    ivCaptured.setImageBitmap(bmp);
                    dstView.setImageBitmap(newbmp);
                    btnCamera.setEnabled(true);
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
                //imgPath = fileUri.getPath();
                imgPath = Environment.getExternalStorageDirectory() + fileUri.getPath();
            } else {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getApplicationContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
            }
            Log.d(LOG_TAG, "imgPath = " + imgPath);
            bmp = BitmapFactory.decodeFile(imgPath);
            btnfp.setEnabled(true);
            Log.d(LOG_TAG, "bitmap = " + imgPath);
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
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                , "FaceParsing-Demo");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
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
                if (permission1 != PackageManager.PERMISSION_GRANTED  ) {
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
        faceParsing.release();
        mMyHandlerThread.quit();
        super.onDestroy();
    }
}