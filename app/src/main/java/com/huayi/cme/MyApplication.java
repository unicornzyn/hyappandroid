package com.huayi.cme;

import android.app.Application;

import com.baidu.idl.face.platform.FaceConfig;
import com.baidu.idl.face.platform.FaceSDKManager;
import com.baidu.idl.face.platform.LivenessTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 2018/1/28.
 */

public class MyApplication extends Application {

    //url参数
    //production
    //private String webSite="http://zshy.91huayi.com/";
    //private String webSiteScan="http://app.kjpt.91huayi.com/";
    //private String webSiteWxpay="http://pay.91huayi.com/";
    //private String webSiteAI="http://111.204.10.248/";

    //dev
    private String webSite="http://zshytest.91huayi.net/";
    private String webSiteScan="http://mobile.kjpt.91huayi.com/";
    private String webSiteWxpay="http://zhifucme.91huayi.net/";
    private String webSiteAI="http://hydbapp.91huayi.net/";

    //百度人脸识别设置
    public static final float VALUE_BRIGHTNESS = 40.0F;
    public static final float VALUE_BLURNESS = 0.7F;
    public static final float VALUE_OCCLUSION = 0.6F;
    public static final int VALUE_HEAD_PITCH = 15;
    public static final int VALUE_HEAD_YAW = 15;
    public static final int VALUE_HEAD_ROLL = 15;
    public static final int VALUE_CROP_FACE_SIZE = 400;
    public static final int VALUE_MIN_FACE_SIZE = 120;
    public static final float VALUE_NOT_FACE_THRESHOLD = 0.6F;

    @Override
    public void onCreate() {
        super.onCreate();

        //设置百度人脸识别参数
        setBaiduAIConfig();
    }

    public String getWebSite(){
        return webSite;
    }

    public String getWebSiteScan(){
        return webSiteScan;
    }

    public String getWebSiteWxpay(){
        return webSiteWxpay;
    }

    public String getWebSiteAI(){
        return webSiteAI;
    }


    private void setBaiduAIConfig(){
        FaceSDKManager.getInstance().initialize(this, Config.licenseID, Config.licenseFileName);
        FaceConfig config = FaceSDKManager.getInstance().getFaceConfig();
        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整
        // 设置活体动作，通过设置list LivenessTypeEnum.Eye，LivenessTypeEnum.Mouth，LivenessTypeEnum.HeadUp，
        // LivenessTypeEnum.HeadDown，LivenessTypeEnum.HeadLeft, LivenessTypeEnum.HeadRight,
        // LivenessTypeEnum.HeadLeftOrRight
        List<LivenessTypeEnum> livenessList = new ArrayList<>();
        livenessList.add(LivenessTypeEnum.Mouth);
        livenessList.add(LivenessTypeEnum.Eye);
        livenessList.add(LivenessTypeEnum.HeadUp);
        livenessList.add(LivenessTypeEnum.HeadDown);
        livenessList.add(LivenessTypeEnum.HeadLeft);
        livenessList.add(LivenessTypeEnum.HeadRight);
        livenessList.add(LivenessTypeEnum.HeadLeftOrRight);
        config.setLivenessTypeList(livenessList);
        // 设置 活体动作是否随机 boolean
        config.setLivenessRandom(true);
        config.setLivenessRandomCount(2);
        // 模糊度范围 (0-1) 推荐小于0.7
        config.setBlurnessValue(VALUE_BLURNESS);
        // 光照范围 (0-1) 推荐大于40
        config.setBrightnessValue(VALUE_BRIGHTNESS);
        // 裁剪人脸大小
        config.setCropFaceValue(VALUE_CROP_FACE_SIZE);
        // 人脸yaw,pitch,row 角度，范围（-45，45），推荐-15-15
        config.setHeadPitchValue(VALUE_HEAD_PITCH);
        config.setHeadRollValue(VALUE_HEAD_ROLL);
        config.setHeadYawValue(VALUE_HEAD_YAW);
        // 最小检测人脸（在图片人脸能够被检测到最小值）80-200， 越小越耗性能，推荐120-200
        config.setMinFaceSize(VALUE_MIN_FACE_SIZE);
        // 人脸置信度（0-1）推荐大于0.6
        config.setNotFaceValue(VALUE_NOT_FACE_THRESHOLD);
        // 人脸遮挡范围 （0-1） 推荐小于0.5
        config.setOcclusionValue(VALUE_OCCLUSION);
        // 是否进行质量检测
        config.setCheckFaceQuality(true);
        // 人脸检测使用线程数
        config.setFaceDecodeNumberOfThreads(2);
        // 是否开启提示音
        config.setSound(true);


        FaceSDKManager.getInstance().setFaceConfig(config);

    }
}
