package com.example.ysl.mywps.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.MessageBean;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.receiver.MyJipushReceiver;
import com.example.ysl.mywps.ui.fragment.ContactFragment;
import com.example.ysl.mywps.ui.fragment.MessageFragment;
import com.example.ysl.mywps.ui.fragment.MineFragment;
import com.example.ysl.mywps.ui.fragment.NewWorkFragment;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.example.ysl.mywps.utils.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    //    TextView tvText;
    private static final int BACK_CODE = 1;
    private static final String TAG = "aaa";
    @BindView(R.id.main_tv_message)
    TextView tvMessage;
    @BindView(R.id.main_ib_message)
    ImageButton ibMessage;
    @BindView(R.id.main_ll_message)
    LinearLayout llMessage;
    @BindView(R.id.badgeView)
    View badgeView;

    @BindView(R.id.main_tv_work)
    TextView tvWork;
    @BindView(R.id.main_ib_work)
    ImageButton ibWork;
    @BindView(R.id.main_ll_work)
    LinearLayout llWork;

    @BindView(R.id.main_tv_contact)
    TextView tvConact;
    @BindView(R.id.main_ib_contact)
    ImageButton ibContact;
    @BindView(R.id.main_ll_contact)
    LinearLayout llContact;

    @BindView(R.id.main_tv_mine)
    TextView tvMine;
    @BindView(R.id.main_ib_mine)
    ImageButton ibMine;
    @BindView(R.id.main_ll_mine)
    LinearLayout llMine;

//    @BindView(R.id.main_vp_container)
//    ViewPager viewPager;


    private Fragment messageFragment, contactFragment, workFragment, mineFragment;
    private ColorStateList colorNomal, colorSelect;
//    private MyclickListener click;

    private Fragment currentFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    float x1, x2, y1, y2;
    int currentIndex = 0;
//    private PagerAdapter pagerAdapter;


    //    公文流转  内部公文 代办事项   流程 调专业版
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

//        click = new MyclickListener();

        llMessage.setOnClickListener(this);
        llWork.setOnClickListener(this);
        llMine.setOnClickListener(this);
        llContact.setOnClickListener(this);

        colorNomal = getResources().getColorStateList(R.color.bottom_normal);
        colorSelect = getResources().getColorStateList(R.color.bottom_selected);
        fragmentManager = getSupportFragmentManager();
        showMessage(0);

        IntentFilter filter = new IntentFilter(MyJipushReceiver.ACTION_RECEIVE_MESSAGE);
        filter.setPriority(2000);
        registerReceiver(pushReceiver, filter);
    }

    /**
     * 当token过期后跳转到登陆界面
     */
    private void jumpToLogin() {
        SharedPreferenceUtils.loginSave(this, "token", "");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * 获取文件类目
     */
    private void saveFileTypes(final String token) {
        final Thread fileTypeThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Call<String> call = HttpUtl.getFileType("User/Share/file_type/", token);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        String data = response.body();
                        Logger.i("fileType  " + data);
                        if (CommonUtil.isEmpty(data))
                            return;
                        try {
                            JSONObject object = new JSONObject(data);
                            int code = object.getInt("code");
                            String msg = object.getString("msg");

                            if (CommonUtil.isNotEmpty(msg) && msg.contains("登陆信息有误") || code == 1) {
                                jumpToLogin();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Logger.i("fileType   " + t.getMessage());
                    }
                });
            }
        });
        fileTypeThread.setDaemon(true);
        fileTypeThread.start();
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    public void initView() {
    }

    @Override
    public void initData() {
        final String token = SharedPreferenceUtils.loginValue(this, "token");
        saveFileTypes(token);
    }

    @Override
    protected void onResume() {
        super.onResume();
        writePermission();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private int currentMyIndex = 1;

    //    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setTextBack(int index) {
        ibWork.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_work_normal));
        ibMessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_message_normal));
        ibContact.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_contact_normal));
        ibMine.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_mine_normal));

        tvWork.setTextColor(colorNomal);
        tvMessage.setTextColor(colorNomal);
        tvConact.setTextColor(colorNomal);
        tvMine.setTextColor(colorNomal);

        showTitle(true);
        switch (index) {
            case 0:
                setTitleText("首页");
                showTitle(false);
                tvWork.setTextColor(colorSelect);
                ibWork.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_work_selected));
                currentMyIndex = 0;
                break;
            case 1:
                tvMessage.setTextColor(colorSelect);
                setTitleText("消息");
                ibMessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_message_selected));
                currentMyIndex = 1;
                break;
            case 2:
                setTitleText("通讯录");
                tvConact.setTextColor(colorSelect);
                ibContact.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_contact_selected));
                currentMyIndex = 2;
                break;
            case 3:
                setTitleText("我的");
                tvMine.setTextColor(colorSelect);
                ibMine.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_mine_selected));
                currentMyIndex = 3;
                break;
        }
    }

    public void showMessage(int index) {
        fragmentTransaction = fragmentManager.beginTransaction();
        if (currentFragment != null && index != currentMyIndex)
            fragmentTransaction.hide(currentFragment);
        switch (index) {
            case 0:
                currentIndex = 0;
                if (workFragment == null) {
                    workFragment = new NewWorkFragment();
                    fragmentTransaction.add(R.id.main_rl_container, workFragment);
                } else {
                    if (workFragment.isHidden()) fragmentTransaction.show(workFragment);
                }
                currentFragment = workFragment;
                setTextBack(0);
                break;
            case 1:
                currentIndex = 1;
                if (messageFragment == null) {
                    messageFragment = new MessageFragment();
                    fragmentTransaction.add(R.id.main_rl_container, messageFragment);
                } else {
                    fragmentTransaction.show(messageFragment);
                    if (badgeView.getVisibility() == View.VISIBLE) {
                        ((MessageFragment) messageFragment).getFirstPageData();
                    }
                }

                badgeView.setVisibility(View.GONE);
                setTextBack(1);
                currentFragment = messageFragment;
                break;
            case 2:
                currentIndex = 2;
                if (contactFragment == null) {
                    contactFragment = new ContactFragment();
                    fragmentTransaction.add(R.id.main_rl_container, contactFragment);
                } else {
                    if (contactFragment.isHidden()) fragmentTransaction.show(contactFragment);
                }
                currentFragment = contactFragment;
                setTextBack(2);
                break;
            case 3:
                currentIndex = 3;
                if (mineFragment == null) {
                    mineFragment = new MineFragment();
                    fragmentTransaction.add(R.id.main_rl_container, mineFragment);
                } else {
                    if (mineFragment.isHidden()) fragmentTransaction.show(mineFragment);
                }
                currentFragment = mineFragment;
                setTextBack(3);
                break;

        }
        fragmentTransaction.setCustomAnimations(R.anim.fragment_out, R.anim.fragment_back, R.anim.fragment_out, R.anim.fragment_back);
        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.main_ll_message || id == R.id.main_ib_message) {
            showMessage(1);
        } else if (id == R.id.main_ll_work || id == R.id.main_ib_work) {
            showMessage(0);
        } else if (id == R.id.main_ll_contact || id == R.id.main_ib_contact) {
            showMessage(2);
        } else if (id == R.id.main_ll_mine || id == R.id.main_ib_mine) {
            showMessage(3);
        }
    }


    /**
     * 检查存储权限，如果没有就请求
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void writePermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 11) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("CMCC", "权限被允许");
            } else {
                Log.i("CMCC", "权限被拒绝");
                ToastUtils.showShort(this, "请开启存储权限");
                writePermission();
            }
        } else {
            writePermission();
//            finish();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(pushReceiver);
        } catch (Exception ex) {

        }
        super.onDestroy();
    }

    BroadcastReceiver pushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyJipushReceiver.ACTION_RECEIVE_MESSAGE.equals(action)) {
                try {
                    String params = intent.getStringExtra("param");
                    Gson gson = new Gson();
                    MessageBean messageBean = gson.fromJson(params, new TypeToken<MessageBean>() {
                    }.getType());
                    //表明是推送消息
                    badgeView.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    Log.i(TAG, "onReceive error: " + ex.getMessage());
                }

            }
        }
    };
}
