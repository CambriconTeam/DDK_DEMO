package com.cambricon.productdisplay.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.huawei.hiai.vision.visionkit.common.Frame;//加载Frame类
import com.huawei.hiai.vision.face.FaceLandMarkDetector;//加载五官检测方法类
import com.huawei.hiai.vision.common.VisionBase;//加载连接服务的静态类
import com.huawei.hiai.vision.common.ConnectionCallback;//加载连接服务的回调函数

import com.cambricon.productdisplay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FaceFeaturesDetectActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = "API";
    private static final int GET_IMAGE_REQUEST_CODE = 1;
    private Button btn_gallery;
    private Button btn_detect;
    private ImageView iv_detect;
    private Toolbar toolbar;
    private String path;
    private Bitmap bitmap;
    private float[] points;
    private final int POINT_NUMBER = 68;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            drawPoint();

        }
    };

    private void drawPoint() {
        Log.d(LOG_TAG, "drawPoint: start");
        Paint paint = new Paint();
        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);//创建原图副本用于绘制关键点
        Canvas canvas = new Canvas(copy);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        for (int i = 0; i < points.length / 2; i++) {
            canvas.drawCircle(points[2 * i], points[2 * i + 1], 10, paint);
        }
        Log.d(LOG_TAG, "drawPoint: setimage");

        iv_detect.setImageBitmap(copy);
    }

    private JSONObject resultObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_features_detect);
        initView();
        setActionbar();
        initService();
    }

    private void initView() {
        toolbar = findViewById(R.id.face_features_detect_toolbar);
        btn_gallery = findViewById(R.id.btn_gallery);
        btn_detect = findViewById(R.id.btn_detect);
        iv_detect = findViewById(R.id.iv_detect);

        btn_gallery.setOnClickListener(this);
        btn_detect.setOnClickListener(this);
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.face);
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

    private void initService() {
//        应用VisionBase静态类进行初始化，异步拿到服务的连接。
        VisionBase.init(FaceFeaturesDetectActivity.this, new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                Log.i(LOG_TAG, "onServiceConnect ");
            }

            @Override
            public void onServiceDisconnect() {
                Log.i(LOG_TAG, "onServiceDisconnect");
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gallery:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GET_IMAGE_REQUEST_CODE);
                break;
            case R.id.btn_detect:
                getPoints();//绘制关键点

        }
    }

    private void getPoints() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                //获取Bitmap图像(ARGB888格式)
                Frame frame = new Frame();//构造Frame对象
                frame.setBitmap(bitmap);
                FaceLandMarkDetector faceLMDetector = new FaceLandMarkDetector(FaceFeaturesDetectActivity.this);
                //进行五官特征检测；
                resultObj = faceLMDetector.detectLandMark(frame, null);
//                不可用api
//                List<FaceLandmark> landMarks = faceLMDetector.convertResult(resultObj);//得到java类型结果  --出错
                //获取第一个点的坐标
//                PointF p = landMarks.get(0).getPositionF();

                JSONArray landmarks = null;
                try {
                    int resultCode = resultObj.getInt("resultCode");
                    Log.d(LOG_TAG, "resultCode:" + resultCode);
//                  修复返回的json串
                    resultObj = new JSONObject("{'landmark':" + resultObj.getString("landmark") + "}");
                    landmarks = resultObj.getJSONArray("landmark");
//                  识别出的点
                    points = new float[POINT_NUMBER * 2];
                    for (int i = 0; i < POINT_NUMBER; i++) {
                        points[i * 2] = Float.valueOf(landmarks.getJSONObject(i).getJSONObject("positionF").get("x").toString());
                        points[i * 2 + 1] = Float.valueOf(landmarks.getJSONObject(i).getJSONObject("positionF").get("y").toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Message obtain = Message.obtain();
                handler.sendMessage(obtain);
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode && GET_IMAGE_REQUEST_CODE == requestCode) {
            if (data != null) {
                path = getPath(data);
            }
            bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null)
                iv_detect.setImageBitmap(bitmap);
        }


    }

    //获得所选图片路径
    private String getPath(Intent data) {
        if (data != null) {
            Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            //获取照片路径
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

}
