package com.example.ysl.mywps.ui.view.autoviewpager;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

public class GalleryTransformer implements ViewPager.PageTransformer {

    private Context mContext;

    public GalleryTransformer(Context context){
        this.mContext = context;
    }

    @Override
    public void transformPage(View view, float position) {
        float scale = 0.1f;
        float scaleValue = 1 - Math.abs(position) * scale;
        view.setScaleX(scaleValue);
        view.setScaleY(scaleValue);
        view.setAlpha(scaleValue*1.9f);
        //view.setPivotX(view.getWidth() * (1 - position - (position > 0 ? 1 : -1) * -0.75f) * scale);
        view.setPivotY(view.getHeight()*0.5f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(position > -0.5 && position < 0.5 ? 1 : 0);
        }else {
            ViewCompat.setElevation(view,position > -0.5 && position < 0.5 ? 1 : 0);
        }

    }

}