package com.cambricon.productdisplay.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.db.CommDB;
import com.cambricon.productdisplay.utils.StatusBarCompat;
import com.kyleduo.switchbutton.SwitchButton;

import java.lang.reflect.Method;

public class HomeActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar toolbar;
    final int PERMISSION_REQUST_CODE = 0x001;

    /*创建一个Drawerlayout和Toolbar联动的开关*/
    private ActionBarDrawerToggle toggle;

    private DrawerLayout drawerLayout;
    private RadioGroup radioGroup;
    private RadioButton testbtn;
    private RadioButton databtn;

    private RadioButton newsbtn;
    private Drawable test_on;
    private Drawable test_off;
    private Drawable data_on;
    private Drawable data_off;
    private Drawable news_on;

    private Drawable news_off;
    private TestFragment testFragment;
    private DataFragment dataFragment;
    private NewsFragment newsFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private SwitchButton cpu_mode_btn;
    private SwitchButton ipu_mode_btn;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private RelativeLayout about_us;

    private static boolean isExit = false;
    private CommDB commDB;
	private boolean isRoot = false;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.main_line));
        setContentView(R.layout.home_layout);
        initView();
        initFragment();
        initRadioBtn();
        setActionBar();
        setListener();
        setDrawerToggle();
    }
    private String getProperty(String key, String defaultvalue){
        String value = defaultvalue;
        try{
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            value = (String) (get.invoke(c,key));
            Log.i("huangyaling", "original verison is : " + value);

            if(value == null){
                value = "";
            }
        }catch (Exception e){
            Log.e("huangyaling", "error info : " + e.getMessage());
            value = "";
        }finally {
            return value;
        }
    }

    private void setDrawerToggle() {
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);
        /*同步drawerlayout的状态*/
        toggle.syncState();
    }

    private void initView() {
        this.toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.mydrawer);
        radioGroup = findViewById(R.id.rel_navigate);
        testbtn = findViewById(R.id.tab_test);
        databtn = findViewById(R.id.tab_data);
        newsbtn = findViewById(R.id.tab_adv);
        cpu_mode_btn = findViewById(R.id.cpu_mode_switchbtn);
        ipu_mode_btn = findViewById(R.id.ipu_mode_switchbtn);
        about_us=findViewById(R.id.about);
        mSharedPreferences = getSharedPreferences("Cambricon_mode", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        ipu_mode_btn.setChecked(mSharedPreferences.getBoolean(String.valueOf(R.string.ipu_mode), false));
        cpu_mode_btn.setChecked(mSharedPreferences.getBoolean(String.valueOf(R.string.cpu_mode), true));
        commDB = new CommDB(this);
        commDB.open();

        String testversion=getProperty("ro.config.hiaiversion","defaultvalue");
        Log.e("huangyaling",testversion);

    }

    private void initFragment() {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        testFragment = new TestFragment();
        fragmentTransaction.add(R.id.main_content, testFragment);
        fragmentTransaction.commit();
    }

    private void initRadioBtn() {
        test_on = getResources().getDrawable(R.mipmap.test_on);
        test_off = getResources().getDrawable(R.mipmap.test_off);
        data_on = getResources().getDrawable(R.mipmap.data_on);
        data_off = getResources().getDrawable(R.mipmap.data_off);
        news_on = getResources().getDrawable(R.mipmap.news_on);
        news_off = getResources().getDrawable(R.mipmap.news_off);
        test_on.setBounds(1, 1, test_on.getIntrinsicWidth(), test_on.getIntrinsicHeight());
        test_off.setBounds(1, 1, test_off.getIntrinsicWidth(), test_off.getIntrinsicHeight());
        data_on.setBounds(1, 1, data_on.getIntrinsicWidth(), data_on.getIntrinsicHeight());
        data_off.setBounds(1, 1, data_off.getIntrinsicWidth(), data_off.getIntrinsicHeight());
        news_on.setBounds(1, 1, news_on.getIntrinsicWidth(), news_on.getIntrinsicHeight());
        news_off.setBounds(1, 1, news_off.getIntrinsicWidth(), news_off.getIntrinsicHeight());
    }

    private void hideAll(FragmentTransaction transaction) {
        if (transaction == null) {
            return;
        }
        if (testFragment != null) {
            transaction.hide(testFragment);
        }

        if (dataFragment != null) {
            transaction.hide(dataFragment);
        }

        if (newsFragment != null) {
            transaction.hide(newsFragment);
        }
    }

    private void setListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.tab_test:
                        databtn.setCompoundDrawables(null, data_off, null, null);
                        testbtn.setCompoundDrawables(null, test_on, null, null);
                        newsbtn.setCompoundDrawables(null, news_off, null, null);
                        databtn.setTextColor(getResources().getColor(R.color.home_text_color));
                        testbtn.setTextColor(getResources().getColor(R.color.main_line));
                        newsbtn.setTextColor(getResources().getColor(R.color.home_text_color));

                        FragmentTransaction testTransaction = fragmentManager.beginTransaction();
                        hideAll(testTransaction);
                        if (testFragment == null) {
                            testFragment = new TestFragment();
                            testTransaction.add(R.id.main_content, testFragment);
                        } else {
                            testTransaction.show(testFragment);
                        }
                        testTransaction.commit();
                        break;
                    case R.id.tab_data:
                        databtn.setCompoundDrawables(null, data_on, null, null);
                        testbtn.setCompoundDrawables(null, test_off, null, null);
                        newsbtn.setCompoundDrawables(null, news_off, null, null);
                        databtn.setTextColor(getResources().getColor(R.color.main_line));
                        testbtn.setTextColor(getResources().getColor(R.color.home_text_color));
                        newsbtn.setTextColor(getResources().getColor(R.color.home_text_color));

                        FragmentTransaction dataTransaction = fragmentManager.beginTransaction();
                        hideAll(dataTransaction);
                        if (dataFragment == null) {
                            dataFragment = new DataFragment();
                            dataTransaction.add(R.id.main_content, dataFragment);
                        } else {
                            dataTransaction.show(dataFragment);
                        }
                        dataTransaction.commit();
                        break;
                    case R.id.tab_adv:
                        databtn.setCompoundDrawables(null, data_off, null, null);
                        testbtn.setCompoundDrawables(null, test_off, null, null);
                        newsbtn.setCompoundDrawables(null, news_on, null, null);
                        databtn.setTextColor(getResources().getColor(R.color.home_text_color));
                        testbtn.setTextColor(getResources().getColor(R.color.home_text_color));
                        newsbtn.setTextColor(getResources().getColor(R.color.main_line));
                        FragmentTransaction newsTransaction = fragmentManager.beginTransaction();
                        hideAll(newsTransaction);
                        if (newsFragment == null) {
                            newsFragment = new NewsFragment();
                            newsTransaction.add(R.id.main_content, newsFragment);
                        } else {
                            newsTransaction.show(newsFragment);
                        }
                        newsTransaction.commit();
                        break;
                }
            }
        });


        cpu_mode_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cpu_mode_btn.isChecked()) {
                    editor.putBoolean(String.valueOf(R.string.cpu_mode), true);
                    editor.putBoolean(String.valueOf(R.string.ipu_mode), false);
                    editor.commit();
                    ipu_mode_btn.setChecked(false);
                } else {
                    editor.putBoolean(String.valueOf(R.string.cpu_mode), false);
                    editor.putBoolean(String.valueOf(R.string.ipu_mode), true);
                    editor.commit();
                    ipu_mode_btn.setChecked(true);
                }
            }
        });
        ipu_mode_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (ipu_mode_btn.isChecked()) {
                    editor.putBoolean(String.valueOf(R.string.ipu_mode), true);
                    editor.putBoolean(String.valueOf(R.string.cpu_mode), false);
                    editor.commit();
                    cpu_mode_btn.setChecked(false);
                } else {
                    editor.putBoolean(String.valueOf(R.string.ipu_mode), false);
                    editor.putBoolean(String.valueOf(R.string.cpu_mode), true);
                    editor.commit();
                    cpu_mode_btn.setChecked(true);
                }
            }
        });

        about_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("huangyaling","about");
                startActivity(new Intent(HomeActivity.this,AboutActivity.class));
            }
        });
    }

    /**
     * 设置ActionBar
     */
    private void setActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle("ProductDisPlay");
        /*显示Home图标*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onDestroy() {
        if (commDB != null) {
            commDB.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(!isExit){
                isExit = true;
                Toast.makeText(this, R.string.exit, Toast.LENGTH_SHORT).show();
                mHandler.sendEmptyMessageDelayed(0, 2000);
            }else{
                finish();
                System.exit(0);
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }
}
