package com.bn.main;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bn.parameter.Preference;
import com.bn.smartclass_android.R;


/**
 * Created by 卢欢 on 2017/8/16.
 *
 */

public class SchoolCampaignActivity extends Activity{


    ImageView backView;
    WebView webView;
    ProgressBar progressBar;

    String mes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);


        Bundle bundle = this.getIntent().getExtras();
        mes = bundle.getString("mes");

        backView = (ImageView)findViewById(R.id.backView);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { finish();}
        });

        progressBar =(ProgressBar)findViewById(R.id.progress);
        progressBar.setMax(100);

        webView = (WebView)findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);


        initWebView(webView);
        webView.loadUrl("http://"+ Preference.serverIP+":8080/"+ Preference.webPath+"/school_campign.jsp?compaign_id="+mes);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            if(webView.canGoBack()){
                webView.goBack();
            }else
            {
                this.finish();
            }
        }
        return true;
    }

    public void  initWebView(WebView wv)
    {
        //获取设置对象
        WebSettings settings = wv.getSettings();
        //启用JavaScript--启用此项jQuery Mobile才能正常工作
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        //设置字体显示的缩放比，默认为100
        settings.setTextZoom(100);
        //设置不使用缓存
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //设置禁用横向滚动条
        wv.setHorizontalScrollBarEnabled(false);
        //设置启用纵向滚动条
        wv.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                if(newProgress>=100)
                {
                    progressBar.setVisibility(view.GONE);
                }else
                {
                    progressBar.setVisibility(view.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view,newProgress);
            }
        });
        wv.setWebViewClient
                (
                        new WebViewClient()
                        {
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                view.loadUrl(url);
                                return true;
                            }
                        }
                );

    }
}
