package com.hy.www;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zxing.activity.CaptureActivity;

import java.io.File;
import java.net.URLEncoder;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private GifImageView loadingView;
    public static final int SCAN_CODE=1;
    public static final int FILECHOOSER_RESULTCODE=2;
    public static final String WEB_SITE="http://yuyin.91huayi.net/";
    public static final String WEB_SITE_SCAN="http://mobile.kjpt.91huayi.com/";
    private String appid="";

    private WebChromeClient.CustomViewCallback myCallback = null;
    private RelativeLayout frameLayout = null;
    private WebChromeClient chromeClient = null;
    private View myView = null;
    private WebChromeClient.CustomViewCallback myCallBack = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        final String USER_AGENT_STRING = webView.getSettings().getUserAgentString() + " Rong/2.0";
        settings.setUserAgentString( USER_AGENT_STRING );
        settings.setSupportZoom(false);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setLoadWithOverviewMode(true);
        webView.addJavascriptInterface(new JavascriptHandler(),"apploading");
        chromeClient = new MyChromeClient();
        webView.setWebChromeClient(chromeClient);
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
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

    public void addJavaScriptMap(Object obj, String objName){
        webView.addJavascriptInterface(obj, objName);
    }

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mValueCallback;
    private int selectImgMax = 1;//选取图片最大数量
    private int photosType = 0;//图库类型
    private String mCameraFilePath="";

    public class MyChromeClient extends WebChromeClient{
        private int mOriginalOrientation = 1;

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if(myView != null){
                callback.onCustomViewHidden();
                return;
            }
            frameLayout.removeView(webView);
            frameLayout.addView(view);
            myView = view;
            myCallBack = callback;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        @Override
        public void onHideCustomView() {
            if(myView == null){
                return;
            }
            frameLayout.removeView(myView);
            myView = null;
            frameLayout.addView(webView);
            myCallBack.onCustomViewHidden();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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