package com.huayi.cme.utils;



/**
 * Created by mac on 2018/1/28.
 */

public interface OnResultListener<T> {
    void onResult(T result);

    void onError(T error);
}
