package com.cambricon.productdisplay.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.adapter.FaceDetectorAdapter;
import com.cambricon.productdisplay.bean.FaceDetectionImage;
import com.cambricon.productdisplay.db.FaceDetectDB;
import com.cambricon.productdisplay.utils.Config;
import com.cambricon.productdisplay.utils.ConvertUtil;
import com.cambricon.productdisplay.utils.DataUtil;
import com.cambricon.productdisplay.view.ResultDialog;
import com.tmall.ultraviewpager.UltraViewPager;
import com.tmall.ultraviewpager.transformer.UltraDepthScaleTransformer;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.ArrayList;

/**
 * Created by cambricon on 18-3-13.
 */

public class FaceDetectorData extends Fragment implements View.OnClickListener {

    private static final String TAG = FaceDetectorData.class.getSimpleName();

    private View mView;
    private String mContent;
    private Context mContext;
    private LinearLayout mLinearLayout;

    private Button mBtn_ipu_result;
    private Button mBtn_result;
    private TextView mTv_ipu_avg_fps;
    private TextView mTv_avg_fps;
    private TextView mTv_ipu_avg_time;
    private TextView mTv_avg_time;
    private GraphicalView mGraphicalView;

    private ArrayList<FaceDetectionImage> mAllTicketsList;
    private ArrayList<FaceDetectionImage> mAllIPUTicketsList;

    private UltraViewPager ultraViewPager_dialog;
    private UltraViewPager ultraViewPager_ipuDialog;

    private PagerAdapter adapter_dialog;

    private FaceDetectDB mFaceDetectDB;

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

    private int max = 0;
    private int min = 10000;

    public FaceDetectorData() {
    }

    @SuppressLint("ValidFragment")
    public FaceDetectorData(Context context) {
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_face_detector_data, container, false);
        initUI();
        return mView;
    }

    private void initUI() {
        mLinearLayout = mView.findViewById(R.id.layout_face_data_chart_line);
        mTv_avg_fps = mView.findViewById(R.id.tv_face_data_avg_fps);
        mTv_ipu_avg_fps = mView.findViewById(R.id.tv_face_data_avg_ipu_fps);
        mTv_avg_time = mView.findViewById(R.id.tv_face_data_avg_time);
        mTv_ipu_avg_time = mView.findViewById(R.id.tv_face_data_avg_ipu_time);
        mBtn_result = mView.findViewById(R.id.btn_face_data_all_result);
        mBtn_ipu_result = mView.findViewById(R.id.btn_face_data_all_ipu_result);

        mBtn_result.setOnClickListener(this);
        mBtn_ipu_result.setOnClickListener(this);

        mFaceDetectDB = new FaceDetectDB(getContext());
        mFaceDetectDB.open();
    }

    private void getData() {

        double allTime = 0.00;
        int allFps = 0;

        double allIPUTime = 0.00;
        int allIPUFps = 0;

        mAllTicketsList = new ArrayList<>();
        mAllTicketsList = mFaceDetectDB.fetchAll();
        points = new int[Config.ChartPointNum];

        mAllIPUTicketsList = new ArrayList<>();
        mAllIPUTicketsList = mFaceDetectDB.fetchIPUAll();
        ipu_points = new int[Config.ChartPointNum];


        if (mAllIPUTicketsList.size() != 0) {
            avgIPUTimes = new double[mAllIPUTicketsList.size()];
            int ipuCount;
            if (Config.ChartPointNum > mAllIPUTicketsList.size()) {
                ipuCount = mAllIPUTicketsList.size();
            } else {
                ipuCount = Config.ChartPointNum;
            }
            for (int j = 0; j < ipuCount; j++) {
                ipu_points[j] = ConvertUtil.getFps(mAllIPUTicketsList.get(mAllIPUTicketsList.size() - j - 1).getFps());
                avgIPUTimes[j] = ConvertUtil.convert2Double(mAllIPUTicketsList.get(mAllIPUTicketsList.size() - j - 1).getTime());
                allIPUTime = allIPUTime + avgIPUTimes[j];
                allIPUFps = allIPUFps + ipu_points[j];
                ipu_points[j]*=60;
            }
            avgIPUTimeValue = allIPUTime / ipuCount;
            avgIPUFpsValue = allIPUFps / ipuCount;
            mTv_ipu_avg_fps.setText(R.string.classification_avg_time);
            mTv_ipu_avg_time.setText(R.string.classification_single_time);
            mTv_ipu_avg_fps.append(String.valueOf(avgIPUFpsValue) + "张/分钟");
            mTv_ipu_avg_time.append(String.valueOf((int) avgIPUTimeValue) + "ms");
        }

        if (mAllTicketsList.size() != 0) {
            avgTimes = new double[mAllTicketsList.size()];
            int dataSum;
            if (Config.ChartPointNum > mAllTicketsList.size()) {
                dataSum = mAllTicketsList.size();
            } else {
                dataSum = Config.ChartPointNum;
            }
            for (int i = 0; i < dataSum; i++) {
                points[i] = ConvertUtil.getFps(mAllTicketsList.get(mAllTicketsList.size() - i - 1).getFps());
                if (points[i] > max) {
                    max = points[i];
                }
                if (points[i] < min) {
                    min = points[i];
                }
                avgTimes[i] = ConvertUtil.convert2Double(mAllTicketsList.get(mAllTicketsList.size() - i - 1).getTime());
                allTime = allTime + avgTimes[i];
                allFps = allFps + points[i];
                points[i]*=60;
            }
            avgTimeValue = allTime / dataSum;
            avgFpsValue = allFps / dataSum;

            mTv_avg_fps.setText(R.string.classification_avg_time);
            mTv_avg_time.setText(R.string.classification_single_time);
            mTv_avg_fps.append(String.valueOf(avgFpsValue) + "张/分钟");
            mTv_avg_time.append(String.valueOf((int) avgTimeValue) + "ms");
        }
    }

    @Override
    public void onResume() {
        getData();
        showChart();
        initUI();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_face_data_all_result:
                ResultDialog dialog = new ResultDialog(getContext());
                View contentView1 = LayoutInflater.from(getContext()).inflate(R.layout.result_dialog, null);
                ultraViewPager_dialog = contentView1.findViewById(R.id.ultra_viewpager_dialog);
                ultraViewPager_dialog.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
                adapter_dialog = new FaceDetectorAdapter(true, mAllTicketsList);
                ultraViewPager_dialog.setAdapter(adapter_dialog);
                ultraViewPager_dialog.setMultiScreen(0.6f);
                ultraViewPager_dialog.setItemRatio(1.0f);
                ultraViewPager_dialog.setAutoMeasureHeight(true);
                ultraViewPager_dialog.setPageTransformer(false, new UltraDepthScaleTransformer());
                dialog.setContentView(contentView1);
                dialog.setTitle("测试结果");
                dialog.setCanceledOnTouchOutside(true);
                if (mAllTicketsList.size() > 0) {
                    dialog.show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.face_detection_dialog_null), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_face_data_all_ipu_result:
                dialog = new ResultDialog(getContext());
                View contentView2 = LayoutInflater.from(getContext()).inflate(
                        R.layout.result_dialog, null);
                ultraViewPager_ipuDialog = contentView2.findViewById(R.id.ultra_viewpager_dialog);
                ultraViewPager_ipuDialog.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
                adapter_dialog = new FaceDetectorAdapter(true, mAllIPUTicketsList);
                ultraViewPager_ipuDialog.setAdapter(adapter_dialog);
                ultraViewPager_ipuDialog.setMultiScreen(0.6f);
                ultraViewPager_ipuDialog.setItemRatio(1.0f);
                ultraViewPager_ipuDialog.setAutoMeasureHeight(true);
                ultraViewPager_ipuDialog.setPageTransformer(false, new UltraDepthScaleTransformer());
                dialog.setContentView(contentView2);
                dialog.setTitle("测试结果");
                dialog.setCanceledOnTouchOutside(true);
                if (mAllIPUTicketsList.size() > 0) {
                    dialog.show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.face_detection_dialog_null), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showChart() {
        if (mAllTicketsList.size() > 0 || mAllIPUTicketsList.size() > 0) {
            String cpu_desc = getContext().getResources().getString(R.string.face_detection_chart_desc);
            String ipu_desc = getContext().getResources().getString(R.string.face_detection_chart_ipu_desc);
            XYMultipleSeriesDataset mDataSet = DataUtil.getDataSet(points, ipu_points, cpu_desc, ipu_desc);
            XYMultipleSeriesRenderer mRefender = DataUtil.getRefender(2,100);
            GraphicalView mGraphicalView = ChartFactory.getLineChartView(getContext(), mDataSet, mRefender);
            mLinearLayout.removeAllViews();
            mLinearLayout.addView(mGraphicalView);
        } else {
            mLinearLayout.removeAllViews();
            TextView nullDate = new TextView(getContext());
            nullDate.setText("暂无功能测评数据");
            nullDate.setGravity(Gravity.CENTER_HORIZONTAL);
            nullDate.setPadding(0, 300, 0, 0);
            mLinearLayout.addView(nullDate);
        }
    }
}
