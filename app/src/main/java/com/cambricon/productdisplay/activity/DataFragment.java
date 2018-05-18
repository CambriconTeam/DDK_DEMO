package com.cambricon.productdisplay.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.adapter.TabAdapter;

import java.util.ArrayList;

/**
 * Created by dell on 18-2-3.
 */

public class DataFragment extends Fragment {
    private View view;
    private ViewPager viewPager;
    private TabAdapter tabAdapter;
    private TabLayout tabLayout;
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private ArrayList<String> mTitles = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.data_fragment, null);
        initView();
        return view;
    }

    public void initView() {
        //添加标题List
        initTitles();

        //添加Fragments
        initFragments();

        //加载
        initTabView();

        //设置分割线
        initDivider();
    }

    private void initTitles() {
        mTitles.add(getString(R.string.gv_text_item1));
        mTitles.add(getString(R.string.gv_text_item2));
        mTitles.add(getString(R.string.gv_text_item3));
    }

    private void initFragments() {
        mFragments.add(new ClassificationData(getContext()));
        mFragments.add(new DetectionData(getContext()));
        mFragments.add(new FaceDetectorData(getContext()));
    }

    private void initTabView() {
        viewPager = view.findViewById(R.id.data_count_viewpager);
        tabLayout = view.findViewById(R.id.tab_layout);

        tabAdapter = new TabAdapter(getActivity().getSupportFragmentManager(), mFragments, mTitles);

        //添加Tab
        for (int i = 0; i < mTitles.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(mTitles.get(i)));
        }

        //关联tabAdapter和ViewPager
        viewPager.setAdapter(tabAdapter);
        //关联ViewPager和TabLayout
        tabLayout.setupWithViewPager(viewPager);
    }

    private void initDivider() {
        //设置分割线
        LinearLayout linear = (LinearLayout) tabLayout.getChildAt(0);
        linear.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        linear.setDividerDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        //设置分割线间隔
        linear.setDividerPadding(dip2px(15));
    }

    //像素单位转换
    public int dip2px(int dip) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
