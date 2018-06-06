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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
//API
import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.image.detector.SceneDetector;
import com.huawei.hiai.vision.image.sr.TxtImageSuperResolution;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.ImageResult;

import java.io.IOException;
import java.io.InputStream;

public class WordSegmentActivity extends AppCompatActivity {
    private final String TAG = "WordSegmentActivity";
    private Toolbar toolbar;
    private Button wordSegment;
    private int resultCode;
    private ImageView segmentResult;
    private Bitmap result;
    private ImageView resourseImg;
    private Bitmap resourseBitmap;
    private int index;

    private final int SEGMENT_RESULT = 1;
    private final int SEGMENT_ERROR = 2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEGMENT_RESULT:
                    segmentResult.setImageBitmap(result);
                    resourseImg.setImageBitmap(resourseBitmap);
                    index++;
                    break;
                case SEGMENT_ERROR:
                    Toast.makeText(WordSegmentActivity.this, "人像分割检测错误:" + resultCode, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_segment);

        initView();
        setActionBar();


        VisionBase.init(WordSegmentActivity.this, new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                Log.i(TAG, "onServiceConnect ");

            }

            @Override
            public void onServiceDisconnect() {
                Log.i(TAG, "onServiceDisconnect");

            }
        });


    }

    public void initView() {
        toolbar = findViewById(R.id.word_segment_toolbar);
        wordSegment = findViewById(R.id.wordsegment_begin);
        segmentResult = findViewById(R.id.segment_img);
        resourseImg = findViewById(R.id.segment_resourse);

        wordSegment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                    startWordSegment();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        });

    }

    public void startWordSegment() throws IOException {

        InputStream is = getAssets().open("hiai/wordseg/" + "wordseg1.png");
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        resourseBitmap = bitmap;
        Frame frame = new Frame();
        frame.setBitmap(bitmap);
        TxtImageSuperResolution tsr = new TxtImageSuperResolution(WordSegmentActivity.this);


        ImageResult srt = tsr.doSuperResolution(frame, null);
        Bitmap newbmp = srt.getBitmap();
        result = newbmp;
        resultCode = srt.getResultCode();

//        if (resultCode == 0) {
            handler.sendEmptyMessage(SEGMENT_RESULT);
//        } else {
//            handler.sendEmptyMessage(SEGMENT_ERROR);
//        }

    }


    /**
     * 设置ActionBar
     */
    private void setActionBar() {
        Log.i(TAG, "setActionBar: " + getString(R.string.segment_title));
        toolbar.setTitle(getString(R.string.segment_title));
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
