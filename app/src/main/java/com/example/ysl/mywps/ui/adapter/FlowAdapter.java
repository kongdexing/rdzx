package com.example.ysl.mywps.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.FlowBean;
import com.example.ysl.mywps.ui.view.CircularImageView;
import com.example.ysl.mywps.utils.CommonUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;

/**
 * Created by ysl on 2018/1/16.
 */

public class FlowAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<FlowBean> list;
    private boolean showFeedback = false;

    public FlowAdapter(Context context, ArrayList<FlowBean> list) {
        this.context = context;
        this.list = list;
    }

    public void updateAdapter(ArrayList<FlowBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    private class ViewHolder {
        TextView tvDate, tvLeader, tvStage, tvOpinion;
        RelativeLayout ivCircle;
        RelativeLayout line2;
        CircularImageView imgHead;
    }

    private ViewHolder holder;

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.listview_item_flow_progress_layout, null);
            holder = new ViewHolder();
            holder.tvDate = (TextView) view.findViewById(R.id.flow_tv_date);
            holder.tvLeader = (TextView) view.findViewById(R.id.flow_tv_leader);
            holder.tvStage = (TextView) view.findViewById(R.id.flow_tv_stage);
            holder.tvOpinion = (TextView) view.findViewById(R.id.flow_tv_opinion);
            holder.ivCircle = (RelativeLayout) view.findViewById(R.id.circle);
            holder.line2 = (RelativeLayout) view.findViewById(R.id.line2);
            holder.imgHead = (CircularImageView) view.findViewById(R.id.imageView);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        FlowBean bean = list.get(position);

        holder.tvStage.setText(bean.getStatus_show());
        if (CommonUtil.isEmpty(bean.getMonth())) holder.tvDate.setText(bean.getCtime());
        else holder.tvDate.setText("  " + bean.getMonth() + "  " + bean.getTime());
        if (bean.getRealname() == null || bean.getRealname().equals("")) {
            holder.tvLeader.setText("测试");
        } else {
            holder.tvLeader.setText(bean.getRealname());
        }
        if (bean.getOpinion() == null || bean.getOpinion().equals("")) {
            holder.tvOpinion.setVisibility(View.GONE);
        } else {
            holder.tvOpinion.setText(bean.getOpinion());
        }

        //反馈阶段，只显示一个小圆点
        if ("反馈".equals(bean.getStatus_show())) {
            if (!showFeedback) {
                showFeedback = true;
                holder.ivCircle.setVisibility(View.VISIBLE);
            } else {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.line2.getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.line1);
                holder.line2.setLayoutParams(params);
                holder.ivCircle.setVisibility(View.INVISIBLE);
            }
        }else{
            holder.ivCircle.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.line2.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.circle);
            holder.line2.setLayoutParams(params);
        }

        ImageLoader.getInstance().displayImage(bean.getAvatar(),
                new ImageViewAware(holder.imgHead), CommonUtil.getDefaultUserImageLoaderOption());
        return view;
    }
}