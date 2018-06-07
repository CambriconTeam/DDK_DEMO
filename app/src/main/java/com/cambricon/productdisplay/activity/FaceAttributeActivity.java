package com.cambricon.productdisplay.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cambricon.productdisplay.R;
import com.huawei.hiai.vision.visionkit.common.Frame;//加载Frame类
import com.huawei.hiai.vision.face.FaceAttributesDetector;//加载五官检测方法类
import com.huawei.hiai.vision.visionkit.face.FaceAttributesInfo;//加载返回结果类
import com.huawei.hiai.vision.common.VisionBase;//加载连接服务的静态类
import com.huawei.hiai.vision.common.ConnectionCallback;//加载连接服务的回调函数

import org.json.JSONObject;


public class FaceAttributeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOG_TAG = "FaceAttributeActivity";
    private static String TAG = "DocumentDetectionAct";
    public android.support.v7.widget.Toolbar toolbar;
    private TextView et;
    private ImageView iv;
    private Button btn_get;
    private Button btn_judge;
    private static final int GET_IMAGE = 2;
    private Bitmap bitmap;
    private String path;
    private TextView tv_result;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FaceAttributesInfo info = (FaceAttributesInfo) msg.obj;
            tv_result.setText("图片识别性别为："+info.getSex());
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_attribute);
        initView();
        setActionBar();
        /*应用VisionBase静态类进行初始化，异步拿到服务的连接。*/
        VisionBase.init(FaceAttributeActivity.this, new ConnectionCallback() {
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

    public void initView(){
        toolbar=findViewById(R.id.face_attribute_toolbar);
        btn_get = findViewById(R.id.btn_get);
        btn_judge = findViewById(R.id.btn_judge);
        tv_result = findViewById(R.id.result);
        iv = findViewById(R.id.image);
        btn_get.setOnClickListener(this);
        btn_judge.setOnClickListener(this);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.e(TAG, "data == null");
                return;
            }
            try {
                Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                //获取照片路径
                path = cursor.getString(columnIndex);
                cursor.close();
                bitmap= BitmapFactory.decodeFile(path);
                if (bitmap!=null)
                    iv.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int requestCode;
        switch (view.getId()) {
            case R.id.btn_get:
                requestCode = GET_IMAGE;
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, requestCode);
                break;
            case R.id.btn_judge:
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        Frame frame = new Frame();//构造Frame对象
                        frame.setBitmap(bitmap);
                        FaceAttributesDetector faceAttributes = new FaceAttributesDetector(FaceAttributeActivity.this);
                        JSONObject obj = faceAttributes.detectFaceAttributes(frame, null);//进行人脸属性检测
                        FaceAttributesInfo info = faceAttributes.convertResult(obj);//将结果转化成FaceAttributesInfo格式
                        Message message = Message.obtain();
                        message.obj = info;
                        handler.sendMessage(message);
                    }
                }.start();
            default:
                break;
        }

    }





}
