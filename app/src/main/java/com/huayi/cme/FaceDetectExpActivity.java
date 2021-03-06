package com.huayi.cme;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.baidu.idl.face.platform.FaceStatusEnum;
import com.baidu.idl.face.platform.ui.FaceDetectActivity;
import com.huayi.cme.models.ResponseResult;
import com.huayi.cme.parser.ResponseParser;
import com.huayi.cme.utils.HttpUtil;
import com.huayi.cme.utils.OnResultListener;
import java.util.HashMap;
import java.util.Timer;

/**
 * Created by mac on 2018/2/5.
 */

public class FaceDetectExpActivity extends FaceDetectActivity {
    private DefaultDialog mDefaultDialog;
    public static String TAG = "mylog";
    private CountDownTimer timer;
    private int timeout;
    private String parakey;
    private CountDownTimer timer2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HttpUtil.getInstance().init();
        timeout = getIntent().getIntExtra("timeout",0);
        timer2 = new CountDownTimer((timeout + 1) * 1000 , 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                //txt_countdown.setText("倒计时:"+(totalseconds--)+"秒");
                FaceDetectExpActivity.super.mTipsBottomTimeoutView.setText("倒计时:"+(--timeout)+"秒");
                if(timeout==0){
                    setResultIntent("restartscancode");
                    finish();
                }
            }

            @Override
            public void onFinish() {
                setResultIntent("restartscancode");
                finish();
            }
        };
        timer2.start();
    }

    @Override
    public void onDetectCompletion(FaceStatusEnum status, String message, HashMap<String, String> base64ImageMap) {
        super.onDetectCompletion(status, message, base64ImageMap);
        if (status == FaceStatusEnum.OK && mIsCompletion) {
            //showMessageDialog("人脸图像采集", "采集成功");
            //super.mTipsTopView.setText("检测成功");
            timer2.cancel();
            String bestimageBase64 = base64ImageMap.get("bestImage0");
            final String idnumber = getIntent().getStringExtra("idnumber");
            ResponseParser parser = new ResponseParser();
            HttpUtil.getInstance().uploadPhoto(((MyApplication)getApplication()).getWebSiteAI()+"api/UploadPhotoClient",idnumber,bestimageBase64,parser,new OnResultListener<ResponseResult>(){
                @Override
                public void onResult(final ResponseResult result) {
                    switch (result.getErrorCode()){
                        case 0:
                            FaceDetectExpActivity.super.mTipsTopView.setText("正在验证身份，请稍侯...");
                            timer = new CountDownTimer(30 * 60 * 1000 ,3 * 1000){
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    ResponseParser parser = new ResponseParser();
                                    HttpUtil.getInstance().queryPhotoValid(((MyApplication) getApplication()).getWebSiteAI() + "api/QueryPhotoValidateAjax", idnumber, parser, new OnResultListener<ResponseResult>() {
                                        @Override
                                        public void onResult(ResponseResult result2) {
                                            switch (result2.getErrorCode()){
                                                case 1:
                                                    timer.cancel();
                                                    showMessageDialog("系统提示", result2.getErrMsg());
                                                    break;
                                                case 2:
                                                    FaceDetectExpActivity.super.mTipsTopView.setText("前方排队人数"+result2.getErrMsg()+"，请耐心等待...");
                                                    break;
                                                case 3:
                                                    FaceDetectExpActivity.super.mTipsTopView.setText("正在处理，请稍侯...");
                                                    break;
                                                case 4:
                                                    timer.cancel();
                                                    setResultIntent("facesuccess");
                                                    finish();
                                                    break;
                                                case 5:
                                                    timer.cancel();
                                                    showMessageDialog("系统提示", result2.getErrMsg(),true);
                                                    break;
                                                case 6:
                                                    break;
                                                case 7:
                                                    timer.cancel();
                                                    showMessageDialog("系统提示", "人脸识别失败");
                                                    break;
                                                default:
                                                    timer.cancel();
                                                    showMessageDialog("系统错误", result2.getErrMsg());
                                                    break;
                                            }
                                        }

                                        @Override
                                        public void onError(ResponseResult error2) {
                                            Log.d(TAG, "onError2: "+error2.getErrMsg());
                                            showMessageDialog("系统错误", "系统错误，请重试");
                                        }
                                    });

                                }

                                @Override
                                public void onFinish() {
                                    showMessageDialog("系统提示", "验证超时");
                                }
                            };
                            timer.start();
                            break;
                        case 1:
                            showMessageDialog("系统提示", result.getErrMsg());
                            break;
                        case 3:
                            showMessageDialog("系统提示", "您还未进行人脸建档，请进行认证。");
                            break;
                        default:
                            showMessageDialog("系统提示", result.getErrMsg());
                            break;
                    }
                }

                @Override
                public void onError(ResponseResult error) {
                    Log.d(TAG, "onError: "+error.getErrMsg());
                    showMessageDialog("系统错误", "系统错误，请重试");
                }
            });
        } else if (status == FaceStatusEnum.Error_DetectTimeout ||
                status == FaceStatusEnum.Error_LivenessTimeout ||
                status == FaceStatusEnum.Error_Timeout) {
            showMessageDialog("人脸图像采集", "采集超时");
        }
    }

    private void showMessageDialog(String title, final String message) {

        showMessageDialog(title,message,false);
    }

    private void showMessageDialog(String title, final String message, final boolean isrestartbaiduai) {

        if (mDefaultDialog == null) {
            DefaultDialog.Builder builder = new DefaultDialog.Builder(this);
            builder.setTitle(title).
                    setMessage(message).
                    setNegativeButton("确认",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mDefaultDialog.dismiss();

                                    //如果验证未通过，点击知道了之后继续人脸识别
                                    if(isrestartbaiduai){
                                        setResultIntent("restartbaiduai");
                                    }
                                    finish();
                                }
                            });
            mDefaultDialog = builder.create();
            mDefaultDialog.setCancelable(true);
        }
        mDefaultDialog.dismiss();
        mDefaultDialog.show();
    }

    private void setResultIntent(String opt){
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("opt", opt);
        bundle.putString("parakey",parakey);
        bundle.putInt("timeout",timeout);
        resultIntent.putExtras(bundle);
        FaceDetectExpActivity.this.setResult(RESULT_OK, resultIntent);
    }
    @Override
    public void finish() {
        super.finish();
    }
}
