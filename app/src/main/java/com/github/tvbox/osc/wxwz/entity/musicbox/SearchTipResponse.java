package com.github.tvbox.osc.wxwz.entity.musicbox;

import java.util.List;

public class SearchTipResponse {
    private List<SearchTip> data;
    private int ErrorCode;
    private int status;
    private int error_code;

    public List<SearchTip> getData() {
        return data;
    }

    public void setData(List<SearchTip> data) {
        this.data = data;
    }

    public int getErrorCode() {
        return ErrorCode;
    }

    public void setErrorCode(int errorCode) {
        ErrorCode = errorCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    @Override
    public String toString() {
        return "SearchTipResponse{" +
                "data=" + data +
                ", ErrorCode=" + ErrorCode +
                ", status=" + status +
                ", error_code=" + error_code +
                '}';
    }
}
