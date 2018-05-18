package com.cambricon.productdisplay.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.cambricon.productdisplay.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.List;

/**
 * Created by dell on 18-4-21.
 */

public class SingleChartService {
    private GraphicalView mGraphicalView;
    private XYMultipleSeriesDataset multipleSeriesDataset;
    private XYMultipleSeriesRenderer multipleSeriesRenderer;
    private XYSeries mSeries;
    private XYSeries mSeries1;
    private XYSeries mSeries2;
    private XYSeriesRenderer mRenderer;
    private XYSeriesRenderer mRenderer1;
    private XYSeriesRenderer mRenderer2;
    private Context context;

    public static boolean ON_CPU=false;
    public static boolean ON_IPU=false;
    public static boolean OFF_IPU=false;

    public SingleChartService(Context context) {
        this.context = context;
    }

    public GraphicalView getGraphicalView() {
        mGraphicalView = ChartFactory.getCubeLineChartView(context,
                multipleSeriesDataset, multipleSeriesRenderer, 0.1f);
        return mGraphicalView;
    }

    public void setXYMultipleSeriesDataset() {
        multipleSeriesDataset = new XYMultipleSeriesDataset();
        mSeries = new XYSeries("online cpu");
        mSeries1=new XYSeries("online ipu");
        mSeries2=new XYSeries("offline ipu");
        multipleSeriesDataset.addSeries(mSeries);
        multipleSeriesDataset.addSeries(mSeries1);
        multipleSeriesDataset.addSeries(mSeries2);
    }

    public void setXYMultipleSeriesRenderer(double maxX, double maxY,
                                            String chartTitle) {
        multipleSeriesRenderer = new XYMultipleSeriesRenderer();
        if (chartTitle != null) {
            multipleSeriesRenderer.setChartTitle(chartTitle);
        }
        multipleSeriesRenderer.setRange(new double[]{0.5, maxX, 0, maxY});//xy轴的范围
        multipleSeriesRenderer.setLabelsColor(Color.GRAY);
        multipleSeriesRenderer.setXLabels(10);
        multipleSeriesRenderer.setYLabels(10);
        multipleSeriesRenderer.setXLabelsAlign(Paint.Align.RIGHT);
        multipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        multipleSeriesRenderer.setAxisTitleTextSize(20);
        multipleSeriesRenderer.setChartTitleTextSize(40);
        multipleSeriesRenderer.setLabelsTextSize(20);
        multipleSeriesRenderer.setLegendTextSize(20);
        multipleSeriesRenderer.setPointSize(2f);//曲线描点尺寸
        multipleSeriesRenderer.setFitLegend(true);
        multipleSeriesRenderer.setMargins(new int[]{60, 30, 15, 20});
        multipleSeriesRenderer.setShowGridX(true);
        multipleSeriesRenderer.setZoomEnabled(false, false);
        multipleSeriesRenderer.setAxesColor(Color.BLACK);
        multipleSeriesRenderer.setGridColor(Color.GRAY);
        multipleSeriesRenderer.setBackgroundColor(Color.WHITE);
        multipleSeriesRenderer.setApplyBackgroundColor(true);
        multipleSeriesRenderer.setMarginsColor(Color.WHITE);

        mRenderer = new XYSeriesRenderer();
        mRenderer.setColor(Color.RED);
        mRenderer.setDisplayChartValues(true);
        mRenderer.setChartValuesTextSize(25);
        mRenderer.setPointStyle(PointStyle.CIRCLE);

        mRenderer1=new XYSeriesRenderer();
        mRenderer1.setDisplayChartValues(true);
        mRenderer1.setChartValuesTextSize(25);
        mRenderer1.setPointStyle(PointStyle.CIRCLE);
        mRenderer1.setColor(Color.BLUE);

        mRenderer2=new XYSeriesRenderer();
        mRenderer2.setDisplayChartValues(true);
        mRenderer2.setChartValuesTextSize(25);
        mRenderer2.setPointStyle(PointStyle.CIRCLE);
        mRenderer2.setColor(Color.BLACK);

        multipleSeriesRenderer.addSeriesRenderer(mRenderer);
        multipleSeriesRenderer.addSeriesRenderer(mRenderer1);
        multipleSeriesRenderer.addSeriesRenderer(mRenderer2);
    }

    public void updateChart(double x, double y) {
        if(ON_CPU && x<=Config.ChartPointNum){
            mSeries.add(x, y);
        }else if(ON_IPU && x<=Config.ChartPointNum){
            mSeries1.add(x, y);
        }else if(OFF_IPU && x<=Config.ChartPointNum){
            mSeries2.add(x,y);
        }
        mGraphicalView.repaint();//此处也可以调用invalidate()
    }

    public void updateChart(List<Double> xList, List<Double> yList) {
        for (int i = 0; i < xList.size(); i++) {
            mSeries.add(xList.get(i), yList.get(i));
        }
        mGraphicalView.repaint();//此处也可以调用invalidate()
    }
}
