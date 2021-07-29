package com.sleticalboy.http;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.sleticalboy.http.converter.FastJsonConverterFactory;
import com.sleticalboy.http.converter.ScalarsConverterFactory;

import retrofit2.Retrofit;

/**
 * Created on 18-9-11.
 *
 * @author leebin
 */
public final class RetrofitClient {
    
    private final Retrofit mRetrofit;
    
    private RetrofitClient() {
        mRetrofit = new Retrofit.Builder()
                .client(HttpClient.get().getOkHttpClient())
                .baseUrl("")
                .addConverterFactory(FastJsonConverterFactory.create()) // json support
                .addConverterFactory(ScalarsConverterFactory.create()) // String, boxed, primitives support
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // rxjava support
                .build();
    }
    
    public static RetrofitClient getInstance() {
        return SingleHolder.RETROFIT_CLIENT;
    }
    
    private static final class SingleHolder {
        final static RetrofitClient RETROFIT_CLIENT = new RetrofitClient();
    }
    
    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }
}
