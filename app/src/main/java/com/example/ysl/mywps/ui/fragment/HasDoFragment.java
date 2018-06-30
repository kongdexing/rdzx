package com.example.ysl.mywps.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.DocumentListBean;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.ui.activity.StayToDoActivity;
import com.example.ysl.mywps.ui.activity.WpsDetailActivity;
import com.example.ysl.mywps.ui.adapter.StayDoAdapter;
import com.example.ysl.mywps.utils.CommonSetting;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.example.ysl.mywps.utils.SysytemSetting;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.orhanobut.logger.Logger;

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
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2018/1/14 0014.
 */

public class HasDoFragment extends BaseFragment {

    private static final int PAGE_SIZE = 20;

    @BindView(R.id.stay_to_listview)
    PullToRefreshListView listView;

    private StayDoAdapter adapter;
    private ArrayList<String> list = new ArrayList<>();
    private int pageNUmber = 1;
    private int pageTotal = 0;
    private ArrayList<DocumentListBean> allDocuments = new ArrayList<>();
    private String wpsMode = "";

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container) {

        View view = inflater.inflate(R.layout.frament_stay_to_do_layout, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        adapter = new StayDoAdapter(getActivity(), allDocuments);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), WpsDetailActivity.class);
                intent.putExtra(SysytemSetting.WPS_MODE, wpsMode);
                Bundle bundle = new Bundle();
                bundle.putParcelable("documentben", allDocuments.get((int) id));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);

        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                Logger.i("Stay downto");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                Logger.i("staqqy  upto");
                if (pageTotal > pageNUmber) {
                    ++pageNUmber;
                    netWork();
                } else {
                    finishLoad();
                }
            }
        });
    }

    @Override
    public void afterView(View view) {

    }

    @Override
    public void initData() {

    }

    public void setWpsMode(String wpsMode) {
        this.wpsMode = wpsMode;
    }

    @Override
    public void onResume() {
        super.onResume();
        pageNUmber = 1;
        netWork();
    }

    private void netWork() {
        ((StayToDoActivity) getActivity()).showProgress();
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                String token = SharedPreferenceUtils.loginValue(getActivity(), "token");
                Logger.i("token  " + token + "  " + CommonSetting.HTTP_TOKEN);
                Call<String> call = HttpUtl.documentList("User/Oa/doc_list/", token, pageNUmber + "", PAGE_SIZE + "", "2");
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (!response.isSuccessful()) {
                            emitter.onNext(response.message());
                            return;
                        }
                        String data = response.body();
                        Logger.i("stay " + data);
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            int code = jsonObject.getInt("code");
                            String msg = jsonObject.getString("msg");
//                            String datas = jsonObject.getString("data");
                            JSONObject childeObject = jsonObject.getJSONObject("data");
                            int total = childeObject.getInt("total");
                            pageTotal = total;
                            JSONArray array = childeObject.getJSONArray("list");
                            Gson gson = new Gson();
                            ArrayList<DocumentListBean> documents = gson.fromJson(array.toString(), new TypeToken<ArrayList<DocumentListBean>>() {
                            }.getType());
                            emitter.onNext("Y");
                            if (pageNUmber == 1) {
                                allDocuments.clear();
                                adapter.updateList(documents);
                            } else {
                                adapter.addList(documents);
                            }
                            allDocuments.addAll(documents);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        emitter.onNext(t.getMessage());
                    }
                });
            }
        });

        Consumer<String> observer = new Consumer<String>() {
            @Override
            public void accept(String s) {
                finishLoad();
                ((StayToDoActivity) getActivity()).hideProgress();

                if (s.equals("Y")) {

                } else if (s.equals("N")) {

                } else {
                    CommonUtil.showShort(getActivity(), s);
                }
//                Logger.i("等待事项  " + s);
            }

        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void finishLoad() {
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.onRefreshComplete();

            }
        }, 1000);
    }

    @Override
    public void setKindFlag(int kindFlag) {

    }
}
