package com.example.ysl.mywps.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;

import com.example.ysl.mywps.R;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/5/14 0014.
 */

public class MeettingActivity extends BaseWebActivity {

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
    public void initData() {
        super.initData();
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
