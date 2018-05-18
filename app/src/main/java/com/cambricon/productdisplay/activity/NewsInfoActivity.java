package com.cambricon.productdisplay.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.utils.StatusBarCompat;

/**
 * 资讯详细内容页面
 */
public class NewsInfoActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private Button button;
    private WebView webView;
    private ImageView errorImage;
    private ProgressBar progressBar;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarCompat.compat(this, Color.parseColor("#256CE0"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_info);
        initView();
        setToolbar();
        initNewsInfo();
    }

    public void initView() {
        toolbar = findViewById(R.id.detection_toolbar);
        webView = findViewById(R.id.news_info);
        progressBar = findViewById(R.id.webProgressBar);
        progressBar.setMax(100);
    }

    /**
     * toolBar返回按钮
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //返回按钮
            case android.R.id.home:
                finish();
                break;
            //分享按钮
            case R.id.share:
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                textIntent.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(Intent.createChooser(textIntent, "Share"));
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 设置toolbar属性
     */
    public void setToolbar() {
        toolbar.setTitle(R.string.newsinfo_title);
        setSupportActionBar(toolbar);
        /*显示Home图标*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /**
     * 添加菜单栏
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_toolbar, menu);
        return true;
    }

    /**
     * 加载资讯页面
     */
    public void initNewsInfo() {
        url = getIntent().getStringExtra("url");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebViewClient());
        webView.setWebViewClient(new WebClient());
        webView.loadUrl(url);
    }

    /**
     * 实现加载进度
     */
    private class WebViewClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
            if (newProgress == 100) {
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, newProgress);
        }

    }

    private class WebClient extends android.webkit.WebViewClient {
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            showErrorPage();
        }


    }

    /**
     * 显示自定义错误提示页面，用一个View覆盖在WebView
     */
    private void showErrorPage() {
        webView.removeAllViews(); //移除加载网页错误时，默认的提示信息
        webView.setVisibility(View.GONE);
        errorImage = findViewById(R.id.error_image);
        errorImage.setVisibility(View.VISIBLE);
        button = findViewById(R.id.recover);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorImage.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
                String url = getIntent().getStringExtra("url");
                webView.loadUrl(url);
            }
        });
    }


}
