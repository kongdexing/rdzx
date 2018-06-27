package com.example.ysl.mywps.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.DocumentListBean;
import com.example.ysl.mywps.bean.WpsdetailFinish;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.utils.CommonFun;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.NoDoubleClickListener;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.example.ysl.mywps.utils.ToastUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

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

public class CommitActivity extends BaseActivity {

    private static final String TAG = CommitActivity.class.getSimpleName();
    @BindView(R.id.commit_rl_upload)
    RelativeLayout rlCommit;
    @BindView(R.id.commit_et_opinion)
    EditText etOpinion;
    @BindView(R.id.commit_tv_dept)
    TextView tvDept;
    @BindView(R.id.commit_tv_people)
    TextView tvPeople;
    @BindView(R.id.commit_tv_title)
    TextView tvTtitle;
    @BindView(R.id.commit_tv_opinion)
    TextView tvOpinion;

    @BindView(R.id.commit_tv_desc)
    TextView tvDocDescrip;

    private MyclickListener click = new MyclickListener();
    private String downloadPath = "";
    private String uploadIamgePath = "";
    private String token = "";
    private DocumentListBean documentInfo;
    private String isSigned = "1";
    private String mOpinion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commit_layout);
        showLeftButton(true, "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitleText("审核意见");

        ButterKnife.bind(this);
        downloadPath = getIntent().getStringExtra("wpspath");
        token = SharedPreferenceUtils.loginValue(this, "token");
        documentInfo = getIntent().getExtras().getParcelable("documentInfo");
        mOpinion = getIntent().getStringExtra("opinion");
        rlCommit.setOnClickListener(click);

        afterData();
    }

    public void returnResult(String result) {
        Intent intent = new Intent();
        intent.putExtra("opinion", result);
        setResult(WpsDetailActivity.GET_OPTION, intent);
        finish();
    }

    private void afterData() {
        Log.d(TAG, "afterData: " + documentInfo.toString());
        tvTtitle.setText("公文标题：  " + documentInfo.getTitle());
        tvPeople.setText("呈报人:   " + documentInfo.getNow_nickname());
        tvDept.setText("拟文单位:   " + documentInfo.getDept_name());
        if (CommonUtil.isEmpty(documentInfo.getOpinion())) {
            tvOpinion.setVisibility(View.GONE);
        } else {
            tvOpinion.setVisibility(View.VISIBLE);
            tvOpinion.setText("审核意见：  " + documentInfo.getOpinion());
        }

        if (documentInfo.getDes() != null && !documentInfo.getDes().equals("")) {
            tvDocDescrip.setText("公文描述：  " + documentInfo.getDes());
        } else {
            tvDocDescrip.setVisibility(View.GONE);
        }

        Log.d(TAG, "afterData: " + documentInfo.getStatus());

        String myAccount = SharedPreferenceUtils.loginValue(this, "name");
//        documentInfo.getStatus().equals("6") && documentInfo.getIs_forward().equals("1")
//        if(documentInfo.getStatus().equals("6") && myAccount.equals(documentInfo.getNow_username())){
//           etOpinion.setVisibility(View.VISIBLE);
//            rlCommit.setVisibility(View.VISIBLE);
//        }if(documentInfo.getStatus().equals("6") && documentInfo.getIs_forward().equals("1")){
//            etOpinion.setVisibility(View.VISIBLE);
//            rlCommit.setVisibility(View.VISIBLE);
//        }else {
//            etOpinion.setVisibility(View.INVISIBLE);
//            rlCommit.setVisibility(View.INVISIBLE);
//        }

        etOpinion.setVisibility(View.INVISIBLE);
        rlCommit.setVisibility(View.INVISIBLE);
        if (documentInfo.getStatus().equals("2") || documentInfo.getStatus().equals("3") || documentInfo.getStatus().equals("6")) {
            if (myAccount.equals(documentInfo.getNow_username()) || myAccount.equals(documentInfo.getNow_nickname())) {
                etOpinion.setVisibility(View.VISIBLE);
                rlCommit.setVisibility(View.VISIBLE);
                if (mOpinion != null) {
                    etOpinion.setText(mOpinion);
                }
            }
        } else if (documentInfo.getStatus().equals("5")) {
            getOpinionFromNet();
        }

    }

    private void getOpinionFromNet() {
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                Call<String> call = HttpUtl.getDocumentMd5("User/Oa/get_opinion/", token, documentInfo.getId());
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        if (!response.isSuccessful()) {
                            emitter.onNext(response.message());
                            return;
                        }
                        String msg = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(msg);

                            int code = jsonObject.getInt("code");
                            String message = jsonObject.getString("msg");
                            JSONObject data = jsonObject.getJSONObject("data");
                            String op = data.getString("opinion");
                            if (code == 0) {
                                if (op != null && !op.equals("")) {
                                    mOpinion = op;
                                    emitter.onNext("Y");
                                } else {
                                    emitter.onNext("N");
                                }
                            } else {
                                emitter.onNext(message);
                            }
                        } catch (JSONException e) {
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
                if (s.equals("Y")) {
                    tvOpinion.setText("审核意见:   " + mOpinion);
                    tvOpinion.setVisibility(View.VISIBLE);
                } else {
                    ToastUtils.showShort(CommitActivity.this, s);
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

    /**
     * 签署审核意见
     */
    private void uploadFile(final String opinion, final boolean isUpload) {
        showProgress(isUpload ? "正在处理文件和数据" : "正在处理数据");

        final Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {

                Call<String> call = HttpUtl.uploadWps("User/Oa/back_doc/", documentInfo.getId(), documentInfo.getProce_id(), token, opinion,
                        documentInfo.getDoc_name(), downloadPath, isUpload);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        if (!response.isSuccessful()) {
                            emitter.onNext(response.message());
                            return;
                        }
                        String msg = response.body();
                        Logger.i("commit  " + msg);
                        try {
                            JSONObject jsonObject = new JSONObject(msg);

                            int code = jsonObject.getInt("code");
                            String message = jsonObject.getString("msg");

                            emitter.onNext(message);
                            if (code == 0) {
                                emitter.onNext("Y");
                            } else {
                                emitter.onNext("N");
                            }

                        } catch (JSONException e) {
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
                hideProgress();
                if (s.equals("Y") || s.equals("N")) {
                    if (s.equals("Y")) {
                        EventBus.getDefault().post(new WpsdetailFinish("commit 提交成功"));
                        finish();
                    }
                } else {
                    ToastUtils.showShort(CommitActivity.this, s);
                }
            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    /**
     * 签署失败返回给拟稿人
     */
    private void signCompleted(final String opinion, final String signed, final boolean ifUpload) {

//        if (CommonUtil.isEmpty(uploadIamgePath)) {
//            ToastUtils.showShort(this, "图片保存失败，请重新点击信息按钮");
//            return;
//        }
        showProgress(ifUpload ? "正在处理文件和数据" : "正在处理数据");

        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<String> emitter) throws Exception {

                Call<String> call = HttpUtl.signedCommit("User/Oa/back_signed_doc/", documentInfo.getProce_id(), documentInfo.getId(), opinion, signed,
                        documentInfo.getDoc_name(), downloadPath, token, ifUpload);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (!response.isSuccessful()) {
                            emitter.onNext(response.message());
                            return;
                        }

                        try {
                            String msg = response.body();
                            Logger.i("commit  " + msg);
                            if (CommonUtil.isEmpty(msg)) {
                                return;
                            }
                            Logger.i("commitSign  " + msg);
                            JSONObject jsonObject = new JSONObject(msg);
                            int code = jsonObject.getInt("code");
                            String message = jsonObject.getString("msg");

                            emitter.onNext(message);
                            if (code == 0) {
                                emitter.onNext("Y");
                            } else {
                                emitter.onNext("N");
                            }
                        } catch (JSONException e) {
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

        Consumer<String> observer = new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                hideProgress();
                if (s.equals("Y") || s.equals("N")) {
                    if (s.equals("Y")) {
                        EventBus.getDefault().post(new WpsdetailFinish("commit 提交成功"));
                        finish();
                    }
                } else {
                    ToastUtils.showLong(getApplicationContext(), s);
                }
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 反馈意见
     */
    private void feedBack(final String opinion) {
        showProgress();
        final Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {

                Call<String> call = HttpUtl.feedBack("User/Oa/feedback/", documentInfo.getId(), opinion, token);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        if (!response.isSuccessful()) {
                            emitter.onNext(response.message());
                            return;
                        }
                        String msg = response.body();
                        Logger.i("commit  " + msg);
                        try {
                            if (msg == null) {
                                emitter.onNext("N");
                                return;
                            }
                            JSONObject jsonObject = new JSONObject(msg);

                            int code = jsonObject.getInt("code");
                            String message = jsonObject.getString("msg");

                            emitter.onNext(message);
                            if (code == 0) {
                                emitter.onNext("Y");
                            } else {
                                emitter.onNext("N");
                            }

                        } catch (JSONException e) {
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
                hideProgress();
                if (s.equals("Y") || s.equals("N")) {
                    if (s.equals("Y")) {
                        EventBus.getDefault().post(new WpsdetailFinish("commit 提交成功"));
                        finish();
                    }
                } else {
                    ToastUtils.showShort(CommitActivity.this, s);
                }

            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    private void checkRemoteMd5(final String opinion) {
        final String wpsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + "/" + documentInfo.getDoc_name();
        File file = new File(wpsPath);
        final String md5Value = CommonFun.getMD5Three(file);

        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                Call<String> call = HttpUtl.getDocumentMd5("User/Oa/get_doc_md5/", token, documentInfo.getId());
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (!response.isSuccessful()) {
                            emitter.onNext(response.message());
                            return;
                        }
                        String msg = response.body();
                        Logger.i("commit  " + msg + "   " + md5Value);
                        try {
                            JSONObject jsonObject = new JSONObject(msg);

                            int code = jsonObject.getInt("code");
                            String message = jsonObject.getString("msg");
                            JSONObject data = jsonObject.getJSONObject("data");
                            String remoteMd5 = data.getString("md5");
                            if (code == 0) {
                                if (remoteMd5.equals(md5Value)) {
                                    emitter.onNext("Y");
                                } else {
                                    emitter.onNext("N");
                                }
                            } else {
                                emitter.onNext(message);
                            }
                        } catch (JSONException e) {
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
                if (s.equals("Y")) {
                    // 2审核人审核阶段（点发送提交返回给拟稿人）
                    // 3领导签署阶段（领导没有签名 点发送提交返回给拟稿人）
                    if (documentInfo.getStatus().equals("2")) {
                        uploadFile(opinion, false);
                    } else if (documentInfo.getStatus().equals("3")) {
                        signCompleted(opinion, "1", false);
                    }
                } else if (s.equals("N")) {
                    if (documentInfo.getStatus().equals("2")) {
                        uploadFile(opinion, true);
                    } else if (documentInfo.getStatus().equals("3")) {
                        signCompleted(opinion, "1", true);
                    }
                } else {
                    ToastUtils.showShort(CommitActivity.this, s);
                }
            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }


    private class MyclickListener extends NoDoubleClickListener {
        @Override
        public void click(View v) {
            switch (v.getId()) {
                case R.id.commit_rl_upload:
                    String opinion = etOpinion.getText().toString();

                    if (CommonUtil.isEmpty(opinion)) {
                        ToastUtils.showShort(CommitActivity.this, "请输入意见");
                        return;
                    }
//            //                拟稿1-》审核2-》审核通过5-》签署3（不同意）-》审核通过5-》4转发状态-》6转发给多人，等待反馈状态
//1.2.3.5 校验md5值
                    if (documentInfo.getStatus().equals("2")) {
//                        uploadFile(opinion);
                        checkRemoteMd5(opinion);
                    } else if (documentInfo.getStatus().equals("3")) {
//                        signCompleted(opinion, isSigned);
                        checkRemoteMd5(opinion);
                    } else if (documentInfo.getStatus().equals("5")) {
//                        uploadFile(opinion);
                    } else if (documentInfo.getStatus().equals("6")) {
                        feedBack(opinion);
                    } else {
                        ToastUtils.showShort(CommitActivity.this, "该文档还在拟稿状态");
                    }
                    break;


            }

        }
    }
}
