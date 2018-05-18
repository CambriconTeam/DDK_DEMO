package com.cambricon.productdisplay.activity;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.caffenative.SingleNetDetection;
import com.cambricon.productdisplay.utils.Config;
import com.cambricon.productdisplay.utils.ConvertUtil;
import com.cambricon.productdisplay.utils.FileUtils;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SingleChartActivity extends AppCompatActivity {
    private final String TAG = "SingleChartActivity";

    public android.support.v7.widget.Toolbar toolbar;
    private Button on_cpu;
    private Button on_ipu;
    private Button off_ipu;
    private LinearLayout single_chart;
    private ImageView back_img;
    private TextView desc_text;
    private GraphicalView mChartView;
    private ProgressBar mProgressBar;

    private int cpu_index = 0;
    private int ipu_index = 0;
    private int off_index = 0;

    private ArrayList<Double> ipu_time = new ArrayList<>();
    private ArrayList<Double> cpu_time = new ArrayList<>();
    private ArrayList<Double> cpu_points = new ArrayList<>();
    private ArrayList<Double> ipu_points = new ArrayList<>();
    private ArrayList<Double> off_Points = new ArrayList<>();

    private double[] mCpuDataArray;

    private String[] xTitles = new String[]{
            "Conv", "ReLU","BN","Pooling", "LRN", "Deconv","FC","Softmax"};

    private int[] colors = new int[]{
            R.color.single_chart_white,
            R.color.single_chart_green,
            R.color.single_chart_blue
    };

    private Double[] off_Points1 = new Double[]{
            30.0, 37.5, 38.7, 33.2, 34.5, 38.5, 31.2, 37.4, 34.5, 32.5
    };

    private Boolean isTest = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    cpu_points.add(ConvertUtil.log(cpu_time.get(cpu_index),10.0));
                    updateChart();
                    cpu_index++;
                    break;
                case 2:
                    ipu_points.add(ConvertUtil.log(ipu_time.get(ipu_index),10.0));
                    updateChart();
                    ipu_index++;
                    break;
                case 3:
                    off_Points.add(off_Points1[off_index]);
                    updateChart();
                    off_index++;
                    break;
                case 4:
                    mProgressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "正在测试中，请稍后", Toast.LENGTH_LONG).show();
                    break;
                case 5:
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "测试完成", Toast.LENGTH_SHORT).show();
                    isTest = false;
                    break;
                case 6:
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "测试停止：Get Root First", Toast.LENGTH_SHORT).show();
                    isTest = false;
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chart);
        initView();
        setListener();
        setActionBar();
    }

    private void initView() {
        toolbar = findViewById(R.id.single_toolbar);
        on_cpu = findViewById(R.id.on_cpu);
        on_ipu = findViewById(R.id.on_ipu);
        off_ipu = findViewById(R.id.off_ipu);
        single_chart = findViewById(R.id.single_chart);
        back_img = findViewById(R.id.back_img);
        desc_text = findViewById(R.id.text_describe);
        mProgressBar = findViewById(R.id.ipu_progress_single);

        mChartView = ChartFactory.getBarChartView(SingleChartActivity.this, getBarDataset(), getBarRenderer(), BarChart.Type.DEFAULT);
//        LinearLayout linear = (LinearLayout) findViewById(R.id.chart_fee_detail);
        single_chart.addView(mChartView);
    }

    private void setListener() {
        on_cpu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //isTest=false;
                if(isTest){
                    Toast.makeText(SingleChartActivity.this, "正在测试中...", Toast.LENGTH_SHORT).show();
                }else{
                    isTest = true;
                    cpu_points.clear();
                    mProgressBar.setVisibility(View.VISIBLE);
                    single_chart.setVisibility(View.VISIBLE);
                    back_img.setVisibility(View.GONE);
                    desc_text.setVisibility(View.GONE);
                    handler.sendEmptyMessage(4);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startCPUSimpleTimer();
                            Log.i(TAG, "startCPUSimpleTimer");
                        }
                    }).start();
                }

            }
        });

        on_ipu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Toast.makeText(SingleChartActivity.this, "正在测试中...", Toast.LENGTH_SHORT).show();
                    isTest = true;
                    ipu_points.clear();
                    single_chart.setVisibility(View.VISIBLE);
                    back_img.setVisibility(View.GONE);
                    desc_text.setVisibility(View.GONE);
                    handler.sendEmptyMessage(2);
                    ipu_index = 0;
                    try {
                        ipu_time = FileUtils.readSingleLayer(Config.single_layer_result);
                        Log.e("huangyaling","ipu_time="+ipu_time.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startIPUSimpleTimer();
                }
        });

        off_ipu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isTest = true;
                off_Points.clear();
                single_chart.setVisibility(View.VISIBLE);
                back_img.setVisibility(View.GONE);
                desc_text.setVisibility(View.GONE);
                off_index = 0;
                startOFFSimpleTimer();
            }
        });
    }

    private void startCPUSimpleTimer() {
        mCpuDataArray = SingleNetDetection.SingleNetTime(Config.simple_modelProto, Config.simple_modelBinary);
        //获取处理图片的时间，微秒，1秒=1,000,000 微秒(μs)
        //这里是将微秒转换为毫秒
        for (double i : mCpuDataArray) {
            cpu_time.add(i * 0.001);
        }
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (cpu_index == cpu_time.size() - 1) {
                    timer.cancel();
                    cpu_time.clear();//清空
                    cpu_index = 0;//清零
                    handler.sendEmptyMessage(5);
                } else {
                    Log.i(TAG, "CPU Time Layer " + cpu_index + ":" + cpu_time.get(cpu_index));
                    handler.sendEmptyMessage(1);
                }
            }
        }, 0, 500);
    }

    private void startIPUSimpleTimer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //String cmd = "su -s sh  -c /data/test/caffe_ipu/singlelayer.sh";
                    Log.e("huangyaling", "cmd");
                    //Process proc = Runtime.getRuntime().exec(cmd);
//                    proc.waitFor();

                    handler.sendEmptyMessage(5);
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(6);
                }
            }
        }).start();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (ipu_index == ipu_time.size()) {
                    timer.cancel();
                } else {
                    handler.sendEmptyMessage(2);
                }
            }
        }, 0, 500);
    }

    public void updateChart() {
        single_chart.removeAllViews();
        single_chart.addView(ChartFactory.getBarChartView(SingleChartActivity.this, getBarDataset(), getBarRenderer(), BarChart.Type.DEFAULT));
    }

    private void startOFFSimpleTimer() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (off_index == 10) {
                    timer.cancel();
                } else {
                    handler.sendEmptyMessage(3);
                }
            }
        }, 0, 500);
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
                if(isTest){
                    Toast.makeText(SingleChartActivity.this, "正在测试中，请待测试结束后返回", Toast.LENGTH_SHORT).show();
                }else{
                    finish();
                }
            }
        });
    }

    public XYMultipleSeriesRenderer getBarRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        for (int i = 0; i < 2; i++) {
            XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
            seriesRenderer.setColor(this.getResources().getColor(colors[i]));
            renderer.addSeriesRenderer(seriesRenderer);
        }

        renderer.setChartTitle(getString(R.string.single_layer_chart_y));
        renderer.setChartTitleTextSize(40);
        renderer.setAxisTitleTextSize(30);
        renderer.setMargins(new int[]{130, 90, 70, 50});
        renderer.setXAxisMin(0);
        renderer.setXAxisMax(9);
        renderer.setXLabels(0);

        renderer.setYAxisMin(-2);
        renderer.setYAxisMax(4);
        renderer.setYLabels(10);

        renderer.setAxisTitleTextSize(32);
        //        renderer.setDisplayChartValues(true);
        renderer.setDisplayValues(true);
        renderer.setShowGrid(true);
        renderer.setMarginsColor(Color.WHITE);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0, Color.BLACK);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setBarSpacing(0.4f);
        renderer.setXLabelsAngle(30);
        renderer.setLegendTextSize(25);
        renderer.setExternalZoomEnabled(false);//设置是否可以缩放
        //X not move,Y move
        renderer.setPanEnabled(false, false);
        // X,Y Text Size
        renderer.setLabelsTextSize(32);

        for (int i = 0; i < xTitles.length; i++) {
            renderer.addXTextLabel(i + 1, xTitles[i]);
        }

        return renderer;
    }

    //柱图数据
    private XYMultipleSeriesDataset getBarDataset() {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        XYSeries series = new XYSeries(getString(R.string.single_on_cpu));
        for (int i = 0; i < cpu_points.size(); i++) {
            series.add(i + 1, cpu_points.get(i));
        }
        dataset.addSeries(series);

        series = new XYSeries(getString(R.string.single_on_ipu));
        for (int i = 0; i < ipu_points.size(); i++) {
            series.add(i + 1, ipu_points.get(i));
        }
        dataset.addSeries(series);

        /*series = new XYSeries("离线IPU");
        for (int i = 0; i < off_Points.size(); i++) {
            series.add(i + 1, off_Points.get(i));
        }
        dataset.addSeries(series);*/

        return dataset;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(isTest){
                Toast.makeText(this, "网络测试中,请待测试结束返回。", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}




