package com.example.ysl.mywps.ui.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.HotBean;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.ui.activity.BaseWebActivity;
import com.example.ysl.mywps.ui.activity.WebviewActivity;
import com.example.ysl.mywps.ui.view.autoviewpager.GlideImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xing on 2017-11-13 0013.
 */
public class HomeNewsView extends LinearLayout {

    @BindView(R.id.rlNewsItem)
    LinearLayout rlNewsItem;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtMode)
    TextView txtMode;
    @BindView(R.id.txtTime)
    TextView txtTime;
    @BindView(R.id.optionImg)
    ImageView optionImg;

    public HomeNewsView(Context context) {
        this(context, null);
    }

    public HomeNewsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.item_home_news, this, true);
        ButterKnife.bind(view);
    }

    public void bindData(final HotBean hots) {
        txtTitle.setText(hots.getTitle());
        txtMode.setText(hots.getModel_name());
        txtTime.setText(hots.getCtime());
        (new GlideImageLoader()).displayImage(this.getContext(), hots.getPic(), optionImg);

        rlNewsItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WebviewActivity.class);
                intent.putExtra(BaseWebActivity.WEB_URL, hots.getBurl());
                intent.putExtra(BaseWebActivity.WEB_TITLE, "热门推荐");
                getContext().startActivity(intent);
            }
        });

    }

}
