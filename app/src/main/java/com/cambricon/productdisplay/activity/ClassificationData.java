package com.cambricon.productdisplay.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.adapter.UltraPagerAdapter;
import com.cambricon.productdisplay.bean.ClassificationImage;
import com.cambricon.productdisplay.db.ClassificationDB;
import com.cambricon.productdisplay.utils.Config;
import com.cambricon.productdisplay.utils.ConvertUtil;
import com.cambricon.productdisplay.utils.DataUtil;
import com.cambricon.productdisplay.view.ResultDialog;
import com.tmall.ultraviewpager.UltraViewPager;
import com.tmall.ultraviewpager.transformer.UltraDepthScaleTransformer;

import org.achartengine.GraphicalView;

import java.util.ArrayList;

/**
 * Created by dell on 18-2-8.
 */

public class ClassificationData extends Fragment {
    private final String TAG = "ClassificationData";
    private View view;
    private Context context;
    private LinearLayout linearLayout;
    private GraphicalView graphicalView;

    /**
     * 离线折线图linearLayout
     */
    private LinearLayout offLinearLayout;

    //cpu param
    private int[] points;
    private double[] avgTimes;
    private static double avgTimeValue = 0.00;
    private static int avgFpsValue;
    //ipu param
    private int[] ipu_points;
    private double[] avgIPUTimes;
    private static double avgIPUTimeValue = 0.00;
    private static int avgIPUFpsValue;

    private TextView fps_tv;
    private TextView time_tv;
    private Button result_btn;
    private TextView fps_ipu;
    private TextView time_ipu;
    private Button ipu_result;
    private ScrollView scrollView;

    private ClassificationDB classificationDB;
    private UltraViewPager ultraViewPager_dialog;
    private UltraViewPager ultraViewPager_ipuDialog;
    private PagerAdapter adapter_dialog;
    private ArrayList<ClassificationImage> allTicketsList;
    private ArrayList<ClassificationImage> allIPUTicketsList;

    /**
     * off_chart
     */
    private ArrayList<ClassificationImage> offAllTickets;
    private ArrayList<ClassificationImage> offAllIPUTickets;
    private int[] offPoints;
    private int[] offIpu_points;

    /**
     * simple_cpu
     */
    private ArrayList<ClassificationImage> simpleAllTickets;
    private int[] simplePoints;

    private ArrayList<ClassificationImage> simpleIpuTickets;
    private int[] simpleIpuPoints;


    private int max = 0;
    private int min = 10000;

    public ClassificationData() {

    }

    @SuppressLint("ValidFragment")
    public ClassificationData(Context contexts) {
        this.context = contexts;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_classification_data, null);
        init();
        setListener();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
        getSimpleCPUData();
        getSimpleIpuData();
        //分类折线图
        DataUtil.showChart(getContext(),linearLayout,allTicketsList,allIPUTicketsList,points,ipu_points);
        //DataUtil.showChart(getContext(),linearLayout,allTicketsList,allIPUTicketsList,simpleAllTickets,simpleIpuTickets,points,ipu_points,simplePoints,simpleIpuPoints);
        getOffIPUData();
    }

    private void init() {
        linearLayout = view.findViewById(R.id.chart_line);
        offLinearLayout = view.findViewById(R.id.offchart_line);

        classificationDB = new ClassificationDB(getContext());
        classificationDB.open();
        fps_tv = view.findViewById(R.id.avg_fps);
        time_tv = view.findViewById(R.id.avg_time);
        result_btn = view.findViewById(R.id.all_result);
        fps_ipu = view.findViewById(R.id.avg_ipu_fps);
        time_ipu = view.findViewById(R.id.avg_ipu_time);
        ipu_result = view.findViewById(R.id.all_ipu_result);
        scrollView = view.findViewById(R.id.scrollView);
    }

    private void getSimpleCPUData() {
        simpleAllTickets = new ArrayList<>();
        simpleAllTickets = classificationDB.fetchCPUSimplelineAll();
        for(ClassificationImage iamge:simpleAllTickets){
            Log.i(TAG, "getSimpleCPUData: "+iamge.toString());
        }
        simplePoints = new int[Config.ChartPointNum];
        if(simpleAllTickets.size() !=0){
            int count = 0;
            if (Config.ChartPointNum > simpleAllTickets.size()) {
                count = allIPUTicketsList.size();
            } else {
                count = Config.ChartPointNum;
            }
            for (int j = 0; j < count; j++) {
                simplePoints[j] = ConvertUtil.getFps(simpleAllTickets.get(simpleAllTickets.size() - j - 1).getFps());
            }
        }

    }

    private void getSimpleIpuData(){
        simpleIpuTickets=new ArrayList<>();
        simpleIpuTickets=classificationDB.fetchIPUSimplelineAll();
        simpleIpuPoints=new int[Config.ChartPointNum];
        if(simpleIpuTickets.size()!=0){
            int count;
            if(Config.ChartPointNum>simpleIpuTickets.size()){
                count=simpleIpuTickets.size();
            }else{
                count=Config.ChartPointNum;
            }

            for(int i=0;i<count;i++){
                simpleIpuPoints[i]=ConvertUtil.getFps(simpleIpuTickets.get(simpleIpuTickets.size()-i-1).getFps());
            }
        }
    }

    private void getData() {
        double allTime = 0.00;
        int allFps = 0;

        double allIPUTime = 0.00;
        int allIPUFps = 0;
        allTicketsList = new ArrayList<>();
        allTicketsList = classificationDB.fetchAll();
        points = new int[Config.ChartPointNum];

        allIPUTicketsList = new ArrayList<>();
        allIPUTicketsList = classificationDB.fetchIPUAll();

        ipu_points = new int[Config.ChartPointNum];
        if (allIPUTicketsList.size() != 0) {
            avgIPUTimes = new double[allIPUTicketsList.size()];
            int ipuCount = 0;
            if (Config.ChartPointNum > allIPUTicketsList.size()) {
                ipuCount = allIPUTicketsList.size();
            } else {
                ipuCount = Config.ChartPointNum;
            }

            for (int j = 0; j < ipuCount; j++) {
                ipu_points[j] = ConvertUtil.getFps(allIPUTicketsList.get(allIPUTicketsList.size() - j - 1).getFps());
                avgIPUTimes[j] = ConvertUtil.convert2Double(allIPUTicketsList.get(allIPUTicketsList.size() - j - 1).getTime());
                allIPUTime = allIPUTime + avgIPUTimes[j];
                allIPUFps = allIPUFps + ipu_points[j];
            }
            avgIPUTimeValue = allIPUTime / ipuCount;
            avgIPUFpsValue = (int) allIPUFps / ipuCount;
            fps_ipu.setText(R.string.classification_avg_time);
            time_ipu.setText(R.string.classification_single_time);
            fps_ipu.append(String.valueOf(avgIPUFpsValue) + "张/分钟");
            time_ipu.append(String.valueOf((int) avgIPUTimeValue) + "ms");
        }
        if (allTicketsList.size() != 0) {
            avgTimes = new double[allTicketsList.size()];
            int dataSum;
            if (Config.ChartPointNum > allTicketsList.size()) {
                dataSum = allTicketsList.size();
            } else {
                dataSum = Config.ChartPointNum;
            }
            for (int i = 0; i < dataSum; i++) {
                points[i] = ConvertUtil.getFps(allTicketsList.get(allTicketsList.size() - i - 1).getFps());
                if (points[i] > max) {
                    max = points[i];
                }
                if (points[i] < min) {
                    min = points[i];
                }
                avgTimes[i] = ConvertUtil.convert2Double(allTicketsList.get(allTicketsList.size() - i - 1).getTime());
                allTime = allTime + avgTimes[i];
                allFps = allFps + points[i];
            }
            avgTimeValue = allTime / dataSum;
            avgFpsValue = (int) allFps / dataSum;
            fps_tv.setText(R.string.classification_avg_time);
            time_tv.setText(R.string.classification_single_time);
            fps_tv.append(String.valueOf(avgFpsValue) + "张/分钟");
            time_tv.append(String.valueOf((int) avgTimeValue) + "ms");
        }
    }

    private void setListener() {
        result_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResultDialog dialog = new ResultDialog(getContext());
                View contentView1 = LayoutInflater.from(getContext()).inflate(
                        R.layout.result_dialog, null);
                ultraViewPager_dialog = contentView1.findViewById(R.id.ultra_viewpager_dialog);
                ultraViewPager_dialog.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
                adapter_dialog = new UltraPagerAdapter(true, allTicketsList, getContext());
                ultraViewPager_dialog.setAdapter(adapter_dialog);
                ultraViewPager_dialog.setMultiScreen(0.6f);
                ultraViewPager_dialog.setItemRatio(1.0f);
                ultraViewPager_dialog.setAutoMeasureHeight(true);
                ultraViewPager_dialog.setPageTransformer(false, new UltraDepthScaleTransformer());
                dialog.setContentView(contentView1);
                dialog.setTitle("测试结果");
                dialog.setCanceledOnTouchOutside(true);
                if (allTicketsList.size() > 0) {
                    dialog.show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.classification_dialog_null), Toast.LENGTH_SHORT).show();
                }
            }
        });

        ipu_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResultDialog dialog = new ResultDialog(getContext());
                View contentView2 = LayoutInflater.from(getContext()).inflate(
                        R.layout.result_dialog, null);
                ultraViewPager_ipuDialog = contentView2.findViewById(R.id.ultra_viewpager_dialog);
                ultraViewPager_ipuDialog.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
                adapter_dialog = new UltraPagerAdapter(true, allIPUTicketsList, getContext());
                ultraViewPager_ipuDialog.setAdapter(adapter_dialog);
                ultraViewPager_ipuDialog.setMultiScreen(0.6f);
                ultraViewPager_ipuDialog.setItemRatio(1.0f);
                ultraViewPager_ipuDialog.setAutoMeasureHeight(true);
                ultraViewPager_ipuDialog.setPageTransformer(false, new UltraDepthScaleTransformer());
                dialog.setContentView(contentView2);
                dialog.setTitle("测试结果");
                dialog.setCanceledOnTouchOutside(true);
                if (allIPUTicketsList.size() > 0) {
                    dialog.show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.classification_dialog_null), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getOffIPUData() {
        offAllTickets = new ArrayList<>();
        offAllTickets=classificationDB.fetchOfflineAll();
        offPoints = new int[Config.ChartPointNum];

        if (offAllTickets.size() != 0) {
            int count = 0;
            if (Config.ChartPointNum > offAllTickets.size()) {
                count = offAllTickets.size();
            } else {
                count = Config.ChartPointNum;
            }
            for (int j = 0; j < count; j++) {
                offPoints[j] = ConvertUtil.getFps(offAllTickets.get(offAllTickets.size() - j - 1).getFps())*60;
            }
        }

        //折线图
        DataUtil.showChart(getContext(),offLinearLayout,offAllTickets,offPoints);
    }
}
