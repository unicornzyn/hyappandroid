package com.huayi.cme;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.SyncStateContract;

import com.huayi.cme.wxapi.WXPayEntryActivity;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by mac on 2017/3/3.
 */

public class AppRegister extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);
        //msgApi.registerApp(WXPayEntryActivity.APP_ID);
    }
}
