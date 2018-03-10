package com.huayi.cme.utils;

import android.os.Handler;
import android.os.Looper;

import com.huayi.cme.models.ResponseResult;
import com.huayi.cme.parser.Parser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.util.Log;

/**
 * Created by mac on 2018/1/28.
 */

public class HttpUtil {
    private OkHttpClient client;
    private Handler handler;
    private static volatile HttpUtil instance;
    private String token = "";
    private String appid = "1CC50D14-3CB1-4C2C-BF27-86959D705071";
    private String photo_from_type = "2";

    private HttpUtil(){}

    public static HttpUtil getInstance() {
        if (instance == null) {
            synchronized (HttpUtil.class) {
                if (instance == null) {
                    instance = new HttpUtil();
                }
            }
        }
        return instance;
    }

    public void init() {
        client = new OkHttpClient();
        handler = new Handler(Looper.getMainLooper());
    }

    public <T> void uploadPhoto(String path, String cert_id, String photo,final Parser<T> parser,final OnResultListener listener){
        RequestBody body = new FormBody.Builder()
                .add("token",token)
                .add("cert_id",cert_id)
                .add("photo",photo)
                .build();
        final Request request = new Request.Builder()
                .url(path)
                .post(body)
                .build();
        if (client == null) {
            ResponseResult err = new ResponseResult(-1, "okhttp inner error");
            listener.onError(err);
            return;
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                final ResponseResult error = new ResponseResult(-2,
                        e.getMessage());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onError(error);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();

                Log.d("mylog", "onResponse json->" + responseString);
                final T result;
                try {
                    result = parser.parse(responseString);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(result);
                        }
                    });
                } catch (final Exception faceError) {
                    faceError.printStackTrace();
                    final ResponseResult r = new ResponseResult(-5,faceError.getMessage());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(r);
                        }
                    });
                }
            }
        });
    }

    public <T> void queryPhotoValid(String path,String cert_id,final Parser<T> parser,final OnResultListener listener){
        String url = path+"?token="+token+"&appid="+appid+"&photo_from_type="+photo_from_type+"&cert_id="+cert_id;
        final Request request = new Request.Builder()
                .url(url)
                .build();
        if (client == null) {
            ResponseResult err = new ResponseResult(-1, "okhttp inner error");
            listener.onError(err);
            return;
        }
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                final ResponseResult error = new ResponseResult(-2,
                        e.getMessage());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onError(error);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();

                Log.d("mylog", "onResponse json->" + responseString);
                final T result;
                try {
                    result = parser.parse(responseString);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(result);
                        }
                    });
                } catch (final Exception faceError) {
                    faceError.printStackTrace();
                    final ResponseResult r = new ResponseResult(-5,faceError.getMessage());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(r);
                        }
                    });
                }
            }
        });

    }

    public <T> void getScanQRCodeTimeOut(String path, final Parser<T> parser, final OnResultListener listener){
        final Request request = new Request.Builder()
                .url(path)
                .build();
        if (client == null) {
            ResponseResult err = new ResponseResult(-1, "okhttp inner error");
            listener.onError(err);
            return;
        }
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                final ResponseResult error = new ResponseResult(-2,
                        e.getMessage());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onError(error);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();

                Log.d("mylog", "onResponse json->" + responseString);
                final T result;
                try {
                    result = parser.parse(responseString);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(result);
                        }
                    });
                } catch (final Exception faceError) {
                    faceError.printStackTrace();
                    final ResponseResult r = new ResponseResult(-5,faceError.getMessage());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(r);
                        }
                    });
                }
            }
        });
    }

}
