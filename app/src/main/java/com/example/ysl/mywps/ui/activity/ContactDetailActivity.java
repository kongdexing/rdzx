package com.example.ysl.mywps.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.BannerBean;
import com.example.ysl.mywps.bean.ContactBean;
import com.example.ysl.mywps.bean.ContactDetailBean;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.ui.view.CircularImageView;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
 * Created by Administrator on 2018/3/18 0018.
 */

public class ContactDetailActivity extends BaseActivity {

    @BindView(R.id.imgHead)
    CircularImageView imgHead;
    @BindView(R.id.txtUserName)
    TextView txtUserName;
    @BindView(R.id.txtPhone)
    TextView txtPhone;
    @BindView(R.id.txtEmail)
    TextView txtEmail;
    @BindView(R.id.txtDept)
    TextView txtDept;
    @BindView(R.id.txtJieBie)
    TextView txtJieBie;

    @BindView(R.id.txtPoliticsStatus)
    TextView txtPoliticsStatus;

    @BindView(R.id.txtDuty)
    TextView txtDuty;

    @BindView(R.id.txtUID)
    TextView txtUID;

    ContactDetailBean contactBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_detail_layout);
        ButterKnife.bind(this);
        showLeftButton(true, "", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
//        setTitleText("通讯录详情");

        ContactBean contactBean = getIntent().getExtras().getParcelable("contact");
        getContactDetail(contactBean.getUid());

    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {
        if (contactBean == null) {
            return;
        }

        ImageLoader.getInstance().displayImage(contactBean.getAvatar(),
                new ImageViewAware(imgHead), CommonUtil.getDefaultUserImageLoaderOption());

        txtUserName.setText(contactBean.getRealname());
        txtPhone.setText(contactBean.getMobile());
        txtEmail.setText(contactBean.getEmail());
        txtDept.setText(contactBean.getDept_name());
        txtJieBie.setText(contactBean.getJiebie());
        txtPoliticsStatus.setText(contactBean.getPolitics_stauts());
        txtDuty.setText(contactBean.getDuty());
//        tvTel.setText(contactBean.getMobile());
    }

    @OnClick(R.id.contact_detail_bt_tel)
    public void onViewClicked() {
        try {
            if (CommonUtil.isNotEmpty(contactBean.getMobile())) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contactBean.getMobile()));
                startActivity(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
            checkPermission();
        }
    }

    /**
     * 获取联系人详情
     */
    private void getContactDetail(final String uid) {
        try {
            Log.i(TAG, "getContactDetail uid: " + uid);
            Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(final ObservableEmitter<String> e) {

                    String token = SharedPreferenceUtils.loginValue(ContactDetailActivity.this, "token");
                    Call<String> call = HttpUtl.contactDetail("User/User/uinfo/", token, uid);

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

                                JSONObject jsonContact = jsonObject.getJSONObject("data");
                                Gson gson = new Gson();

                                contactBean = gson.fromJson(jsonContact.toString(), new TypeToken<ContactDetailBean>() {
                                }.getType());
                                initData();
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
            Log.i(TAG, "getContactDetail: " + ex.getMessage());
        }
    }

    /**
     * 检查电话，如果没有就请求
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
//       动态的请求权限
//        ActivityCompat.requestPermissions(getActivity(), new String[]{
//                Manifest.permission.CALL_PHONE
//        }, 11);
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 11);
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
                checkPermission();
                Log.i("CMCC", "权限被拒绝");
                Toast.makeText(this, "请开启电话权限", Toast.LENGTH_SHORT).show();
            }
        } else {


//            finish();
        }
    }

}
