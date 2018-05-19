package com.theo.ocr;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

public class SimpleOCREngine {

    public enum Status {
        IDLE,
        INITING,
        INITED,
        PROCESSING,
        PROCESSED,
        ERROR
    }

    public interface OnInitListener {
        void onIniting();

        void onInited();
    }

    public interface OnProcessListener {
        void onProcessing();

        void onProcessed();
    }

    public interface OnErrorListener {
        void onError();
    }

    private TessBaseAPI mTessBaseAPI;

    private volatile Status mCurrentStatus = Status.IDLE;

    private OnInitListener mOnInitListener;

    private OnProcessListener mOnProcessListener;

    private OnErrorListener mOnErrorListener;

    public SimpleOCREngine() {
        mCurrentStatus = Status.IDLE;
        mTessBaseAPI = new TessBaseAPI();
    }

    private class InitRunnable implements Runnable {
        private String datapath;
        private String language;

        public InitRunnable(String datapath, String language) {
            this.datapath = datapath;
            this.language = language;
        }

        @Override
        public void run() {
            try {
                notifyIniting();
                if (mTessBaseAPI.init(datapath, language)) {
                    mTessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
                    notifyInited();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            notifyError();
        }
    }

    private class ProcessRunnable implements Runnable {
        private Bitmap bitmap;

        public ProcessRunnable(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            try {
                notifyProcessing();
                mTessBaseAPI.setImage(bitmap);
                notifyProcessed();
            } catch (Exception e) {
                e.printStackTrace();
            }
            notifyError();
        }
    }

    public void init(String datapath, String language) {
        new Thread(new InitRunnable(datapath, language)).start();
    }

    public boolean isInited() {
        return mCurrentStatus == Status.INITED || mCurrentStatus == Status.PROCESSING
                || mCurrentStatus == Status.PROCESSED;
    }

    /**
     * process the bitmap
     *
     * @param bitmap
     * @return
     */
    public void process(final Bitmap bitmap) {
        if (!isInited()) {
            return;
        }
        new Thread(new ProcessRunnable(bitmap)).start();
    }

    /**
     * @return
     */
    public String getResult() {
        if (mCurrentStatus == Status.PROCESSED) {
            return mTessBaseAPI.getUTF8Text();
        }
        return null;
    }

    /**
     * reset the tess
     */
    public void reset() {
        mCurrentStatus = Status.IDLE;
    }

    public void setOnInitListener(OnInitListener mOnInitListener) {
        this.mOnInitListener = mOnInitListener;
    }

    public void setOnProcessListener(OnProcessListener mOnProcessListener) {
        this.mOnProcessListener = mOnProcessListener;
    }

    public void setOnErrorListener(OnErrorListener mOnErrorListener) {
        this.mOnErrorListener = mOnErrorListener;
    }

    private void notifyIniting() {
        mCurrentStatus = Status.INITING;
        if (mOnInitListener != null) {
            mOnInitListener.onIniting();
        }
    }

    private void notifyInited() {
        mCurrentStatus = Status.INITED;
        if (mOnInitListener != null) {
            mOnInitListener.onInited();
        }
    }

    private void notifyProcessing() {
        mCurrentStatus = Status.PROCESSING;
        if (mOnProcessListener != null) {
            mOnProcessListener.onProcessing();
        }
    }

    private void notifyProcessed() {
        mCurrentStatus = Status.PROCESSED;
        if (mOnProcessListener != null) {
            mOnProcessListener.onProcessed();
        }
    }

    private void notifyError() {
        mCurrentStatus = Status.ERROR;
        if (mOnErrorListener != null) {
            mOnErrorListener.onError();
        }
    }
}
