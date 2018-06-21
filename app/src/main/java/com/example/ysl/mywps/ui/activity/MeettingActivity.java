package com.example.ysl.mywps.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.interfaces.JSCallBack;
import com.example.ysl.mywps.interfaces.JavascriptBridge;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.example.ysl.mywps.utils.SysytemSetting;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;

/**
 * Created by Administrator on 2018/5/14 0014.
 */

public class MeettingActivity extends BaseWebActivity implements JSCallBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_layout);
        ButterKnife.bind(this);
        setTitleText("会议助手");
        initWebView();
    }

    @Override
    public void initView() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            Log.d(TAG, "onActivityResult: " + 1);
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
            Log.d(TAG, "onActivityResult: " + 2);
        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
            Log.d(TAG, "onActivityResult: " + 3);
            if (null == mUploadMessage5) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mUploadMessage5.onReceiveValue(WebChromeClient.FileChooserParams
                        .parseResult(resultCode, intent));

                Log.d(TAG, "onActivityResult: " + 4);
            }
            mUploadMessage5 = null;
        }
    }
}
