package com.huayi.cme.models;

import java.io.File;
import java.util.Map;

/**
 * Created by mac on 2018/1/28.
 */

public interface RequestParams {
    Map<String, File> getFileParams();
    Map<String, String> getStringParams();
}
