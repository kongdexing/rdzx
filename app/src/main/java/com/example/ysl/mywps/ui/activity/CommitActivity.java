package com.example.ysl.mywps.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.DocumentListBean;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.NoDoubleClickListener;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.example.ysl.mywps.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

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
    private String token = "";
    private DocumentListBean documentInfo;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class MyclickListener extends NoDoubleClickListener {
        @Override
        public void click(View v) {
            switch (v.getId()) {
                case R.id.commit_rl_upload:
                    String opinion = etOpinion.getText().toString().trim();
                    if (CommonUtil.isEmpty(opinion)) {
                        ToastUtils.showShort(CommitActivity.this, "请输入意见");
                        return;
                    }
                    returnResult(opinion);
                    break;
            }

        }
    }
}
