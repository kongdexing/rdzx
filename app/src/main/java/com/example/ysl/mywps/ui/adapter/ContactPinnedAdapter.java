package com.example.ysl.mywps.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.ContactBean;
import com.example.ysl.mywps.bean.Item;
import com.example.ysl.mywps.ui.activity.ContactDetailActivity;
import com.example.ysl.mywps.ui.view.CircularImageView;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.ToastUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import de.halfbit.pinnedsection.PinnedSectionListView;

/**
 * Created by dexing on 2018-6-10 0010.
 */

public class ContactPinnedAdapter extends ArrayAdapter<Item> implements PinnedSectionListView.PinnedSectionListAdapter {


    private ArrayList<ContactBean> list = new ArrayList<>();
    private LinkedHashMap<Integer, ContactBean> selected = new LinkedHashMap<>();

    public ContactPinnedAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public void selectAll(boolean isSelect) {
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
//        if (selected.size() == 0) {
//            ToastUtils.showShort(context, "请选择要转发的人");
//            return;
//        }
//
//        for (ContactBean bean : selected.values()) {
//            if (uids == null) {
//                uids = bean.getUid();
//            } else {
//                uids += "," + uids;
//            }
//        }
//        passsString.setString(uids);
    }

    public void loadData(List<ContactBean> contactBeans) {
//        generateDataSet(contactBeans);
    }

    public void generateDataSet(List<ContactBean> contactBeans) {
        final int sectionsNumber = 3;
        prepareSections(sectionsNumber);

        int sectionPosition = 0, listPosition = 0;
        for (char i = 0; i < sectionsNumber; i++) {
            Item section = new Item(Item.SECTION, String.valueOf((char) ('A' + i)));
            section.sectionPosition = sectionPosition;
            section.listPosition = listPosition++;
            onSectionAdded(section, sectionPosition);
            //头
            add(section);

            final int itemsNumber = contactBeans.size();
            for (int j = 0; j < itemsNumber - 8; j++) {
                Item item = new Item(Item.ITEM, contactBeans.get(j));
                item.sectionPosition = sectionPosition;
                item.listPosition = listPosition++;
                //子分组
                add(item);
            }
            sectionPosition++;
        }
    }

    protected void prepareSections(int sectionsNumber) {

    }

    protected void onSectionAdded(Item section, int sectionPosition) {

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);
        if (item.type == Item.SECTION) {
            //组
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_contact_group, null);
            TextView txtGroup = view.findViewById(R.id.txtGroup);
            txtGroup.setText(item.group);
            return view;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_listview_item_layout, null);

            TextView tvName = (TextView) view.findViewById(R.id.contact_tv_name);
            TextView dpName = (TextView)view.findViewById(R.id.dept_name);
            TextView tvPhone = (TextView) view.findViewById(R.id.contact_tv_phone);
            CircularImageView headImg = (CircularImageView)view.findViewById(R.id.imgHead);

            final ContactBean contactBean = item.contactBean;
            tvName.setText(contactBean.getRealname());
            dpName.setText(contactBean.getDept_name());
            tvPhone.setText(contactBean.getMobile());

            ImageLoader.getInstance().displayImage(contactBean.getAvatar(),
                    new ImageViewAware(headImg), CommonUtil.getDefaultUserImageLoaderOption());

//            (new GlideImageLoader()).displayImage(this.getContext(), contactBean.getAvatar(), headImg);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ContactDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("contact", contactBean);
                    intent.putExtras(bundle);
                    getContext().startActivity(intent);
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
