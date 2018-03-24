package com.huayi.cme;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import android.widget.TextView;


import com.baidu.idl.face.platform.utils.FileUtils;
import com.huayi.cme.models.ResponseResult;
import com.huayi.cme.parser.ResponseParser;
import com.huayi.cme.utils.HttpUtil;
import com.huayi.cme.utils.OnResultListener;
import com.huayi.cme.utils.UploadUtil.OnUploadProcessListener;
import com.huayi.cme.utils.UploadUtil;
import com.huayi.cme.utils.Util;


public class UploadActivity extends AppCompatActivity implements OnClickListener,OnUploadProcessListener {

    public static final String TAG = "mylog"; //日志输出标记
    /**
     * 去上传文件
     */
    protected static final int TO_UPLOAD_FILE = 1;
    /**
     * 上传文件响应
     */
    protected static final int UPLOAD_FILE_DONE = 2; //
    /**
     * 选择文件
     */
    public static final int TO_SELECT_PHOTO = 3;
    /**
     * 上传初始化
     */
    private static final int UPLOAD_INIT_PROCESS = 4;
    /**
     * 上传中
     */
    private static final int UPLOAD_IN_PROCESS = 5;
    private ProgressDialog progressDialog;

    private Button btn_select,btn_upload;
    private ImageView img_photo;
    private ImageView detect_close;
    private LinearLayout tip_box;
    private TextView tip_msg;
    private ImageView tip_img;
    private String picPath = null;
    //private ProgressBar progressBar;
    //private TextView uploadImageResult;

    private String requestURL;
    private String token;
    private String cert_id;
    private String parakey;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HttpUtil.getInstance().init();

        setContentView(R.layout.activity_upload);

        //uploadImageResult = (TextView) findViewById(R.id.uploadImageResult);
        btn_select = (Button)findViewById(R.id.btn_select);
        btn_upload = (Button)findViewById(R.id.btn_upload);
        img_photo = (ImageView)findViewById(R.id.img_photo);
        detect_close = (ImageView)findViewById(R.id.detect_close);
        tip_box = (LinearLayout)findViewById(R.id.tip_box);
        tip_msg = (TextView)findViewById(R.id.tip_msg);
        tip_img = (ImageView)findViewById(R.id.tip_img);
        btn_select.setOnClickListener(this);
        btn_upload.setOnClickListener(this);
        detect_close.setOnClickListener(this);

        btn_select.setVisibility(View.INVISIBLE);
        btn_upload.setVisibility(View.INVISIBLE);

        progressDialog = new ProgressDialog(this);
        //progressBar = (ProgressBar) findViewById(R.id.progressBar1);

        MyApplication app = (MyApplication) this.getApplication();

        requestURL = app.getWebSitePhoto()+"api/CertUploadAjaxApp";

        String tip_msg_txt = getIntent().getStringExtra("tip_msg");
        if(!(tip_msg_txt == null || tip_msg_txt.equals(""))){
            tip_msg.setText(tip_msg_txt);
        }else{
            tip_box.setVisibility(View.INVISIBLE);
            tip_img.setVisibility(View.INVISIBLE);
        }

        token = getIntent().getStringExtra("token");
        cert_id = getIntent().getStringExtra("cert_id");
        parakey = getIntent().getStringExtra("parakey");
        //获取已传图片
        getPhoto(app.getWebSitePhoto()+"api/GetImage/?id="+cert_id+"&token="+token);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_select){
            Intent intent = new Intent(this,SelectPicActivity.class);
            startActivityForResult(intent, TO_SELECT_PHOTO);
        }else if(v.getId() == R.id.btn_upload) {
            if(picPath!=null)
            {
                handler.sendEmptyMessage(TO_UPLOAD_FILE);
            }else{
                Toast.makeText(this, "请选择照片", Toast.LENGTH_LONG).show();
            }
        }else if(v.getId() == R.id.detect_close){
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: "+requestCode);
        if (resultCode == Activity.RESULT_OK && requestCode == TO_SELECT_PHOTO){
            picPath = data.getStringExtra(SelectPicActivity.KEY_PHOTO_PATH);
            Log.i(TAG, "最终选择的图片="+picPath);

            compressImage();
            Bitmap bm = BitmapFactory.decodeFile(picPath);
            img_photo.setImageBitmap(bm);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void compressImage(){
        //压缩照片大小
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 10;  // 图片的大小设置为原来的十分之一
        Bitmap bm = BitmapFactory.decodeFile(picPath,options);
        options = null;

        //获取角度
        int angle = readPictureDegree(picPath);

        //旋转
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap returnBm = null;
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }


        String targetPath = Util.getCacheDirectory(this,"") + "compressPic.jpg";
        File outputFile=new File(targetPath);
        try {
            if (!outputFile.exists()) {
                outputFile.getParentFile().mkdirs();
                //outputFile.createNewFile();
            }else{
                outputFile.delete();
            }
            FileOutputStream out = new FileOutputStream(outputFile);
            //质量压缩
            returnBm.compress(Bitmap.CompressFormat.JPEG, 90, out);
        }catch (Exception e){}
        picPath = outputFile.getPath();
    }

    private int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    @Override
    public void onUploadDone(int responseCode, String message) {
        progressDialog.dismiss();
        Message msg = Message.obtain();
        msg.what = UPLOAD_FILE_DONE;
        msg.arg1 = responseCode;
        msg.obj = message;
        handler.sendMessage(msg);
    }

    private void toUploadFile()
    {
        //uploadImageResult.setText("正在上传中...");
        progressDialog.setMessage("正在上传文件,请稍候...");
        progressDialog.show();
        String fileKey = "photo";
        UploadUtil uploadUtil = UploadUtil.getInstance();;
        uploadUtil.setOnUploadProcessListener(this);  //设置监听器监听上传状态
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("cert_id", cert_id);
        uploadUtil.uploadFile( picPath,fileKey, requestURL,params);
    }

    @Override
    public void onUploadProcess(int uploadSize) {
        Message msg = Message.obtain();
        msg.what = UPLOAD_IN_PROCESS;
        msg.arg1 = uploadSize;
        handler.sendMessage(msg );
    }

    @Override
    public void initUpload(int fileSize) {
        Message msg = Message.obtain();
        msg.what = UPLOAD_INIT_PROCESS;
        msg.arg1 = fileSize;
        handler.sendMessage(msg );
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TO_UPLOAD_FILE:
                    toUploadFile();
                    break;

                case UPLOAD_INIT_PROCESS:
                    //progressBar.setMax(msg.arg1);
                    break;
                case UPLOAD_IN_PROCESS:
                    //progressBar.setProgress(msg.arg1);
                    break;
                case UPLOAD_FILE_DONE:
                    //String result = "响应码："+msg.arg1+"\n响应信息："+msg.obj+"\n耗时："+UploadUtil.getRequestTime()+"秒";
                    if(msg.arg1 == UploadUtil.UPLOAD_SUCCESS_CODE){
                        Log.d(TAG, "handleMessage: "+msg.obj.toString());
                        ResponseResult res = new ResponseParser().parse(msg.obj.toString());
                        if(res.getErrorCode() == 0){
                            //上传成功 启动轮询
                            validCertUpload();

                        }else{
                            Toast.makeText(getApplication().getBaseContext(), res.getErrMsg(), Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(getApplication().getBaseContext(), "上传失败，请重试", Toast.LENGTH_LONG).show();
                    }
                    //uploadImageResult.setText(result);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

    private Handler hander2 = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: "+msg);
            if (msg.what == 0x0001) {
                if(null != msg.obj){
                    Bitmap bm = (Bitmap) msg.obj;
                    img_photo.setImageBitmap(bm);
                }else{
                    btn_select.setVisibility(View.VISIBLE);
                    btn_upload.setVisibility(View.VISIBLE);
                }

            }
            return false;
        }
    });

    private void getPhoto(final String photopath){
        Log.d(TAG, "getPhoto: "+photopath);
        new Thread(new Runnable() {  //开启线程上传文件
            @Override
            public void run() {
                HttpURLConnection conn= null;
                InputStream is= null;
                InputStreamReader istr= null;
                BufferedReader br= null;
                try {
                    //1.定义一个URL对象
                    URL url = new URL(photopath);
                    //2.通过url获取一个HttpURLConnection对象
                    conn = (HttpURLConnection) url.openConnection();
                    //3.设置请求方式
                    conn.setRequestMethod("GET");
                    //4.设置超时时间
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    //5.判断响应码200
                    Log.d(TAG, "run: notnull4:pk->"+HttpURLConnection.HTTP_OK+"->"+conn.getResponseCode());
                    if (conn.getResponseCode() ==
                            201) {
                        //6.获取到网络输入的字符流
                        is = conn.getInputStream();
                        Bitmap bm = null;
                        Log.d(TAG, "run: notnull2");
                        if(null != is){
                            Log.d(TAG, "run: notnull1");
                            bm = BitmapFactory.decodeStream(is);
                        }
                        //8.将获取的数据发送到主线程中去
                        Message msg = hander2.obtainMessage();
                        msg.what = 0x0001;
                        msg.obj = bm;
                        hander2.sendMessage(msg);

                    }else{
                        Message msg = hander2.obtainMessage();
                        msg.what = 0x0001;
                        msg.obj = null;
                        hander2.sendMessage(msg);
                    }

                    //9.释放资源
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }finally{
                    try {
                        if(br != null){

                            br.close();
                        }
                        if(istr != null){

                            istr.close();
                        }
                        if(is!= null){

                            is.close();
                        }
                        if(conn!= null){

                            conn.disconnect();
                        }

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    private void validCertUpload(){
        progressDialog.setMessage("正在处理，请稍侯...");
        progressDialog.show();
        timer = new CountDownTimer(30 * 60 * 1000 ,3 * 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                ResponseParser parser = new ResponseParser();
                HttpUtil.getInstance().queryCertValid(((MyApplication) getApplication()).getWebSitePhoto() + "api/CertUploadIndexAjax", token, cert_id, parser, new OnResultListener<ResponseResult>() {
                    @Override
                    public void onResult(ResponseResult result2) {
                        switch (result2.getErrorCode()){
                            case 1:
                                timer.cancel();
                                progressDialog.dismiss();
                                Toast.makeText(getApplication().getBaseContext(), result2.getErrMsg(), Toast.LENGTH_LONG).show();
                                break;
                            case 2:
                                progressDialog.setMessage("前方排队人数"+result2.getErrMsg()+"，请耐心等待...");
                                break;
                            case 3:
                                progressDialog.setMessage("人脸建档进行中，请耐心等待");
                                break;
                            case 4:
                                timer.cancel();
                                progressDialog.dismiss();
                                if(parakey!=null && !parakey.equals("2")){
                                    Intent resultIntent2 = new Intent();
                                    Bundle bundle2 = new Bundle();
                                    bundle2.putString("parakey", parakey);
                                    resultIntent2.putExtras(bundle2);
                                    UploadActivity.this.setResult(RESULT_OK, resultIntent2);
                                    finish();
                                }else{
                                    Toast.makeText(getApplication().getBaseContext(), "人脸建档成功", Toast.LENGTH_LONG).show();
                                }

                                break;
                            case 5:
                                timer.cancel();
                                progressDialog.dismiss();
                                Toast.makeText(getApplication().getBaseContext(), result2.getErrMsg(), Toast.LENGTH_LONG).show();
                                break;
                            case 6:
                                progressDialog.setMessage("人脸建档进行中，请耐心等待");
                                break;
                            default:
                                timer.cancel();
                                progressDialog.dismiss();
                                Toast.makeText(getApplication().getBaseContext(), result2.getErrMsg(), Toast.LENGTH_LONG).show();
                                break;
                        }
                    }

                    @Override
                    public void onError(ResponseResult error2) {
                        Log.d(TAG, "onError2: "+error2.getErrMsg());
                        timer.cancel();
                        progressDialog.dismiss();
                        Toast.makeText(getApplication().getBaseContext(), error2.getErrMsg(), Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onFinish() {
                timer.cancel();
                progressDialog.dismiss();
                Toast.makeText(getApplication().getBaseContext(), "验证超时", Toast.LENGTH_LONG).show();
            }
        };
        timer.start();
    }
}
