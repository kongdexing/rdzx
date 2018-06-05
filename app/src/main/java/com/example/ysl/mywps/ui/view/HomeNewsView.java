package com.example.ysl.mywps.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.ysl.mywps.R;

import butterknife.ButterKnife;

/**
 * Created by xing on 2017-11-13 0013.
 */
public class HomeNewsView extends LinearLayout {

    public HomeNewsView(Context context) {
        this(context,null);
    }

    public HomeNewsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.item_home_news, this, true);
        ButterKnife.bind(view);
    }


}
