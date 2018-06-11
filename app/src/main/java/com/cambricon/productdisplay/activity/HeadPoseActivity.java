package com.cambricon.productdisplay.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cambricon.productdisplay.R;
import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.face.HeadposeDetector;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.face.HeadPoseResult;

import org.json.JSONObject;

/**
 * Created by xiaoxiao on 18-6-7.
 */

public class HeadPoseActivity extends AppCompatActivity{

    private static final String TAG = "VlHeadPoseActivity";

    private static final int REQUEST_CHOOSE_PHOTO_CODE = 2;

    private static final int TYPE_CHOOSE_PHOTO = 1;
    private static final int TYPE_SHOW_RESULE = 2;

    private Object mWaitResult = new Object();

    private Bitmap mBitmap;
    private Button mBtnSeletct;
    private Button mBtnStartDetect;
    private ImageView mImageView;
    private TextView mTxtViewResult;


    private String headpose_result[] = {"nobody", "upward", "rightward", "downward", "leftward"};

    HeadposeDetector mHeadposeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head_pose);

        mImageView = (ImageView) findViewById(R.id.iv_head_pose);
        mBtnSeletct = (Button) findViewById(R.id.btn_head_pose_select);
        mBtnStartDetect = (Button) findViewById(R.id.btn_identify);
        mTxtViewResult = (TextView) findViewById(R.id.idetify_result);

        mBtnSeletct.setOnClickListener(new onClickBtn());
        mBtnStartDetect.setOnClickListener(new onClickBtn());

        /* To connect vision service */
        VisionBase.init(getApplicationContext(), new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                Log.i(TAG, "onServiceConnect.");
            }

            @Override
            public void onServiceDisconnect() {
                Log.i(TAG, "onServiceDisconnect.");
            }
        });

        mThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* release headpose instance and free the npu resources*/
        if (mHeadposeDetector != null) {
            mHeadposeDetector.release();
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            /* get WRITE_EXTERNAL_STORAGE Permissions*/
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    class onClickBtn implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick !!! ");
            HeadPoseActivity.this.requestPermission();
            switch (v.getId()) {
                case R.id.btn_head_pose_select: {
                    Log.d(TAG, "Select an image");
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_CHOOSE_PHOTO_CODE);
                    break;
                }
                case R.id.btn_identify: {
                    startCompare();
                    break;
                }
                default:
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_PHOTO_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }

            Uri selectedImage = data.getData();
            //getBitmap(selectedImage);
            String[] pathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, pathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(pathColumn[0]);
        /* get image path */
            String picturePath = cursor.getString(columnIndex);
           cursor.close();

           // Log.d(TAG, "url : " + picturePath);

            //BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inJustDecodeBounds = true;
            //BitmapFactory.decodeFile(picturePath,options);
            //options.inSampleSize = RegisterTool.calculateInSampleSize(options,640,480);
            //options.inJustDecodeBounds = false;

//            InputStream is = null;
//            try {
//                is = getAssets().open("faceimage/headpose.jpg");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Bitmap bitmap = BitmapFactory.decodeStream(is);
//            mBitmap = bitmap;

            mBitmap = BitmapFactory.decodeFile(picturePath);
            //mBitmap =
            mBitmap = getResizedBitmap(mBitmap,640,480);

            mHander.sendEmptyMessage(TYPE_CHOOSE_PHOTO);

        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight){
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float)newWidth)/width;
        float scaleHeight = ((float)newHeight)/height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm,0,0,width,height,matrix,false);
        bm.recycle();
        return resizedBitmap;
    }

    private Handler mHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int status = msg.what;
            Log.d(TAG, "handleMessage status = " + status);
            switch (status) {
                case TYPE_CHOOSE_PHOTO: {
                    if (mBitmap == null) {
                        Log.e(TAG, "bitmap is null !!!! ");
                        return;
                    }
                    mImageView.setImageBitmap(mBitmap);
                    break;
                }

                case TYPE_SHOW_RESULE: {
                    HeadPoseResult result = (HeadPoseResult) msg.obj;
                    Log.e("xiaoxiao","result:"+result);

                    if (result == null) {
                        mTxtViewResult.setText("Failed to detect headpose, result is null.");
                        break;
                    }
                    Log.d(TAG, "headpose : " + headpose_result[result.getHeadpose()] + ", confidence : " + result.getConfidence());

                    mTxtViewResult.setText("headpose : " + headpose_result[result.getHeadpose()] + "\nconfidence : " + result.getConfidence());

                    break;
                }
                default:
                    break;
            }
        }
    };

    private void startCompare() {
        mTxtViewResult.setText("headpose result");
        synchronized (mWaitResult) {
            mWaitResult.notifyAll();//唤醒线程

        }
    }

    private Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {
            mHeadposeDetector = new HeadposeDetector(getApplicationContext());
            HeadposeDetector headposeDetector = mHeadposeDetector;
            while (true) {
                try {
                    synchronized (mWaitResult) {
                        mWaitResult.wait();//一直等待直到其他线程调用notify
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }

                Log.d(TAG, "start to detect headpose.");
                /* create frame and set images*/
                Frame frame = new Frame();
                frame.setBitmap(mBitmap);

                /* create a HeadposeDetector instance firstly */
                //HeadposeDetector headposeDetector = new HeadposeDetector(getApplicationContext());



                /* start to detect and get the json object, which can be analyzed as HeadPoseResult */
                JSONObject jsonObject = headposeDetector.detect(frame, null);
                Log.d(TAG, "end to detect head pose. json: " + jsonObject.toString());  /*jsonObject never be null*/

                /* analyze the result */
                HeadPoseResult result = headposeDetector.convertResult(jsonObject);
                Log.d(TAG, "convert result.");

                /* do something follow your heart*/
                Message msg = new Message();
                msg.what = TYPE_SHOW_RESULE;
                msg.obj = result;
                mHander.sendMessage(msg);

               // headposeDetector.release();
            }
        }
    });
}
