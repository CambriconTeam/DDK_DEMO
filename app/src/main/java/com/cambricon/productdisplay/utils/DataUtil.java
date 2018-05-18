package com.cambricon.productdisplay.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cambricon.productdisplay.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * 折线图
 */
public class DataUtil {

    private static final int Y_MAX = 1000;

    /**
     * 检测折线图
     */

    public void showDChart(Context context,LinearLayout linearLayout, GraphicalView graphicalView, ArrayList
            allTicketsList, ArrayList ipu_allTickets, int[] points, int[] rePoints,int[] ipu_points,int[] ipu_rePoints,int max) {
        if (allTicketsList.size() > 0 || ipu_allTickets.size() > 0) {
            XYMultipleSeriesDataset mDataSet = getDataSet(context, points, rePoints, ipu_points, ipu_rePoints);
            XYMultipleSeriesRenderer mRefender = DataUtil.getRefender(4,max);
            graphicalView = ChartFactory.getLineChartView(context, mDataSet, mRefender);
            linearLayout.removeAllViews();
            linearLayout.addView(graphicalView);
        } else {
            linearLayout.removeAllViews();
            TextView nullDate = new TextView(context);
            nullDate.setText("暂无检测测评数据");
            nullDate.setGravity(Gravity.CENTER_HORIZONTAL);
            nullDate.setPadding(0, 300, 0, 0);
            linearLayout.addView(nullDate);
        }
    }
    public XYMultipleSeriesDataset getDataSet(Context context, int[] points, int[] repoints, int[] ipu_points, int[] ipu_repoints) {
        XYMultipleSeriesDataset seriesDataset = new XYMultipleSeriesDataset();
        XYSeries xySeries1 = new XYSeries(context.getResources().getString(R.string.detection_resnet50_cpu));
        XYSeries xySeries2 = new XYSeries(context.getResources().getString(R.string.detection_fastrcnn_cpu));
        XYSeries xySeries3 = new XYSeries(context.getResources().getString(R.string.detection_resnet50_ipu));
        XYSeries xySeries4 = new XYSeries(context.getResources().getString(R.string.detection_fastrcnn_ipu));
        for (int i = 1; i <= Config.ChartPointNum; i++) {
            if(points!=null){
                if(points[i - 1] != 0){
                    xySeries1.add(i, points[i - 1]);
                }
            }
        }
        for (int i = 1; i <= Config.ChartPointNum; i++) {
            if(repoints!=null){
                if(repoints[i - 1] != 0){
                    xySeries2.add(i, repoints[i - 1]);
                }
            }
        }
        for (int i = 1; i <= Config.ChartPointNum; i++) {
            if(ipu_points!=null){
                if(ipu_points[i - 1] != 0){
                    xySeries3.add(i, ipu_points[i - 1]);
                }
            }
        }
        for (int i = 1; i <= Config.ChartPointNum; i++) {
            if(ipu_repoints!=null){
                if(ipu_repoints[i - 1] != 0){
                    xySeries4.add(i, ipu_repoints[i - 1]);
                }
            }
        }

        seriesDataset.addSeries(xySeries1);
        seriesDataset.addSeries(xySeries2);
        seriesDataset.addSeries(xySeries3);
        seriesDataset.addSeries(xySeries4);

        return seriesDataset;
    }

    /**
     * simple_classify
     */
    public static void showChart(Context context, LinearLayout linearLayout, List allTicketsList, List allIPUTicketsList, List simpleAllList, List simpleIPUList,int[] points, int[] ipu_points, int[] simplePoints,int[] simpleIPUPoints) {
        if (allTicketsList.size() > 0 || allIPUTicketsList.size() > 0
                || simpleAllList.size()>0 || simpleIPUList.size()>0) {
            String cpu_desc = context.getResources().getString(R.string.classification_chart_desc);
            String ipu_desc = context.getResources().getString(R.string.classification_chart_ipu_desc);
            String simple_desc = "CPU单层分类模型";
            String simple_ipu="IPU单层分类模型";
            XYMultipleSeriesDataset mDataSet = DataUtil.getDataSet(points, ipu_points, simplePoints, simpleIPUPoints,cpu_desc, ipu_desc, simple_desc,simple_ipu);
            XYMultipleSeriesRenderer mRefender = DataUtil.getRefender(4, 200);
            GraphicalView graphicalView = ChartFactory.getLineChartView(context, mDataSet, mRefender);
            linearLayout.removeAllViews();
            linearLayout.addView(graphicalView);
        } else {
            linearLayout.removeAllViews();
            TextView nullDate = new TextView(context);
            nullDate.setText("暂无功能测评数据");
            nullDate.setGravity(Gravity.CENTER_HORIZONTAL);
            nullDate.setPadding(0, 300, 0, 0);
            linearLayout.addView(nullDate);
        }
    }

    public static XYMultipleSeriesDataset getDataSet(int points[], int ipu_points[], int[] simplePoints,int[] simpleIpuPoints, String cpu_desc, String ipu_desc, String simple_desc,String simple_ipu) {

        XYMultipleSeriesDataset seriesDataset = new XYMultipleSeriesDataset();
        XYSeries xySeries1 = new XYSeries(cpu_desc);
        XYSeries xySeries2 = new XYSeries(ipu_desc);
        XYSeries xySeries3 = new XYSeries(simple_desc);
        XYSeries xySeries4 = new XYSeries(simple_ipu);

        for (int i = 1; i <= Config.ChartPointNum; i++) {
            if(points!=null){
                if (points[i - 1] != 0) {
                    xySeries1.add(i, points[i - 1] / 60);
                }
            }
            if(ipu_points!=null){
                if (ipu_points[i - 1] != 0) {
                    xySeries2.add(i, ipu_points[i - 1] / 60);
                }
            }
            if(simplePoints!=null){
                if (simplePoints[i - 1] != 0) {
                    xySeries3.add(i, simplePoints[i-1]/60);
                }
            }
            if(simpleIpuPoints!=null){
                if(simpleIpuPoints[i-1]!=0){
                    xySeries4.add(i, simpleIpuPoints[i-1]/60);
                }
            }
        }
        seriesDataset.addSeries(xySeries1);
        seriesDataset.addSeries(xySeries2);
        seriesDataset.addSeries(xySeries3);
        seriesDataset.addSeries(xySeries4);


        return seriesDataset;
    }

    /**
     * 分类折线图
     */
    public static void showChart(Context context, LinearLayout linearLayout, List allTicketsList, List allIPUTicketsList, int[] points, int[] ipu_points) {
        if (allTicketsList.size() > 0 || allIPUTicketsList.size() > 0) {
            String cpu_desc = context.getResources().getString(R.string.classification_chart_desc);
            String ipu_desc = context.getResources().getString(R.string.classification_chart_ipu_desc);
            XYMultipleSeriesDataset mDataSet = DataUtil.getDataSet(points, ipu_points, cpu_desc, ipu_desc);
            XYMultipleSeriesRenderer mRefender = DataUtil.getRefender(2, 200);
            GraphicalView graphicalView = ChartFactory.getLineChartView(context, mDataSet, mRefender);
            linearLayout.removeAllViews();
            linearLayout.addView(graphicalView);
        } else {
            linearLayout.removeAllViews();
            TextView nullDate = new TextView(context);
            nullDate.setText("暂无功能测评数据");
            nullDate.setGravity(Gravity.CENTER_HORIZONTAL);
            nullDate.setPadding(0, 300, 0, 0);
            linearLayout.addView(nullDate);
        }
    }

    public static XYMultipleSeriesDataset getDataSet(int points[], int ipu_points[], String cpu_desc, String ipu_desc) {

        XYMultipleSeriesDataset seriesDataset = new XYMultipleSeriesDataset();
        XYSeries xySeries1 = new XYSeries(cpu_desc);
        XYSeries xySeries2 = new XYSeries(ipu_desc);
        for (int i = 1; i <= Config.ChartPointNum; i++) {
            if (points[i - 1] != 0) {
                xySeries1.add(i, points[i - 1] / 60);
            }
            if (ipu_points[i - 1] != 0) {
                xySeries2.add(i, ipu_points[i - 1] / 60);
            }
        }
        seriesDataset.addSeries(xySeries1);
        seriesDataset.addSeries(xySeries2);

        return seriesDataset;
    }

    /**
     * ipu_off_chart
     */
    public static void showChart(Context context, LinearLayout linearLayout, List allTicketsList, int[] points) {
        if (allTicketsList.size() > 0) {
            String off_classify = "IPU离线模型图片分类数据";
            XYMultipleSeriesDataset mDataSet = DataUtil.getDataSet(points, off_classify);
            XYMultipleSeriesRenderer mRefender = DataUtil.getRefender(1, 200);
            GraphicalView graphicalView = ChartFactory.getLineChartView(context, mDataSet, mRefender);
            linearLayout.removeAllViews();
            linearLayout.addView(graphicalView);
        } else {
            linearLayout.removeAllViews();
            TextView nullDate = new TextView(context);
            nullDate.setText("暂无功能测评数据");
            nullDate.setGravity(Gravity.CENTER_HORIZONTAL);
            nullDate.setPadding(0, 300, 0, 0);
            linearLayout.addView(nullDate);
        }
    }

    public static XYMultipleSeriesDataset getDataSet(int points[], String off_classify) {
        XYMultipleSeriesDataset seriesDataset = new XYMultipleSeriesDataset();
        XYSeries xySeries1 = new XYSeries(off_classify);
        for (int i = 1; i <= Config.ChartPointNum; i++) {
            if (points[i - 1] != 0) {
                xySeries1.add(i, points[i - 1] / 60);
            }
        }
        seriesDataset.addSeries(xySeries1);

        return seriesDataset;
    }

    /**
     * 折线图参数
     *
     * @param num 折线图折线数量
     * @param max 折线图最大取值
     * @return
     */

    public static XYMultipleSeriesRenderer getRefender(int num, int max) {
        /*描绘器，设置图表整体效果，比如x,y轴效果，缩放比例，颜色设置*/
        XYMultipleSeriesRenderer seriesRenderer = new XYMultipleSeriesRenderer();

        seriesRenderer.setChartTitleTextSize(50);//设置图表标题的字体大小(图的最上面文字)
        seriesRenderer.setMargins(new int[]{20, 40, 40, 40});//设置外边距，顺序为：上左下右
        //坐标轴设置
        seriesRenderer.setAxisTitleTextSize(30);//设置坐标轴标题字体的大小
        seriesRenderer.setYAxisMin(0);//设置y轴的起始值
        seriesRenderer.setYAxisMax(max);//设置y轴的最大值
        seriesRenderer.setYLabels(10);//设置y轴显示点数
        seriesRenderer.setXAxisMin(0.5);//设置x轴起始值
        seriesRenderer.setXAxisMax(10.5);//设置x轴最大值
        seriesRenderer.setYLabelsColor(0, Color.BLACK);
        seriesRenderer.setXLabelsColor(Color.BLACK);
        //颜色设置
        seriesRenderer.setLabelsColor(0xFF85848D);//设置标签颜色
        seriesRenderer.setBackgroundColor(R.color.gridview_bg);//设置图表的背景颜色
        //缩放设置
        seriesRenderer.setZoomButtonsVisible(false);//设置缩放按钮是否可见
        seriesRenderer.setZoomEnabled(false); //图表是否可以缩放设置
        seriesRenderer.setZoomInLimitX(7);
        //图表移动设置
        seriesRenderer.setPanEnabled(false);//图表是否可以移动

        //坐标轴标签设置
        seriesRenderer.setLabelsTextSize(25);//设置标签字体大小
        seriesRenderer.setXLabelsAlign(Paint.Align.CENTER);
        seriesRenderer.setYLabelsAlign(Paint.Align.LEFT);
        seriesRenderer.setXLabels(0);//显示的x轴标签的个数
        for (int i = 1; i <= Config.ChartPointNum; i++) {
            seriesRenderer.addXTextLabel(i, String.valueOf(i));
        }
        seriesRenderer.setPointSize(5);//设置坐标点大小
        seriesRenderer.setMarginsColor(Color.WHITE);//设置外边距空间的颜色
        seriesRenderer.setClickEnabled(false);

        for (int i = 0; i < num; i++) {
            int color = 0;
            PointStyle style = null;
            switch (i) {
                case 0:
                    color = 0xFFF46C48;
                    style = PointStyle.SQUARE;
                    break;
                case 1:
                    color = R.color.main_line;
                    style = PointStyle.TRIANGLE;
                    break;
                case 2:
                    color = Color.BLUE;
                    style = PointStyle.DIAMOND;
                    break;
                case 3:
                    color = Color.GREEN;
                    style = PointStyle.CIRCLE;
                    break;
                default:
                    break;
            }

            //某一组数据的描绘器，描绘该组数据的个性化显示效果，主要是字体跟颜色的效果
            XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
            xySeriesRenderer.setAnnotationsColor(0xFFFF0000);//设置注释（注释可以着重标注某一坐标）的颜色
            xySeriesRenderer.setAnnotationsTextAlign(Paint.Align.CENTER);//设置注释的位置
            xySeriesRenderer.setAnnotationsTextSize(20);//设置注释文字的大小
            xySeriesRenderer.setPointStyle(style);//坐标点的显示风格
            xySeriesRenderer.setFillPoints(true);
            xySeriesRenderer.setPointStrokeWidth(3);//坐标点的大小
            xySeriesRenderer.setColor(color);//表示该组数据的图或线的颜色
            xySeriesRenderer.setDisplayChartValues(true);//设置是否显示坐标点的y轴坐标值
            xySeriesRenderer.setChartValuesTextSize(25);//设置显示的坐标点值的字体大小
            xySeriesRenderer.setDisplayChartValuesDistance(30);

            seriesRenderer.addSeriesRenderer(xySeriesRenderer);
        }

        return seriesRenderer;
    }
}
