package com.huayi.cme.models;

/**
 * Created by mac on 2018/1/28.
 */

public class ResponseResult {
    public ResponseResult(){}
    public ResponseResult(int errorCode,String errMsg){
        errorCode = errorCode;
        errMsg = errMsg;
    }

    private int errorCode;

    public void setErrorCode(int v){
        errorCode = v;
    }
    public int getErrorCode(){
        return errorCode;
    }

    private String errMsg;

    public void setErrMsg(String v){
        errMsg = v;
    }
    public String getErrMsg(){
        return errMsg;
    }
}
