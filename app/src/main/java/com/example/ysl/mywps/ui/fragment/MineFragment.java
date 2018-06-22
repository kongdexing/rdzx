package com.example.ysl.mywps.ui.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.ui.activity.LoginActivity;
import com.example.ysl.mywps.ui.activity.MainActivity;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.DataCleanManager;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.example.ysl.mywps.utils.SysytemSetting;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/12/23 0023.
 */

public class MineFragment extends BaseFragment {

    @BindView(R.id.imgHead)
    ImageView imgHead;

    @BindView(R.id.mine_rl_loginout)
    RelativeLayout rlLoginOut;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtCache)
    TextView txtCache;

    @Override
    public void initData() {

    }

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_mine_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void afterView(View view) {
        String name = SharedPreferenceUtils.loginValue(getActivity(), SysytemSetting.REAL_NAME);
        txtName.setText(name);

        String avatar = SharedPreferenceUtils.loginValue(getContext(), SysytemSetting.AVATAR);
        ImageLoader.getInstance().displayImage(avatar, new ImageViewAware(imgHead), CommonUtil.getDefaultUserImageLoaderOption());

        calculateCache();

    }

    private void calculateCache() {
        //获得应用内部缓存(/data/data/com.example.androidclearcache/cache)
        File file = new File(this.getContext().getCacheDir().getPath());
        try {
            txtCache.setText(DataCleanManager.getCacheSize(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setKindFlag(int kindFlag) {

    }

    @OnClick({R.id.rlClearCache, R.id.mine_rl_loginout, R.id.rlChangePwd, R.id.rlFeedback})
    void viewClick(View view) {
        switch (view.getId()) {
            case R.id.mine_rl_loginout:
                SharedPreferenceUtils.loginSave(getActivity(), "token", "");
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
                break;
            case R.id.rlClearCache:
                ((MainActivity) this.getContext()).showProgress("清理中...");
                try {
                    Thread.sleep(1000);
//                    DataCleanManager.cleanInternalCache(getContext());
                    DataCleanManager.deleteFolderFile(this.getContext().getCacheDir().getPath(), false);
                    calculateCache();
                    ((MainActivity) this.getContext()).hideProgress();
                } catch (Exception e) {
                    e.printStackTrace();
                    ((MainActivity) this.getContext()).hideProgress();
                }
                break;
            case R.id.rlChangePwd:

                break;
            case R.id.rlFeedback:

                break;

        }
    }

}
