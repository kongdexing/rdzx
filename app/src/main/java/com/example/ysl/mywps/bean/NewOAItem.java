package com.example.ysl.mywps.bean;

import android.content.Intent;

/**
 * Created by dexing on 2018-6-7 0007.
 */

public class NewOAItem {
    //图标
    private int iconId;
    //标题
    private String title;
    //跳转至某个Activity之Intent
    private Intent intent;

    public int getIconId() {
        return iconId;
    }

    public NewOAItem setIconId(int iconId) {
        this.iconId = iconId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public NewOAItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public Intent getIntent() {
        return intent;
    }

    public NewOAItem setIntent(Intent intent) {
        this.intent = intent;
        return this;
    }

}
