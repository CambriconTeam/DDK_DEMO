package com.cambricon.productdisplay.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.adapter.NewsAdapter;
import com.cambricon.productdisplay.bean.News;
import com.cambricon.productdisplay.utils.NewsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 18-2-3.
 */

public class NewsFragment extends Fragment {
    public static final int AUTOBANNER_CODE = 0x1001;
    public static final int LOAD_MORE = 1;
    public static final int LOAD_BOTTOM = 2;
    public static final int LOAD_START = 3;

    private View view;
    private ImageButton upBtn;
    private NewsAdapter adapter;
    private ProgressBar loadMore;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayoutManager mLinearLayoutManager;

    List<News> testList = new ArrayList<>();
    private List<News> newsList = new ArrayList<>();
    int max = 50;
    boolean start = true;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_MORE:
                    adapter.notifyDataSetChanged();
                    loadMore.setVisibility(View.GONE);
                    break;
                case LOAD_BOTTOM:
                    adapter.setFootCount(0);
                    adapter.notifyDataSetChanged();
                    loadMore.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "已经到底部啦", Toast.LENGTH_SHORT).show();
//                    upBtn.setVisibility(View.VISIBLE);
                    break;
                case LOAD_START:
                    loadMore.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.news_fragment, null);
        initNews();
        return view;
    }

    /**
     * 加载资讯列表
     */
    public void initNews() {
//        initNewsData();
        newsList.addAll(NewsUtil.getList());

        upBtn = view.findViewById(R.id.up);
        loadMore = view.findViewById(R.id.load_more);
        recyclerView = view.findViewById(R.id.recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLinearLayoutManager);
        adapter = new NewsAdapter(newsList);
        recyclerView.setAdapter(adapter);
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefresh = view.findViewById(R.id.news_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.scrollToPosition(0);
                upBtn.setVisibility(View.GONE);
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //加载界面数据
//                                addNews();
                                adapter.notifyDataSetChanged();
                                //刷新结束，隐藏刷新进度条
                                swipeRefresh.setRefreshing(false);
                            }
                        });

                    }
                }).start();
            }
        });

        /*recyclerView.addOnScrollListener(new EndLessOnScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadMoreData();
                    }
                }).start();
            }
        });*/


    }

    /**
     * 资讯测试集合
     *//*
    public void test() {
        News news1 = new News(1, R.drawable.news1, "CB lnsights 最新发布全球AI 100榜单,寒武纪首度入选", "2017年12月19日",
                "http://mp.weixin.qq.com/s?__biz=MzIwOTM3NDcxNQ==&mid=2247484072&idx=1&sn=1f59bc3d0e00f017c4d1462275e07810&chksm=97759e3ca002172aacbeb80f0d8240cd77a728d0676b3c08da872b595d68d90577a76d4d5938&mpshare=1&scene=23&srcid=0201yeAOaOULwORmRuKWM8gY#rd\n");
        News news2 = new News(2, R.drawable.news2, "寒武纪成功举办第22届国际体系结构...", "2017年4月15日",
                "http://mp.weixin.qq.com/s?__biz=MzIwOTM3NDcxNQ==&mid=2247483879&idx=1&sn=eb46f7cabe394f387f5657e060ff429d&chksm=97759d73a0021465cc7388598fb422566e1abf4b73b2846f64f9665c159e123b5e151f4ae293&mpshare=1&scene=23&srcid=0201wq0DSNP5Adx2Z1nknJDF#rd\n");
        News news3 = new News(3, R.drawable.news3, "重磅|寒武纪捷报频传--再获两项荣誉", "2017年12月5日",
                "http://mp.weixin.qq.com/s?__biz=MzIwOTM3NDcxNQ==&mid=2247484061&idx=1&sn=a5426e92d04556edbe50bf9faf3d5646&chksm=97759e09a002171fa68b0281cc7c3da7cab7a28e74ca1251a6a4f630f586baf8df9db2d1c1f7&mpshare=1&scene=23&srcid=02013br22Ve8RSzwmauPTH0i#rd\n");
        News news4 = new News(4, R.drawable.news4, "寒武纪科技收场发布会回顾", "2017年11月8日",
                "http://mp.weixin.qq.com/s?__biz=MzIwOTM3NDcxNQ==&mid=2247484044&idx=1&sn=16140175ff1b2d848983ee862d8c720c&chksm=97759e18a002170e357197aa5e342910f8404ed5006b9b75d8b3fcbfb85c0702cb75afc8d176&mpshare=1&scene=23&srcid=0201AblJ1T1xUDPzwEz3wBjA#rd\n");
        News news5 = new News(5, R.drawable.news5, "央视年末盘点2017黑科技，寒武纪芯片主力智能时代", "2017年12月19日",
                "http://mp.weixin.qq.com/s?__biz=MzIwOTM3NDcxNQ==&mid=2247484083&idx=1&sn=ac8fac2ae228c8ce328d1a20d925f618&chksm=97759e27a0021731fc5016f5bc7e14c223d7037e155ea3f3462fba34ee60de7d5fc0cf253bca&mpshare=1&scene=23&srcid=0201h9a9R87nRbNZOc8T20mx#rd\n");
        testList.add(news1);
        testList.add(news2);
        testList.add(news3);
        testList.add(news4);
        testList.add(news5);
    }
*/


    /**
     * 加载初始页面资讯
     */
 /*   public void initNewsData() {
        newsList.addAll(NewsUtil.getList());

    }*/


    /**
     * 上划加载更多数据
     */
/*    public void loadMoreData() {
        Message msgtemp = Message.obtain();
        msgtemp.what = LOAD_START;
        mHandler.sendMessage(msgtemp);
        if (adapter.getItemCount() < NewsUtil.getList().size()) {
            //添加刷新数据
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg = Message.obtain();
            msg.what = LOAD_MORE;
            mHandler.sendMessage(msg);
        }else{
            Message msg = Message.obtain();
            msg.what = LOAD_BOTTOM;
            mHandler.sendMessage(msg);
        }
    }
}*/

/**
 * 监听实现上拉加载更多的功能
 */

/*abstract class EndLessOnScrollListener extends RecyclerView.OnScrollListener {

    //声明一个LinearLayoutManager
    private LinearLayoutManager mLinearLayoutManager;
    //当前页
    private int currentPage = 0;
    //已经加载出来的Item的数量
    private int totalItemCount;
    //主要用来存储上一个totalItemCount
    private int previousTotal = 0;
    //在屏幕上可见的item数量
    private int visibleItemCount;
    //在屏幕可见的Item中的第一个
    private int firstVisibleItem;
    //是否正在上拉数据
    private boolean loading = true;

    public EndLessOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
        if (loading) {
            if (totalItemCount > previousTotal) {
                //说明数据已经加载结束
                loading = false;
                previousTotal = totalItemCount;
            }
        }

        if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem) {
            currentPage++;
            onLoadMore(currentPage);
            loading = true;
        }
    }

    *//**
     * 提供一个抽闲方法，在Activity中监听到这个EndLessOnScrollListener
     * 并且实现这个方法
     *//*
    public abstract void onLoadMore(int currentPage);
  */
}
