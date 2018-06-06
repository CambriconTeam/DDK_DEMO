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


import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.json.JSONObject;

//API
import com.google.gson.JsonObject;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.barcode.BarcodeDetector;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.visionkit.barcode.Barcode;


public class CodeDetecActivity extends AppCompatActivity {
    private final String TAG = "CodeDetecActivity";
    private Toolbar toolbar;
    private TextView codeService;
    private Button codeDetecte;
    private TextView code_describe;
    private TextView codeResult;

    private final int CODE_RESULT = 1;
    private final int CODE_ERROR = 2;
    private Barcode resultBarcode;
    private ImageView codeResourse;
    private Bitmap resourse;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CODE_RESULT:
                    codeResult.setBackgroundResource(R.color.color_white);
                    codeResult.setText(resultBarcode.getText().getText());
                    codeResourse.setImageBitmap(resourse);
                    break;
                case 2:
                    codeResult.setBackgroundResource(R.color.color_white);
                    codeResult.setText("No barcode detected!");
                    Toast.makeText(CodeDetecActivity.this, "No barcode detected!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //contentTypes=9
    private String[] codeArray = {
        "code1.png","code2.png","code4.png"
    };

    private int index = 0;

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
        codeResourse = findViewById(R.id.code_resourse);


        codeDetecte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code_describe.setVisibility(View.GONE);
                code_describe.setBackgroundColor(getResources().getColor(R.color.color_white));
                codeResourse.setVisibility(View.VISIBLE);
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

        InputStream is = getAssets().open("hiai/code/"+codeArray[index%codeArray.length]);
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        resourse = bitmap;
        BarcodeDetector detector = new BarcodeDetector(CodeDetecActivity.this);
        Frame frame = new Frame();
        frame.setBitmap(bitmap);
        JSONObject jsonRes = detector.detect(frame, null);

        List<Barcode> list = detector.convertResult(jsonRes);
        if(null==list||list.size()==0){
            handler.sendEmptyMessage(CODE_ERROR);
        }else{
            resultBarcode = list.get(0);
            handler.sendEmptyMessage(CODE_RESULT);
        }

        index++;

    }





}


