package com.example.ysl.mywps.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ysl on 2018/1/16.
 */

public class ContactBean implements Parcelable {

    /**
     * uid : 215
     * username : 18688654478
     * mobile : 18688654478
     * realname : null
     * dept : zhengxie
     * dept_name : 政协
     */
    private String capital;
    private String uid;
    private String username;
    private String mobile;
    private String realname;
    private String dept;
    private String dept_name;
    private String avatar;
    private String title;   //首字母

    public ContactBean() {

    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRealname() {
        return realname == null ? "" : realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getDept_name() {
        return dept_name;
    }

    public void setDept_name(String dept_name) {
        this.dept_name = dept_name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.capital);
        dest.writeString(this.uid);
        dest.writeString(this.username);
        dest.writeString(this.mobile);
        dest.writeString(this.realname);
        dest.writeString(this.dept);
        dest.writeString(this.dept_name);
        dest.writeString(this.avatar);
        dest.writeString(this.title);
    }

    protected ContactBean(Parcel in) {
        this.capital = in.readString();
        this.uid = in.readString();
        this.username = in.readString();
        this.mobile = in.readString();
        this.realname = in.readString();
        this.dept = in.readString();
        this.dept_name = in.readString();
        this.avatar = in.readString();
        this.title = in.readString();
    }

    public static final Creator<ContactBean> CREATOR = new Creator<ContactBean>() {
        @Override
        public ContactBean createFromParcel(Parcel source) {
            return new ContactBean(source);
        }

        @Override
        public ContactBean[] newArray(int size) {
            return new ContactBean[size];
        }
    };
}
