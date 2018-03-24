package com.huayi.cme;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;
import com.huayi.cme.wxapi.WXPayEntryActivity;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.zxing.activity.CaptureActivity;
import com.huayi.cme.UploadActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    private IWXAPI api;
    private WebView webView;
    private GifImageView loadingView;
    public static final int SCAN_CODE=5;
    public static final int FILECHOOSER_RESULTCODE=2;
    public static final int REQUEST_READ_PHONE_STATE = 1;
    public static final int REQUEST_CAMERA = 99; //人脸识别专用-因为设置授权后回调
    public static final int REQUEST_CAMERA_1 = 100; //扫描二维码用-因为设置授权后回调
    public static final int REQUEST_CAMERA_2 = 101; //相册那用-请求授权后不做操作
    public static final int REQUEST_CAMERA_3 = 102; //扫描二维码考勤用-
    public static final int REQUEST_CAMERA_4 = 103; //人脸认证-
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 201; //存储卡写入权限
    public static final int BaiDuAI=250;
    public static final int UploadPhoto = 6;
    public static boolean isForeground = false;

    public static final String TAG = "mylog"; //日志输出标记

    public String WEB_SITE;
    public String WEB_SITE_SCAN;
    public String WEB_SITE_WXPAY;
    public String WEB_SITE_AI;
    public String WEB_SITE_PHOTO;

    //for receive customer msg from jpush server
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    private String appid="";

    private RelativeLayout frameLayout = null;
    private WebChromeClient chromeClient = null;
    private View myView = null;
    private WebChromeClient.CustomViewCallback myCallback = null;
    private ProgressDialog pd;

    private String homeurl;
    private String returnurl;
    private String errorurl;

    private WebChromeClient mywebchromeclient;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mValueCallback;

    private String idnumber;
    private int timeout;
    private String token;
    private String parakey;
    private String tip_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyApplication app = (MyApplication) this.getApplication();
        WEB_SITE = app.getWebSite();
        WEB_SITE_SCAN = app.getWebSiteScan();
        WEB_SITE_WXPAY = app.getWebSiteWxpay();
        WEB_SITE_AI = app.getWebSiteAI();
        WEB_SITE_PHOTO = app.getWebSitePhoto();

        requestPermissions(REQUEST_READ_PHONE_STATE, android.Manifest.permission.READ_PHONE_STATE);

        if(Build.VERSION.SDK_INT>=19){
            isNotificationEnabled(this); //判断通知栏是否开启
            //requestPermissions(REQUEST_WRITE_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }


        //Toast.makeText(getApplicationContext(),"2.0版本来啦",Toast.LENGTH_LONG).show();

        //检查版本更新
        CheckUpdate();

        Log.d(TAG, "appid: "+appid);

        //设置极光推送别名和标签
        SetJPushTag();



        //微信api
        api = WXAPIFactory.createWXAPI(this, WXPayEntryActivity.APP_ID);

        //webview初始化设置
        setMyWebView(savedInstanceState);

    }

    private void setMyWebView(Bundle savedInstanceState){
        loadingView=(GifImageView)findViewById(R.id.loadView);
        ViewGroup.LayoutParams  lm = loadingView.getLayoutParams();
        int w=getWindowManager().getDefaultDisplay().getWidth();
        lm.width=(int)(w*0.3);
        lm.height=lm.width*109/329;
        //Intent intent = getIntent();
        //String s= intent.getStringExtra(SplashScreen.EXTRA_MESSAGE);
        webView=(WebView)findViewById(R.id.webView);
        frameLayout = (RelativeLayout)findViewById(R.id.framelayout);

        WebSettings settings=webView.getSettings();
        //设置 缓存模式
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        //final String USER_AGENT_STRING = webView.getSettings().getUserAgentString() + " Rong/2.0";
        //settings.setUserAgentString( USER_AGENT_STRING );
        settings.setUserAgentString("Mozilla/5.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/600.1.3 (KHTML, like Gecko) Version/8.0 Mobile/12A4345d Safari/600.1.4");
        settings.setSupportZoom(false);
        //settings.setBuiltInZoomControls(true);
        settings.setSupportMultipleWindows(true);
        //settings.setPluginState(WebSettings.PluginState.ON);
        settings.setLoadWithOverviewMode(true);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.addJavascriptInterface(new JavascriptHandler(),"apploading");
        mywebchromeclient = new MyChromeClient();
        webView.setWebChromeClient(mywebchromeclient);
        webView.setWebViewClient(new WebViewClient(){


            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                try{
                    if(url.endsWith("jquery.js")){
                        java.io.InputStream is = getAssets().open("jquery.js");
                        WebResourceResponse response = new WebResourceResponse("text/javascript", "utf-8", is);
                        return response;

                    }else if(url.endsWith("underscore.min.js")){
                        java.io.InputStream is = getAssets().open("underscore.min.js");
                        WebResourceResponse response = new WebResourceResponse("text/javascript", "utf-8", is);
                        return response;

                    }else if(url.endsWith("jquery.min.js")){
                        java.io.InputStream is = getAssets().open("jquery.min.js");
                        WebResourceResponse response = new WebResourceResponse("text/javascript", "utf-8", is);
                        return response;

                    }else if(url.endsWith("bootstrap.min.js")){
                        java.io.InputStream is = getAssets().open("bootstrap.min.js");
                        WebResourceResponse response = new WebResourceResponse("text/javascript", "utf-8", is);
                        return response;

                    }else if(url.endsWith("angular.min.js")){
                        java.io.InputStream is = getAssets().open("angular.min.js");
                        WebResourceResponse response = new WebResourceResponse("text/javascript", "utf-8", is);
                        return response;

                    }else if(url.endsWith("angular-cookies.min.js")){
                        java.io.InputStream is = getAssets().open("angular-cookies.min.js");
                        WebResourceResponse response = new WebResourceResponse("text/javascript", "utf-8", is);
                        return response;

                    }else if(url.endsWith("angular-resource.min.js")){
                        java.io.InputStream is = getAssets().open("angular-resource.min.js");
                        WebResourceResponse response = new WebResourceResponse("text/javascript", "utf-8", is);
                        return response;

                    }else if(url.endsWith("angular-sanitize.min.js")){
                        java.io.InputStream is = getAssets().open("angular-sanitize.min.js");
                        WebResourceResponse response = new WebResourceResponse("text/javascript", "utf-8", is);
                        return response;

                    }else if(url.endsWith("angular-touch.min.js")){
                        java.io.InputStream is = getAssets().open("angular-touch.min.js");
                        WebResourceResponse response = new WebResourceResponse("text/javascript", "utf-8", is);
                        return response;

                    }else if(url.endsWith("hhSwipe.js")){
                        java.io.InputStream is = getAssets().open("hhSwipe.js");
                        WebResourceResponse response = new WebResourceResponse("text/javascript", "utf-8", is);
                        return response;

                    }else if(url.endsWith("ng-infinite-scroll.min.js")){
                        java.io.InputStream is = getAssets().open("ng-infinite-scroll.min.js");
                        WebResourceResponse response = new WebResourceResponse("text/javascript", "utf-8", is);
                        return response;

                    }
                }catch (java.io.IOException e){
                    //Log.v(TAG,e.getMessage());
                }
                return super.shouldInterceptRequest(view, url);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, WebResourceRequest request) {
                final String url = request.getUrl().toString();
                return handleUri(view,url);
            }
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, String url) {
                //Log.d(TAG,url);
                return handleUri(view,url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loadingView.setVisibility(View.INVISIBLE);
                view.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadingView.setVisibility(View.VISIBLE);
                view.setVisibility(View.INVISIBLE);
                super.onPageStarted(view, url, favicon);
            }


            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                //super.onReceivedError(view, request, error);
                //showerror(error.getDescription().toString(),view.getUrl());
            }


        });
        try {
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            homeurl=WEB_SITE + "Home/androidversion?version=" +info.versionCode+"&appid="+appid;

            Intent intent = getIntent();
            if (null != intent) {
                Bundle bundle = getIntent().getExtras();
                if(bundle!=null){
                    String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
                    if(null!=extra&&!extra.equals("")){
                        final JSONObject json = new JSONObject(extra);
                        if(json.has("mykey")) {
                            String val = json.getString("mykey");
                            if (val.length() > 0) {
                                homeurl=WEB_SITE + "Home/ToProject?mykey="+val;
                            }
                        }
                    }

                }

            }

            webView.loadUrl(homeurl); //m/index.html
        }catch (Exception ex){}
        //webView.loadUrl("http://z.puddingz.com/t.html");
        if(savedInstanceState != null){
            webView.restoreState(savedInstanceState);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.wxpayoff");
        intentFilter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        /*
       if(Integer.parseInt(s)==0) {
            webView.loadUrl("http://yuyin.91huayi.net/m/#/login");
        }else{
            webView.loadUrl("http://yuyin.91huayi.net/m/index.html");
        }
        */

    }

    //显示错误页
    private void showerror(String msg, String url){

        loadingView.setVisibility(View.INVISIBLE);
        webView.setVisibility(View.VISIBLE);
        String errorhtml = "<html><head><title>自定义错误页</title></head><body><center style='padding:10px;'><h1>"+msg+"</h1><br /></br /></br><p><a href='"+homeurl+"'>返回首页</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href='"+url+"'>重新加载</a></p></center></body></html>";
        //webView.loadUrl("about:blank");
        webView.loadData(errorhtml,"text/html;charset=utf-8",null);
    }

    private BroadcastReceiver mRefreshBroadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive mRefreshBroadcastReceiver: "+action);
            if (action.equals("action.wxpayoff"))
            {
                wxpayoff();
            }else if(action.equals(MESSAGE_RECEIVED_ACTION)) {
                String message = intent.getStringExtra(KEY_MESSAGE);
                Log.d(TAG, "onReceive message: "+message);
                String extras = intent.getStringExtra(KEY_EXTRAS);
                Log.d(TAG, "onReceive extras: "+extras);
            }
        }
    };
    private void wxpayoff(){
        webView.loadUrl(returnurl);
    }

    private boolean handleUri(final WebView view,final String url){
        Log.d(TAG, url);
        if(url.contains("alipay")){
            try {
                final PayTask task = new PayTask(MainActivity.this);
                final String ex = task.fetchOrderInfoFromH5PayUrl(url);
                if (!TextUtils.isEmpty(ex)) {
                    //Log.d(TAG,url);
                    new Thread(new Runnable() {
                        public void run() {
                            //Log.d("payTask:::", ex);
                            final H5PayResultModel result = task.h5Pay(ex, true);
                            if (!TextUtils.isEmpty(result.getReturnUrl())) {
                                MainActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        view.loadUrl(result.getReturnUrl());
                                    }
                                });
                            }
                        }
                    }).start();
                }else {
                    view.loadUrl(url);
                }
                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                //startActivity(intent);
            }catch(Exception e){ Log.d(TAG,e.getMessage());return true;}
        }else if (url.contains(WEB_SITE_SCAN+"AppScanning.aspx")){
            view.loadUrl(WEB_SITE_SCAN+"Index.htm");
            requestPermissions(REQUEST_CAMERA_3, android.Manifest.permission.CAMERA);
        }else if ((WEB_SITE_SCAN+"AppScaningProj.aspx").equalsIgnoreCase(url)){
            view.loadUrl(WEB_SITE_SCAN+"Index.htm");
            requestPermissions(REQUEST_CAMERA_1, android.Manifest.permission.CAMERA);
        }else if (url.contains(WEB_SITE+"face.html")) { //人脸识别
            Uri uri = Uri.parse(url);
            idnumber = uri.getQueryParameter("cardid");
            timeout = Integer.valueOf(uri.getQueryParameter("timeout")).intValue();
            parakey = uri.getQueryParameter("parakey");
            if(idnumber.length()>0) {
                requestPermissions(REQUEST_CAMERA, android.Manifest.permission.CAMERA);
            }else{
                Toast.makeText(MainActivity.this, "未获取到身份证号", Toast.LENGTH_SHORT).show();
            }
            view.loadUrl(WEB_SITE_SCAN+"Index.htm");
        }else if((WEB_SITE_SCAN+"GetAppId.aspx").equalsIgnoreCase(url)){
            view.loadUrl(WEB_SITE_SCAN+"GetAppIdReceive.aspx?para="+appid);
        }else if(url.contains(WEB_SITE_PHOTO+"api/CertUpload/")){
            Uri uri = Uri.parse(url);
            token = uri.getQueryParameter("token");
            idnumber = uri.getQueryParameter("cert_id");
            parakey = uri.getQueryParameter("parakey");
            tip_msg = uri.getQueryParameter("tip_msg");
            view.loadUrl(WEB_SITE_SCAN+"Index.htm");
            requestPermissions(REQUEST_WRITE_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }else if(url.contains(WEB_SITE_WXPAY+"wx_pay.aspx")){
            Uri uri = Uri.parse(url);
            api.registerApp(WXPayEntryActivity.APP_ID);
            if(api.isWXAppInstalled()) {
                PayReq req = new PayReq();

                req.appId = uri.getQueryParameter("appid");
                req.partnerId = uri.getQueryParameter("partnerid");
                req.prepayId = uri.getQueryParameter("prepayid");
                req.nonceStr = uri.getQueryParameter("noncestr");
                req.timeStamp = uri.getQueryParameter("timestamp");
                req.packageValue = uri.getQueryParameter("package");
                req.sign = uri.getQueryParameter("sign");
                returnurl=uri.getQueryParameter("return_url");
                errorurl=uri.getQueryParameter("error_backurl");
                //req.extData = "app data"; // optional
                api.sendReq(req);
            }else{
                Toast.makeText(MainActivity.this, "请先安装微信", Toast.LENGTH_SHORT).show();
                view.loadUrl(errorurl);
            }
        }else if(url.contains(WEB_SITE+"appidtestal.html")){ //测试用
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, APPIDActivity.class);
            MainActivity.this.startActivity(intent);
        }else {
            view.loadUrl(url);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(myView == null){
            if(webView.canGoBack()){webView.goBack();}else{super.onBackPressed();  }
        }
        else{
            chromeClient.onHideCustomView();
            quitFullScreen();
        }
    }

    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
        webView.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
        webView.onPause();
        MobclickAgent.onPause(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshBroadcastReceiver);
    }

    //如果对应权限用户授权，执行操作
    public void getPermissionDo(int requestCode){
        switch (requestCode){
            case REQUEST_READ_PHONE_STATE:
                appid=Installation.id(this);
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                requestPermissions(REQUEST_CAMERA_4,android.Manifest.permission.CAMERA);
                break;
            case REQUEST_CAMERA:
                if(idnumber!=null&&idnumber.length()>0){
                    //Intent faceIntent = new Intent(MainActivity.this, FaceLivenessExpActivity.class);
                    Intent faceIntent = new Intent(MainActivity.this, FaceDetectExpActivity.class);
                    faceIntent.putExtra("idnumber", idnumber);
                    faceIntent.putExtra("timeout",timeout);
                    faceIntent.putExtra("parakey", parakey);
                    startActivityForResult(faceIntent, BaiDuAI);
                }
                break;
            case REQUEST_CAMERA_1:
                Intent intent=new Intent(MainActivity.this, CaptureActivity.class);
                intent.putExtra("source","AppScaningProj");
                startActivityForResult(intent,SCAN_CODE);
                break;
            case REQUEST_CAMERA_3:
                Intent intent2=new Intent(MainActivity.this, CaptureActivity.class);
                intent2.putExtra("source","AppScanning");
                startActivityForResult(intent2,SCAN_CODE);
                break;
            case REQUEST_CAMERA_4:
                Intent faceIntent = new Intent(MainActivity.this, UploadActivity.class);
                faceIntent.putExtra("token", token);
                faceIntent.putExtra("cert_id", idnumber);
                faceIntent.putExtra("parakey", parakey);
                faceIntent.putExtra("tip_msg",tip_msg);
                startActivityForResult(faceIntent, UploadPhoto);
                break;
        }
    }
    //请求权限
    public void requestPermissions(int requestCode, String permission) {
        if (permission != null && permission.length() > 0) {
            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    // 检查是否有权限
                    int hasPer = checkSelfPermission(permission);
                    if (hasPer != PackageManager.PERMISSION_GRANTED) {
                        // 是否应该显示权限请求
                        /*
                        boolean isShould = shouldShowRequestPermissionRationale(permission);
                        if(requestCode == REQUEST_WRITE_EXTERNAL_STORAGE || requestCode == REQUEST_READ_PHONE_STATE){
                            isShould = true;
                        }

                        if(isShould){
                            //ActivityCompat.requestPermissions(this, new String[]{permission},requestCode);
                            requestPermissions(new String[]{permission}, requestCode);
                        }else{
                            Toast.makeText(MainActivity.this, "请手动开启掌上华医需要的权限信息", Toast.LENGTH_LONG).show();
                        }
                        */
                        requestPermissions(new String[]{permission}, requestCode);
                    }else{
                        getPermissionDo(requestCode);
                    }
                } else {
                    getPermissionDo(requestCode);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean flag = false;
        for (int i = 0; i < permissions.length; i++) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[i]) {
                flag = true;
            }
        }
        if (!flag) {
            switch (requestCode){
                case REQUEST_READ_PHONE_STATE:
                    requestPermissions(requestCode, Manifest.permission.READ_PHONE_STATE);
                    break;
                case REQUEST_WRITE_EXTERNAL_STORAGE:
                    requestPermissions(requestCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    break;
                case REQUEST_CAMERA:
                case REQUEST_CAMERA_1:
                case REQUEST_CAMERA_3:
                case REQUEST_CAMERA_4:
                    requestPermissions(requestCode, android.Manifest.permission.CAMERA);
                    break;
            }

        }else{
            getPermissionDo(requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case SCAN_CODE:
                if(resultCode==RESULT_OK){
                    String result = data.getStringExtra("result");
                    if(!"".equals(result)){
                        try{
                            String source = data.getStringExtra("source");
                            if(source.equals("BaiDuAI")||source.equals("AppScanning")){
                                webView.loadUrl(WEB_SITE_SCAN+"AppScanReceive.aspx?para="+ URLEncoder.encode(result,"UTF-8"));
                            }else if(source.equals("AppScaningProj")){
                                webView.loadUrl(WEB_SITE_SCAN+"AppScanProjValue.aspx?para="+ URLEncoder.encode(result,"UTF-8"));
                            }

                        }catch (java.io.UnsupportedEncodingException e){
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        String over = data.getStringExtra("over");
                        if(over.equals("yes")){
                            Intent faceIntent = new Intent(MainActivity.this, FaceDetectExpActivity.class);
                            faceIntent.putExtra("idnumber", idnumber);
                            startActivityForResult(faceIntent, BaiDuAI);
                        }
                    }
                }
                break;
            case BaiDuAI:
                if(resultCode==RESULT_OK){
                    String opt = data.getStringExtra("opt");
                    if(opt.equals("facesuccess")) {
                        webView.loadUrl(WEB_SITE_SCAN+"SaveScan.aspx?parakey="+parakey);
                    }else if(opt.equals("restartbaiduai")){
                        Intent faceIntent = new Intent(MainActivity.this, FaceDetectExpActivity.class);
                        faceIntent.putExtra("idnumber", idnumber);
                        faceIntent.putExtra("timeout", data.getIntExtra("timeout",0));
                        startActivityForResult(faceIntent, BaiDuAI);
                    }else if(opt.equals("restartscancode")){
                        requestPermissions(REQUEST_CAMERA_3, android.Manifest.permission.CAMERA);
                    }
                }
                break;
            case UploadPhoto:
                if(resultCode==RESULT_OK) {
                    webView.loadUrl(WEB_SITE_SCAN + "MiddleFaceVilidate.aspx?parakey=" + parakey);
                }
                break;
            case FILECHOOSER_RESULTCODE:
                if(resultCode == android.app.Activity.RESULT_CANCELED){
                    Log.d(TAG, "onActivityResult: ");
                    if(mUploadMessage!=null){
                        mUploadMessage.onReceiveValue(null);
                        mUploadMessage=null;
                    }
                    if(mValueCallback!=null) {
                        mValueCallback.onReceiveValue(null);
                        mValueCallback = null;
                    }

                }else{
                    if (mUploadMessage != null) {//5.0以下
                        Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                        if (result == null && data == null && resultCode == RESULT_OK) {
                            File cameraFile = new File(mCameraFilePath);
                            if (cameraFile.exists()) {
                                result = Uri.fromFile(cameraFile);
                                // Broadcast to the media scanner that we have a new photo
                                // so it will be added into the gallery for the user.
                                sendBroadcast(
                                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                            }
                        }
                        mUploadMessage.onReceiveValue(result);
                        mUploadMessage = null;
                    } else if (mValueCallback != null) {//5.0+
                        Uri[] uris = new Uri[1];
                        uris[0] = data == null || resultCode != RESULT_OK ? null : data.getData();
                        if (uris[0] == null && data == null && resultCode == RESULT_OK) {
                            File cameraFile = new File(mCameraFilePath);
                            if (cameraFile.exists()) {
                                uris[0] = Uri.fromFile(cameraFile);
                                // Broadcast to the media scanner that we have a new photo
                                // so it will be added into the gallery for the user.
                                sendBroadcast(
                                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uris[0]));
                            }
                        }
                        if (uris[0]!=null){
                            mValueCallback.onReceiveValue(uris);
                        }else{
                            mValueCallback.onReceiveValue(null);
                        }
                        mValueCallback = null;
                    }
                }
                break;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }
    private int selectImgMax = 1;//选取图片最大数量
    private int photosType = 0;//图库类型
    private String mCameraFilePath="";

    public class MyChromeClient extends WebChromeClient{
        private int mOriginalOrientation = 1;
        private int dip2px(Context context, float dipValue)
        {
            Resources r = context.getResources();
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            //super.onReceivedTitle(view, title);
            //Log.d(TAG, "onReceivedTitle: "+title);
            if(!"".equals(title)&&title.toLowerCase().contains("网页无法打开")){
                showerror("网页无法打开，请检查您的网络",view.getUrl());
                //Log.d(TAG, "onReceivedTitle: "+view.getUrl());
            }
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if(myView != null){
                callback.onCustomViewHidden();
                return;
            }
            //frameLayout.removeView(webView);
            webView.setVisibility(View.GONE);
            frameLayout.addView(view);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
            lp.width=dip2px(MainActivity.this,frameLayout.getWidth());
            lp.height=dip2px(MainActivity.this,frameLayout.getHeight());
            view.setLayoutParams(lp);
            myView = view;
            myCallback = callback;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            //全屏
            setFullScreen();
            chromeClient=this;
        }
        @Override
        public void onHideCustomView() {
            if(myView == null){
                return;
            }

            if(myCallback!=null) {
                myCallback.onCustomViewHidden();
                myCallback=null;
            }

            frameLayout.removeView(myView);
            myView = null;
            //frameLayout.addView(webView);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            webView.setVisibility(View.VISIBLE);
            //退出全屏
            quitFullScreen();
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            // TODO Auto-generated method stub
            Log.d(TAG, consoleMessage.message()+" at "+consoleMessage.sourceId()+":"+consoleMessage.lineNumber());
            return super.onConsoleMessage(consoleMessage);
        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback uploadMsg) {
            mUploadMessage = uploadMsg;
            selectImgMax = 1;
            goToPhotos(selectImgMax);
        }


        //3.0--版本
        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            openFileChooser(uploadMsg);
        }

        // For Android 4.1

        public void openFileChooser(ValueCallback uploadMsg, String acceptType, String capture) {
            openFileChooser(uploadMsg);
        }
        // For Android > 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mValueCallback = filePathCallback;
            selectImgMax = selectImgMax > 1 ? selectImgMax : 1;
            goToPhotos(selectImgMax);
            return true;
        }
        private void goToPhotos(int select_image_max) {
            /*
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            */
            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    // 检查是否有权限
                    int hasPer = checkSelfPermission(android.Manifest.permission.CAMERA);
                    if (hasPer != PackageManager.PERMISSION_GRANTED) {
                        // 是否应该显示权限请求
                        boolean isShould = shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA);
                        if(isShould){
                            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_2);
                        }else{
                            Toast.makeText(MainActivity.this, "请打开掌上华医的摄像头权限", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            Log.d(TAG, "goToPhotos: ...");
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");

            Intent chooser = createChooserIntent(createCameraIntent());
            chooser.putExtra(Intent.EXTRA_INTENT, i);

            MainActivity.this.startActivityForResult(chooser, FILECHOOSER_RESULTCODE);
        }

        private Intent createChooserIntent(Intent... intents) {
            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
            chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
            return chooser;
        }

        private Intent createCameraIntent() {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File externalDataDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM);
            File cameraDataDir = new File(externalDataDir.getAbsolutePath() +
                    File.separator + "browser-photos");
            cameraDataDir.mkdirs();
            mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator +
                    System.currentTimeMillis() + ".jpg";
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
            return cameraIntent;
        }
    }

    //public void addJavaScriptMap(Object obj, String objName){
    //    webView.addJavascriptInterface(obj, objName);
    //}

    public void SetJPushTag(){
        JPushInterface.setAlias(getApplicationContext(),1,appid);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection=null;
                try {
                    URL url = new URL(WEB_SITE+"Account/tagTest/"+appid);
                    httpURLConnection=(HttpURLConnection)url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setRequestMethod("GET");
                    int responsecode = httpURLConnection.getResponseCode();

                    if(responsecode == HttpURLConnection.HTTP_OK){
                        InputStream is = httpURLConnection.getInputStream();
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        int len=0;
                        byte buffer[] = new byte[1024];
                        while ((len = is.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        is.close();
                        os.close();
                        String result = new String(os.toByteArray());
                        if(result.length()>0){
                            final JSONObject json = new JSONObject(result);
                            String str = json.getString("data");

                            if(str.length()>0){ //设置标签
                                Log.d(TAG, "run: "+str);
                                String[] arr = str.split(",");
                                Set<String> tags = new LinkedHashSet<String>();
                                for (int i = 0;i<arr.length;i++) {
                                    if (isValidTagAndAlias(arr[i])) {
                                        tags.add(arr[i]);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "标签【" + arr[i] + "】无效", Toast.LENGTH_LONG).show();
                                    }

                                }
                                tags = JPushInterface.filterValidTags(tags);
                                JPushInterface.setTags(getApplicationContext(),2,tags);

                            }else{
                                JPushInterface.cleanTags(getApplicationContext(),3);
                            }
                        }
                    }
                    httpURLConnection.disconnect();

                }catch(Exception ex){
                    if(null!=httpURLConnection) {
                        httpURLConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    // 校验Tag Alias 只能是数字,英文字母和中文
    public boolean isValidTagAndAlias(String s) {
        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_!@#$&*+=.|]+$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public void CheckUpdate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection=null;
                try {
                    PackageManager pm = getPackageManager();
                    PackageInfo info = pm.getPackageInfo(getPackageName(),0);
                    Log.d(TAG,info.versionCode+"");
                    URL url = new URL(WEB_SITE+"home/CheckVersion?versioncode="+info.versionCode);
                    httpURLConnection=(HttpURLConnection)url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setRequestMethod("GET");
                    int responsecode = httpURLConnection.getResponseCode();

                    if(responsecode == HttpURLConnection.HTTP_OK){
                        InputStream is = httpURLConnection.getInputStream();
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        int len=0;
                        byte buffer[] = new byte[1024];
                        while ((len = is.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        is.close();
                        os.close();
                        String result = new String(os.toByteArray());
                        if(result.length()>0){
                            final JSONObject json = new JSONObject(result);
                            if(json.getInt("update")==1){ //有更新
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("发现新版本"+json.getString("new_version"))
                                                    .setMessage(json.getString("update_log"))
                                                    .setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog,
                                                                            int which) {
                                                            dialog.dismiss();
                                                            pd=new ProgressDialog(MainActivity.this);
                                                            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                                            pd.setTitle("下载中...");
                                                            pd.setCancelable(false);
                                                            pd.show();
                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    final File file = DownAPK(json);
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            pd.dismiss();
                                                                            InstallApk(file);
                                                                        }
                                                                    });
                                                                }
                                                            }).start();
                                                        }
                                                    })
                                                    .setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog,
                                                                            int whichButton) {
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .setCancelable(false)
                                                    .create();

                                            dialog.show();
                                        }catch (Exception ex){
                                            Log.d(TAG,ex.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    }
                    httpURLConnection.disconnect();

                }catch(Exception ex){
                    if(null!=httpURLConnection) {
                        httpURLConnection.disconnect();
                    }
                }
            }
        }).start();
    }
    private File DownAPK(JSONObject json){
        try{
            URL url = new URL(json.getString("apk_url"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if ( code == 200 ) {
                int fileSize = conn.getContentLength()/1024;
                pd.setMax(fileSize);
                int total = 0;
                InputStream is = conn.getInputStream();
                File file = new File(this.getExternalCacheDir(),"hyapp_"+json.getString("new_version")+".apk");
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ( (len = is.read(buffer)) != -1 ) {
                    fos.write(buffer, 0, len);
                    total += (len/1024);
                    pd.setProgress(total);
                }
                fos.flush();
                fos.close();
                is.close();
                return file;
            }else{
                return null;
            }
        }catch (Exception ex){
            Log.d(TAG,ex.getMessage());
            return null;
        }
    }
    private void InstallApk(File file){
        if(null==file){return;}
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    //设置全屏
    public void setFullScreen(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT>=14){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }
    //退出全屏
    public void quitFullScreen(){
        final WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(attrs);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }


    public class JavascriptHandler{
        @JavascriptInterface
        public void show(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingView.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.INVISIBLE);
                }
            });
        }
        @JavascriptInterface
        public void hide(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingView.setVisibility(View.INVISIBLE);
                    webView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    //判断是否打开通知栏权限
    @SuppressLint("NewApi")
    private boolean isNotificationEnabled(Context context) {
        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        Class appOpsClass = null;
    /* Context.APP_OPS_MANAGER */
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (Integer) opPostNotificationValue.get(Integer.class);
            if ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED){
                //Toast.makeText(this,"true",Toast.LENGTH_SHORT).show();
            }else {
                //Toast.makeText(this,"false",Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("\"通知\"可能包括提醒、声音和图标标记。这些可在\"设置\"中配置。");
                builder.setTitle("\"掌上华医\"想给您发送通知");
                builder.setPositiveButton("允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToSet();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("不允许",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void goToSet(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            // 进入设置系统应用权限界面
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 运行系统在5.x环境使用
            // 进入设置系统应用权限界面
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
            return;
        }
    }




}