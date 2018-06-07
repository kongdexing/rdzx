package com.example.ysl.mywps.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.NewOAItem;

import java.util.ArrayList;
import java.util.List;

public class OAItemGridAdapter extends BaseAdapter {
    private Context mContext;
    private String TAG = getClass().getSimpleName();
    public List<NewOAItem> homeItems = new ArrayList<>();

    public OAItemGridAdapter(Context mContext) {
        super();
        this.mContext = mContext;
    }

    public void reloadData(List<NewOAItem> items) {
        this.homeItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return homeItems.size();
    }

    @Override
    public NewOAItem getItem(int position) {
        return homeItems.size() > position ? homeItems.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView: " + position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_oa_option, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.llHomeItem = (LinearLayout) convertView.findViewById(R.id.llHomeItem);
            viewHolder.optionImg = (ImageView) convertView.findViewById(R.id.optionImg);
            viewHolder.optionText = (TextView) convertView.findViewById(R.id.optionText);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final NewOAItem item = getItem(position);
        if (item == null || item.getIntent() == null) {
            viewHolder.llHomeItem.setBackgroundColor(Color.WHITE);
            return convertView;
        }

        viewHolder.llHomeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(item.getIntent());
            }
        });

        viewHolder.optionImg.setBackgroundResource(item.getIconId());
        viewHolder.optionText.setText(item.getTitle());
        return convertView;
    }

    class ViewHolder {
        LinearLayout llHomeItem;
        TextView optionText;
        ImageView optionImg;

    }

}
