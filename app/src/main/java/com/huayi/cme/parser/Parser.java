package com.huayi.cme.parser;


/**
 * Created by mac on 2018/1/28.
 */

public interface Parser<T> {
    T parse(String json);
}
