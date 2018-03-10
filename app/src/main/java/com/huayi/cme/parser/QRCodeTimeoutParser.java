package com.huayi.cme.parser;

import com.huayi.cme.models.QRCodeTimeoutResponseResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mac on 2018/2/24.
 */

public class QRCodeTimeoutParser implements Parser<QRCodeTimeoutResponseResult>  {
    @Override
    public QRCodeTimeoutResponseResult parse(String json) {
        QRCodeTimeoutResponseResult m = new QRCodeTimeoutResponseResult();
        try {
            JSONObject jsonObject = new JSONObject(json);
            m.setTimeout(jsonObject.optInt("timeout"));
        }catch (JSONException e) {
            m.setTimeout(0);
        }
        return m;
    }
}
