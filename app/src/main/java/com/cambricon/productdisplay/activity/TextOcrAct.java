package com.cambricon.productdisplay.activity;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cambricon.productdisplay.R;
import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.text.TextDetector;
import com.huawei.hiai.vision.visionkit.common.BoundingBox;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.text.Text;
import com.huawei.hiai.vision.visionkit.text.TextBlock;
import com.huawei.hiai.vision.visionkit.text.TextConfiguration;
import com.huawei.hiai.vision.visionkit.text.TextElement;
import com.huawei.hiai.vision.visionkit.text.TextLine;

import org.json.JSONObject;
import java.util.List;

/**
 * Created by dell on 18-5-31.
 */

public class TextOcrAct extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "TextOcrAct";
    private TextView et;
    private ImageView iv;
    private Button btn4ocr;
    private Paint paint;
    private Uri savedImage;
    private CheckBox checkBlock;
    private CheckBox checkLine;
    private CheckBox checkChar;
    private CheckBox checkRR;
    private static final int REQUEST_CHOOSE_PHOTO_CODE4OCR = 2;
    private TextDetector textDetector;
    private Bitmap bmDraw;
    private Toolbar toolbar;
    private TextView describe;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    String data = (String) msg.obj;
                    ClipboardManager cmb = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(data);
                    Log.e(TAG,"handle message bmDraw="+bmDraw);
                    //iv.setImageBitmap(bmDraw);
                    et.setText(data);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.textocr_layout);
        initView();
        setActionBar();
        /*get connection to service*/
        VisionBase.init(getApplicationContext(), new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                Log.i(TAG, "HwVisionManager onServiceConnect OK.");
            }

            @Override
            public void onServiceDisconnect() {
                Log.i(TAG, "HwVisionManager onServiceDisconnect OK.");
            }
        });

    }
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

    public void initView(){
        bmDraw = null;
        //create TextDetector
        textDetector = new TextDetector(TextOcrAct.this);
        toolbar=findViewById(R.id.textocr_toolbar);
        describe = findViewById(R.id.editText);
        btn4ocr = findViewById(R.id.btn4ocr);

        checkBlock = findViewById(R.id.checkBlock);
        checkBlock.setOnClickListener(this);
        checkLine = findViewById(R.id.checkLine);
        checkLine.setOnClickListener(this);
        checkChar = findViewById(R.id.checkChar);
        checkChar.setOnClickListener(this);
        checkRR = findViewById(R.id.checkRR);
        checkRR.setOnClickListener(this);

        checkBlock.setChecked(true);
        iv = findViewById(R.id.imageView);
        et = findViewById(R.id.ocr_result);
        et.setTextIsSelectable(true);

        et.setMovementMethod(ScrollingMovementMethod.getInstance());
        btn4ocr.setOnClickListener(this);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent：" + getClass().getSimpleName() + " TaskId: " + getTaskId() + " hasCode:" + this.hashCode());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String imgPath;
//            if (data == null) {
//                Log.e(TAG, "data == null");
//                return;
//            }
            if (requestCode == REQUEST_CHOOSE_PHOTO_CODE4OCR) {
                final Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getApplication().getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();

                Log.e(TAG, "select uri:" + selectedImage.toString());
            /*start OCR in a new thread*/
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        go(selectedImage);
                    }
                }) {
                }.start();
                Log.d(TAG, "imgPath = " + imgPath);
                bmDraw = BitmapFactory.decodeFile(imgPath);
                iv.setImageBitmap(bmDraw);
            }
        }
    }

    @Override
    protected void onDestroy() {
        //unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int requestCode;
        switch (view.getId()) {
            case R.id.btn4ocr:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                requestCode = REQUEST_CHOOSE_PHOTO_CODE4OCR;
                startActivityForResult(intent, requestCode);
                describe.setVisibility(View.GONE);
                break;
            case R.id.checkBlock:
                if (checkBlock.isChecked()) {
                    checkChar.setChecked(false);
                    checkLine.setChecked(false);
                }
                break;

            case R.id.checkLine:
                if (checkLine.isChecked()) {
                    checkBlock.setChecked(false);
                    checkChar.setChecked(false);
                }
                break;

            case R.id.checkChar:
                if (checkChar.isChecked()) {
                    checkBlock.setChecked(false);
                    checkLine.setChecked(false);
                }
                break;

            default:
                break;
        }
        if (view.getId() != R.id.btn4ocr && null != savedImage) {
            /*start OCR in a new thread*/
            new Thread(new Runnable() {
                @Override
                public void run() {
                    go(savedImage);
                }
            }) {
            }.start();
        }
    }

    private String putTextLine(TextBlock block, Canvas canvas) {
        if (null == block) {
            return "";
        }
        paint.setStrokeWidth(4);
        paint.setColor(Color.BLUE);
        canvas.drawRect(toRect(block.getBoundingBox()), paint);
        if (null != block.getTextLines()) {
            for (TextLine line : block.getTextLines()) {
                paint.setStrokeWidth(2);
                paint.setColor(Color.GREEN);
                canvas.drawRect(toRect(line.getLineRect()), paint);
                List<TextElement> elements = line.getElements();
                if (null != elements) {
                    for (TextElement element : elements) {
                        paint.setStrokeWidth(2);
                        paint.setColor(Color.GREEN);
                        Rect eRect = toRect(element.getElementRect());
                        canvas.drawRect(eRect, paint);
                    }
                }
            }
        }

        return block.getValue();
    }

    private Rect toRect(BoundingBox box) {
        return new Rect(box.getLeft(), box.getTop(), box.getLeft() + box.getWidth(), box.getTop() + box.getHeight());
    }

    private void go(Uri selectedImage) {
        Log.e(TAG, "OCR GO1:" + selectedImage.toString());
        savedImage = selectedImage;
        String[] pathColumn = {MediaStore.Images.Media.DATA};

        //get image by Uri
        Cursor cursor = getContentResolver().query(selectedImage, pathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(pathColumn[0]);
        String picturePath = cursor.getString(columnIndex);  //获取照片路径
        cursor.close();

        Bitmap bm = BitmapFactory.decodeFile(picturePath);
        int randValue = (int) (500 * Math.random());
        int x = (int) (bm.getWidth() * Math.random());
        int y = (int) (bm.getHeight() * Math.random());
        int left = Math.max(0, x - randValue);
        int right = Math.min(bm.getWidth(), x + randValue);
        int top = Math.max(0, y - randValue);
        int bottom = Math.min(bm.getHeight(), y + randValue);

        Rect roi = new Rect(left, top, right, bottom);
        int level = checkChar.isChecked() ? TextConfiguration.TEXT_LEVAL_CHAR : checkLine.isChecked() ? TextConfiguration.TEXT_LEVAL_LINE : TextConfiguration.TEXT_LEVAL_BLOCK;

        // prepare image
        Frame frame = new Frame();
        frame.setBitmap(bm);

        // prepare configuration for ocr
        TextConfiguration config = new TextConfiguration();
        config.setLevel(level);
        if (checkRR.isChecked()) {
            config.setROI(roi);
        }
        // set configuration to textDetector
        textDetector.setTextConfiguration(config);
        long t0 = System.currentTimeMillis();

        // run detect for textDetector
        JSONObject jsonResult = textDetector.detect(frame, null);

        //convert result
        Text text = textDetector.convertResult(jsonResult);
        long t1 = System.currentTimeMillis();
        Log.e(TAG, "ocrRes1:" + jsonResult.toString());
        if (null == text) {
            bmDraw = null;
            Message msg = new Message();
            msg.what = 0;
            msg.obj = jsonResult.toString();
            mHandler.sendMessage(msg);
            return;
        }
        List contents = text.getBlocks();
        if (null == contents) {
            bmDraw = null;
            Message msg = new Message();
            msg.what = 0;
            msg.obj = jsonResult.toString();
            mHandler.sendMessage(msg);
            return;
        }
        // draw rects in a new bitmap
        bmDraw = bm.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bmDraw);
        if (checkRR.isChecked() && roi != null) {
            paint.setStrokeWidth(4);
            paint.setColor(Color.BLACK);
            canvas.drawRect(roi, paint);
        }
        Log.e(TAG,"bmDraw="+bmDraw);

        Log.e(TAG, "lines.length:" + contents.size());

        String result_final = "";
        for (int i = 0; i < contents.size(); i += 1) {
            String res_str = putTextLine((TextBlock) contents.get(i), canvas);
            if (!res_str.isEmpty()) {
                result_final += res_str + "\n";
            }
        }
        Message msg = new Message();
        msg.what = 0;
        msg.obj = new Long(t1 - t0).toString() + "ms chars：" + result_final.length() + "  " + String.format("%.2f", (t1 - t0) * 1.0 / result_final.length()) + "ms/char" + "\n" + result_final;
        mHandler.sendMessage(msg);
        Log.e(TAG,"sendMessage success");
    }
}
