package com.cambricon.productdisplay.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.utils.ConnectManager;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.image.sr.ImageSuperResolution;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.ImageResult;
import com.huawei.hiai.vision.visionkit.image.sr.SuperResolutionConfiguration;

/**
 * Created by xiaoxiao on 18-6-5.
 */

public class SuperResolutionActivity extends AppCompatActivity {
    private static final String TAG = "SISR MainActivity";
    private static final int PHOTO_REQUEST_GALLERY = 2;
    private static final int STORAGE_REQUEST = 0x0010;

    private static final int TYPE_SHOW_SRC_IMG = 1;
    private static final int TYPE_SHOW_SR_IMG = 2;

    private Bitmap mBitmap;
    private Bitmap mBitmapSR;

    private Button mBtnSrcImg;
    private Button mBtnStartSR;

    private ImageView mImageViewSrc;
    private ImageView mImageViewSR;

    private TextView mTxtViewResult;
    private TextView describe;
    private Toolbar toolbar;

    private Context mContext;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "SISR MainActivity");
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        setContentView(R.layout.activity_super_resolution);
        toolbar = (Toolbar)findViewById(R.id.super_toolbar);
        describe = (TextView) findViewById(R.id.editText);
        mImageViewSrc = (ImageView) findViewById(R.id.imgViewSrc);
        mImageViewSR = (ImageView) findViewById(R.id.imgViewSR);
        mBtnSrcImg = (Button) findViewById(R.id.btn_srcimage);
        mBtnStartSR = (Button) findViewById(R.id.btn_startSR);
        mTxtViewResult = (TextView) findViewById(R.id.SISR_result);
        mBtnSrcImg.setOnClickListener(new onClickBtn());
        mBtnStartSR.setOnClickListener(new onClickBtn());
        setActionBar();
       // VisionBase.init(getApplicationContext(), ConnectManager.getInstance().getmConnectionCallback());

    }

    class onClickBtn implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_srcimage: {
                    int rslt = requestPermissions();

                    if (0 == rslt) {
                        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        //intent.setType("image/*");
                        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                    }

                    break;
                }
                case R.id.btn_startSR: {
                    startSR();
                    break;
                }
                default:
                    break;
            }
        }
    }

    /**
     * 设置ActionBar
     */
    private void setActionBar() {
        toolbar.setTitle(getString(R.string.super_resolution));
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;

            }

            Uri selectedImage = data.getData();
            String[] pathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = SuperResolutionActivity.this.getContentResolver().query(selectedImage, pathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(pathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            mBitmap = BitmapFactory.decodeFile(picturePath);
            mHander.sendEmptyMessage(TYPE_SHOW_SRC_IMG);


        }
    }

    private Handler mHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int status = msg.what;

            switch (status) {
                case TYPE_SHOW_SRC_IMG: {
                    if (mBitmap == null) {
                        Log.e(TAG, "Input Bitmap is null!");

                        mTxtViewResult.setText("Input Bitmap is null!");
                        return;
                    }

                    mImageViewSrc.setImageBitmap(mBitmap);
                    describe.setVisibility(View.GONE);
                    mTxtViewResult.setText("Ready to run SISR!");

                    break;
                }
                case TYPE_SHOW_SR_IMG: {
                    if (msg.obj == null) {
                        Log.e(TAG, "SISR result is null!");

                        mTxtViewResult.setText("SISR result is null!");
                        return;
                    }

                    mTxtViewResult.setText("Succeed!");

                    ImageResult result = (ImageResult) msg.obj;
                    mBitmapSR = result.getBitmap();
                    mImageViewSR.setImageBitmap(mBitmapSR);
                    break;
                }
                default:
                    break;
            }
        }
    };

    private void startSR() {
        if (!isRunning) {
            isRunning = true;
            mTxtViewResult.setText("Begin to run SISR.");

            new Thread(new Runnable() {
                @Override
                public void run() {

                    if (mBitmap == null) {
                        Log.e(TAG, "Input Bitmap is null!");

                        mTxtViewResult.setText("Input Bitmap is null!");
                        return;
                    }

                    Log.d(TAG, "Start SISR");

                    // 连接AI引擎
                    // Connect to AI Engine
                    VisionBase.init(getApplicationContext(), ConnectManager.getInstance().getmConnectionCallback());

                    if (!ConnectManager.getInstance().isConnected()) {
                        ConnectManager.getInstance().waitConnect();
                    }

                    if (!ConnectManager.getInstance().isConnected()) {
                        Log.e(TAG, "Can't connect to server.");

                        mTxtViewResult.setText("Can't connect to server!");

                        return;
                    }

                    // 准备输入图片
                    // Prepare input bitmap
                    Frame frame = new Frame();
                    frame.setBitmap(mBitmap);

                    // 创建超分对象
                    // Create SR object
                    ImageSuperResolution superResolution = new ImageSuperResolution(mContext);

                    // 准备超分配置
                    // Prepare SR configuration
                    SuperResolutionConfiguration paras = new SuperResolutionConfiguration(
                            SuperResolutionConfiguration.SISR_SCALE_3X,
                            SuperResolutionConfiguration.SISR_QUALITY_HIGH);

                    // 设置超分
                    // Config SR
                    superResolution.setSuperResolutionConfiguration(paras);

                    // 执行超分
                    // Run SR
                    ImageResult result = superResolution.doSuperResolution(frame, null);

                    if (result == null) {
                        Log.e(TAG, "Result is null!");

                        mTxtViewResult.setText("SISR result is null!");

                        return;
                    }

                    if (0 != result.getResultCode()) {
                        Log.e(TAG, "Failed to run super-resolution, return : " + result.getResultCode());

                        mTxtViewResult.setText("Failed to run SISR!");
                        return;
                    }

                    if (result.getBitmap() == null) {
                        Log.e(TAG, "Result bitmap is null!");

                        mTxtViewResult.setText("SISR result has null bitmap!");

                        return;
                    }

                    Message msg = new Message();
                    msg.what = TYPE_SHOW_SR_IMG;
                    msg.obj = result;
                    mHander.sendMessage(msg);
                }
            }).start();

            isRunning = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ((requestCode == STORAGE_REQUEST) && (grantResults.length > 0) && (grantResults[0] ==
                PackageManager.PERMISSION_GRANTED)) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
        }
    }

    private int requestPermissions() {

        int rslt = -1;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x0010);
                } else {
                    rslt = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rslt;
    }

}
