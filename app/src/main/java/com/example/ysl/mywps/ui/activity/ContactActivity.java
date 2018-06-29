package com.example.ysl.mywps.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.ContactBean;
import com.example.ysl.mywps.bean.DocumentListBean;
import com.example.ysl.mywps.bean.Item;
import com.example.ysl.mywps.bean.WpsdetailFinish;
import com.example.ysl.mywps.interfaces.PassString;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.ui.adapter.ContactPinnedAdapter;
import com.example.ysl.mywps.utils.CommonFun;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.NoDoubleClickListener;
import com.example.ysl.mywps.utils.PingYinUtils;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.example.ysl.mywps.utils.ToastUtils;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.halfbit.pinnedsection.PinnedSectionListView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ysl on 2018/1/16.
 */

public class ContactActivity extends BaseActivity {

    @BindView(R.id.list)
    PinnedSectionListView listView;
    @BindView(R.id.contact_et_search)
    EditText etSearch;
    @BindView(R.id.contact_rl_bottom)
    RelativeLayout rlBottom;
    @BindView(R.id.contact_bt_confirm)
    ButtonRectangle btConfirm;
    @BindView(R.id.contact_bt_cancel)
    ButtonRectangle btCancel;
    @BindView(R.id.contact_cb_all)
    CheckBox cbAll;
    @BindView(R.id.contact_tv_all)
    TextView tvAll;

    //    private ContactMyAdapter adapter;
    private ContactPinnedAdapter adapter;

    private ArrayList<ContactBean> list = new ArrayList<>();
    private ArrayList<ContactBean> searchList = new ArrayList<>();
    private String token = "";
    private String docPath = "";
    private DocumentListBean documentInfo;
    private MyclickListener click = new MyclickListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_layout);
        ButterKnife.bind(this);

        docPath = getIntent().getStringExtra("path");
        documentInfo = getIntent().getExtras().getParcelable("documentInfo");

        initPageView();

        token = SharedPreferenceUtils.loginValue(this, "token");
        showLeftButton(true, "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitleText("通讯录");
    }

    private void initPageView() {
        tvAll.setOnClickListener(click);
        btConfirm.setOnClickListener(click);
        btCancel.setOnClickListener(click);

        adapter = new ContactPinnedAdapter(this, R.layout.layout_contact_group);
        listView.setAdapter(adapter);

        adapter.setJumpType(ContactPinnedAdapter.Single, new ContactPinnedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ContactBean contactBean) {
                //拟稿1-》审核2-》审核通过5-》签署3（不同意）-》审核通过5
                Log.i(TAG, "提交人  " + contactBean.getUsername());
                checkRemoteMd5(contactBean);
            }
        });

        if (documentInfo.getStatus().equals("4")) {
            //可多选
            adapter.setJumpType(ContactPinnedAdapter.Multiple, new PassString() {
                @Override
                public void setString(String... datas) {
                    if (datas.length > 0) {
                        docForward(datas[0]);
                    }
                }
            });
            rlBottom.setVisibility(View.VISIBLE);
            cbAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (adapter == null) return;
                    adapter.selectAll(isChecked);
                }
            });
        }

        /*start checkbox 全选测试*/
        /*end*/
        etSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == keyEvent.KEYCODE_ENTER) {
                    // do some your things
                    final String value = etSearch.getText().toString();
                    loadingContact(value.trim());
                    CommonUtil.hideSoftInput();
                }
                return false;
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!CommonUtil.isEmpty(s.toString())) {
                    final String value = etSearch.getText().toString();
                    loadingContact(value.trim());
                }
            }
        });
    }

    private void checkRemoteMd5(final ContactBean contactBean) {
        if (docPath == null || docPath.equals("")) {
            postNetNoFile(contactBean,false);
            return;
        }
        File file = new File(docPath);
        if (!file.exists()) {
            postNetNoFile(contactBean,false);
            return;
        }

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
                    postNetNoFile(contactBean,false);
                } else if (s.equals("N")) {
                    postNetNoFile(contactBean,true);
                } else {
                    ToastUtils.showShort(ContactActivity.this, s);
                }
            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    private void postNetNoFile(ContactBean contactBean,boolean isUpload) {
        //文件未下载，不上传文件
        if (documentInfo.getStatus().equals("1")) {
            //1拟稿阶段（点发送调起通讯录选人提交）
            commitAudit(contactBean.getUid(), isUpload);
        } else if (documentInfo.getStatus().equals("5")) {
            //5拟稿人审核阶段 （点发送调起通讯录选人提交）
            commitSign(contactBean.getUid(), isUpload);
        }
    }

    /**
     * 拟稿1
     * 提交审核
     */
    private void commitAudit(final String uid, final boolean ifUpload) {
        if (docPath == null || docPath.equals("")) {
            ToastUtils.showShort(this, "文件不存在");
            return;
        }
        showProgress(ifUpload ? "正在处理文件和数据" : "正在处理数据");
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) {

                Call<String> call = HttpUtl.commitAudit("User/Oa/submit_review/", documentInfo.getId(), uid, token, documentInfo.getDoc_name(), docPath, ifUpload);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            if (!response.isSuccessful()) {
                                emitter.onNext(response.message());
                                return;
                            }
                            Log.i(TAG, "contact  " + response.body());
                            JSONObject jsonObject = new JSONObject(response.body());

                            int code = jsonObject.getInt("code");
                            String msg = jsonObject.getString("msg");

                            emitter.onNext(msg);
                            if (code == 0) emitter.onNext("Y");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        emitter.onNext(t.getMessage());
                        emitter.onNext("N");
                    }
                });


            }
        }); //18511234650
        Consumer<String> observer = new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                hideProgress();
                if (s.equals("Y") || s.equals("N")) {

                    if (s.equals("Y")) {
                        EventBus.getDefault().post(new WpsdetailFinish("通讯录提交成功"));
                        finish();
                    }

                } else {
                    ToastUtils.showShort(ContactActivity.this, s);
                }

            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 审核通过5
     * 提交文件领导签署（拟稿人审核阶段）
     */
    private void commitSign(final String uid, final boolean ifUpload) {
        if (docPath == null || docPath.equals("")) {
            ToastUtils.showShort(this, "文件不存在");
            return;
        }
        showProgress(ifUpload ? "正在处理文件和数据" : "正在处理数据");
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) {

                Call<String> call = HttpUtl.commitSign("User/Oa/doc_sign/", documentInfo.getId(), token, documentInfo.getDoc_name(), docPath, uid, ifUpload);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (!response.isSuccessful()) {
                            emitter.onNext(response.message());
                            return;
                        }

                        try {
                            Log.i(TAG, "contact  " + response.body());
                            JSONObject jsonObject = new JSONObject(response.body());

                            int code = jsonObject.getInt("code");
                            String msg = jsonObject.getString("msg");

                            emitter.onNext(msg);
                            if (code == 0) emitter.onNext("Y");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        emitter.onNext(t.getMessage());
                        emitter.onNext("N");
                    }
                });

            }
        }); //18511234650
        Consumer<String> observer = new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                hideProgress();
                if (s.equals("Y") || s.equals("N")) {
                    if (s.equals("Y")) {
                        EventBus.getDefault().post(new WpsdetailFinish("通讯录提交成功"));
                        finish();
                    }
                } else {
                    ToastUtils.showShort(ContactActivity.this, s);
                }
            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }

    /**
     * 转发进入反馈流程
     */
    private void docForward(final String uids) {
        showProgress();
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<String> emitter) {

                Call<String> call = HttpUtl.docForward("User/Oa/doc_forward/", documentInfo.getId(), uids, token);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        if (!response.isSuccessful()) {
                            emitter.onNext(response.message());
                            return;
                        }

                        String data = response.body();
                        Log.i(TAG, "反馈流程  " + data);
                        if (CommonUtil.isEmpty(data)) {
                            emitter.onNext("N");
                            emitter.onComplete();
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            int code = jsonObject.getInt("code");
                            String msg = jsonObject.getString("msg");

                            emitter.onNext(msg);
                            if (code == 0) emitter.onNext("Y");
                            else emitter.onNext("N");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        emitter.onComplete();


                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                        emitter.onNext(t.getMessage());
                        emitter.onNext("N");
                        emitter.onComplete();

                    }
                });

            }
        });

        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull String s) {
                if (s.equals("Y") || s.equals("N")) {
                    if (s.equals("Y")) EventBus.getDefault().post(new WpsdetailFinish("转发成功"));
                } else ToastUtils.showShort(getApplicationContext(), s);


            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                hideProgress();
            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    @Override
    public void initView() {
        Log.i(TAG, "initView: ");
    }

    @Override
    public void initData() {
        Log.i(TAG, "initData: ");
        netWork();
    }

    /**
     * 获取通讯录联系人
     */
    private void netWork() {
        Log.i(TAG, "netWork: ");
        showProgress();
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) {

                String token = SharedPreferenceUtils.loginValue(ContactActivity.this, "token");
                Call<String> call = HttpUtl.contact("User/User/contacts/", token);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (!response.isSuccessful()) {
                            e.onNext(response.message());
                            return;
                        }
                        Log.i(TAG, "response " + response.body());
                        String data = response.body().toString();
                        String msg = null;
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            int code = jsonObject.getInt("code");
                            msg = jsonObject.getString("msg");

                            JSONArray jsonArray = jsonObject.getJSONArray("data");

                            Gson gson = new Gson();
                            for (int i = 0; i < jsonArray.length(); ++i) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                String title = object.getString("title");
                                JSONArray jsonArray1 = object.getJSONArray("contact");

                                for (int j = 0; j < jsonArray1.length(); ++j) {
                                    JSONObject childObject = jsonArray1.getJSONObject(j);
                                    ContactBean bean = gson.fromJson(childObject.toString(), ContactBean.class);
                                    bean.setCapital(PingYinUtils.getPinYinHeadChar(bean.getUsername()));
                                    bean.setTitle(title);
                                    list.add(bean);
                                }
                            }
                            e.onNext("Y");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                            e.onNext(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.i(TAG, "通讯录  " + t.getMessage());
                        e.onNext(t.getMessage());
                    }
                });

            }
        });
        Consumer<String> consumer = new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                hideProgress();
                if (s.equals("Y")) {
                    loadingContact("");
//                    boolean shouldHide = false;
//                    if (documentInfo.getStatus().equals("4")) {
//                        shouldHide = true;
//                    }
//                    adapter = new ContactPinnedAdapter(getContext(), R.layout.layout_contact_group);
////                    adapter = new ContactMyAdapter(list, ContactActivity.this, shouldHide, ContactActivity.this);
//                    listView.setAdapter(adapter);
                } else {
                    ToastUtils.showShort(ContactActivity.this, s);
                }
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);

    }

    private void loadingContact(String target) {
        List<String> groups = new ArrayList<>();
        int sectionPosition = 0, listPosition = 0;

        searchList.clear();
        adapter.clear();

        if (target == null || target.isEmpty()) {
            searchList.addAll(list);
        } else {
            for (int i = 0; i < list.size(); i++) {
                ContactBean bean = list.get(i);
                if (bean.getMobile().contains(target) ||
                        bean.getRealname().contains(target)) {
                    searchList.add(bean);
                }
            }
        }

        adapter.loadData(searchList);

        for (int i = 0; i < searchList.size(); i++) {
            ContactBean contactBean = searchList.get(i);

            if (!groups.contains(contactBean.getTitle())) {
                groups.add(contactBean.getTitle());

                //添加组头
                Item section = new Item(Item.SECTION, contactBean.getTitle());
                section.sectionPosition = sectionPosition;
                section.listPosition = listPosition++;
                adapter.add(section);
                //添加子分组
                Item child = new Item(Item.ITEM, contactBean);
                child.sectionPosition = sectionPosition;
                child.listPosition = listPosition++;
                adapter.add(child);
            } else {
                //添加子分组
                Item child = new Item(Item.ITEM, contactBean);
                child.sectionPosition = sectionPosition;
                child.listPosition = listPosition++;
                adapter.add(child);
            }

        }

    }

    private class MyclickListener extends NoDoubleClickListener {
        @Override
        public void click(View v) {
            switch (v.getId()) {
                case R.id.contact_itv_search:

                    break;
                case R.id.contact_bt_confirm:
                    if (adapter != null) {
                        adapter.docFroward();
                    }
                    break;
                case R.id.contact_bt_cancel:
                    cbAll.setChecked(false);
                    break;
                case R.id.contact_tv_all:
                    cbAll.setChecked(!cbAll.isChecked());
                    break;
            }
        }
    }
}
