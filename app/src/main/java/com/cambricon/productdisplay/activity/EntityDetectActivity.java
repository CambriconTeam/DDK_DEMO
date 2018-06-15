package com.cambricon.productdisplay.activity;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.huawei.hiai.nlu.model.ResponseResult;//接口返回的结果类
import com.huawei.hiai.nlu.sdk.NLUAPIService;//接口服务类
import com.huawei.hiai.nlu.sdk.NLUConstants;//接口常量类
import com.huawei.hiai.nlu.sdk.OnResultListener;//异步函数，执行成功的回调结果类
import com.cambricon.productdisplay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;


public class EntityDetectActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private TextView tv_result;
    private EditText et_input;
    private Button btn_detect;
    private Button btn_sample;
    //例子文本
    String[] sample = {"今天晚上我坐KN2987到上海,准备去电影院看机器之心,我的电话是:18177888888.", "你明天晚上帮我拿个快递吧,单号是:364149932117",
            "今天下午浦东的天气挺好的,我看了墨迹天气"
    };
    int current = 0;


    private static HashMap entityNameMap = new HashMap<>();

    static {
        entityNameMap.put("time", "时间");
        entityNameMap.put("location", "地点");
        entityNameMap.put("name", "人名");
        entityNameMap.put("phoneNum", "电话号码");
        entityNameMap.put("email", "邮箱");
        entityNameMap.put("url", "url");
        entityNameMap.put("movie", "电影");
        entityNameMap.put("tv", "电视剧");
        entityNameMap.put("varietyshow", "综艺");
        entityNameMap.put("anime", "动漫");
        entityNameMap.put("league", "联赛");
        entityNameMap.put("team", "球队");
        entityNameMap.put("music", "单曲");
        entityNameMap.put("musicAlbum", "专辑");
        entityNameMap.put("singer", "歌手");
        entityNameMap.put("trainNo", "火车车次");
        entityNameMap.put("flightNo", "航班号");
        entityNameMap.put("expressNo", "快递单号");
        entityNameMap.put("idNo", "证件号");
        entityNameMap.put("verificationCode", "验证码");
        entityNameMap.put("app", "手机应用");
        entityNameMap.put("carNo", "车牌号");
        entityNameMap.put("bankCardNo", "银行卡号");
        entityNameMap.put("book", "图书");
        entityNameMap.put("cate", "菜名");
        entityNameMap.put("famousBrand", "名牌");
        entityNameMap.put("stockName", "股票名");
        entityNameMap.put("stockCode", "股票代码");
        entityNameMap.put("fundName", "基金名");
        entityNameMap.put("fundCode", "基金代码");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_detect);
        initView();
        setActionbar();
        initService();
    }

    private void initService() {
        NLUAPIService.getInstance().init(EntityDetectActivity.this, new OnResultListener<Integer>() {

            @Override
            public void onResult(Integer result) {
                // 初始化成功回调，在服务出初始化成功调用该函数
            }
        }, true);
    }


    private void initView() {
        toolbar = findViewById(R.id.entity_detect_toolbar);
        btn_sample = findViewById(R.id.btn_sample);
        btn_detect = findViewById(R.id.btn_detect);
        et_input = findViewById(R.id.et_input);
        tv_result = findViewById(R.id.tv_result);

        btn_sample.setOnClickListener(this);
        btn_detect.setOnClickListener(this);
        et_input.setOnClickListener(this);
        tv_result.setOnClickListener(this);
//显示第一个例子
        et_input.setText(sample[0]);
    }

    private void setActionbar() {
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
            case R.id.btn_sample:
                et_input.setText(sample[++current % sample.length]);//切换例子文本
                break;

//                获得结果并显示
            case R.id.btn_detect:
                String input = et_input.getText().toString().trim();
                String result = pasare(input);
                if (null != result) {
                    tv_result.setText(result);
                }
                break;
        }
    }

    //  获得结果并解析
    private String pasare(String input) {
        String requestJson = "{text:'" + input + "'}";//module:'movie',module可有可无
        String result = null;
        ResponseResult respResult = NLUAPIService.getInstance().getEntity(requestJson, NLUConstants.REQUEST_TYPE_LOCAL);
        if (null != respResult) {
            //获取接口返回结果，参考接口文档返回使用
            result = respResult.getJsonRes();
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject entity = jsonObject.getJSONObject("entity");
                Iterator<String> entityNames = entity.keys();
                StringBuffer buffer = new StringBuffer();
//                解析文本拼接显示结果
                while (entityNames.hasNext()) {
                    String name = entityNames.next();
                    buffer.append(entityNameMap.get(name) + ":");
                    JSONArray jsonArray = entity.getJSONArray(name);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        buffer.append(jsonArray.getJSONObject(i).getString("oriText"));
                        if (i == jsonArray.length() - 1) {
                            buffer.append(".\n");
                        } else {
                            buffer.append(",");
                        }
                    }
                }
                result = buffer.toString();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
