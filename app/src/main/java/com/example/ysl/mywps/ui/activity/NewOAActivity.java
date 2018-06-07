package com.example.ysl.mywps.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.bean.NewOAItem;
import com.example.ysl.mywps.ui.adapter.OAItemGridAdapter;
import com.example.ysl.mywps.ui.view.MyGridView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 移动办公
 */
public class NewOAActivity extends BaseActivity {

    @BindView(R.id.grd_oa)
    MyGridView grd_oa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_oa);
        ButterKnife.bind(this);
        showLeftButton(true, "", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        setTitleText("移动办公");
    }

    @Override
    public void initData() {

    }

    @Override
    public void initView() {
        List<NewOAItem> oaItems = new ArrayList<>();
        oaItems.add(new NewOAItem()
                .setIconId(R.drawable.icon_oa_1)
                .setTitle("公文流转")
                .setIntent(new Intent(this, DocumentDetailActivity.class)));

        oaItems.add(new NewOAItem()
                .setIconId(R.drawable.icon_oa_2)
                .setTitle("素材共享")
                .setIntent(new Intent(this, MaterialActivity.class)));
        oaItems.add(new NewOAItem()
                .setIconId(R.drawable.icon_oa_3)
                .setTitle("通知公告")
                .setIntent(new Intent(this, ReportActivity.class)));
        oaItems.add(new NewOAItem()
                .setIconId(R.drawable.icon_oa_4)
                .setTitle("会议助手")
                .setIntent(new Intent(this, MeettingActivity.class)));
        oaItems.add(new NewOAItem()
                .setIconId(R.drawable.icon_oa_5)
                .setTitle("主题活动")
                .setIntent(new Intent(this, ThemeActivity.class)));
        oaItems.add(new NewOAItem()
                .setIconId(R.drawable.icon_oa_6)
                .setTitle("同事吧")
                .setIntent(new Intent(this, ColleagueAcitivity.class)));
        oaItems.add(new NewOAItem()
                .setIconId(R.drawable.icon_oa_7)
                .setTitle("问卷调查")
                .setIntent(new Intent(this, QuestionActivity.class)));

//        try {
//            RongIM.getInstance().startCustomerServiceChat(getActivity(), "KEFU152077670318138", "在线客服1", csInfo);
//        } catch (Exception e) {
//            e.printStackTrace();
//            ToastUtils.showShort(getActivity(), "融云登陆异常，请回到登陆界面重新登陆!");
//        }
        oaItems.add(new NewOAItem()
                .setIconId(R.drawable.icon_oa_8)
                .setTitle("在线客服")
                .setIntent(new Intent(this, DocumentDetailActivity.class)));

        oaItems.add(null);
        OAItemGridAdapter itemAdapter = new OAItemGridAdapter(this);

        grd_oa.setAdapter(itemAdapter);
        itemAdapter.reloadData(oaItems);
    }


}
