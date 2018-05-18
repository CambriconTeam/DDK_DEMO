package com.cambricon.productdisplay.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.activity.NewsInfoActivity;
import com.cambricon.productdisplay.bean.News;
import com.cambricon.productdisplay.task.BannerTimerTask;
import com.cambricon.productdisplay.view.DepthPageTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer; 

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = "NewsAdapter";

    //选项类型
    private static final int HEAD_TYPE = 1;
    private static final int BODY_TYPE = 2;
    private static final int FOOT_TYPE = 3;


    //头部个数
    private int headCount = 1;
    //尾部个数
    private int footCount = 0;
    //存放数据集合
    private static List<News> newsList;

    //构造器
    public NewsAdapter(List<News> newsList){
        this.newsList = newsList;
    }

    /**
     * 返回Body数量
     * @return
     */
    private int getBodySize() {
        return newsList.size();
    }

    /**
     * 判断是否为头部
     * @param position
     * @return
     */
    private boolean isHead(int position) {
        return headCount!=0&&position<headCount;
    }

    /**
     * 判断是否为尾部
     * @param position
     * @return
     */
    private boolean isFoot(int position) {
        return footCount!=0&&(position>=(getBodySize()+headCount));
    }

    /**
     * 返回选项类型
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if (isHead(position)) {
            return HEAD_TYPE;
        }else if (isFoot(position)) {
            return FOOT_TYPE;
        }else {
            return BODY_TYPE;
        }
    }

    /**
     * 创建ViewHolder实例
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mLayoutInflater = LayoutInflater.from(parent.getContext());
        switch (viewType){
            case HEAD_TYPE:
                return new HeadViewHolder(mLayoutInflater.inflate(R.layout.news_viewpager, parent,false));
            case BODY_TYPE:
                return new BodyViewHolder(mLayoutInflater.inflate(R.layout.news_item, parent,false));
            case FOOT_TYPE:
                return new FootViewHolder(mLayoutInflater.inflate(R.layout.news_foot, parent,false));
            default:
                return null;
        }

    }

    /**
     * 对RecyclerView子项的数据进行赋值
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeadViewHolder) {
            HeadViewHolder viewHolder = (HeadViewHolder) holder;
        }
        else if (holder instanceof BodyViewHolder) {
            BodyViewHolder viewHolder = (BodyViewHolder) holder;
            News news = newsList.get(position-1);
            viewHolder.newsImage.setImageResource(news.getImageNum());
            viewHolder.newsTitle.setText(news.getTitle());
            viewHolder.newsDate.setText(news.getDate());
            viewHolder.line.setText(news.getUrl());
        }
        else if (holder instanceof FootViewHolder) {
            FootViewHolder viewHolder = (FootViewHolder) holder;
            viewHolder.foot.setText("————————已无更多————————");
        }

    }

    public void setFootCount(int footCount) {
        this.footCount = footCount;
    }

    /**
     * 返回选项数量
     * @return
     */
    @Override
    public int getItemCount() {
        return headCount+newsList.size()+footCount;
    }

    /**
     *创建各部分ViewHolder实例，并将布局加载
     */

    /**
     * 头部
     */
    private static class HeadViewHolder extends RecyclerView.ViewHolder implements PreferenceManager.OnActivityDestroyListener,ViewPager.OnPageChangeListener,View.OnTouchListener{

        private ViewPager mViewPager;
        private LinearLayout mLinearLayout;
        private List<Integer> pictureList = new ArrayList<>();
        private BannerPagerAdapter mBannerPagerAdapter;
        private boolean mIsUserTouched = false;
        private int mBannerPosition;
        private int mPagerindex = 0;
        private Timer timer = new Timer();
        private BannerTimerTask mBannerTimerTask;
        private View itemview;

        public HeadViewHolder(View itemView) {
            super(itemView);
            this.itemview = itemView;
            mViewPager = itemView.findViewById(R.id.ad_banner);
            mLinearLayout = itemView.findViewById(R.id.indicator_layout);

            mBannerPagerAdapter = new BannerPagerAdapter(itemView.getContext(), pictureList);
            mViewPager.setAdapter(mBannerPagerAdapter);
            mViewPager.setCurrentItem(pictureList.size() * 100);
            mViewPager.setPageTransformer(true, new DepthPageTransformer());
            mViewPager.setOnTouchListener(this);
            mViewPager.setOnPageChangeListener(this);

            initData();
            startBannerTimer();
        }

        private void initData() {
            pictureList.add(R.drawable.ch1);
            pictureList.add(R.drawable.ch2);
            pictureList.add(R.drawable.ch3);
            pictureList.add(R.drawable.ch4);
            pictureList.add(R.drawable.ch5);
            pictureList.add(R.drawable.ch6);

            for (int i = 0; i < pictureList.size(); i++) {
                View view = new View(itemView.getContext());
                view.setBackgroundResource(R.drawable.indicator_bg);
                view.setEnabled(false);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(15, 15);
                layoutParams.leftMargin = 10;
                mLinearLayout.addView(view, layoutParams);
            }
            mLinearLayout.getChildAt(mBannerPosition).setEnabled(true);
        }


        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            switch (action){
                case MotionEvent.ACTION_DOWN:
                    mIsUserTouched = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    mIsUserTouched = true;
                    break;
                case MotionEvent.ACTION_UP:
                    mIsUserTouched = false;
                    break;
                default:
                    break;
            }
            return false;
        }

        private void startBannerTimer() {
            if (timer == null) {
                timer = new Timer();
            }
            if (mBannerTimerTask != null) {
                mBannerTimerTask.cancel();
            }
            mBannerTimerTask = new BannerTimerTask(bannerHandler);
            if (timer != null && mBannerTimerTask != null) {
                timer.schedule(mBannerTimerTask, 2000, 2000);
            }
        }

        Handler bannerHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (!mIsUserTouched) {
                    mBannerPosition = mViewPager.getCurrentItem();
                    mBannerPosition = (mBannerPosition + 1) % mBannerPagerAdapter.FAKE_BANNER_SIZE;
                    mViewPager.setCurrentItem(mBannerPosition);
                    mLinearLayout.getChildAt(mPagerindex).setEnabled(false);
                    if (mBannerPosition > pictureList.size() - 1) {
                        mPagerindex = mBannerPosition % pictureList.size();
                    } else {
                        mPagerindex = mBannerPosition;
                    }
                    mLinearLayout.getChildAt(mPagerindex).setEnabled(true);
                }
                return true;
            }
        });

        @Override
        public void onActivityDestroy() {
            if (mBannerTimerTask != null) {
                mBannerTimerTask.cancel();
                mBannerTimerTask = null;
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            position %= pictureList.size();
            mLinearLayout.getChildAt(mPagerindex).setEnabled(false);
            mLinearLayout.getChildAt(position).setEnabled(true);
            mPagerindex = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

    }

    /**
     * 中部
     */
    public  static class BodyViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage;
        TextView newsTitle;
        TextView newsDate;
        TextView line;
        RelativeLayout newsitem;

        @SuppressLint("ClickableViewAccessibility")
        public BodyViewHolder(View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.new_image);
            newsTitle = itemView.findViewById(R.id.new_title);
            newsDate = itemView.findViewById(R.id.news_date);
            line = itemView.findViewById(R.id.news_line);

            newsitem = itemView.findViewById(R.id.news_item);
            newsitem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, NewsInfoActivity.class);
                    intent.putExtra("url",line.getText());
                    context.startActivity(intent);
                }
            });


        }

    }

    /**
     * 尾部
     */
    private static class FootViewHolder extends RecyclerView.ViewHolder {
        TextView foot;
        public FootViewHolder(View itemView) {
            super(itemView);
            foot = itemView.findViewById(R.id.news_foot);
        }
    }




}




