package com.sleticalboy.http;

import androidx.annotation.WorkerThread;

import com.sleticalboy.http.builder.GetBuilder;
import com.sleticalboy.http.builder.RequestBuilder;
import com.sleticalboy.http.callback.DownloadCallback;
import com.sleticalboy.http.callback.ProgressCallback;
import com.sleticalboy.http.interceptor.ProgressInterceptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created on 18-9-19.
 *
 * @author leebin
 */
public final class OkDownloader {
    
    private static final int STATE_RESUME = 0x100;
    private static final int STATE_PAUSE = 0x101;
    private static final int STATE_CANCEL = 0x102;
    
    private final String mUrl;
    private final File mSaveFile;
    private DownloadCallback mCallback;
    private boolean mIsPause = false;
    private boolean mIsCancel = false;
    private boolean mIsDownloading = false;
    /*** 断点的位置 ***/
    private long breakPoint = 0L;
    
    public OkDownloader(String downloadUrl, String savePath) {
        this(downloadUrl, new File(savePath), false);
    }
    
    public OkDownloader(String downloadUrl, File saveFile, boolean isNeedProgress) {
        mUrl = downloadUrl;
        mSaveFile = saveFile;
        if (isNeedProgress) {
            HttpClient.get().addInterceptor(ProgressInterceptor.newInstance());
            ProgressInterceptor.addCallback(mUrl, new ProgressCallback.SimpleCallback() {
                
                @Override
                public void onPreExecute(long total) {
                    if (mCallback != null) {
                        // 断点续传时此处为剩余文件的长度
                        // total + breakPoint 是文件的总长度
                        HttpClient.get().getMainHandler().post(() -> mCallback.onStart(total));
                    }
                }
                
                @Override
                public void onProgress(float progress, long bytesTotalRead) {
                    if (mCallback != null) {
                        HttpClient.get().getMainHandler().post(() -> mCallback.onProgress(progress));
                    }
                }
            });
        }
    }
    
    /**
     * 开始下载, after {@link OkDownloader#setDownloadCallback(DownloadCallback)}
     */
    public void start() {
        if (mIsPause) {
            resume();
            return;
        }
        mIsCancel = false;
        mIsPause = false;
        download(0L);
    }
    
    /**
     * 开始下载
     *
     * @param startPoint 位置
     */
    private void download(final long startPoint) {
        RequestBuilder getBuilder = new GetBuilder()
                .url(mUrl)
                .breakPoint(startPoint, null);
        HttpClient.get().getOkHttpClient().newCall(getBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (mCallback != null) {
                    HttpClient.get().getMainHandler().post(() -> mCallback.onError(e));
                }
            }
            
            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final ResponseBody responseBody = response.body();
                if (response.isSuccessful() && responseBody != null) {
                    saveFile(responseBody, startPoint);
                }
            }
        });
    }
    
    // okio 实现
    // @WorkerThread
    // private void saveFile(ResponseBody responseBody, long startPoint) {
    //     BufferedSink sink = null;
    //     Buffer buffer = null;
    //     BufferedSource source = null;
    //     try {
    //         sink = Okio.buffer(Okio.sink(mSaveFile));
    //         buffer = sink.buffer();
    //         long total = 0;
    //         long len;
    //         long bufferSize = 1 << 18; // 缓冲区: 256 kb
    //         source = responseBody.source();
    //         while ((len = source.read(buffer, bufferSize)) != -1) {
    //             if (mIsCancel) {
    //                 notifyStateChange(STATE_CANCEL);
    //                 return;
    //             } else if (mIsPause) {
    //                 notifyStateChange(STATE_PAUSE);
    //                 return;
    //             } else {
    //                 sink.emit();
    //                 breakPoint = (total += len);
    //             }
    //         }
    //         if (mCallback != null) {
    //             ProgressInterceptor.removeCallback(mUrl);
    //             resetDownloader();
    //             mHttpClient.getMainHandler().post(() -> mCallback.onComplete());
    //         }
    //     } catch (IOException e) {
    //         if (mCallback != null) {
    //             mHttpClient.getMainHandler().post(() -> mCallback.onError(e));
    //         }
    //     } finally {
    //         OkUtils.closeSilently(source);
    //         OkUtils.closeSilently(sink);
    //         OkUtils.closeSilently(buffer);
    //         OkUtils.closeSilently(responseBody);
    //     }
    // }
    
    // java.io 库实现
    @WorkerThread
    private void saveFile(ResponseBody body, long startPoint) {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        try {
            inputStream = body.byteStream();
            randomAccessFile = new RandomAccessFile(mSaveFile, "rw");
            randomAccessFile.seek(startPoint);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                if (mIsCancel) {
                    notifyStateChange(STATE_CANCEL);
                    return;
                } else if (mIsPause) {
                    notifyStateChange(STATE_PAUSE);
                    return;
                } else {
                    breakPoint = (startPoint += len);
                    randomAccessFile.write(buffer, 0, len);
                }
            }
            if (mCallback != null) {
                ProgressInterceptor.removeCallback(mUrl);
                resetDownloader();
                HttpClient.get().getMainHandler().post(() -> mCallback.onComplete(mSaveFile));
            }
        } catch (IOException ignored) {
            if (mCallback != null) {
                HttpClient.get().getMainHandler().post(() -> mCallback.onError(ignored));
            }
        } finally {
            OkUtils.closeSilently(inputStream);
            OkUtils.closeSilently(randomAccessFile);
        }
    }
    
    private void resetDownloader() {
        mIsCancel = false;
        mIsDownloading = false;
        mIsPause = false;
    }
    
    private void notifyStateChange(int state) {
        if (mCallback != null) {
            if (state == STATE_CANCEL) {
                HttpClient.get().getMainHandler().post(() -> mCallback.onCancel());
            } else if (state == STATE_PAUSE) {
                HttpClient.get().getMainHandler().post(() -> mCallback.onPause());
            } else if (state == STATE_RESUME) {
                HttpClient.get().getMainHandler().post(() -> mCallback.onResume());
            }
        }
    }
    
    /**
     * 暂停下载
     */
    public void pause() {
        if (mIsPause) {
            return;
        }
        mIsPause = true;
        mIsCancel = false;
        mIsDownloading = false;
    }
    
    /**
     * 恢复下载
     */
    public void resume() {
        if (mIsDownloading) {
            return;
        }
        mIsDownloading = true;
        mIsPause = false;
        mIsCancel = false;
        notifyStateChange(STATE_RESUME);
        download(breakPoint);
    }
    
    /**
     * 设置回调, before {@link OkDownloader#start()}
     *
     * @param callback {@link DownloadCallback}
     */
    public void setDownloadCallback(DownloadCallback callback) {
        mCallback = callback;
    }
    
    /**
     * 取消下载
     */
    public void cancel() {
        if (mIsCancel) {
            return;
        }
        mIsCancel = true;
        mIsDownloading = false;
        mIsPause = false;
    }
}
