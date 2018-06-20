package com.example.ysl.mywps.bean;

public class ContactDetailBean extends ContactBean{

    private String email;
    private String politics_stauts; //政治面貌
    private String jiebie;      //界别
    private String duty;        //职务

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPolitics_stauts() {
        return politics_stauts;
    }

    public void setPolitics_stauts(String politics_stauts) {
        this.politics_stauts = politics_stauts;
    }

    public String getJiebie() {
        return jiebie;
    }

    public void setJiebie(String jiebie) {
        this.jiebie = jiebie;
    }

    public String getDuty() {
        return duty;
    }

    public void setDuty(String duty) {
        this.duty = duty;
    }
}
