package com.hy.www;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.umeng.analytics.MobclickAgent;
import com.zxing.activity.CaptureActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private GifImageView loadingView;
    public static final int SCAN_CODE=1;
    public static final int FILECHOOSER_RESULTCODE=2;
    public static final String WEB_SITE="http://zshy.91huayi.com/";
    public static final String WEB_SITE_SCAN="http://app.kjpt.91huayi.com/";
    private String appid="";

    private WebChromeClient.CustomViewCallback myCallback = null;
    private RelativeLayout frameLayout = null;
    private WebChromeClient chromeClient = null;
    private View myView = null;
    private WebChromeClient.CustomViewCallback myCallBack = null;
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //检查版本更新
        CheckUpdate();
        //发送版本号给服务端
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection=null;
                try {
                    URL url = new URL(WEB_SITE + "Home/android_version?version=" + Build.VERSION.SDK_INT + "&flag=1");
                    httpURLConnection=(HttpURLConnection)url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setRequestMethod("GET");
                    int responsecode = httpURLConnection.getResponseCode();

                    if(responsecode == HttpURLConnection.HTTP_OK){
                        httpURLConnection.getInputStream();
                    }
                    httpURLConnection.disconnect();
                }catch(Exception ex){
                    if(null!=httpURLConnection) {
                        httpURLConnection.disconnect();
                    }
                    Log.d("mylog",ex.getMessage());
                }
            }
        }).start();

        appid=Installation.id(this);

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
        settings.setSupportMultipleWindows(true);
        //settings.setPluginState(WebSettings.PluginState.ON);
        settings.setLoadWithOverviewMode(true);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.addJavascriptInterface(new JavascriptHandler(),"apploading");
        if(Build.VERSION.SDK_INT<23) {
            chromeClient = new MyChromeClient();
            webView.setWebChromeClient(chromeClient);
        }
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
                    //Log.v("mylog",e.getMessage());
                }
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if ((WEB_SITE_SCAN+"AppScan.aspx").equalsIgnoreCase(url)){
                    Intent intent=new Intent(MainActivity.this, CaptureActivity.class);
                    startActivityForResult(intent,SCAN_CODE);
                }if((WEB_SITE_SCAN+"GetAppId.aspx").equalsIgnoreCase(url)){
                     view.loadUrl(WEB_SITE_SCAN+"GetAppIdReceive.aspx?para="+appid);
                }else {
                    view.loadUrl(url);
                }
                return true;
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
                Toast toast = Toast.makeText(getApplicationContext(),"数据加载失败,请检查网络状态",Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                //super.onReceivedError(view, request, error);
            }
        });
        webView.loadUrl(WEB_SITE+"m/index.html");
        //webView.loadUrl("http://z.puddingz.com/t.html");
        if(savedInstanceState != null){
            webView.restoreState(savedInstanceState);
        }

        /*
       if(Integer.parseInt(s)==0) {
            webView.loadUrl("http://yuyin.91huayi.net/m/#/login");
        }else{
            webView.loadUrl("http://yuyin.91huayi.net/m/index.html");
        }
        */
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
        super.onResume();
        webView.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
        MobclickAgent.onPause(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case SCAN_CODE:
                if(resultCode==RESULT_OK){
                    String result = data.getStringExtra("result");
                    if(!"".equals(result)){
                        try{
                            webView.loadUrl(WEB_SITE_SCAN+"AppScanReceive.aspx?para="+ URLEncoder.encode(result,"UTF-8"));
                        }catch (java.io.UnsupportedEncodingException e){
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        webView.loadUrl(WEB_SITE_SCAN+"AppScanning.aspx");
                    }
                }
                break;
            case FILECHOOSER_RESULTCODE:
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
                    }
                    mValueCallback = null;
                }
                break;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    //public void addJavaScriptMap(Object obj, String objName){
    //    webView.addJavascriptInterface(obj, objName);
    //}

    public void CheckUpdate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection=null;
                try {
                    PackageManager pm = getPackageManager();
                    PackageInfo info = pm.getPackageInfo(getPackageName(),0);
                    URL url = new URL("http://z.puddingz.com/t.aspx?versioncode="+info.versionCode);
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
                                            Log.d("mylog",ex.getMessage());
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
                File file = new File(Environment.getExternalStorageDirectory(),"hyapp_"+json.getString("new_version")+".apk");
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
            Log.d("mylog",ex.getMessage());
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

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mValueCallback;
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
            myCallBack = callback;
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
                myCallBack.onCustomViewHidden();
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
            Log.d("ZR", consoleMessage.message()+" at "+consoleMessage.sourceId()+":"+consoleMessage.lineNumber());
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
}