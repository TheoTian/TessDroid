package com.theo.ocr.traindata;

public interface IDownloader {

    /**
     * @param url
     * @param dstDir
     */
    void download(String url, String dstDir);

    /**
     * set result callback
     *
     * @param resultCallback
     */
    void setResultCallback(ResultCallback resultCallback);

    interface ResultCallback {

        void onProgress(int percent);

        void onSuccess();

        void onFailed();
    }
}
