package com.example.ysl.mywps.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.ysl.mywps.ui.activity.LoginActivity;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by ysl on 2018/1/23.
 * 获取极光通知
 */

public class MyJipushReceiver extends BroadcastReceiver {

    private NotificationManager nm;
    private String TAG = MyJipushReceiver.class.getSimpleName();
    public static String ACTION_RECEIVE_MESSAGE = "com.mywps.receiver.push.message";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (null == nm) {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        Bundle bundle = intent.getExtras();

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            Log.i(TAG, "onReceive JPush用户注册成功");

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.i(TAG, "onReceive 接受到推送下来的自定义消息");

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.i(TAG, "onReceive 接受到推送下来的通知");

            receivingNotification(context, bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.i(TAG, "onReceive 用户点击打开了通知");
            openNotification(context, bundle);
        } else {
            Log.i(TAG, "onReceive Unhandled intent - " + intent.getAction());
        }
    }

    private void receivingNotification(Context context, Bundle bundle) {
        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
        Log.i(TAG, "receivingNotification  title : " + title);
        String message = bundle.getString(JPushInterface.EXTRA_ALERT);
        Log.i(TAG, "receivingNotification message : " + message);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        Log.i(TAG, "receivingNotification extras : " + extras);
        try {
            JSONObject extrasJson = new JSONObject(extras);
            String type = extrasJson.optString("type");
            if (type.equals("2")) {
                //异地登录
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

//                String param = "{\"burl\":\"\\/meeting\\/details.html?id=67\",\"ctime\":\"2018-06-24 23:14:22\",\"detail_id\":\"67\",\"from_uid\":\"215\",\"message\":\"会议标题\",\"model_code\":\"MEET\",\"model_name\":\"会议助手\",\"title\":\"会议通知\"}";
//
//                Intent intent = new Intent(ACTION_RECEIVE_MESSAGE);
//                intent.putExtra("param", param);
//                context.sendOrderedBroadcast(intent, null);

            } else if (type.equals("1")) {
                //消息
                Intent intent = new Intent(ACTION_RECEIVE_MESSAGE);
                intent.putExtra("param", extrasJson.optString("param"));
                context.sendOrderedBroadcast(intent, null);
            }
        } catch (Exception ex) {

        }
    }

    private void openNotification(Context context, Bundle bundle) {
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        Log.i(TAG, "openNotification extras: " + extras);
        String type = "";
        try {
            JSONObject extrasJson = new JSONObject(extras);
            type = extrasJson.optString("type");
            if (type.equals("2")) {
                //异地登录
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            Logger.w("Unexpected: extras is not a valid json", e);
            return;
        }
//        if (TYPE_THIS.equals(myValue)) {
//            Intent mIntent = new Intent(context, ThisActivity.class);
//            mIntent.putExtras(bundle);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(mIntent);
//        } else if (TYPE_ANOTHER.equals(myValue)){
//            Intent mIntent = new Intent(context, AnotherActivity.class);
//            mIntent.putExtras(bundle);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(mIntent);
//        }
    }


}
