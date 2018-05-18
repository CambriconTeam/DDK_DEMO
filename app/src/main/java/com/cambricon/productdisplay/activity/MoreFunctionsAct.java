package com.cambricon.productdisplay.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.adapter.MoreFunctionRecylerAdaper;
import com.cambricon.productdisplay.utils.StatusBarCompat;

public class MoreFunctionsAct extends AppCompatActivity {
    private String[] mData, mSummary;
    private Integer[] mDraw;
    private RecyclerView recyclerView;
    private MoreFunctionRecylerAdaper adapter;
    private android.support.v7.widget.Toolbar toolbar;
    private Integer[] iconBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarCompat.compat(this, Color.parseColor("#256CE0"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_functions);
        initdata();
        initView();
        setActionBar();
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        adapter = new MoreFunctionRecylerAdaper(this, mData, mSummary, mDraw);
        setListener(adapter);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.scheduleLayoutAnimation();
    }

    private void initdata() {
        mData = getResources().getStringArray(R.array.more_functions);
        mSummary = getResources().getStringArray(R.array.more_functions_summary);
        TypedArray head = getResources().obtainTypedArray(R.array.more_function_img);
        mDraw = new Integer[head.length()];
        for (int i = 0; i < head.length(); i++) {
            mDraw[i] = head.getResourceId(i, 0);
        }

    }

    /**
     * 设置ActionBar
     */
    private void setActionBar() {
        toolbar.setTitle(getString(R.string.more_function_title));
        setSupportActionBar(toolbar);
        /*显示Home图标*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        this.toolbar = findViewById(R.id.more_toolbar);
    }

    /**
     * rececyle view item click lister
     *
     * @param adapter
     */
    public void setListener(MoreFunctionRecylerAdaper adapter) {
        adapter.setmItemClickListener(new MoreFunctionRecylerAdaper.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (position) {
                    case 0:
                        Intent intent = new Intent(MoreFunctionsAct.this, OffLineClassifiactionAct.class);
                        intent.putExtra("BaseToolBarTitle", getString(R.string.offline_classification));
                        startActivity(intent);
                        break;
                    case 1:
                        Intent intent1 = new Intent(MoreFunctionsAct.this, OffLineDetectionAct.class);
                        intent1.putExtra("BaseToolBarTitle", getString(R.string.offline_detection));
                        startActivity(intent1);
                        break;
                    case 2:
                        Intent intent2 = new Intent(MoreFunctionsAct.this, SingleChartActivity.class);
                        startActivity(intent2);

                        break;
                    case 3:
                        /*Intent intent3 = new Intent(MoreFunctionsAct.this, SimpleClassificationAct.class);
                        intent3.putExtra("BaseToolBarTitle", "单层在线分类");
                        startActivity(intent3);*/
                        break;
                    case 4:
                        /*Intent intent4 = new Intent(MoreFunctionsAct.this, ClassificationActivity.class);
                        intent4.putExtra("deploy_prototxt", false);
                        startActivity(intent4);*/
                        break;
                    case 5:
                        /*Intent intent5 = new Intent(MoreFunctionsAct.this, SingleNetFuncAct.class);
                        startActivity(intent5);*/
                        break;
                    case 6:
                        break;
                    case 7:
                        break;


                }
            }
        });
    }
}
