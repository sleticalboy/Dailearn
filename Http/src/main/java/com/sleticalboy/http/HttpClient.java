package com.sleticalboy.http;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.sleticalboy.http.builder.DeleteBuilder;
import com.sleticalboy.http.builder.GetBuilder;
import com.sleticalboy.http.builder.PatchBuilder;
import com.sleticalboy.http.builder.PostBuilder;
import com.sleticalboy.http.builder.PutBuilder;
import com.sleticalboy.http.builder.RequestBuilder;
import com.sleticalboy.http.callback.HttpCallback;
import com.sleticalboy.http.cookie.CookieJarImpl;
import com.sleticalboy.http.interceptor.LoggerInterceptor;
import com.sleticalboy.http.ssl.Https;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public final class HttpClient {

    private static final boolean DBG = true;

    private static final HttpClient HTTP_CLIENT = new HttpClient();

    private final Handler mMainHandler;
    private OkHttpClient mOkHttpClient;

    private HttpClient() {
        mMainHandler = new Handler(Looper.getMainLooper());
        internalInit();
    }

    private void internalInit() {
        if (mOkHttpClient != null) {
            return;
        }
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        builder.cookieJar(CookieJarImpl.ALL_COOKIE);
        builder.retryOnConnectionFailure(true);
        builder.eventListener(new DefaultEventListener());
        // builder.dispatcher(new Dispatcher());
        // builder.pingInterval(10000, TimeUnit.MILLISECONDS);
        // builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));
        // 还不支持某些网站的 https, 在查找原因
        // final Https.SSLParams sslParams = Https.getSSLParams(null, "ca");
        // builder.sslSocketFactory(sslParams.sslSocketFactory, sslParams.trustManager);
        // 不安全
        builder.hostnameVerifier(Https.trustAll());

        // 拦截器
        if (DBG) {
            final LoggerInterceptor loggerInterceptor = new LoggerInterceptor()
                    .setLevel(LoggerInterceptor.Level.BODY);
            builder.addInterceptor(loggerInterceptor);
        }
        // builder.addInterceptor(ProgressInterceptor.newInstance()); // 下载的时候会用到
        // builder.addInterceptor(InjectHeaderInterceptor.newInstance()); // 用于 header 注入
        // builder.addInterceptor(GzipResponseInterceptor.newInstance()); // gzip[目前已知的是工作圈用到了]

//         final boolean vpnEnable = MXKit.getInstance().getKitConfiguration().isVpnEnable(ContextProvider.getContext());
//         if (vpnEnable) {
//             // 初始化时设置一次代理
//             builder.proxy(OkUtils.createProxy(MXKit.getInstance().getProxyPort()));
//             MXKit.getInstance().setProxyPortChangeCallback(new OnProxyPortChangeCallback.SimpleCallback() {
//                 @Override
//                 public void onProxyPortChange(int port) {
//                     while (!MXKit.getInstance().proxyServerReady()) {
//                         SystemClock.sleep(500);
//                     }
//                     // 当代理服务器端口发生变化时重置代理
// //                    mOkHttpClient = rebuildWithProxy(OkUtils.createProxy(port));
//                     mOkHttpClient = mOkHttpClient.newBuilder().proxy(OkUtils.createProxy(port)).build();
//                 }
//             });
//         }
        mOkHttpClient = builder.build();
    }

//    private OkHttpClient rebuildWithProxy(Proxy proxy) {
//        return mOkHttpClient.newBuilder().proxy(proxy).build();
//    }

    public static HttpClient get() {
        return HTTP_CLIENT;
    }

    public OkHttpClient getOkHttpClient() {
        checkInitialize();
        return mOkHttpClient.newBuilder().build();
    }

    private void checkInitialize() {
        if (mOkHttpClient == null) {
            internalInit();
        }
    }

    /**
     * 添加拦截器
     *
     * @param interceptor {@link Interceptor}
     * @return {@link HttpClient}
     */
    public HttpClient addInterceptor(Interceptor interceptor) {
        checkInitialize();
        mOkHttpClient.interceptors().add(interceptor);
        return this;
    }

    // public <T> void asyncExecute(retrofit2.Call<String> call, HttpCallback<T> callback) {
    //     call.enqueue(new retrofit2.Callback<String>() {
    //
    //         @SuppressWarnings("unchecked")
    //         @Override
    //         public void onResponse(final retrofit2.Call<String> call, final retrofit2.Response<String> response) {
    //             if (callback != null && response != null) {
    //                 final String result = response.body();
    //                 Object object;
    //                 if (callback.isStringType()) {
    //                     object = result;
    //                 } else {
    //                     object = JSON.parseObject(result, callback.getType());
    //                 }
    //                 mMainHandler.post(() -> callback.onSuccess((T) object));
    //                 OkUtils.releaseCall(call);
    //             }
    //         }
    //
    //         @Override
    //         public void onFailure(final retrofit2.Call<String> call, final Throwable t) {
    //             if (callback != null) {
    //                 mMainHandler.post(() -> callback.onFailure(t));
    //             }
    //             OkUtils.releaseCall(call);
    //         }
    //     });
    // }

    /**
     * 执行异步网络请求
     *
     * @param <T>      请求返回类型
     * @param builder  {@link RequestBuilder} see also {@link GetBuilder}, {@link PostBuilder},
     *                 {@link PutBuilder}, {@link DeleteBuilder}, {@link PatchBuilder}
     * @param callback {@link HttpCallback} 网络请求回调[都已经回调到了主线程]
     */
    public <T> void request(@NonNull RequestBuilder builder, HttpCallback<T> callback) {
        checkInitialize();
        mOkHttpClient.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (callback != null) {
                    mMainHandler.post(() -> callback.onFailure(e));
                }
                OkUtils.releaseCall(call);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    final ResponseBody responseBody = response.body();
                    if (callback != null && responseBody != null) {
                        final String result = responseBody.string();
                        final T object = callback.isStringType()
                                ? (T) result
                                : JSON.parseObject(result, callback.getType());
                        mMainHandler.post(() -> callback.onSuccess(object));
                    }
                } else {
                    final Exception error = new Exception(String.format("[code: %d, message: %s]%s",
                            response.code(), response.message(), response.body().string()));
                    if (callback != null) {
                        mMainHandler.post(() -> callback.onFailure(error));
                    }
                }
                OkUtils.releaseCall(call);
            }
        });
    }

    public Handler getMainHandler() {
        checkInitialize();
        return mMainHandler;
    }

    public void callbackError(com.sleticalboy.http.Callback callback, IOException e) {
        if (callback == null) return;
        mMainHandler.post(() -> callback.onError(e));
    }
}
