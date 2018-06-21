package com.example.ysl.mywps.ui.fragment;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.MessageBean;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.net.ResultPage;
import com.example.ysl.mywps.ui.adapter.MessageAdapter;
import com.example.ysl.mywps.ui.view.LoadMoreRecyclerView;
import com.example.ysl.mywps.ui.view.WrapContentLinearLayoutManager;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.example.ysl.mywps.utils.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
 * 消息
 * Created by Administrator on 2017/12/23 0023.
 */
public class MessageFragment extends BaseFragment {

    public ResultPage resultPage = new ResultPage();
    @BindView(R.id.recyclerview)
    LoadMoreRecyclerView recyclerview;

    @BindView(R.id.swipe_refresh_widget)
    SwipeRefreshLayout swipeRefresh;
    private WrapContentLinearLayoutManager mLayoutManager;
    List<MessageBean> bannerBeans = new ArrayList<>();
    private MessageAdapter adapter;

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container) {
        Log.i(TAG, "setView: ");
        View view = inflater.inflate(R.layout.fragment_message_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void afterView(View view) {
        Log.i(TAG, "afterView: ");
        initRecyclerView(recyclerview, swipeRefresh);
    }

    public void initRecyclerView(LoadMoreRecyclerView recyclerView, final SwipeRefreshLayout swipeRefreshLayout) {
        Log.i(TAG, "initRecyclerView: ");
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            mLayoutManager = new WrapContentLinearLayoutManager(this.getContext());
            recyclerView.setLayoutManager(mLayoutManager);
//            recyclerView.addItemDecoration(new CardDividerItemDecoration(this.getContext(),
//                    LinearLayoutManager.VERTICAL));
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.google_colors));
        }
//        swipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue
//                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
//                        .getDisplayMetrics()));

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    resultPage.setPage(1);
                    adapter.refreshData(new ArrayList<MessageBean>());
                    swipeRefreshLayout.setRefreshing(true);
                    getMessageData();
                }
            });
        }

        recyclerView.setLoadMoreListener(new LoadMoreRecyclerView.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                resultPage.setPage(resultPage.getPage() + 1);
                getMessageData();
            }
        });

        recyclerview.setAdapter(adapter);
//        recyclerView.setAutoLoadMoreEnable(true);
        resultPage.setPage(1);
        getMessageData();
    }

    @Override
    public void initData() {
        Log.i(TAG, "initData: ");
        adapter = new MessageAdapter(this.getContext());

    }

    /**
     * 获取消息接口
     */
    private void getMessageData() {
        Log.i(TAG, "getMessageData: ");
        try {
            Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(final ObservableEmitter<String> e) {

                    String token = SharedPreferenceUtils.loginValue(getActivity(), "token");
                    Call<String> call = HttpUtl.getMessageList("User/Public/message_list/", token, resultPage.getPage() + "", "20");

                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            swipeRefresh.setRefreshing(false);
                            if (!response.isSuccessful()) {
                                e.onNext(response.message());
                                return;
                            }
                            Log.i(TAG, "onResponse: " + response.body());
                            String data = response.body().toString();
                            String msg = null;
                            try {
                                JSONObject jsonObject = new JSONObject(data);
                                int code = jsonObject.getInt("code");
                                msg = jsonObject.getString("msg");

                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                Gson gson = new Gson();
                                bannerBeans = gson.fromJson(jsonArray.toString(), new TypeToken<List<MessageBean>>() {
                                }.getType());
                                e.onNext("Y");
                            } catch (Exception e1) {
                                Log.i(TAG, "onResponse: " + e1.getMessage());
                                e1.printStackTrace();
                                e.onNext(msg);
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            swipeRefresh.setRefreshing(false);
                            e.onNext(t.getMessage());
                        }
                    });
                }
            });

            Consumer<String> consumer = new Consumer<String>() {
                @Override
                public void accept(String s) throws Exception {
                    Log.i(TAG, "accept: " + s);
                    if (s.equals("Y")) {
                        handler.sendEmptyMessage(1);
//                        adapter.notifyDataSetChanged();
                    } else {
                        ToastUtils.showShort(getActivity(), s);
                    }
                }
            };

            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer);
        } catch (Exception ex) {
            swipeRefresh.setRefreshing(false);
            Log.i(TAG, "getBannerData: " + ex.getMessage());
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (resultPage.getPage() == 1) {
                        //第一页数据
                        recyclerview.removeAllViews();
                        adapter.refreshData(bannerBeans);
                    } else {
                        adapter.appendData(bannerBeans);
                    }
                    if (bannerBeans.size() == 20) {
                        recyclerview.setAutoLoadMoreEnable(true);
                        recyclerview.notifyMoreFinish(true);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void setKindFlag(int kindFlag) {
        Log.i(TAG, "setKindFlag: ");
    }

}
