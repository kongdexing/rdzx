package com.example.ysl.mywps.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.MessageBean;
import com.example.ysl.mywps.utils.CommonUtil;
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: " + position);
        final ViewHolder mHolder = (ViewHolder) holder;
        final MessageBean message = messageBeans.get(position);
        try {
            mHolder.txtTitle.setText(message.getTitle());
            mHolder.txtTime.setText(message.getCtime());
            String code = message.getModel_code().toUpperCase();
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
        } catch (Exception ex) {
            Log.i(TAG, "onBindViewHolder: " + ex.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageBeans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Unbinder unbinder;

        //        @BindView(R.id.llCheckInItem)
//        LinearLayout llCheckInItem;
//
        @BindView(R.id.txtTime)
        TextView txtTime;

        @BindView(R.id.imgMsgLogo)
        ImageView imgMsgLogo;

        @BindView(R.id.txtTitle)
        TextView txtTitle;

        @BindView(R.id.optionImg)
        ImageView optionImg;

        public ViewHolder(View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }

    }
}
