package com.huayi.cme;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.baidu.idl.face.platform.FaceStatusEnum;
import com.baidu.idl.face.platform.ui.FaceLivenessActivity;
import com.huayi.cme.models.ResponseResult;
import com.huayi.cme.parser.ResponseParser;
import com.huayi.cme.utils.HttpUtil;
import com.huayi.cme.utils.OnResultListener;

import java.util.HashMap;
import java.util.Timer;

/**
 * Created by mac on 2018/1/28.
 */

public class FaceLivenessExpActivity extends FaceLivenessActivity {
    private DefaultDialog mDefaultDialog;
    public static String Tag = "mylog";

    private CountDownTimer timer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HttpUtil.getInstance().init();
    }

    @Override
    public void onLivenessCompletion(FaceStatusEnum status, String message, HashMap<String, String> base64ImageMap) {
        super.onLivenessCompletion(status, message, base64ImageMap);
        if (status == FaceStatusEnum.OK && mIsCompletion) {
            //showMessageDialog("活体检测", "检测成功");
            super.mTipsTopView.setText("检测成功");
            String bestimageBase64 = base64ImageMap.get("bestImage0");
            final String idnumber = getIntent().getStringExtra("idnumber");
            ResponseParser parser = new ResponseParser();
            HttpUtil.getInstance().uploadPhoto(((MyApplication)getApplication()).getWebSiteAI()+"FaceRecognition/api/UploadPhotoClient",idnumber,bestimageBase64,parser,new OnResultListener<ResponseResult>(){
                @Override
                public void onResult(final ResponseResult result) {
                    switch (result.getErrorCode()){
                        case 0:
                            FaceLivenessExpActivity.super.mTipsTopView.setText("正在验证身份，请稍侯...");
                            timer = new CountDownTimer(30 * 60 * 1000 ,3 * 1000){
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    ResponseParser parser = new ResponseParser();
                                    HttpUtil.getInstance().queryPhotoValid(((MyApplication) getApplication()).getWebSiteAI() + "FaceRecognition/api/QueryPhotoValidateAjax", idnumber, parser, new OnResultListener<ResponseResult>() {
                                        @Override
                                        public void onResult(ResponseResult result2) {
                                            switch (result2.getErrorCode()){
                                                case 1:
                                                    timer.cancel();
                                                    showMessageDialog("系统提示", result2.getErrMsg());
                                                    break;
                                                case 2:
                                                    FaceLivenessExpActivity.super.mTipsTopView.setText("前方排队人数"+result2.getErrMsg()+"，请耐心等待...");
                                                    break;
                                                case 3:
                                                    FaceLivenessExpActivity.super.mTipsTopView.setText("正在处理，请稍侯...");
                                                    break;
                                                case 4:
                                                    timer.cancel();
                                                    Intent resultIntent = new Intent();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("result", "");
                                                    resultIntent.putExtras(bundle);
                                                    FaceLivenessExpActivity.this.setResult(RESULT_OK, resultIntent);
                                                    finish();
                                                    break;
                                                case 5:
                                                    timer.cancel();
                                                    showMessageDialog("系统提示", "验证未通过");
                                                    break;
                                                default:
                                                    timer.cancel();
                                                    showMessageDialog("系统错误", result2.getErrMsg());
                                                    break;
                                            }
                                        }

                                        @Override
                                        public void onError(ResponseResult error2) {
                                            showMessageDialog("系统错误", "系统错误，请重试");
                                        }
                                    });
                                    /*
                                    Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
                                     */
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
                            showMessageDialog("系统提示", "身份证还未上传认证");
                            break;
                        default:
                            showMessageDialog("系统提示", result.getErrMsg());
                            break;
                    }
                }

                @Override
                public void onError(ResponseResult error) {
                    showMessageDialog("系统错误", "系统错误，请重试");
                }
            });
        } else if (status == FaceStatusEnum.Error_DetectTimeout ||
                status == FaceStatusEnum.Error_LivenessTimeout ||
                status == FaceStatusEnum.Error_Timeout) {
            showMessageDialog("活体检测", "采集超时");
        }
    }

    private void showMessageDialog(String title, String message) {
        if (mDefaultDialog == null) {
            DefaultDialog.Builder builder = new DefaultDialog.Builder(this);
            builder.setTitle(title).
                    setMessage(message).
                    setNegativeButton("确认",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mDefaultDialog.dismiss();
                                    finish();
                                }
                            });
            mDefaultDialog = builder.create();
            mDefaultDialog.setCancelable(true);
        }
        mDefaultDialog.dismiss();
        mDefaultDialog.show();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
