package com.cambricon.productdisplay.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.utils.StatusBarCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dell on 18-4-3.
 */

public class AboutActivity extends AppCompatActivity {
    public String[] listTitle = null;
    public String[] listSummary;
    public ListView aboutList;
    private android.support.v7.widget.Toolbar toolbar;
    ArrayList<Map<String, Object>> data = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarCompat.compat(this, Color.parseColor("#256CE0"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);
        init();
        setActionBar();
    }

    public void init() {
        toolbar = findViewById(R.id.about_toolbar);
        aboutList = findViewById(R.id.about_list);
        listTitle = this.getResources().getStringArray(R.array.about_list_title);
        listSummary = this.getResources().getStringArray(R.array.about_list_summary);
        int length = listTitle.length;
        for (int i = 0; i < length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("title", listTitle[i]);
            if (i < listSummary.length) {
                item.put("summary", listSummary[i]);
            }
            data.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.about_list_item, new String[]{"title", "summary"}, new int[]{R.id.about_list_title,
                R.id.about_list_summary});
        aboutList.setAdapter(adapter);
    }

    /**
     * 设置ActionBar
     */
    private void setActionBar() {
        toolbar.setTitle(getString(R.string.about_us));
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
}
