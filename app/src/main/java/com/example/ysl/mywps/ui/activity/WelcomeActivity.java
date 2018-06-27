package com.example.ysl.mywps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.utils.CommonSetting;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.SysytemSetting;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

public class WelcomeActivity extends BaseActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        showTitle(false);
        getLogin();
    }

    private void getLogin() {

        preferences = getSharedPreferences(SysytemSetting.USER_FILE, Context.MODE_PRIVATE);
        String token = preferences.getString(SysytemSetting.USER_TOKEN, "");
        String imToken = preferences.getString(SysytemSetting.ROIM_TOKEN, "");

        if (CommonUtil.isEmpty(imToken) || CommonUtil.isEmpty(token)) {
            gotoLogin();
            return;
        }
        CommonSetting.HTTP_TOKEN = token;
        if (CommonUtil.isNotEmpty(token)) {
            rongYun(imToken);
            loginSuccess();
        }
    }

    private void gotoLogin() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginSuccess() {
        //判断跳转类型
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void rongYun(String imToken) {

        try {
            RongIM.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RongIM.connect(imToken, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {

                Log.i(TAG, "onTokenIncorrect");
            }

            @Override
            public void onSuccess(String s) {
                Log.i(TAG, "链接成功" + s);

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                Log.i(TAG, "链接失败" + errorCode);

            }
        });

    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {

    }
}
