package com.theo.ocr.traindata;

import com.theo.downloader.DownloaderFactory;
import com.theo.downloader.IDownloader;
import com.theo.downloader.Task;
import com.theo.downloader.info.SnifferInfo;
import com.theo.ocr.utils.RunnableUtil;

import java.io.File;

public class TrainDataDownloader extends BaseDownloader {

    private IDownloader mDownloader;

    private boolean mAutoStart;

    private final static String TEMP_SUFFIX = ".theotmpfile";

    /**
     * when complete,delete .theotmp suffix
     *
     * @param tempName
     * @return
     */
    private static String deleteTempSuffix(String tempName) {
        return tempName.replace(TEMP_SUFFIX, "");
    }

    private IDownloader.DownloadListener mListener = new IDownloader.DownloadListener() {
        @Override
        public void onCreated(Task task, SnifferInfo snifferInfo) {
            task.setFileName(task.getFileName() + TEMP_SUFFIX);
            if (mAutoStart) {
                mDownloader.start();
            }
        }

        @Override
        public void onStart(Task task) {

        }

        @Override
        public void onPause(Task task) {

        }

        @Override
        public void onProgress(Task task, final long total, final long down) {
            RunnableUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mResultCallback != null) {
                        mResultCallback.onProgress((int) (total > 0 ? down * 100 / total : 0));
                    }
                }
            });
        }

        @Override
        public void onError(Task task, int error, String msg) {
            RunnableUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mResultCallback != null) {
                        mResultCallback.onFailed();
                    }
                }
            });
        }

        @Override
        public void onComplete(Task task, long total) {
            //rename temp file to final file when finish
            String filePath = task.getFilePath();
            File tempFile = new File(filePath);
            File newFile = new File(deleteTempSuffix(filePath));
            tempFile.renameTo(newFile);

            //update task info
            task.setFileName(newFile.getName());
            task.setFilePath(newFile.getAbsolutePath());

            RunnableUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mResultCallback != null) {
                        mResultCallback.onSuccess();
                    }
                }
            });
        }

        @Override
        public void onSaveInstance(Task task, byte[] data) {

        }
    };

    @Override
    public void download(String url, String dstDir) {
        if (mDownloader != null) {
            mDownloader.setListener(null);
            mDownloader.pause();
            mDownloader.delete();
        }
        mAutoStart = true;
        mDownloader = DownloaderFactory.create(IDownloader.Type.NORMAL, new Task(url, dstDir));
        mDownloader.setListener(mListener);
        mDownloader.create();
    }
}
