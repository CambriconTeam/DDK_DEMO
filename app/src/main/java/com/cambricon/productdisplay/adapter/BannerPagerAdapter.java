package com.cambricon.productdisplay.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.activity.NewsInfoActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by huangyaling on 18-1-29.
 */

public class BannerPagerAdapter extends PagerAdapter implements View.OnClickListener{
    private String TAG = "BannerPagerAdapter";
    /**
     * 上下文
     */
    private Context mContext;
    /**
     *图像列表
     */
    private List<Integer> pictureList=new ArrayList<>();
    /**
     *默认轮播个数
     */
    public static final int FAKE_BANNER_SIZE=100000;
    private int positionNum;

    public BannerPagerAdapter(Context context, List<Integer> pictureList){
        this.mContext=context;
        this.pictureList=pictureList;
    }

    @Override
    public int getCount() {
        return FAKE_BANNER_SIZE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.ad_banner_item,container,false);
        ImageView imageView=view.findViewById(R.id.ad_banner_item);
        position %= pictureList.size();
        positionNum = position;
        imageView.setImageResource(pictureList.get(position));
        imageView.setOnClickListener(this);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), NewsInfoActivity.class);
        switch (positionNum){
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
        }
        intent.putExtra("url","http://www.cambricon.com/");
        view.getContext().startActivity(intent);
    }
}
