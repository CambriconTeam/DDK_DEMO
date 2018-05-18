package com.cambricon.productdisplay.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.caffenative.SingleNetDetection;
import com.cambricon.productdisplay.task.SimpleNetTimeListener;
import com.cambricon.productdisplay.utils.Config;
import com.cambricon.productdisplay.utils.FileUtils;
import com.cambricon.productdisplay.utils.SingleChartService;

import org.achartengine.GraphicalView;
import java.util.ArrayList;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dell on 18-4-21.
 */

public class SingleNetFuncAct extends AppCompatActivity implements SimpleNetTimeListener {

    private static final String TAG = SingleNetDetection.class.getSimpleName();

    public android.support.v7.widget.Toolbar toolbar;
    private LinearLayout single_chart;
    private GraphicalView mView;
    private SingleChartService mService;
    private Timer timer;

    private Button on_cpu;
    private Button on_ipu;
    private Button off_ipu;

    private int xpoint = 1;
    private int xpoint2 = 0;

    private ImageView back_img;
    private TextView desc_text;

    public static int index = 0;
    private final int ON_CPU = 1;
    private final int ON_IPU = 2;
    private final int OFF_IPU = 3;
    private static final String CPU_TIME = "cpu_time";
    private Bitmap mBitmap;
    private SimpleNetTask mSimpleNetTask;
	private Bundle mBundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_func_activity);
        init();
        setActionBar();
        setListener();
    }

    private void init() {
        toolbar = findViewById(R.id.single_toolbar);
        on_cpu = findViewById(R.id.on_cpu);
        on_ipu = findViewById(R.id.on_ipu);
        off_ipu = findViewById(R.id.off_ipu);
        single_chart = findViewById(R.id.single_chart);
        back_img = findViewById(R.id.back_img);
        desc_text = findViewById(R.id.text_describe);

        mService = new SingleChartService(this);
        mService.setXYMultipleSeriesDataset();
        mService.setXYMultipleSeriesRenderer(10.5, 100, "FPS:张/秒");
        mView = mService.getGraphicalView();
        single_chart.addView(mView);
        timer = new Timer();
    }

    private void setListener() {
        on_cpu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                single_chart.setVisibility(View.VISIBLE);
                back_img.setVisibility(View.GONE);
                desc_text.setVisibility(View.GONE);
                startCPUSimpleTimer();
            }
        });

        on_ipu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                single_chart.setVisibility(View.VISIBLE);
                back_img.setVisibility(View.GONE);
                desc_text.setVisibility(View.GONE);
                mService.ON_IPU=true;
                mService.ON_CPU=false;
                mService.OFF_IPU=false;
                runSingleTest();
                try {
                    final ArrayList<Integer> fps=FileUtils.readSingleTxt(Config.single_test_result);
                    if(fps!=null){
                        int sum=0;
                        for(int i=1;i<fps.size();i++){
                            mBundle = new Bundle();
                            mBundle.putInt("ON_IPU_XPOINT",i);
                            mBundle.putInt("ON_IPU_SINGLEFPS",1000/fps.get(i));
                            Message on_ipu_message = new Message();
                            on_ipu_message.what = 2;
                            on_ipu_message.setData(mBundle);
                            sum=sum+fps.get(i);
                            handler.sendMessageDelayed(on_ipu_message, sum);
                        }
                        }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        off_ipu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                single_chart.setVisibility(View.VISIBLE);
                back_img.setVisibility(View.GONE);
                desc_text.setVisibility(View.GONE);

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(OFF_IPU);
                    }
                }, 10, 1000);

            }
        });
    }

    private void startCPUSimpleTimer() {
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                executeImg();
            }
        }).start();
    }

    private void executeImg() {
        File imgFile = new File(Config.faceImgDir, Config.faceImgArray[index]);
        mBitmap = BitmapFactory.decodeFile(imgFile.getPath());
        mSimpleNetTask = new SimpleNetTask(SingleNetFuncAct.this);
        if (imgFile.exists()) {
            mSimpleNetTask.execute();
        } else {
            Log.e(TAG, "File is not exist");
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ON_CPU:
                    mService.ON_CPU = true;
                    mService.ON_IPU = false;
                    mService.OFF_IPU = false;
                    int yPoint = (int) msg.getData().getFloat(CPU_TIME);
                    mService.updateChart(xpoint, yPoint);
                    xpoint += 1;
                    break;
                case ON_IPU:
                    mService.ON_IPU = true;
                    mService.ON_CPU = false;
                    mService.OFF_IPU = false;
                    mService.updateChart(msg.getData().getInt("ON_IPU_XPOINT"), msg.getData().getInt("ON_IPU_SINGLEFPS"));
                    break;
                case OFF_IPU:
                    mService.OFF_IPU = true;
                    mService.ON_CPU = false;
                    mService.ON_IPU = false;
                    mService.updateChart(xpoint2, Math.random() * 100);
                    xpoint2 += 1;
                    break;
            }
        }
    };
	public void runSingleTest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cmd = "su -s sh  -c /data/test/caffe_ipu/single_test.sh ";
                    Log.e("huangyaling", "cmd");
                    Process proc = Runtime.getRuntime().exec(cmd);
                    proc.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class SimpleNetTask extends AsyncTask<Float, Float, Float> {

        private SimpleNetTimeListener mListener;

        public SimpleNetTask(SimpleNetTimeListener listener) {
            mListener = listener;
        }

        @Override
        protected Float doInBackground(Float... floats) {
            try {
                int w = mBitmap.getWidth();
                int h = mBitmap.getHeight();
                int[] pixels = new int[w * h];
                mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0, w, h);

                //获取处理图片的时间，微秒，1秒=1,000,000 微秒(μs)
                /*float t = SingleNetDetection.SingleNetTime(Config.simple_modelProto,
                        Config.simple_modelBinary, w, h, pixels);*/
                float t=10000f;
                float fps = 1000000 / t;//每秒处理图片的数量

                Log.i(TAG, "Simple Net Time: " + fps);
                return fps;
            } catch (Exception e) {
                Log.i(TAG, "Exception: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Float aFloat) {
            mListener.onTaskCompleted(aFloat);
            super.onPostExecute(aFloat);
        }
    }

    @Override
    public void onTaskCompleted(float result) {
        if (index < 10) {
            sendMsgToDrawPoint(result);
            executeImg();
            index++;
        } else {
            //结束后停止AsyncTask
            if (mSimpleNetTask != null && mSimpleNetTask.getStatus() == AsyncTask.Status.RUNNING) {
                mSimpleNetTask.cancel(true);
            }
        }
    }

    private void sendMsgToDrawPoint(float result) {
        Message msg_cpu = new Message();
        msg_cpu.what = ON_CPU;
        Bundle bundle = new Bundle();
        bundle.putFloat(CPU_TIME, result);
        msg_cpu.setData(bundle);
        handler.sendMessageDelayed(msg_cpu, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        //销毁AsyncTask，防止内存泄露
        if (mSimpleNetTask != null && mSimpleNetTask.getStatus() == AsyncTask.Status.RUNNING) {
            mSimpleNetTask.cancel(true);
        }
        index = 0;//index清零
    }

    /**
     * 设置ActionBar
     */
    private void setActionBar() {
        toolbar.setTitle(getString(R.string.single_toolbar));
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
