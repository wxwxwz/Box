package com.github.tvbox.osc.wxwz.util.okhttp;

import com.github.tvbox.osc.wxwz.util.okhttp.entity.DownloadInfo;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebDav {
    private OkHttpClient client;
    public WebDav() {
    }

    public void init(String username,String password){
        client = new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(username, password))
                .build();
    }

    /*public String getWebDavFileSize(String url){
        String fileSize = "";
        final Request request = new Request.Builder()
                .url(url)
                .head()
                .build();

        try {
            Response response = client.newCall(request).execute();
            fileSize = response.header("Content-Length");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return fileSize;
    }*/

    public DownloadInfo getWebDavFile(String url){
        InputStream inputStream = null;
        long fileSize = 0;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            inputStream = response.body().byteStream();
            fileSize = response.body().contentLength();
        } catch (IOException e) {
            e.printStackTrace();
            return new DownloadInfo(0,null);
        }
        return new DownloadInfo(fileSize,inputStream);
    }
}
