package com.cambricon.productdisplay.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.utils.StatusBarCompat;

//API
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.image.segmentation.ImageSegmentation;
import com.huawei.hiai.vision.image.*;
import com.huawei.hiai.vision.visionkit.image.ImageResult;
import com.huawei.hiai.vision.visionkit.image.segmentation.SegmentationConfiguration;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.common.ConnectionCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class SegmentActivity extends AppCompatActivity {
    private final String TAG = "SegmentActivity";
    private Toolbar toolbar;
    private Button segment;
    private TextView segment_describe;
    private ImageView segmentResult;

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
                    Toast.makeText(SegmentActivity.this, "人像分割检测错误:"+resultCode, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private Bitmap result;
    private int resultCode;
    private TextView initService;
    private ImageView resourseImg;
    private Bitmap resourseBitmap;
    private TextView test_guide;
    private Button imgSegment;

    private String[] imgPath = {
        "segment1.png", "segment2.jpg", "segment3.png", "segment4.jpg",
        "segment5.jpg", "segment6.jpg", "segment7.jpg", "segment8.jpg"
    };

    private String[] imgArray={
        "imgseg1.png","imgseg2.jpg","imgseg3.jpg","imgseg4.jpg",
            "imgseg5.jpg","imgseg6.jpg"
    };

    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.colorPrimary));
        setContentView(R.layout.activity_segment);
        initView();
        setActionBar();

        VisionBase.init(SegmentActivity.this, new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                Log.i(TAG, "onServiceConnect ");
                initService.setText("人像分割服务初始化");

            }

            @Override
            public void onServiceDisconnect() {
                Log.i(TAG, "onServiceDisconnect");
            }
        });

    }

    private void initView() {
        toolbar = findViewById(R.id.segment_toolbar);
        segment = findViewById(R.id.segment_begin);
        segment_describe = findViewById(R.id.segment_describe);
        segmentResult = findViewById(R.id.segment_img);
        initService = findViewById(R.id.load_caffe);
        resourseImg = findViewById(R.id.segment_resourse);
        test_guide = findViewById(R.id.test_guide);
        imgSegment = findViewById(R.id.img_segment_begin);

        segment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                segment_describe.setVisibility(View.GONE);
                resourseImg.setVisibility(View.VISIBLE);
                test_guide.setVisibility(View.GONE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            startSegment();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        imgSegment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                segment_describe.setVisibility(View.GONE);
                resourseImg.setVisibility(View.VISIBLE);
                test_guide.setVisibility(View.GONE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            startImgSegment();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

    }

    private void startImgSegment() throws IOException {
        InputStream is = getAssets().open("hiai/imgseg/"+imgArray[index%imgArray.length]);
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        resourseBitmap = bitmap;
        Frame frame = new Frame();
        frame.setBitmap(bitmap);
        ImageSegmentation ism = new ImageSegmentation (SegmentActivity.this);
        SegmentationConfiguration sc = new SegmentationConfiguration();
        sc. setSegmentationType (SegmentationConfiguration.TYPE_SEMANTIC);
        ism.setSegmentationConfiguration(sc);
        ImageResult sr = ism.doSegmentation (frame, null);
        int resultCode = sr.getResultCode();
        result = sr.getBitmap();

        if (resultCode == 0) {
            handler.sendEmptyMessage(SEGMENT_RESULT);
        } else {
            handler.sendEmptyMessage(SEGMENT_ERROR);
        }


    }

    private void startSegment() throws IOException {

        InputStream is = getAssets().open("hiai/segment/"+imgPath[index%imgPath.length]);
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        resourseBitmap = bitmap;
//        Bitmap bitmap = BitmapFactory.decodeFile(filePath);//获取Bitmap图像
        Frame frame = new Frame();//构造Frame对象
        frame.setBitmap(bitmap);
        SegmentationConfiguration sc = new SegmentationConfiguration();
        sc.setSegmentationType(SegmentationConfiguration.TYPE_PORTRAIT);
        ImageSegmentation ssEngine = new ImageSegmentation(SegmentActivity.this);
        ssEngine.setSegmentationConfiguration(sc);
        ImageResult srt = ssEngine.doSegmentation(frame, null);//进行人像分割
        result = srt.getBitmap();//将结果转化成bitmap格式
        resultCode = srt.getResultCode();

        if (resultCode == 0) {
            handler.sendEmptyMessage(SEGMENT_RESULT);
        } else {
            handler.sendEmptyMessage(SEGMENT_ERROR);
        }


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