package com.cambricon.productdisplay.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.image.docrefine.DocRefine;
import com.huawei.hiai.vision.visionkit.image.ImageResult;
import com.huawei.hiai.vision.visionkit.image.detector.DocCoordinates;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.common.ConnectionCallback;
//import com.huawei.hiai.vision.visionkit.image.sr.ImageSRResult;


import org.json.JSONObject;

/**
 * Created by dell on 18-5-31.
 */

public class DocumentDetectionAct extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "DocumentDetectionAct";
    public android.support.v7.widget.Toolbar toolbar;
    private TextView et;
    private ImageView iv;
    private Button btn_gallery;
    private Button btnUpdate;
    private static final int REQUEST_CHOOSE_PHOTO_CODE4Gallery = 2;
    private Bitmap bitmap;


    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_detection_layout);
        initView();
        setActionBar();
        /*get connection to service*/
        VisionBase.init(getApplicationContext(), new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                Log.i(TAG, "HwVisionManager onServiceConnect OK.");
            }

            @Override
            public void onServiceDisconnect() {
                Log.i(TAG, "HwVisionManager onServiceDisconnect OK.");
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
        toolbar=findViewById(R.id.document_detection_toolbar);
        btn_gallery = findViewById(R.id.btn_gallery);
        btnUpdate = findViewById(R.id.btnRefine);
        iv = findViewById(R.id.image);

        btn_gallery.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

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
            case R.id.btn_gallery:
                requestCode = REQUEST_CHOOSE_PHOTO_CODE4Gallery;
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, requestCode);
                break;
            case R.id.btnRefine:
                // 构造校验器
                DocRefine docResolution = new DocRefine (this);
                Frame frame = new Frame();
                frame.setBitmap(bitmap);
//              进行文档检测
                JSONObject jsonDoc = docResolution.docDetect(frame, null);
                DocCoordinates sc = docResolution.convertResult(jsonDoc);
                ImageResult imageResult = docResolution.docRefine(frame, sc, null);
                bitmap= imageResult.getBitmap();
                iv.setImageBitmap(bitmap);

            default:
                break;
        }

    }





}
