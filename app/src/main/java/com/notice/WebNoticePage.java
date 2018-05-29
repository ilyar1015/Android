package com.notice;

import android.app.Activity;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.iflytek.voicedemo.R;

/**
 * Created by ilyar on 2017/9/10.
 *
 */

public class WebNoticePage extends Activity {

//    private WebView web,b;
    private String webUrl="https://zsb.nwnu.edu.cn";
    private WebView webview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_web_page);
//        init();
//        web.setWebChromeClient(new WebChromeClient(){
//            @Override
//            public void onProgressChanged(WebView view,int newProgress){
//                if(newProgress==100){
//
//                }else{
//
//                }
//            }
//        });
        webview=(WebView) findViewById(R.id.webNotice);
        WebSettings webSettings=webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webview.loadUrl(webUrl);
        webview.setWebViewClient(new webViewClient());
        webUrl=webview.getUrl();
    }

    public boolean onKeyDown(int keyCoder,KeyEvent event){
        if(keyCoder==KeyEvent.KEYCODE_BACK){
            if(webview.getUrl().equals(webUrl)) {
                finish();
                return false;
            }else {
                webview.goBack();
                return false;
            }
        }
        finish();
        return false;
    }

    private class webViewClient extends WebViewClient{
        public boolean shouldOverrideUrlLoading(WebView view,String url){
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
            super.onReceivedSslError(view, handler, error);
        }
    }

//    private void init(){
//        web=(WebView) findViewById(R.id.webNotice);
//        web.loadUrl("http://zsb.nwnu.edu.cn");
//        web.setWebViewClient(new WebViewClient(){
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view,String url){
//                view.loadUrl(url);
//                return true;
//            }
//        });
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode,KeyEvent event){
//        if(keyCode==KeyEvent.KEYCODE_BACK){
//            if(web.canGoBack()){
//                web.goBack();
//                return true;
//            }else{
//                System.exit(0);
//            }
//        }
//        return super.onKeyDown(keyCode,event);
//    }
}
