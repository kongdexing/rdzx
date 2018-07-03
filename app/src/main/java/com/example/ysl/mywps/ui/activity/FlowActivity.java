package com.example.ysl.mywps.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.FlowBean;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.ui.adapter.FlowAdapter;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ysl on 2018/1/15.
 */

public class FlowActivity extends BaseActivity {

    @BindView(R.id.flow_listview)
    ListView listView;

    private FlowAdapter adapter;
    private ArrayList<FlowBean> flows = new ArrayList<>();
    private String token;
    private String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_flow_layout);
        token = SharedPreferenceUtils.loginValue(this, "token");
        docId = getIntent().getStringExtra("docId");

        ButterKnife.bind(this);
        showLeftButton(true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitleText("流程进度");

        adapter = new FlowAdapter(this, flows);
        listView.setAdapter(adapter);
        afterData();

    }

    private void afterData() {

        netWork();
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {

    }

    private void netWork() {
        flows.clear();
        showProgress();
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<String> emitter) {

                Call<String> call = HttpUtl.getFlow("User/Oa/doc_log/", docId, token);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (!response.isSuccessful()) {
                            emitter.onNext(response.message());
                            return;
                        }

                        if (!response.isSuccessful()) {
                            emitter.onNext(response.message());
                            emitter.onNext("N");
                            return;
                        }

                        String data = response.body();
                        Log.i(TAG, "流程  " + data);
                        if (CommonUtil.isEmpty(data)) {
                            emitter.onNext("N");
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            int code = jsonObject.getInt("code");
                            String msg = jsonObject.getString("msg");

                            if (code == 0) {
                                emitter.onNext("N");
                                emitter.onNext(msg);
                            }

                            JSONObject dataJson = jsonObject.getJSONObject("data");

                            JSONArray jsonArray = dataJson.getJSONArray("multi");
                            Gson gson = new Gson();
                            for (int i = 0; i < jsonArray.length(); ++i) {
                                JSONObject childObject = jsonArray.getJSONObject(i);
                                FlowBean bean = gson.fromJson(childObject.toString(), FlowBean.class);
                                try {
                                    String[] split1 = bean.getCtime().split(" ");
//                                    String month = split1[0].substring(5, split1[0].length());
//                                    String time = split1[1].substring(0, 5);
                                    bean.setMonth(split1[0]);
                                    bean.setTime(split1[1]);
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                                flows.add(bean);
                            }

                            //反馈阶段，会反馈给多个人
                            JSONArray singleArray = dataJson.getJSONArray("single");
                            for (int i = 0; i < singleArray.length(); ++i) {
                                JSONObject childObject = singleArray.getJSONObject(i);
                                FlowBean bean = gson.fromJson(childObject.toString(), FlowBean.class);
                                flows.add(bean);
                            }
                            emitter.onNext(msg);
                            emitter.onNext("Y");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        emitter.onNext(t.getMessage());
                        emitter.onNext("N");
                    }
                });
            }
        });

        Consumer<String> observer = new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                hideProgress();
                if (s.equals("N") || s.equals("Y")) {
                    adapter.updateAdapter(flows);
                } else {
//                    ToastUtils.showShort(getApplicationContext(), s);
                }
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

}
