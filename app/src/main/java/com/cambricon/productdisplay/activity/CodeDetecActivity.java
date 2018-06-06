package com.cambricon.productdisplay.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.cambricon.productdisplay.R;

//API
import android.widget.Button;
import android.widget.TextView;

import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.barcode.BarcodeDetector;

import org.json.JSONObject;
import org.w3c.dom.Text;

import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.common.ConnectionCallback;

import java.io.IOException;
import java.io.InputStream;

import com.huawei.hiai.vision.visionkit.common.Frame;//加载Frame类
import com.huawei.hiai.vision.face.FaceParsing;//加载人脸解析方法类
import com.huawei.hiai.vision.visionkit.image.ImageResult;//加载返回结果类
import com.huawei.hiai.vision.common.VisionBase;//加载连接服务的静态类
import com.huawei.hiai.vision.common.ConnectionCallback;//加载连接服务的回调函数

public class CodeDetecActivity extends AppCompatActivity {
    private final String TAG = "CodeDetecActivity";
    private Toolbar toolbar;
    private TextView codeService;
    private Button codeDetecte;
    private TextView code_describe;
    private TextView codeResult;

    private final int CODE_RESULT = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CODE_RESULT:
                    codeResult.setBackgroundResource(R.color.color_white);
                    codeResult.setText(String.valueOf(jsonRes));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_detec);
        initView();
        setActionBar();

        Log.i(TAG, "onCreate: ");
        VisionBase.init(CodeDetecActivity.this, new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                Log.i(TAG, "onServiceConnect ");
                codeService.setText("码检测服务开启");
            }

            @Override
            public void onServiceDisconnect() {
                Log.i(TAG, "onServiceDisconnect");
                codeService.setText("码检测服务未开启");
            }
        });


    }

    private void initView() {
        toolbar = findViewById(R.id.CodeDetec_toolbar);
        codeService = findViewById(R.id.code_service);
        codeDetecte = findViewById(R.id.codeDetec_begin);
        code_describe = findViewById(R.id.code_describe);
        codeResult = findViewById(R.id.code_result);

        codeDetecte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code_describe.setVisibility(View.GONE);
                code_describe.setBackgroundColor(getResources().getColor(R.color.color_white));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            setCodeDetecte();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

    }


    /**
     * 设置ActionBar
     */
    private void setActionBar() {
        toolbar.setTitle(getString(R.string.code_title));
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


    public void setCodeDetecte() throws IOException {

        InputStream is = getAssets().open("hiai/code/code1.png");
        Bitmap bitmap = BitmapFactory.decodeStream(is);

        BarcodeDetector detector = new BarcodeDetector(CodeDetecActivity.this);
        Frame frame = new Frame();
        frame.setBitmap(bitmap);
        jsonRes = detector.detect(frame, null);

        handler.sendEmptyMessage(CODE_RESULT);



    }

    private JSONObject jsonRes;


}


