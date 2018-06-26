package com.example.ysl.mywps.ui.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.ContactBean;
import com.example.ysl.mywps.bean.Item;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.ui.adapter.ContactPinnedAdapter;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.PingYinUtils;
import com.example.ysl.mywps.utils.SharedPreferenceUtils;
import com.example.ysl.mywps.utils.ToastUtils;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.halfbit.pinnedsection.PinnedSectionListView;
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

public class ContactFragment extends BaseFragment {

    @BindView(R.id.list)
    PinnedSectionListView listView;

    @BindView(R.id.contact_et_search)
    EditText etSearch;

    private ContactPinnedAdapter adapter;

    private ArrayList<ContactBean> list = new ArrayList<>();
    private ArrayList<ContactBean> searchList = new ArrayList<>();

    @Override
    public void initData() {
        adapter = new ContactPinnedAdapter(getContext(), R.layout.layout_contact_group);
        netWork();
        checkPermission();
    }

    /**
     * 获取通讯录联系人
     */
    private void netWork() {
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) {

                String token = SharedPreferenceUtils.loginValue(getActivity(), "token");
                Call<String> call = HttpUtl.contact("User/User/contacts/", token);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (!response.isSuccessful()) {
                            e.onNext(response.message());
                            return;
                        }
                        Logger.i("response " + response.body());
                        String data = response.body().toString();
                        String msg = null;
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            int code = jsonObject.getInt("code");
                            msg = jsonObject.getString("msg");

                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            Gson gson = new Gson();
                            int sectionPosition = 0, listPosition = 0;
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
                        Logger.i("通讯录  " + t.getMessage());
                        e.onNext(t.getMessage());
                    }
                });

            }
        });

        Consumer<String> consumer = new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                if (s.equals("Y")) {
                    loadingContact("");
                } else {
                    ToastUtils.showShort(getActivity(), s);
                }
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_contact_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void afterView(View view) {
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

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

                if (CommonUtil.isEmpty(s.toString())) {
                    final String value = etSearch.getText().toString();
                    loadingContact(value.trim());
                }

            }
        });

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
        if (getActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
                Toast.makeText(getActivity(), "请开启电话权限", Toast.LENGTH_SHORT).show();
            }
        } else {
//            finish();
        }
    }

    @Override
    public void setKindFlag(int kindFlag) {

    }
}
