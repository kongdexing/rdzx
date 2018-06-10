package com.example.ysl.mywps.bean;

/**
 * Created by dexing on 2018-6-10 0010.
 */

public class Item {

    public static final int ITEM = 0;
    public static final int SECTION = 1;

    public final int type;
    public final ContactBean contactBean;

    public int sectionPosition;
    public int listPosition;

    public Item(int type, ContactBean bean) {
        this.type = type;
        contactBean = bean;
    }

}
