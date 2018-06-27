package com.example.ysl.mywps.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.ContactBean;
import com.example.ysl.mywps.bean.Item;
import com.example.ysl.mywps.interfaces.PassString;
import com.example.ysl.mywps.ui.activity.ContactDetailActivity;
import com.example.ysl.mywps.ui.view.CircularImageView;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.ToastUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.halfbit.pinnedsection.PinnedSectionListView;

/**
 * Created by dexing on 2018-6-10 0010.
 */

public class ContactPinnedAdapter extends ArrayAdapter<Item> implements PinnedSectionListView.PinnedSectionListAdapter {

    private String TAG = ContactPinnedAdapter.class.getSimpleName();
    private ArrayList<ContactBean> list = new ArrayList<>();
    private LinkedHashMap<Integer, ContactBean> selected = new LinkedHashMap<>();
    //点击联系人后的跳转类型（单选|多选|跳转至详情）
    private String type = Detail;
    public static String Single = "singleSelection";
    public static String Multiple = "multipleSelection";
    public static String Detail = "detail";
    private OnItemClickListener onItemClickListener;
    private PassString passString;

    public interface OnItemClickListener {
        void onItemClick(ContactBean contactBean);
    }

    public ContactPinnedAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public void loadData(List<ContactBean> contactBeans) {
        list.clear();
        list.addAll(contactBeans);
    }

    public void setJumpType(String jumpType, OnItemClickListener listener) {
        type = jumpType;
        onItemClickListener = listener;
    }

    public void setJumpType(String jumpType, PassString passsString) {
        type = jumpType;
        passString = passsString;
    }

    public void selectAll(boolean isSelect) {
        Log.i(TAG, "selectAll: " + isSelect);
        if (isSelect) {
            for (int i = 0; i < list.size(); ++i) {
                selected.put(i, list.get(i));
            }
        } else {
            selected.clear();
        }

        notifyDataSetChanged();
    }

    public void docFroward() {
        String uids = null;
        if (selected.size() == 0) {
            ToastUtils.showShort(getContext(), "请选择要转发的人");
            return;
        }

        for (ContactBean bean : selected.values()) {
            if (uids == null) {
                uids = bean.getUid();
            } else {
                uids += "," + bean.getUid();
            }
        }
        if (passString != null)
            passString.setString(uids);
    }

    protected void prepareSections(int sectionsNumber) {

    }

    protected void onSectionAdded(Item section, int sectionPosition) {

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);
        if (item.type == Item.SECTION) {
            //组
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_contact_group, null);
            TextView txtGroup = view.findViewById(R.id.txtGroup);
            Log.i(TAG, "getView group: " + item.group);
            txtGroup.setText(item.group);
            return view;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_listview_item_layout, null);

            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.contact_item_cb);
            TextView tvName = (TextView) view.findViewById(R.id.contact_tv_name);
            TextView dpName = (TextView) view.findViewById(R.id.dept_name);
            TextView tvPhone = (TextView) view.findViewById(R.id.contact_tv_phone);
            CircularImageView headImg = (CircularImageView) view.findViewById(R.id.imgHead);

            final ContactBean contactBean = item.contactBean;
            Log.i(TAG, "getView item: " + contactBean.getMobile());

            if (Multiple.equals(type)) {
                checkBox.setVisibility(View.VISIBLE);
                //取出是否选中状态
                Iterator<Map.Entry<Integer, ContactBean>> iterator = selected.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = iterator.next();
                    int keyPosition = (Integer) entry.getKey();
                    if (keyPosition == position) {
                        checkBox.setChecked(true);
                        break;
                    }
                }

                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkBox.isChecked()) {
                            selected.put(position, contactBean);
                        } else {
                            selected.remove(position);
                        }
                    }
                });

            } else {
                checkBox.setVisibility(View.GONE);
            }

            tvName.setText(contactBean.getRealname());
            dpName.setText(contactBean.getDept_name());
            tvPhone.setText(contactBean.getMobile());

            ImageLoader.getInstance().displayImage(contactBean.getAvatar(),
                    new ImageViewAware(headImg), CommonUtil.getDefaultUserImageLoaderOption());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //1.首页联系人界面，点击跳转至联系人详情
                    //2.公文处理选择联系人界面，点击后返回联系人信息
                    if (Single.equals(type)) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(contactBean);
                        }
                    } else if (Multiple.equals(type)) {
                        checkBox.setChecked(!checkBox.isChecked());
                        if (checkBox.isChecked()) {
                            selected.put(position, contactBean);
                        } else {
                            selected.remove(position);
                        }
                    } else {
                        Intent intent = new Intent(getContext(), ContactDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("contact", contactBean);
                        intent.putExtras(bundle);
                        getContext().startActivity(intent);
                    }
                }
            });
            return view;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == Item.SECTION;
    }


}
