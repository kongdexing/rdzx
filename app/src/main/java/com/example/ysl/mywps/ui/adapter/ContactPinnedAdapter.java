package com.example.ysl.mywps.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.ContactBean;
import com.example.ysl.mywps.bean.Item;

import java.util.List;
import java.util.Locale;

import de.halfbit.pinnedsection.PinnedSectionListView;

/**
 * Created by dexing on 2018-6-10 0010.
 */

public class ContactPinnedAdapter extends ArrayAdapter<Item> implements PinnedSectionListView.PinnedSectionListAdapter {

    public ContactPinnedAdapter(@NonNull Context context, int resource) {
        super(context, resource);

    }

    public void loadData(List<ContactBean> contactBeans) {
        generateDataSet(contactBeans);
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
//        TextView view = (TextView) super.getView(position, convertView, parent);
//        view.setTextColor(Color.DKGRAY);
//        view.setTag("" + position);
        Item item = getItem(position);
        if (item.type == Item.SECTION) {
            //组
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_contact_group, null);
            TextView txtGroup = view.findViewById(R.id.txtGroup);
            txtGroup.setText(item.group);
            return view;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_listview_item_layout, null);

            TextView tvName = (TextView) view.findViewById(R.id.contact_tv_name);
            TextView tvPhone = (TextView) view.findViewById(R.id.contact_tv_phone);
            ContactBean contactBean = item.contactBean;
            tvName.setText(contactBean.getUsername());
            tvPhone.setText(contactBean.getMobile());

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
