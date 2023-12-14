package com.github.tvbox.osc.wxwz.ui.dialog;

import static com.github.tvbox.osc.wxwz.util.DownloadDriveUtils.thread;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ui.dialog.BaseDialog;
import com.github.tvbox.osc.wxwz.util.FileUtils;

import java.io.File;

public class DownloadDialog extends BaseDialog {
    private TextView downloadTitle;
    private TextView downloadInfo;
    private ProgressBar downloadProgress;
    private TextView downloadPercent;
    private TextView downloadResult;
    private TextView leftBtn;
    private TextView rightBtn;
    public OnListener onListener;
    private String url = "";
    private boolean isDone = false;
    public String root = Environment.getExternalStorageDirectory().getAbsolutePath();

    public DownloadDialog(@NonNull Context context,String title,String info,String result) {
        super(context);
        setContentView(R.layout.dialog_download);
        setCanceledOnTouchOutside(false);
        initView();
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onListener!=null){
                    onListener.left();
                }

            }
        });
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onListener!=null){
                    onListener.right();
                }

            }
        });

        downloadTitle.setText(title);
        downloadInfo.setText(info);
        downloadResult.setText(result);
    }


    private void initView() {
        downloadTitle = (TextView) findViewById(R.id.download_title);
        downloadInfo = (TextView) findViewById(R.id.download_info);
        downloadProgress = (ProgressBar) findViewById(R.id.download_progress);
        downloadPercent = (TextView) findViewById(R.id.download_percent);
        downloadResult = (TextView) findViewById(R.id.download_result);
        leftBtn = (TextView) findViewById(R.id.leftBtn);
        rightBtn = (TextView) findViewById(R.id.rightBtn);
    }

    public void setTitle(String title){
        downloadTitle.setText(title);
    }
    public void setDownloadInfo(String downloadInfo){
        this.downloadInfo.setText(downloadInfo);
    }

    public void setDownloadProgress(int downloadProgress){
        this.downloadProgress.setProgress(downloadProgress);
    }

    public void setDownloadProgressMax(int max){
        this.downloadProgress.setMax(max);
    }

    public void setSecondProgress(int downloadSecondProgress){
        this.downloadProgress.setSecondaryProgress(downloadSecondProgress);
    }

    public void setDownloadResult(String result){
        this.downloadResult.setText(result);
    }

    public void setDownloadPercent(int percent){
        this.downloadPercent.setText(percent + "%");
    }

    public void setUrl(String url){
        this.url = url;
    }

    public void setIsDone(boolean isDone){
        this.isDone = isDone;
    }

    public void setOnClickListner(OnListener listener){
        this.onListener = listener;
    }

    public interface OnListener {
        void left();

        void right();

    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (isDone){
            if (thread!=null||thread.isAlive())
                thread.interrupt();
        }else {
            if (thread!=null||thread.isAlive())
                thread.interrupt();
            FileUtils.delFile(new File(root + "/tvbox/Download",FileUtils.getFileName(url)));
        }

    }
}
