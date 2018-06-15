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
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.ui.activity.BaseWebActivity;
import com.example.ysl.mywps.ui.activity.MembersActivity;
import com.example.ysl.mywps.ui.activity.NewOAActivity;
import com.example.ysl.mywps.ui.activity.ProposalActivity;
import com.example.ysl.mywps.ui.activity.WebviewActivity;
import com.example.ysl.mywps.ui.view.HomeNewsView;
import com.example.ysl.mywps.ui.view.autoviewpager.GlideImageLoader;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.viewpagerindicator.CirclePageIndicator;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2017/12/23 0023.
 */

public class NewWorkFragment extends BaseFragment {

    Unbinder unbinder;
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

    List<String> advertList = new ArrayList<>();
    private String token = "";

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

        loadData();
    }

    private void loadData() {
        final List<String> listBannerImages = new ArrayList<>();
        final List<String> listTitles = new ArrayList<>();

        listBannerImages.add("http://h.hiphotos.baidu.com/image/pic/item/d043ad4bd11373f0f5413134a80f4bfbfbed041a.jpg");
        listBannerImages.add("http://d.hiphotos.baidu.com/image/pic/item/54fbb2fb43166d2234a291024a2309f79152d24b.jpg");
        listBannerImages.add("http://a.hiphotos.baidu.com/image/pic/item/55e736d12f2eb9389b3e3860d9628535e4dd6fd4.jpg");
        listBannerImages.add("http://f.hiphotos.baidu.com/image/pic/item/b812c8fcc3cec3fdf10120e3da88d43f8794276c.jpg");
        listBannerImages.add("http://c.hiphotos.baidu.com/image/pic/item/f11f3a292df5e0fe43250e97506034a85edf7263.jpg");

        listTitles.add("balabala0");
        listTitles.add("balabala1");
        listTitles.add("balabala2");
        listTitles.add("balabala3");
        listTitles.add("balabala4");
        tipTitle.setText(listTitles.get(0));

        if (topBanner == null) {
            return;
        }
        topBanner.update(listBannerImages);
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

        llGroup.removeAllViews();
        for (int i = 0; i < 5; i++) {
            HomeNewsView view = new HomeNewsView(this.getContext());
            llGroup.addView(view);
        }
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
                intent = new Intent(getActivity(), ProposalActivity.class);
                break;
            case R.id.llItem3://社情民意
                intent = new Intent(getActivity(), WebviewActivity.class);
                intent.putExtra(BaseWebActivity.WEB_URL, HttpUtl.HTTP_WEB_URL + "sqmy/");
                break;
            case R.id.llItem4://委员之家
                intent = new Intent(getActivity(), MembersActivity.class);
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


}
