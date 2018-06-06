package com.cambricon.productdisplay.activity;
/**
 * 分词
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.bean.Assistant;
import com.cambricon.productdisplay.bean.Participle;
import com.cambricon.productdisplay.utils.StatusBarCompat;

//API
import com.google.gson.Gson;
import com.huawei.hiai.nlu.model.ResponseResult;
import com.huawei.hiai.nlu.sdk.NLUAPIService;
import com.huawei.hiai.nlu.sdk.NLUConstants;
import com.huawei.hiai.nlu.sdk.OnResultListener;


public class ParticipleActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "SegmentActivity";
    private Toolbar toolbar;
    private Button participle;
    //    private String testText;
    private EditText editText;
    private int testType = 1;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private TextView result_show;
    private TextView serviceInit;
    private Button assistant;
    private Button imAssistant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.colorPrimary));
        setContentView(R.layout.activity_participle);
        initView();
        setActionBar();


        NLUAPIService.getInstance().init(ParticipleActivity.this, new OnResultListener<Integer>() {

            @Override
            public void onResult(Integer result) {
                Log.i(TAG, "onResult: " + result);
                serviceInit.setText("服务初始化成功");
            }
        }, true);

    }


    private void initView() {
        toolbar = findViewById(R.id.participle_toolbar);
        participle = findViewById(R.id.participle_begin);
        editText = findViewById(R.id.testText);
        result_show = findViewById(R.id.result_show);
        serviceInit = findViewById(R.id.load_caffe);
        assistant = findViewById(R.id.test_assistant);
        imAssistant = findViewById(R.id.imAssistant);

        button1 = findViewById(R.id.text1);
        button2 = findViewById(R.id.text2);
        button3 = findViewById(R.id.text3);
        button4 = findViewById(R.id.text4);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);


        participle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testBegin();
                //                testAssistant();
            }
        });

        assistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testAssistant();
            }
        });

        imAssistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imAssistant();
            }
        });


    }

    public void imAssistant(){
        String result = null;
        String requestJson = "{text:'"+editText.getText()+"'}";
        ResponseResult respResult = NLUAPIService.getInstance().getChatIntention(requestJson, NLUConstants.REQUEST_TYPE_LOCAL);
        if (null != respResult) {
            result = respResult.getJsonRes();
        }
        if (null == result) {
            result_show.setText("IM类意图识别失败");
            return;
        }
        result_show.setText(result);


    }



    public void testAssistant() {
        String result = null;
        String requestJson = "{text:'" + editText.getText() + "'}";
        ResponseResult respResult = NLUAPIService.getInstance().getAssistantIntention(requestJson, NLUConstants.REQUEST_TYPE_LOCAL);
        Log.i(TAG, "testAssistant: " + respResult);
        if (null != respResult) {
            result = respResult.getJsonRes();
        }
        if (null == result) {
            result_show.setText("助手类意图识别失败");
            return;
        }

        Log.i(TAG, "testAssistant: " + result);

        // String result = " {'intentions':[{'name':'openBluetooth','confidence':1}],'code':0,'message':'success'} ";
        // result = " {'code':0,'message':'success'} ";

        Gson gson = new Gson();
        Assistant assistant = gson.fromJson(result, Assistant.class);
        if (assistant.getCode() != 0) {
            Toast.makeText(this, "分词检测失败：" + assistant.getMessage(), Toast.LENGTH_SHORT).show();
            result_show.setText("分词检测失败：" + assistant.getMessage());
        } else {
            StringBuffer backwords = new StringBuffer();
            backwords.append("检测到助手类意图： ");
            Assistant.Intention[] intentions = assistant.getIntentions();
            if(null==intentions){
                result_show.setText("测试用例未检测到助手类意图");
                return;
            }
            for (Assistant.Intention intention : intentions) {
                backwords.append(intention.getName() + "\t");
            }
            result_show.setText(backwords);
        }

    }

    /**
     * 设置ActionBar
     */
    private void setActionBar() {
        toolbar.setTitle(getString(R.string.participle_title));
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

    private void testBegin() {
        final String testText = String.valueOf(editText.getText());
        String result = null;
        Log.i(TAG, "testBegin: " + testText);
        String requestJson = "{text:'" + testText + "',type:" + testType + "}";
        Log.i(TAG, "requestJson: " + requestJson);
        ResponseResult respResult = NLUAPIService.getInstance().getWordSegment(requestJson,
                NLUConstants.REQUEST_TYPE_LOCAL);
        if (null != respResult) {
            result = respResult.getJsonRes();
        }


        Log.i(TAG, "result: " + result);
        if (null == result) {
            Toast.makeText(this, "分词检测失败，返回数据为空", Toast.LENGTH_SHORT).show();
            result_show.setText("分词检测失败，返回数据为空");
        } else {
            result_gson(result);
        }


    }

    private void result_gson(String resultText) {
        Gson gson = new Gson();
        Participle participle = gson.fromJson(resultText, Participle.class);

        if (participle.getCode() != 0) {
            Toast.makeText(this, "分词检测失败：" + participle.getMessage(), Toast.LENGTH_SHORT).show();
            result_show.setText("分词检测失败：" + participle.getMessage());
        } else {
            StringBuffer result = new StringBuffer();
            for (String str : participle.getWords()) {
                result.append(str + "\t");
            }
            result_show.setText(result);
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text1:
                editText.setText(R.string.participle_test1);
                testBegin();
                break;
            case R.id.text2:
                editText.setText(R.string.participle_test2);
                testBegin();
                break;
            case R.id.text3:
                editText.setText(R.string.participle_test3);
                testAssistant();
                break;
            case R.id.text4:
                editText.setText(R.string.participle_test4);
                imAssistant();
                break;
        }
    }
}
