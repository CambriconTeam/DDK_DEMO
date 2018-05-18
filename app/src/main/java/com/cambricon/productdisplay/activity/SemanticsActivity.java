package com.cambricon.productdisplay.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.adapter.SemanticsAdapter;
import com.cambricon.productdisplay.bean.SpeechItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SemanticsActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "SemanticsActivity";
    private android.support.v7.widget.Toolbar toolbar;

    private Button left;
    private Button right;
    private Button change;
    private ImageView speech;
    private SemanticsAdapter adapter;
    private RecyclerView recyclerView;
    private RelativeLayout speechLayout;

    private File soundFile;
    private MediaRecorder mRecorder;
    // 最短录音时长
    public static final int MIN_LENGTH = 1000;
    // 最大录音时长
    public static final int MAX_LENGTH = 1000 * 60 * 10;

    public long startTime;


    private List<SpeechItem> mItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semantics);
        initView();
        setToolbar();


        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SemanticsAdapter(mItemList);
        recyclerView.setAdapter(adapter);

    }

    public void initView() {
        toolbar = findViewById(R.id.semantics_toolbar);
        left = findViewById(R.id.language_left);
        right = findViewById(R.id.language_rigth);
        change = findViewById(R.id.change);
        speech = findViewById(R.id.Voice);
        speechLayout = findViewById(R.id.speechLayout);

        left.setOnClickListener(this);
        right.setOnClickListener(this);
        change.setOnClickListener(this);
        speech.setOnClickListener(this);
    }

    /**
     * toolBar返回按钮
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            case R.id.delete:
                mItemList.clear();
                adapter.notifyDataSetChanged();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 设置toolbar属性
     */
    public void setToolbar() {
        toolbar.setTitle(R.string.gv_text_item3);
        toolbar.setDrawingCacheBackgroundColor(getResources().getColor(R.color.test_background));
        setSupportActionBar(toolbar);
        /*显示Home图标*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.semantics_toolbar, menu);
        return true;
    }

    int i = 0;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change:
                exchangeLan();
                break;
            case R.id.language_left:
                change(left);
                break;
            case R.id.language_rigth:
                change(right);
                break;
            case R.id.Voice:
                if (ContextCompat.checkSelfPermission(SemanticsActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SemanticsActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                } else {
                    recorde();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 交换语言顺序
     */
    public void exchangeLan() {
        String lStr = String.valueOf(left.getText());
        String rStr = String.valueOf(right.getText());
        left.setText(rStr);
        right.setText(lStr);
    }


    String[] array = new String[]{"简体中文", "English", "Dansk", "Italiano"};

    /**
     * 对话框
     * @param v
     */
    public void change(final View v) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置标题
        builder.setTitle("请选择所需要的语言").
                // 设置可选择的内容，并添加点击事件
                        setItems(array, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // which代表的是选择的标签的序列号
                        Log.e(TAG, "onClick: " + array[which]);
                        Button button = findViewById(v.getId());
                        button.setText(array[which]);


                    }
                }).create().show();

    }

    boolean isRecorde = false;

    /**
     * 录音
     */
    public void recorde() {

        if (isRecorde == false) {
            speechLayout.setBackgroundColor(Color.GREEN);
            isRecorde = true;
            //开始录音
            startRecord();
        } else {
            speechLayout.setBackgroundColor(Color.GRAY);
            isRecorde = false;
            //结束录音
            stopRecord();

        }
    }


    /**
     * 语音处理
     */
    public void Semantics() {
        mItemList.add(0, new SpeechItem("Models and optimization are defined by configuration without hard-coding. " + i,
                "模型和优化是通过配置而无需硬编码来定义的。"));
        i++;
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(0);
    }


    /**
     * 录音开始
     */
    public void startRecord() {
        //创建保存录音的音频文件
        startTime = System.currentTimeMillis();
        try {
            soundFile = new File(getExternalCacheDir(), "sound.amr");
            if (soundFile.exists()) {
                soundFile.delete();
            }
            soundFile.createNewFile();

            mRecorder = new MediaRecorder();
            //设置录音的声音来源
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置录音的声音的输出格式
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            //设置声音的编码格式
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(soundFile.getAbsolutePath());
            mRecorder.prepare();
            //开始录音
            mRecorder.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        if (System.currentTimeMillis() - startTime < MIN_LENGTH) {
            Toast.makeText(this, "录音时间太短", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - startTime > MAX_LENGTH) {
            Toast.makeText(this, "录音时间太长", Toast.LENGTH_SHORT).show();
        } else if (soundFile != null && soundFile.exists()) {
            //停止录音
            mRecorder.stop();
            //释放资源
            mRecorder.release();
            Toast.makeText(this, "录音结束", Toast.LENGTH_SHORT).show();
            Semantics();
            
        }
        mRecorder = null;
    }

    /**
     * 权限申请
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startRecord();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
