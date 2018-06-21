package com.example.ysl.mywps.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.interfaces.JSCallBack;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/5/14 0014.
 */

public class QuestionActivity extends BaseWebActivity implements JSCallBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_layout);
        ButterKnife.bind(this);
        setTitleText("问卷调查");
        initWebView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
            if (null == mUploadMessage5) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mUploadMessage5.onReceiveValue(WebChromeClient.FileChooserParams
                        .parseResult(resultCode, intent));
            }
            mUploadMessage5 = null;
        }
    }

}
