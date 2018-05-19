package com.theo.ocr.traindata;

public abstract class BaseDownloader implements IDownloader {

    ResultCallback mResultCallback;

    public BaseDownloader() {

    }

    @Override
    public void setResultCallback(ResultCallback resultCallback) {
        mResultCallback = resultCallback;
    }
}
