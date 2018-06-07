package com.cambricon.productdisplay.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huawei.hiai.nlu.model.ResponseResult;//接口返回的结果类
import com.huawei.hiai.nlu.sdk.NLUAPIService;//接口服务类
import com.huawei.hiai.nlu.sdk.NLUConstants;//接口常量类
import com.huawei.hiai.nlu.sdk.OnResultListener;//异步函数，执行成功的回调结果类

import com.cambricon.productdisplay.R;

import java.util.ArrayList;

public class WordSplitAct extends AppCompatActivity implements View.OnClickListener{

    private static final int IMAGE_REQUEST_CODE = 1;
    private Toolbar toolbar;
    private Button btn_analyze;
    private EditText et_source;
    private TextView et_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_split);
        initView();
        setActionBar();

        NLUAPIService.getInstance().init(this , new OnResultListener<Integer>(){

            @Override
            public void onResult(Integer result)
            {
                // 初始化成功回调，在服务出初始化成功调用该函数
            }
        },true);

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
        toolbar=findViewById(R.id.world_split_toolbar);
        btn_analyze = findViewById(R.id.btn_analyze);
        et_source = findViewById(R.id.et_source);
        et_result = findViewById(R.id.et_result);
        btn_analyze.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_analyze:
                String requestJson = "{text:'"+et_source.getText().toString().trim()+"',type:1}";
                ResponseResult respResult = NLUAPIService.getInstance().getWordPos(requestJson,
                        NLUConstants.REQUEST_TYPE_LOCAL);
                if (null != respResult)
                {
                    //获取接口返回结果，参考接口文档返回使用
                    String result = respResult.getJsonRes();

                    Gson gson = new Gson();
                    Data data = gson.fromJson(result, Data.class);
                    StringBuffer buffer = new StringBuffer();
                    String word;
                    String curr;
                    String next=null;
                    for(int i=0;i<data.pos.size();i++) {
                        word = data.pos.get(i).word;
                        buffer.append(word);
                        curr = word.substring(word.length() - 1, word.length());
                        if (i<data.pos.size()-1)
                            next = data.pos.get(i+1).word.substring(0,1);
                        if (!curr.equals(",")&&!curr.equals("，")&&!curr.equals("。")&&!next.equals(",")&&!next.equals("，")&&!next.equals("。"))
                            buffer.append("/");
                    }
                    result=buffer.toString();
                    et_result.setText(result);
                }



        }
    }
//Gson 实体类
    class Data {
        public int code;
        public String message;
        public ArrayList<Pos> pos;

        class Pos {
            public String word;
            public String tag;
        }

    }

}




