package com.example.ysl.mywps.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.ysl.mywps.R;
import com.example.ysl.mywps.interfaces.JSCallBack;
import com.example.ysl.mywps.net.HttpUtl;
import com.example.ysl.mywps.net.ProgressListener;
import com.example.ysl.mywps.utils.CommonUtil;
import com.example.ysl.mywps.utils.ToastUtils;
import com.gc.materialdesign.views.ButtonRectangle;
import com.iceteck.silicompressorr.VideoCompress;
import com.lx.fit7.Fit7Utils;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ysl on 2018/2/28.
 * 介绍: 社情民意
 */

public class WebViewActivity extends BaseWebActivity implements JSCallBack {

    private String path = "";
    private final int CAMERA_REQUEST_CODE = 111;
    private final int DOCUMENT_REQUEST_CODE = 222;
    private final int VIDEO_WITH_CAMERA = 333;
    private static final int WEBVIEW_LOADED = 444;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview_layout);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String title = bundle.getString(WEB_TITLE);
            setTitleText(title);
        }

    }

    /**
     * 获取文件类目
     */
    private void saveFileTypes(final String token) {

        final Thread fileTypeThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Call<String> call = HttpUtl.getFileType("User/Share/file_type/", token);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        String data = response.body();
                        Logger.i("fileType  " + data);
                        if (CommonUtil.isEmpty(data))
                            return;
                        try {
                            JSONObject object = new JSONObject(data);
                            int code = object.getInt("code");
                            String msg = object.getString("msg");

                            if (CommonUtil.isNotEmpty(msg) && msg.contains("登陆信息有误") || code == 1) {
                                jumpToLogin();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Logger.i("fileType   " + t.getMessage());
                    }
                });
            }
        });
        fileTypeThread.setDaemon(true);
        fileTypeThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        writePermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 检查存储权限，如果没有就请求
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void writePermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 11);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 11) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "权限被允许");
            } else {
                Log.i(TAG, "权限被拒绝");
                ToastUtils.showShort(this, "请开启文件存储权限");
                finish();
//                writePermission();
            }

        } else {
            ToastUtils.showShort(this, "请开启文件存储权限");
            finish();
//            writePermission();
//            finish();
        }
    }

    @Override
    public String jsCallBack(String method, String msg) {
        Log.i(TAG, "jsCallBack: " + method);
        String message = "";
        switch (method) {
            case "callCamera":
                ToastUtils.showShort(this, "随意拍");
                callCamera();
                break;
            case "callDocument":
                ToastUtils.showShort(this, "选择文件");
                findDocuments();
                break;
            case "callCommit":

                break;
            case "callToken":
                setToken();
//                saveFileTypes(token);
//                Logger.i("CALLtOKEN");
                message = realname + "," + token;
                break;
            case "invalidToken":
                jumpToLogin();
                break;
        }
        Log.i(TAG, "jscallBack");
        return message;
    }

    private void fileHaveUpload(final String filePath, final String fileName) {
        Log.i(TAG, "fileHaveUpload version: " + version);
        webView.post(new Runnable() {
            @Override
            public void run() {
                if (version < 18) {
                    webView.loadUrl("javascript:setFile('" + filePath + "','" + fileName + "')");
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView.evaluateJavascript("javascript:setFile('" + filePath + "','" + fileName + "')", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                Log.i(TAG, "javascript:setFile return  " + s);
                            }
                        });
                    } else {

                    }
                }
            }
        });
    }

    /**
     * 压缩视频
     */
    private void comprossVideo(final String name) {
        final String outPutPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "wpsSign";
        File file = new File(outPutPath);
        if (!file.exists())
            file.mkdirs();
        final String myPath = outPutPath + File.separator + name;
        Logger.i("outputPath " + outPutPath);

        WebViewActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                VideoCompress.compressVideoLow(path, myPath, new VideoCompress.CompressListener() {
                    @Override
                    public void onStart() {
                        ToastUtils.showShort(WebViewActivity.this, "正在压缩");
                        showProgress("正在压缩");
                    }

                    @Override
                    public void onSuccess() {
                        ToastUtils.showShort(WebViewActivity.this, "压缩完成");
                        hideProgress();
                        Logger.i("success");
                        uploadFile(myPath, name);
                    }

                    @Override
                    public void onFail() {
                        ToastUtils.showShort(WebViewActivity.this, "压缩完成");
                        hideProgress();
                        Logger.i("fail");
                        uploadFile(path, name);
                    }

                    @Override
                    public void onProgress(float percent) {

                    }
                });
            }
        });
    }

    /**
     * 上传社情文件
     */
    private void uploadFile(final String filePath, final String name) {
        Log.i(TAG, "uploadFile: " + filePath + "  name:" + name + " token:" + token);
        progressbar.setVisibility(View.VISIBLE);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                Call<String> call = HttpUtl.socialUpload("User/sheqing/uploadfile/", token, name, filePath, new ProgressListener() {
                    @Override
                    public void onProgress(long hasWrittenLen, long totalLen, boolean hasFinish) {

                        int percent = (int) (hasWrittenLen * 100 / totalLen);
                        Log.i(TAG, percent + "");

                        if (percent > 99) try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        progressHandler.sendEmptyMessage(percent);
                    }
                });
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Message msg = new Message();
                        if (response.isSuccessful()) {
                            try {
                                JSONObject object = new JSONObject(response.body());
                                int code = object.getInt("code");
                                String message = object.getString("msg");
                                if (code == 0) {
                                    msg.obj = "Y";
                                    JSONObject child = object.getJSONObject("data");
                                    final String filePath = child.getString("file_full_path");
                                    final String fileName = child.getString("file_name");

                                    WebViewActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            fileHaveUpload(filePath, fileName);
                                        }
                                    });
//                                {"code":0,"msg":"","data":{
// "file_full_path":"2018-03-24\/magazine-unlock-01-2.3.928-_15efd780b6fc47318a9752c04866da9a.jpg",
// "file_name":"magazine-unlock-01-2.3.928-_15efd780b6fc47318a9752c04866da9a.jpg"
// }
// }
                                } else {
                                    msg.obj = "N";
                                    if (CommonUtil.isNotEmpty(message)) msg.obj = message;
                                }
                                Log.i(TAG, "文件上传成功 " + response.body());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            msg.obj = "N";
                        }

                        handler.sendMessage(msg);
                        Log.i(TAG, "data  " + response.body());
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Message msg = new Message();
                        msg.obj = "N";
                        handler.sendMessage(msg);
                        Log.i(TAG, "onFailure data  " + t.getMessage());
                    }
                });
            }
        });
        thread.start();
    }

    private final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null) {
                if (msg.obj.toString().equals("Y")) {

                    if (msg.obj.equals("Y")) {
                        ToastUtils.showShort(WebViewActivity.this, "上传成功");
                    } else if (msg.obj.equals("N")) {
                        ToastUtils.showShort(WebViewActivity.this, "上传失败");
                    } else {
                        ToastUtils.showShort(WebViewActivity.this, msg.obj.toString());
                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Log.i(TAG, "onActivityResult: CAMERA_REQUEST_CODE");
                path = imgPath;
            } else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    try {
                        path = getPath(this, data.getData());
                    } catch (Exception e) {

                    }
                } else {//4.4以下下系统调用方法
                    path = getRealPathFromURI(data.getData());
                }
            }

            if (CommonUtil.isEmpty(path)) {
                ToastUtils.showShort(this, "请选择要上传的文件");
                return;
            }
            final File file = new File(path);
            if (!file.exists()) {
                ToastUtils.showShort(this, "文件不存在");
                return;
            }
            final long size = file.length() / 1024 / 1024;
            Log.i(TAG, "onActivityResult file " + path + " size: " + size);
            WebViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    loading.setVisibility(View.VISIBLE);
                    if (size > 5) {
                        if (CommonUtil.isVideo(file.getName()))
                            comprossVideo(file.getName());
                    } else {
                        uploadFile(path, file.getName());
                    }
                }
            });
        }
        Log.i(TAG, "默认content地址：" + path);
    }

    /**
     * 调用随意拍选择拍照或拍视频
     */
    private void callCamera() {

        showBottomWindow();

    }

    String imgPath = "";

    private void takePhoto() {
        final String outPutPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "wpsSign";
        File file = new File(outPutPath);
        if (!file.exists())
            file.mkdirs();
        String name = "sheqing" + ".jpg";
        imgPath = outPutPath + File.separator + name;
        Intent intent = new Intent();
        // 指定开启系统相机的Action
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        file = new File(imgPath);
        if (file.exists()) {
            file.delete();
        }
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = Fit7Utils.getUriForFile(this, file);
        } else {
            uri = Uri.fromFile(file);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    private void takeVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//设置视频录制的最长时间
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 20);
//设置视频录制的画质
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, VIDEO_WITH_CAMERA);
    }

    private void findDocuments() {
        /***
         *打开文件管理器
         */
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, DOCUMENT_REQUEST_CODE);
    }


    private PopupWindow bottomWindow;
    float alpha = 1;

    private void showBottomWindow() {
        if (bottomWindow == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.choose_photo_or_video, null);
            bottomWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);

            LinearLayout llDismiss = (LinearLayout) view.findViewById(R.id.ll_dismiss);
            ButtonRectangle btPhoto = (ButtonRectangle) view.findViewById(R.id.popupwind_bt_photo);
            ButtonRectangle btVideo = (ButtonRectangle) view.findViewById(R.id.popupwind_bt_video);

            btPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePhoto();
                    bottomWindow.dismiss();
                }
            });
            btVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takeVideo();
                    bottomWindow.dismiss();
                }

            });
            llDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomWindow.dismiss();
                }
            });
            bottomWindow.setBackgroundDrawable(new BitmapDrawable());
            bottomWindow.setOutsideTouchable(true);
            bottomWindow.setAnimationStyle(R.style.Popupwindow);
            bottomWindow.setFocusable(false);

            bottomWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    bottomWindow.dismiss();
                }
            });
            bottomWindow.showAtLocation(webView, Gravity.BOTTOM, 0, 0);
        } else {
            bottomWindow.showAtLocation(webView, Gravity.BOTTOM, 0, 0);
        }
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {

            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];

                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


}
