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
import android.widget.TextView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.adapter.DetectIPUPagerAdapter;
import com.cambricon.productdisplay.adapter.DetectPagerAdapter;
import com.cambricon.productdisplay.bean.DetectionImage;
import com.cambricon.productdisplay.db.DetectionDB;
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

public class DetectionData extends Fragment implements View.OnClickListener {
    private static final String TAG = DetectionData.class.getSimpleName();

    private View view;
    private Context context;
    private String content;
    private LinearLayout linearLayout;
    private LinearLayout linearLayoutoffline;
    private GraphicalView graphicalView;
    private GraphicalView graphicalViewOffline;
    private int[] points;
    private int[] rePoints;
    private TextView fps_tv;
    private TextView time_tv;
    private TextView fps_ipu;
    private TextView time_ipu;
    private Button ipu_result;
    private Button result_btn;
    private DetectionDB detectionDB;
    private UltraViewPager ultraViewPager_dialog;
    private PagerAdapter adapter_dialog;

    private ArrayList<DetectionImage> allTicketsList;
    private ArrayList<DetectionImage> ipu_allTickets;

    private ArrayList<DetectionImage> allOfflineList;
    private int[] offline_points;

    private int[] ipu_points;
    private int[] ipu_rePoints;

    private TextView mTv_title_cpu;
    private TextView mTv_title_ipu;

    public DetectionData() {

    }

    @SuppressLint("ValidFragment")
    public DetectionData(Context contexts) {
        this.context = contexts;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_detection_data, null);
        init();
        return view;
    }

    private void init() {
        linearLayout = view.findViewById(R.id.chart_line);
        linearLayoutoffline = view.findViewById(R.id.chart_line_offline);

        fps_tv = view.findViewById(R.id.avg_fps);
        time_tv = view.findViewById(R.id.avg_time);
        result_btn = view.findViewById(R.id.all_result);

        fps_ipu = view.findViewById(R.id.avg_ipu_fps);
        time_ipu = view.findViewById(R.id.avg_ipu_time);
        ipu_result = view.findViewById(R.id.all_ipu_result);

        mTv_title_cpu = view.findViewById(R.id.tv_data_detection_title_cpu);
        mTv_title_ipu = view.findViewById(R.id.tv_data_detection_title_ipu);

        mTv_title_cpu.setText(this.getResources().getString(R.string.title_data_detection_cpu));
        mTv_title_ipu.setText(this.getResources().getString(R.string.title_data_detection_ipu));

        result_btn.setOnClickListener(this);
        ipu_result.setOnClickListener(this);

        detectionDB = new DetectionDB(getContext());
        detectionDB.open();
    }

    private void getData() {
        getCPUDetectionData();
        getIPUDetectionData();
        getOfflineData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.all_result:
                showDetetcionPic(allTicketsList);
                break;
            case R.id.all_ipu_result:
                Log.i(TAG, "onClick: "+ipu_allTickets.size());
                showIPUDetetcionPic(ipu_allTickets);
                break;
        }
    }

    private void showDetetcionPic(ArrayList<DetectionImage> list) {
        ResultDialog dialog = new ResultDialog(getContext());
        View contentView1 = LayoutInflater.from(getContext()).inflate(R.layout.result_dialog, null);
        ultraViewPager_dialog = contentView1.findViewById(R.id.ultra_viewpager_dialog);
        ultraViewPager_dialog.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
        adapter_dialog = new DetectPagerAdapter(true, list);
        ultraViewPager_dialog.setAdapter(adapter_dialog);
        ultraViewPager_dialog.setMultiScreen(0.6f);
        ultraViewPager_dialog.setItemRatio(1.0f);
        ultraViewPager_dialog.setAutoMeasureHeight(true);
        ultraViewPager_dialog.setPageTransformer(false, new UltraDepthScaleTransformer());
        dialog.setContentView(contentView1);
        dialog.setTitle("测试结果");
        dialog.setCanceledOnTouchOutside(true);
        if (list.size() > 0) {
            dialog.show();
        } else {
            Toast.makeText(getContext(), getString(R.string.classification_dialog_null), Toast.LENGTH_SHORT).show();
        }
    }

    private void showIPUDetetcionPic(ArrayList<DetectionImage> list) {
        ResultDialog dialog = new ResultDialog(getContext());
        View contentView1 = LayoutInflater.from(getContext()).inflate(R.layout.result_dialog, null);
        ultraViewPager_dialog = contentView1.findViewById(R.id.ultra_viewpager_dialog);
        ultraViewPager_dialog.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
        adapter_dialog = new DetectIPUPagerAdapter(true, list);
        ultraViewPager_dialog.setAdapter(adapter_dialog);
        ultraViewPager_dialog.setMultiScreen(0.6f);
        ultraViewPager_dialog.setItemRatio(1.0f);
        ultraViewPager_dialog.setAutoMeasureHeight(true);
        ultraViewPager_dialog.setPageTransformer(false, new UltraDepthScaleTransformer());
        dialog.setContentView(contentView1);
        dialog.setTitle("测试结果");
        dialog.setCanceledOnTouchOutside(true);
        if (list.size() > 0) {
            dialog.show();
        } else {
            Toast.makeText(getContext(), getString(R.string.classification_dialog_null), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
        DataUtil dataUtil=new DataUtil();
        dataUtil.showDChart(getContext(),linearLayout, graphicalView, allTicketsList, ipu_allTickets, points, rePoints,ipu_points,ipu_rePoints,1500);
        dataUtil.showDChart(getContext(),linearLayoutoffline, graphicalViewOffline, allOfflineList, ipu_allTickets, offline_points, null,null,null,200);
    }

    private void getOfflineData(){
        allOfflineList=detectionDB.fetchOfflineAll();
        offline_points=new int[Config.ChartPointNum];
        Log.e("huangyaling","points="+allOfflineList.size());
        if (allOfflineList.size() != 0) {
            int x = 0;
            int y = 0;
            for (int i = allOfflineList.size() - 1; i >= 0; i--) {
                if (allOfflineList.get(i).getNetType().equals("faster_rcnn") && x < offline_points.length) {
                    offline_points[x] = ConvertUtil.getFps(allOfflineList.get(i).getFps());
                    Log.e("huangyaling",""+offline_points[x]);
                    x++;
                } else if (y < rePoints.length) {
                    rePoints[y] = ConvertUtil.getFps(allOfflineList.get(i).getFps());
                    y++;
                }
            }
        }

    }




    private void getCPUDetectionData() {
        int all50 = 0, allFps50 = 0;
        int allFastRCNN = 0, allFpsFastRCNN = 0;
        int avg50 = 0, avgFastRCNN = 0;
        int time50 = 0, timeFastRCNN = 0;

        allTicketsList = new ArrayList<>();
        allTicketsList = detectionDB.fetchAll();

        points = new int[Config.ChartPointNum];
        rePoints = new int[Config.ChartPointNum];

        if (allTicketsList.size() != 0) {
            int x = 0;
            int y = 0;
            for (int i = allTicketsList.size() - 1; i >= 0; i--) {
                Log.w(TAG, "getData: show" + allTicketsList.get(i));
                if (allTicketsList.get(i).getNetType().equals("ResNet50") && x < points.length) {
                    points[x] = ConvertUtil.getFps(allTicketsList.get(i).getFps());
                    allFps50 += points[x];
                    all50 += Integer.valueOf(allTicketsList.get(i).getTime());
                    x++;
                } else if (y < rePoints.length) {
                    rePoints[y] = ConvertUtil.getFps(allTicketsList.get(i).getFps());
                    allFpsFastRCNN += rePoints[y];
                    allFastRCNN += Integer.valueOf(allTicketsList.get(i).getTime());
                    y++;
                }
            }
            if (x > 0) {
                avg50 = allFps50 / x;
                time50 = all50 / x;
            }
            if (y > 0) {
                avgFastRCNN = allFpsFastRCNN / y;
                timeFastRCNN = allFastRCNN / y;
            }
        }

        fps_tv.setText("平均速率：\n");
        time_tv.setText("平均时间：\n");
        fps_tv.append(String.valueOf(avg50) + "/" + String.valueOf(avgFastRCNN) + "(张/分钟)");
        time_tv.append(String.valueOf(time50) + "/" + String.valueOf(timeFastRCNN) + "(ms)");
    }

    private void getIPUDetectionData() {
        int ipu_avg50 = 0, ipu_avg101 = 0;
        int ipu_time50 = 0, ipu_time101 = 0;
        int ipu_all50 = 0, ipu_allFps50 = 0;
        int ipu_all101 = 0, ipu_allFps101 = 0;

        ipu_allTickets = new ArrayList<>();
        ipu_allTickets = detectionDB.fetchIPUAll();

        for (DetectionImage image : ipu_allTickets) {
            Log.w(TAG, "getData:ipudetecte: " + image.toString());
        }

        Log.w(TAG, "getData:ipu " + ipu_allTickets.size());

        ipu_points = new int[Config.ChartPointNum];
        ipu_rePoints = new int[Config.ChartPointNum];

        if (ipu_allTickets.size() != 0) {
            int a = 0;
            int b = 0;
            for (int i = ipu_allTickets.size() - 1; i >= 0; i--) {
                if (ipu_allTickets.get(i).getNetType().equals("ResNet50") && a < ipu_points.length) {
                    ipu_points[a] = ConvertUtil.getFps(ipu_allTickets.get(i).getFps());
                    ipu_allFps50 += ipu_points[a];
                    ipu_all50 += Integer.valueOf(ipu_allTickets.get(i).getTime());
                    a++;
                } else if (b < rePoints.length) {
                    ipu_rePoints[b] = ConvertUtil.getFps(ipu_allTickets.get(i).getFps());
                    ipu_allFps101 += ipu_rePoints[b];
                    ipu_all101 += Integer.valueOf(ipu_allTickets.get(i).getTime());
                    b++;
                }
            }
            if (a > 0) {
                ipu_avg50 = ipu_allFps50 / a;
                ipu_time50 = ipu_all50 / a;
            }
            if (b > 0) {
                ipu_avg101 = ipu_allFps101 / b;
                ipu_time101 = ipu_all101 / b;
            }
        }

        Log.w(TAG, "getData: " + ipu_points.length);
        Log.w(TAG, "getData: " + ipu_rePoints.length);

        fps_ipu.setText("平均速率：\n");
        time_ipu.setText("平均时间：\n");
        fps_ipu.append(String.valueOf(ipu_avg50) + "/" + String.valueOf(ipu_avg101) + "(张/分钟)");
        time_ipu.append(String.valueOf(ipu_time50) + "/" + String.valueOf(ipu_time101) + "(ms)");
    }





}
