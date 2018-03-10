package com.huayi.cme.parser;


import com.huayi.cme.models.ResponseResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mac on 2018/1/28.
 */

public class ResponseParser implements Parser<ResponseResult> {
    @Override
    public ResponseResult parse(String json) {
        ResponseResult m = new ResponseResult();
        try {
            JSONObject jsonObject = new JSONObject(json);
            m.setErrorCode(jsonObject.optInt("errorCode"));
            m.setErrMsg(jsonObject.optString("errMsg"));
        }catch (JSONException e) {
            m.setErrorCode(-10);
            m.setErrMsg(e.getMessage());
        }
        return m;
    }
}
