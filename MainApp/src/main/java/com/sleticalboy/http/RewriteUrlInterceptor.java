package com.sleticalboy.http;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created on 21-3-20.
 *
 * @author binlee sleticalboy@gmail.com
 */
public class RewriteUrlInterceptor implements Interceptor {

    private static final String TAG = "RewriteUrl";

    static {
        Log.d(TAG, "first static init");
    }

    private static final Object sObj = new Object();
    static {
        Log.d(TAG, "second static init sObj: " + sObj);
    }

    private static final Object sObj2 = new Object();
    static {
        Log.d(TAG, "third static init sObj2: " + sObj2);
    }

    private static int sCounter = 0;

    public RewriteUrlInterceptor() {
        Log.d(TAG, "RewriteUrlInterceptor() called");
        new Thread(() -> {
            while (sCounter <= 100) {
                synchronized (sObj) {
                    Log.d(TAG, "Thread A -> produce A");
                    sCounter++;
                    waitOn(sObj);
                }
            }
        }).start();
        new Thread(() -> {
            while (sCounter <= 100) {
                synchronized (sObj) {
                    Log.d(TAG, "Thread B -> produce B");
                    sCounter++;
                    sObj.notify();
                }
            }
        }).start();
    }

    private static void waitOn(Object obj) {
        try {
            obj.wait(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request rawRequest = chain.request();
        List<String> newHost = rawRequest.headers(Constants.REWRITE_HOST);
        if (newHost.size() == 0) return chain.proceed(rawRequest);
        HttpUrl url = HttpUrl.parse(newHost.get(0));
        if (url == null) return chain.proceed(rawRequest);
        Log.v(TAG, "intercept() new url: " + url + ", host: " + url.host());
        Request.Builder newBuilder = rawRequest.newBuilder();
        newBuilder.removeHeader(Constants.REWRITE_HOST);
        url = rawRequest.url().newBuilder().host(url.host()).build();
        return chain.proceed(newBuilder.url(url).build());
    }
}
