package com.cambricon.productdisplay.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.bean.DetectionImage;
import com.cambricon.productdisplay.utils.Config;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by cambricon on 18-3-21.
 */

public class DetectIPUPagerAdapter extends PagerAdapter {
    private boolean isMultiScr;
    private Bitmap sdBitmap;
    private ArrayList<DetectionImage> allTicketsList;

    public DetectIPUPagerAdapter(boolean isMultiScr, ArrayList<DetectionImage> allTicketsList) {
        this.isMultiScr = isMultiScr;
        this.allTicketsList = allTicketsList;
    }

    @Override
    public int getCount() {
        return allTicketsList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.layout_child, null);
        TextView textView = linearLayout.findViewById(R.id.pager_result_tv);
        TextView timeTV = linearLayout.findViewById(R.id.pager_time_tv);
        ImageView imageView = linearLayout.findViewById(R.id.pager_image);
        File file = new File(Config.dImagePath, "detecIPU-" + (position+1) + ".jpg");
        sdBitmap = BitmapFactory.decodeFile(file.getPath());
        imageView.setImageBitmap(sdBitmap);
        timeTV.setText("检测网络类型：" + getNetType(position));
        textView.setText("检测所需时间：" + getTime(position) + "ms");
        linearLayout.setId(R.id.item_id);
        imageView.setId(R.id.item_id);
        switch (position % 4) {
            case 0:
                linearLayout.setBackgroundColor(Color.parseColor("#2196F3"));
                break;
            case 1:
                linearLayout.setBackgroundColor(Color.parseColor("#673AB7"));
                break;
            case 2:
                linearLayout.setBackgroundColor(Color.parseColor("#009688"));
                break;
            case 3:
                linearLayout.setBackgroundColor(Color.parseColor("#607D8B"));
                break;
        }
        container.addView(linearLayout);
        return linearLayout;
    }

    private String getResult(int position) {
        String result = allTicketsList.get(position).getResult();
        return result;
    }

    private String getImage(int position) {
        String result = allTicketsList.get(position).getName();
        return result;
    }

    private String getTime(int position) {
        String result = allTicketsList.get(position).getTime();
        return result;
    }

    private String getNetType(int position) {
        String result = allTicketsList.get(position).getNetType();
        return result;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        LinearLayout view = (LinearLayout) object;
        container.removeView(view);
    }
}
