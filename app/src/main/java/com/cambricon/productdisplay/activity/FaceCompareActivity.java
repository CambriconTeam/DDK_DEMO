package com.cambricon.productdisplay.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.hiai.vision.visionkit.common.Frame;
//加载人脸比对类
import com.huawei.hiai.vision.face.FaceComparator;
//加载人脸比对结果类
import com.huawei.hiai.vision.visionkit.face.FaceCompareResult;
//加载连接服务的静态类
import com.huawei.hiai.vision.common.VisionBase;
//加载连接服务的回调函数
import com.huawei.hiai.vision.common.ConnectionCallback;


import com.cambricon.productdisplay.R;

import org.json.JSONObject;

public class FaceCompareActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = "FaceCompareActivity";
    private Button btn_img1;
    private Button btn_img2;
    private ImageView image;
    private Toolbar toolbar;
    private final int GET_IMAGE1 = 1;
    private final int GET_IMAGE2 = 2;
    private String path;
    private Bitmap bitmap1;
    private Bitmap bitmap2;
    private TextView tv_result;
    private Button btn_compare;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String comstr = (String) msg.obj;
            tv_result.setText(comstr);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_compare);
        initView();
        setActionBar();

        VisionBase.init(FaceCompareActivity.this, new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                Log.i(LOG_TAG, "onServiceConnect ");
            }

            @Override
            public void onServiceDisconnect() {
                Log.i(LOG_TAG, "onServiceDisconnect");
            }
        });

    }


    private void initView() {
        btn_img1 = findViewById(R.id.btn_img1);
        btn_img2 = findViewById(R.id.btn_img2);
        btn_compare = findViewById(R.id.btn_compare);
        toolbar = findViewById(R.id.face_compare_toolbar);
        image = findViewById(R.id.image);
        tv_result = findViewById(R.id.tv_result);
        btn_img1.setOnClickListener(this);
        btn_img2.setOnClickListener(this);
        btn_compare.setOnClickListener(this);
    }

    private void setActionBar() {
        Log.d("tag", getIntent().getStringExtra("BaseToolBarTitle"));
        toolbar.setTitle(getIntent().getStringExtra("BaseToolBarTitle"));
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_img1:
                getImage(GET_IMAGE1);
                break;
            case R.id.btn_img2:
                getImage(GET_IMAGE2);
                break;
            case R.id.btn_compare:
                compare();
                break;
        }
    }

    private void getImage(int get) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, get);
    }


    private void compare() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                FaceComparator faceComparator = new FaceComparator(FaceCompareActivity.this);
                Frame frame1 = new Frame();
                Frame frame2 = new Frame();
                frame1.setBitmap(bitmap1);
                frame2.setBitmap(bitmap2);
                JSONObject jsonObject = faceComparator.faceCompare(frame1, frame2, null);
                FaceCompareResult result = faceComparator.convertResult(jsonObject);
                float score = result.getSocre();
                boolean isSamePerson = result.isSamePerson();
                String comstr = "比较得分：" + score + "\n" + "是否为同一人：" + isSamePerson;
                Message message = Message.obtain();
                message.obj = comstr;
                handler.sendMessage(message);

            }
        }.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            //获取照片路径
            path = cursor.getString(columnIndex);
            cursor.close();
            if (requestCode == GET_IMAGE1) {
                bitmap1 = BitmapFactory.decodeFile(path);
                image.setImageBitmap(bitmap1);
            } else {
                bitmap2 = BitmapFactory.decodeFile(path);
                image.setImageBitmap(bitmap2);
            }
        }
    }
}
