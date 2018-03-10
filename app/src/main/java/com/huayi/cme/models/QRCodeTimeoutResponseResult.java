package com.huayi.cme.models;

/**
 * Created by mac on 2018/2/24.
 */

public class QRCodeTimeoutResponseResult {
    public QRCodeTimeoutResponseResult() {
    }

    public QRCodeTimeoutResponseResult(int v){
        timeout = v;
    }

    private int timeout;

    public void setTimeout(int v) {
        timeout = v;
    }

    public int getTimeout() {
        return timeout;
    }
}
