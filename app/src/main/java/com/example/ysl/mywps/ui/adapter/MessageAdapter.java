package com.example.ysl.mywps.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.MessageBean;

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
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

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
//        @BindView(R.id.txtSignType)
//        TextView txtSignType;
//
//        @BindView(R.id.txtSTime)
//        TextView txtSTime;
//
//        @BindView(R.id.txtStatus)
//        TextView txtStatus;
//
//        @BindView(R.id.txtInterZone)
//        TextView txtInterZone;

        public ViewHolder(View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }

    }
}
