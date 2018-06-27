package com.example.ysl.mywps.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.MessageBean;
import com.example.ysl.mywps.ui.activity.BaseWebActivity;
import com.example.ysl.mywps.ui.activity.WebViewActivity;
import com.example.ysl.mywps.ui.activity.WpsDetailActivity;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.SysytemSetting;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2017/12/24 0024.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public Context mContext;
    public String TAG = "";
    private final TypedValue mTypedValue = new TypedValue();
    public int mBackground;
    private List<MessageBean> messageBeans = new ArrayList<>();

    public MessageAdapter(Context context) {
        super();
        mContext = context;
//        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
//        mBackground = mTypedValue.resourceId;
        TAG = getClass().getSimpleName();
//        Log.i(TAG, "BaseRecycleAdapter: ");
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mContext = context;
        mBackground = mTypedValue.resourceId;
    }

    public void refreshData(List<MessageBean> messages) {
        Log.i(TAG, "refreshData: ");
        messageBeans = messages;

    }

    public void appendData(List<MessageBean> messages) {
        Log.i(TAG, "appendData: ");
        messageBeans.addAll(messages);
    }

    public void appendTop(MessageBean messageBean) {
        Log.i(TAG, "appendTop before size: " + messageBeans.size());
        messageBeans.add(0, messageBean);
        Log.i(TAG, "appendTop after size: " + messageBeans.size());
        notifyItemInserted(0);
        notifyItemRangeChanged(0, messageBeans.size());
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_message, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: " + position);
        final MyViewHolder mHolder = (MyViewHolder) holder;
        //MyViewHolder
        final MessageBean message = messageBeans.get(position);
        try {
            mHolder.txtTitle.setText(message.getMessage());
            mHolder.txtTime.setText(message.getCtime());
            final String code = message.getModel_code().toUpperCase();
            if (code.equals("ODC")) {
                //公文流转
                mHolder.imgMsgLogo.setBackgroundResource(R.drawable.icon_oa_1);
            } else if ("SQMY".equals(code)) {
                //社情民意
                mHolder.imgMsgLogo.setBackgroundResource(R.drawable.icon_3);
            } else if ("TAXT".equals(code)) {
                //提案系统
                mHolder.imgMsgLogo.setBackgroundResource(R.drawable.icon_2);
            } else if ("ACTIVE".equals(code)) {
                //主题活动
                mHolder.imgMsgLogo.setBackgroundResource(R.drawable.icon_oa_5);
            } else if ("MEET".equals(code)) {
                //会议助手
                mHolder.imgMsgLogo.setBackgroundResource(R.drawable.icon_oa_4);
            } else if ("NOTICES".equals(code)) {
                //通话公告
                mHolder.imgMsgLogo.setBackgroundResource(R.drawable.icon_oa_3);
            } else if ("TOPIC".equals(code)) {
                //话题
                mHolder.imgMsgLogo.setBackgroundResource(R.drawable.icon_oa_3);
            } else if ("BBS".equals(code)) {
                //同事吧
                mHolder.imgMsgLogo.setBackgroundResource(R.drawable.icon_oa_6);
            } else if ("PAPER".equals(code)) {
                //问卷
                mHolder.imgMsgLogo.setBackgroundResource(R.drawable.icon_oa_7);
            } else if ("MEET_ROOM".equals(code)) {
                //会议室
                mHolder.imgMsgLogo.setBackgroundResource(R.drawable.icon_oa_4);
            } else if ("REC".equals(code)) {
                //热门推荐
                mHolder.imgMsgLogo.setBackgroundResource(R.drawable.icon_oa_5);
            }
            ImageLoader.getInstance().displayImage(message.getPic(),
                    new ImageViewAware(mHolder.optionImg), CommonUtil.getDefaultUserImageLoaderOption());

            mHolder.rlNewsItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (code.equals("ODC")) {
                        //公文流转
                        Intent intent = new Intent(mContext, WpsDetailActivity.class);

                        intent.putExtra(SysytemSetting.WPS_MODE, SysytemSetting.INSIDE_WPS);
//                intent.putExtra(SysytemSetting.ACTIVITY_KIND,SysytemSetting.HANDLE_WPS);
                        Bundle bundle = new Bundle();
                        bundle.putString("doc_id", message.getDetail_id());
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                    } else {
                        Intent intent = new Intent(mContext, WebViewActivity.class);
                        intent.putExtra(BaseWebActivity.WEB_TITLE, message.getModel_name());
                        intent.putExtra(BaseWebActivity.WEB_URL, message.getBurl());
                        mContext.startActivity(intent);
                    }
                }
            });

        } catch (Exception ex) {
            Log.i(TAG, "onBindViewHolder: " + ex.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageBeans.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private Unbinder unbinder;

        @BindView(R.id.rlNewsItem)
        LinearLayout rlNewsItem;

        @BindView(R.id.txtTime)
        TextView txtTime;

        @BindView(R.id.imgMsgLogo)
        ImageView imgMsgLogo;

        @BindView(R.id.txtTitle)
        TextView txtTitle;

        @BindView(R.id.optionImg)
        ImageView optionImg;

        public MyViewHolder(View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }

    }
}
