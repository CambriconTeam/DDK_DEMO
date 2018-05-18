package com.cambricon.productdisplay.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cambricon.productdisplay.R;

/**
 * Created by dell on 18-4-10.
 */

public class BaseActivity extends AppCompatActivity {
    /**
     * 相关组件
     */
    public android.support.v7.widget.Toolbar toolbar;
    public Button basebtn_begin;
    public Button basebtn_end;
    public ImageView base_img;
    public TextView textFps;
    public TextView testResult;
    public TextView loadCaffe;
    public TextView testTime;
    public TextView function_text;
    public TextView testPro;
    public ProgressBar ipu_progress;
    public TextView ipu_text_pro;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);
        init();
        setActionBar();
    }

    /**
     * 初始化组件
     */
    private void init() {
        base_img = findViewById(R.id.base_img);
        testResult = findViewById(R.id.test_result_base);
        testTime = findViewById(R.id.test_time_base);
        loadCaffe = findViewById(R.id.load_caffe_base);
        function_text = findViewById(R.id.function_describe_base);
        textFps = findViewById(R.id.test_fps_base);
        testPro = findViewById(R.id.test_guide_base);
        basebtn_begin = findViewById(R.id.basebtn_begin);
        basebtn_end = findViewById(R.id.basebtn_end);
        toolbar = findViewById(R.id.base_toolbar);
        loadCaffe.setText("");
        ipu_progress=findViewById(R.id.ipu_progress_base);
        ipu_text_pro=findViewById(R.id.ipu_pro_text_base);
    }
    /**
     * 设置ActionBar
     */
    private void setActionBar() {
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
}
