package com.github.tvbox.osc.wxwz.util.okhttp.entity;

import java.io.InputStream;

public class DownloadInfo {
    private long fileSize = 0;
    private InputStream file ;

    public DownloadInfo(long fileSize, InputStream file) {
        this.fileSize = fileSize;
        this.file = file;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public InputStream getFile() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }
}
