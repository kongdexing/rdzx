package com.example.ysl.mywps.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.ysl.mywps.bean.DocumentListBean;
import com.example.ysl.mywps.bean.MessageBean;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.receiver.MyJipushReceiver;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.example.ysl.mywps.utils.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by dexing on 2018-6-27 0027.
 */

public class WpsDetailBaseActivity extends BaseActivity {

    public WpsBroadCast reciver = new WpsBroadCast();
    public String token = "";
    public DocumentListBean documentInfo = null;

    private class WpsBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "mybitmap", Toast.LENGTH_SHORT).show();
            Logger.i("static  " + CommonUtil.myPath);
        }
    }

    BroadcastReceiver pushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyJipushReceiver.ACTION_RECEIVE_MESSAGE.equals(action)) {
                String param = intent.getStringExtra("param");
                try {
                    Gson gson = new Gson();
                    MessageBean messageBean = gson.fromJson(param, new TypeToken<MessageBean>() {
                    }.getType());
                    if (documentInfo.getId().equals(messageBean.getId())) {
                        getWpsInfo(messageBean.getDetail_id());
                    }
                } catch (Exception ex) {

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.registerReceiver(reciver, new IntentFilter("com.example.ysl.mywps.sign"));
        token = SharedPreferenceUtils.loginValue(this, "token");

        IntentFilter filter = new IntentFilter(MyJipushReceiver.ACTION_RECEIVE_MESSAGE);
        filter.setPriority(4000);
        registerReceiver(pushReceiver, filter);
    }

    public void getWpsInfo(final String doc_id) {
        showProgress();
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                Call<String> call = HttpUtl.getDocumentMd5("User/Oa/detail/", token, doc_id);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        hideProgress();
                        if (!response.isSuccessful()) {
                            emitter.onNext(response.message());
                            return;
                        }
                        String data = response.body();
                        Log.i(TAG, "onResponse: " + data);
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            int code = jsonObject.getInt("code");
                            String msg = jsonObject.getString("msg");

                            if (code == 0) {
                                Gson gson = new Gson();
                                documentInfo = gson.fromJson(jsonObject.getString("data"), new TypeToken<DocumentListBean>() {
                                }.getType());

                                if (documentInfo != null) {
                                    emitter.onNext("Y");
                                } else {
                                    emitter.onNext("N");
                                }
                                Log.i(TAG, "onResponse documentInfo: " + documentInfo.getId());
                            } else {
                                ToastUtils.showShort(WpsDetailBaseActivity.this, msg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            emitter.onNext(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        emitter.onNext(t.getMessage());
                    }
                });
            }
        });
        Consumer<String> consumer = new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.i(TAG, "accept: " + s);
                if (s != null && s.equals("Y")) {
                    afterData();
                } else {
                    ToastUtils.showShort(WpsDetailBaseActivity.this, s);
                }
            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {

    }

    protected void afterData() {
    }

}
