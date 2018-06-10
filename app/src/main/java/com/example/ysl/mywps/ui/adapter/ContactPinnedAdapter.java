package com.example.ysl.mywps.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ysl.mywps.bean.ContactBean;
import com.example.ysl.mywps.bean.Item;

import java.util.List;
import java.util.Locale;

import de.halfbit.pinnedsection.PinnedSectionListView;
import de.halfbit.pinnedsection.examples.pinnedsection.R;

/**
 * Created by dexing on 2018-6-10 0010.
 */

public class ContactPinnedAdapter extends ArrayAdapter<ContactBean> implements PinnedSectionListView.PinnedSectionListAdapter {

    public ContactPinnedAdapter(@NonNull Context context, int resource) {
        super(context, resource);

    }

    public void loadData(List<ContactBean> contactBeans) {
        generateDataSet(contactBeans);
    }

    public void generateDataSet(List<ContactBean> contactBeans) {
        final int sectionsNumber = 6;
        prepareSections(sectionsNumber);

        int sectionPosition = 0, listPosition = 0;
        for (char i = 0; i < sectionsNumber; i++) {
            Item section = new Item(Item.SECTION, String.valueOf((char) ('A' + i)));
            section.sectionPosition = sectionPosition;
            section.listPosition = listPosition++;
            onSectionAdded(section, sectionPosition);
            //头
            add(section);

            final int itemsNumber = (int) Math.abs((Math.cos(2f * Math.PI / 3f * sectionsNumber / (i + 1f)) * 25f));
            for (int j = 0; j < itemsNumber; j++) {
                Item item = new Item(Item.ITEM, section.text.toUpperCase(Locale.ENGLISH) + " - " + j);
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

    protected void onSectionAdded(ContactBean section, int sectionPosition) {

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTextColor(Color.DKGRAY);
        view.setTag("" + position);
        Item item = getItem(position);
        if (item.type == Item.SECTION) {
            view.setBackgroundColor(parent.getResources().getColor(R.color.green_light));
            //view.setOnClickListener(PinnedSectionListActivity.this);
//            view.setBackgroundColor(parent.getResources().getColor(COLORS[item.sectionPosition % COLORS.length]));
        }
        return view;
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
