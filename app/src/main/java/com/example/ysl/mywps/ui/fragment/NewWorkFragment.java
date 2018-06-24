package com.example.ysl.mywps.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.BannerBean;
import com.example.ysl.mywps.bean.HotBean;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.net.ResultPage;
import com.example.ysl.mywps.ui.activity.BaseWebActivity;
import com.example.ysl.mywps.ui.activity.NewOAActivity;
import com.example.ysl.mywps.ui.activity.WebViewActivity;
import com.example.ysl.mywps.ui.view.HomeNewsView;
import com.example.ysl.mywps.ui.view.LoadmoreScrollView;
import com.example.ysl.mywps.ui.view.autoviewpager.GlideImageLoader;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viewpagerindicator.CirclePageIndicator;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
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
 * Created by Administrator on 2017/12/23 0023.
 */

public class NewWorkFragment extends BaseFragment {

    Unbinder unbinder;

    @BindView(R.id.scrollView)
    LoadmoreScrollView scrollView;

    @BindView(R.id.rlTipAD)
    RelativeLayout rlTipAD;
    @BindView(R.id.topBanner)
    Banner topBanner;
    @BindView(R.id.indicator)
    CirclePageIndicator indicator;
    @BindView(R.id.tipTitle)
    TextView tipTitle;

    @BindView(R.id.llGroup)
    LinearLayout llGroup;

    final List<String> listBannerImages = new ArrayList<>();
    final List<String> listTitles = new ArrayList<>();
    public ResultPage resultPage = new ResultPage();
    List<String> advertList = new ArrayList<>();
    private String token = "";
    private boolean requesting = false;

    @Override
    public void initData() {
        Log.i(TAG, "initData: ");
        token = SharedPreferenceUtils.loginValue(getActivity(), "token");
//        saveFileTypes(token);
    }

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container) {
        Log.i(TAG, "setView: ");
        View view = inflater.inflate(R.layout.fragment_newwork_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void afterView(View view) {
        Log.i(TAG, "afterView: ");
        int[] WHs = CommonUtil.getScreenWH(this.getContext());
        int width = WHs[0];
        int height = width * 49 / 72;
        Log.i(TAG, "initView: " + width + "  " + height);

        try {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rlTipAD.getLayoutParams();
            lp.width = width;
            lp.height = height;
            rlTipAD.setLayoutParams(lp);
        } catch (Exception ex) {
            Log.i(TAG, "initView setLayoutParams error: " + ex.getMessage());
        }

        //设置图片加载器
        // 1.设置幕后item的缓存数目
        topBanner.setOffscreenPageLimit(1);
        topBanner.setBackgroundResource(R.drawable.tip_ad_def);
//        topBanner.setImages(new ArrayList<Integer>(){R.drawable.tip_ad_def});
//        topBanner.setImages(listBannerImages);
        topBanner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        //设置banner动画效果
//        topBanner.setPageTransformer(true, new GalleryTransformer(getActivity()));
        //设置自动轮播，默认为true
        topBanner.isAutoPlay(true);
        //设置轮播时间
        topBanner.setDelayTime(3000);
        topBanner.start();
        //banner设置方法全部调用完毕时最后调用

        topBanner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {

            }
        });

        scrollView.setOnScrollToBottomListener(new LoadmoreScrollView.OnScrollToBottomListener() {
            @Override
            public void onScrollBottomListener(boolean isBottom) {
                Log.i(TAG, "onScrollBottomListener isBottom: " + isBottom);
                if (isBottom && !requesting) {
                    resultPage.setPage(resultPage.getPage() + 1);
                    getRecommendList();
                }
            }
        });
        loadData();
    }

    private void loadData() {
        if (topBanner == null) {
            return;
        }
        getBannerData();
        topBanner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected: " + position);
                try {
                    tipTitle.setText(listTitles.get(position - 1));
                } catch (Exception ex) {
                    Log.i(TAG, "onPageSelected error: " + ex.getMessage());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        resultPage.setPage(1);
        getRecommendList();
    }

    @Override
    public void setKindFlag(int kindFlag) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @OnClick({R.id.llItem1, R.id.llItem2, R.id.llItem3, R.id.llItem4})
    void viewClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.llItem1://移动办公
                intent = new Intent(getActivity(), NewOAActivity.class);
                break;
            case R.id.llItem2://提案系统
                intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(BaseWebActivity.WEB_TITLE, "提案系统");
                intent.putExtra(BaseWebActivity.WEB_URL, HttpUtl.HTTP_WEB_URL + "TiAn/");
                break;
            case R.id.llItem3://社情民意
                intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(BaseWebActivity.WEB_TITLE, "社情民意");
                intent.putExtra(BaseWebActivity.WEB_URL, HttpUtl.HTTP_WEB_URL + "sqmy/");
                break;
            case R.id.llItem4://委员之家
                intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(BaseWebActivity.WEB_TITLE, "委员之家");
                intent.putExtra(BaseWebActivity.WEB_URL, "http://oa.qupeiyi.cn/index.php/User/Weiyuan/index.html");
                break;
        }
        if (intent != null) startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (topBanner != null)
            topBanner.startAutoPlay();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (topBanner != null)
            topBanner.stopAutoPlay();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * 获取Banner滚动图
     */
    private void getBannerData() {
        try {
            Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(final ObservableEmitter<String> e) {

                    String token = SharedPreferenceUtils.loginValue(getActivity(), "token");
                    Call<String> call = HttpUtl.contact("User/Public/banner_list/", token);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
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
                                List<BannerBean> bannerBeans = gson.fromJson(jsonArray.toString(), new TypeToken<List<BannerBean>>() {
                                }.getType());

                                listBannerImages.clear();
                                listTitles.clear();

                                for (int i = 0; i < bannerBeans.size(); i++) {
                                    if (bannerBeans.get(i) != null) {
                                        listBannerImages.add(bannerBeans.get(i).getBanner());
                                        listTitles.add(bannerBeans.get(i).getTitle());
                                    }
                                }

                                tipTitle.setText(listTitles.get(0));
                                topBanner.update(listBannerImages);

                                e.onNext("Y");
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                e.onNext(msg);
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            e.onNext(t.getMessage());
                        }
                    });

                }
            });

            Consumer<String> consumer = new Consumer<String>() {
                @Override
                public void accept(String s) throws Exception {
                    Log.i(TAG, "accept: " + s);
//                if (s.equals("Y")) {
//                    adapter.loadData(list);
//                } else {
//                    ToastUtils.showShort(getActivity(), s);
//                }
                }
            };

            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer);
        } catch (Exception ex) {
            Log.i(TAG, "getBannerData: " + ex.getMessage());
        }
    }

    private void getRecommendList() {
        requesting = true;
        try {
            Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(final ObservableEmitter<String> e) {

                    String token = SharedPreferenceUtils.loginValue(getActivity(), "token");
                    Call<String> call = HttpUtl.getRecommendList("User/Public/recommend_list/", token, resultPage.getPage() + "", "5");
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            requesting = false;
                            if (!response.isSuccessful()) {
                                e.onNext(response.message());
                                return;
                            }
                            Log.i(TAG, "getRecommendList onResponse: " + response.body());
                            String data = response.body().toString();
                            String msg = null;
                            try {
                                JSONObject jsonObject = new JSONObject(data);
                                int code = jsonObject.getInt("code");
                                msg = jsonObject.getString("msg");

                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                Gson gson = new Gson();
                                List<HotBean> hotBeans = gson.fromJson(jsonArray.toString(), new TypeToken<List<HotBean>>() {
                                }.getType());
                                Log.i(TAG, "onResponse hotBeans size: " + hotBeans.size());
                                if (resultPage.getPage() == 1) {
                                    llGroup.removeAllViews();
                                }
                                for (int i = 0; i < hotBeans.size(); i++) {
                                    HomeNewsView view = new HomeNewsView(getContext());
                                    view.bindData(hotBeans.get(i));
                                    llGroup.addView(view);
                                }
                                e.onNext("Y");
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                e.onNext(msg);
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            requesting = false;
                            e.onNext(t.getMessage());
                        }
                    });

                }
            });

            Consumer<String> consumer = new Consumer<String>() {
                @Override
                public void accept(String s) throws Exception {
                    Log.i(TAG, "getRecommendList accept: " + s);
                    requesting = false;
//                if (s.equals("Y")) {
//                    adapter.loadData(list);
//                } else {
//                    ToastUtils.showShort(getActivity(), s);
//                }
                }
            };

            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer);
        } catch (Exception ex) {
            requesting = false;
            Log.i(TAG, "getBannerData: " + ex.getMessage());
        }

    }

}
