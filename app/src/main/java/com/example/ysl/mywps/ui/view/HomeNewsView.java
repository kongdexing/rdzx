package com.example.ysl.mywps.ui.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.HotBean;
import com.example.ysl.mywps.ui.activity.BaseWebActivity;
import com.example.ysl.mywps.ui.activity.WebViewActivity;
import com.example.ysl.mywps.ui.activity.WpsDetailActivity;
import com.example.ysl.mywps.ui.view.autoviewpager.GlideImageLoader;
import com.example.ysl.mywps.utils.SysytemSetting;

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
        if (hots.getPic() == null || hots.getPic().isEmpty()) {
            optionImg.setVisibility(GONE);
        } else {
            optionImg.setVisibility(VISIBLE);
            (new GlideImageLoader()).displayImage(this.getContext(), hots.getPic(), optionImg);
        }

        rlNewsItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hots.getModel_code().equals("ODC")) {
                    //公文流转
                    Intent intent = new Intent(getContext(), WpsDetailActivity.class);
                    intent.putExtra(SysytemSetting.WPS_MODE, SysytemSetting.INSIDE_WPS);
//                intent.putExtra(SysytemSetting.ACTIVITY_KIND,SysytemSetting.HANDLE_WPS);
                    Bundle bundle = new Bundle();
                    bundle.putString("doc_id", hots.getDetail_id());
                    intent.putExtras(bundle);
                    getContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(getContext(), WebViewActivity.class);
                    intent.putExtra(BaseWebActivity.WEB_URL, hots.getBurl());
                    intent.putExtra(BaseWebActivity.WEB_TITLE, "热门推荐");
                    getContext().startActivity(intent);
                }
            }
        });

    }

}
