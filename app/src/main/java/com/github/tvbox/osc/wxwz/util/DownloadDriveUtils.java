package com.github.tvbox.osc.wxwz.util;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.github.tvbox.osc.bean.DriveFolderFile;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.activity.DriveActivity;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.util.StorageDriveType;
import com.github.tvbox.osc.viewmodel.drive.AbstractDriveViewModel;
import com.github.tvbox.osc.wxwz.ui.dialog.DownloadDialog;
import com.github.tvbox.osc.wxwz.ui.dialog.MusicDialog;
import com.github.tvbox.osc.wxwz.ui.dialog.SelectMoreDialog;
import com.github.tvbox.osc.wxwz.util.okhttp.WebDav;
import com.github.tvbox.osc.wxwz.util.okhttp.entity.DownloadInfo;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class DownloadDriveUtils {
    public static AbstractDriveViewModel viewModelD = null;
    public static DriveFolderFile selectedItemD = null;
    public static boolean isDone = false;
    public static void downloadSelect(Activity activity,AbstractDriveViewModel viewModel , DriveFolderFile selectedItem){
        SelectMoreDialog<DownloadSelect.Select> dialog = new SelectMoreDialog<>(activity);
        viewModelD = viewModel;
        selectedItemD = selectedItem;
        DriveFolderFile currentDrive = viewModel.getCurrentDrive();
        String[] typeName = DownloadSelect.getTypeNames();
        DownloadSelect.Select[] selects = DownloadSelect.Select.values();
        dialog.setTip("文件选项");
        dialog.setItemCheckDisplay(false);
        dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<DownloadSelect.Select>() {
            @Override
            public void click(DownloadSelect.Select value, int pos) {
                if (value == DownloadSelect.Select.OPEN){
                    openFile(activity,currentDrive,selectedItem);
                    dialog.dismiss();
                }else if (value == DownloadSelect.Select.DOWNLOAD){
                    downloadFile(activity,currentDrive,selectedItem);
                    dialog.dismiss();
                }else if (value == DownloadSelect.Select.DELETE){
                    deleteFile(activity,currentDrive,selectedItem);
                    dialog.dismiss();
                }else if (value == DownloadSelect.Select.CANCEL){
                    dialog.dismiss();
                }
            }

            @Override
            public String getDisplay(DownloadSelect.Select val) {
                return typeName[val.ordinal()];
            }
        }, new DiffUtil.ItemCallback<DownloadSelect.Select>() {
            @Override
            public boolean areItemsTheSame(@NonNull DownloadSelect.Select oldItem, @NonNull DownloadSelect.Select newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull DownloadSelect.Select oldItem, @NonNull DownloadSelect.Select newItem) {
                return oldItem.equals(newItem);
            }
        }, Arrays.asList(selects),0);
        dialog.show();
    }

    private static void deleteFile(Activity activity,DriveFolderFile currentDrive,DriveFolderFile selectedItem) {
        if (currentDrive.getDriveType() == StorageDriveType.TYPE.LOCAL){
            boolean res = FileUtils.delFile(new File(currentDrive.name + selectedItem.getAccessingPathStr() + selectedItem.name));
            EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_FILE_CHANGE));
            if (res){
                Toast.makeText(activity, "删除完成！", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(activity, "删除失败！", Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(activity, "建设中...", Toast.LENGTH_SHORT).show();
        }
    }

    private static void downloadFile(Activity activity,DriveFolderFile currentDrive, DriveFolderFile selectedItem) {
        JsonObject config = currentDrive.getConfig();
        String targetPath = selectedItem.getAccessingPathStr() + selectedItem.name;
        if (StorageDriveType.isMusicType(selectedItem.fileType)){
            if (currentDrive.getDriveType() == StorageDriveType.TYPE.LOCAL){
                downloadError(activity);
            }else {
                startDownload(activity,currentDrive,config.get("url").getAsString() + targetPath,"");
            }
        }else if (StorageDriveType.isOtherType(selectedItem.fileType)) {
            if (currentDrive.getDriveType() == StorageDriveType.TYPE.LOCAL) {
                downloadError(activity);
            } else if (currentDrive.getDriveType() == StorageDriveType.TYPE.WEBDAV) {
                startDownload(activity,currentDrive,config.get("url").getAsString() + targetPath,"");
            }
        }else if (StorageDriveType.isTextType(selectedItem.fileType)){
            if (currentDrive.getDriveType() == StorageDriveType.TYPE.LOCAL){
                downloadError(activity);
            }else if (currentDrive.getDriveType() == StorageDriveType.TYPE.WEBDAV){
                startDownload(activity,currentDrive,config.get("url").getAsString() + targetPath,"");
            }
        }else if (StorageDriveType.isImageType(selectedItem.fileType)){
            if (currentDrive.getDriveType() == StorageDriveType.TYPE.LOCAL){
                downloadError(activity);
            }else if (currentDrive.getDriveType() == StorageDriveType.TYPE.WEBDAV){
                startDownload(activity,currentDrive,config.get("url").getAsString() + targetPath,"");
            }
        } else {
            if (currentDrive.getDriveType() == StorageDriveType.TYPE.LOCAL){
                downloadError(activity);
            }else if (currentDrive.getDriveType() == StorageDriveType.TYPE.WEBDAV){
                startDownload(activity,currentDrive,config.get("url").getAsString() + targetPath,"");
            }

        }
    }

    private static void openFile(Activity activity, DriveFolderFile currentDrive, DriveFolderFile selectedItem) {
        if (StorageDriveType.isMusicType(selectedItem.fileType)){
            if (currentDrive.getDriveType() == StorageDriveType.TYPE.LOCAL){
                MusicDialog musicDialog = new MusicDialog(activity);
                musicDialog.playSong(activity,currentDrive.name + selectedItem.getAccessingPathStr() + selectedItem.name,viewModelD,selectedItem);

                musicDialog.show();
            }else {
                playWebMusic(activity,currentDrive, selectedItem);
            }
        }else if (StorageDriveType.isOtherType(selectedItem.fileType)) {
            if (currentDrive.getDriveType() == StorageDriveType.TYPE.LOCAL) {
                if (selectedItem.fileType.equals("APK")) {
                    ApkUtils.startInstallO(activity, currentDrive.name + selectedItem.getAccessingPathStr() + selectedItem.name);
                }
            } else if (currentDrive.getDriveType() == StorageDriveType.TYPE.WEBDAV) {
                downloadError(activity);
            }
        }else if (StorageDriveType.isTextType(selectedItem.fileType)){
            if (currentDrive.getDriveType() == StorageDriveType.TYPE.LOCAL){

            }else if (currentDrive.getDriveType() == StorageDriveType.TYPE.WEBDAV){

            }
        }else if (StorageDriveType.isImageType(selectedItem.fileType)){
            if (currentDrive.getDriveType() == StorageDriveType.TYPE.LOCAL){
            }else if (currentDrive.getDriveType() == StorageDriveType.TYPE.WEBDAV){
            }
        } else {
            Toast.makeText(activity, "Media Unsupported ：" + selectedItem.fileType, Toast.LENGTH_SHORT).show();
        }
    }

    public static DownloadDialog downloadDialog;
    public static Thread thread;
    public static String root = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static void startDownload(Activity activity, DriveFolderFile currentDrive, String url, String savePath){

        downloadDialog = new DownloadDialog(activity, "下载文件", "下载地址： " + url, "正在准备下载..." );
        downloadDialog.show();
        downloadDialog.setUrl(url);
        downloadDialog.setIsDone(false);
        downloadDialog.setOnClickListner(new DownloadDialog.OnListener() {
            @Override
            public void left() {
                if (isDone){
                    openFile(activity,currentDrive,selectedItemD);
                    downloadDialog.dismiss();
                }else {
                    Toast.makeText(activity,"文件暂未下载完成，请等待下载完成!",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void right() {
                if (isDone){
                    if (thread!=null||thread.isAlive())
                        thread.interrupt();
                }else {
                    if (thread!=null||thread.isAlive())
                        thread.interrupt();
                    FileUtils.delFile(new File(root + "/tvbox/Download",FileUtils.getFileName(url)));
                }
                downloadDialog.dismiss();
            }
        });
        thread = new Thread(){
            @Override
            public void run() {
                isDone = false;
                JsonObject config = currentDrive.getConfig();
                WebDav webDav = new WebDav();
                if (config.has("username") && config.has("password")) {
                    webDav.init(config.get("username").getAsString(), config.get("password").getAsString());
                }
                //判断文件是否存在
                DownloadInfo downloadInfo;
                downloadInfo = webDav.getWebDavFile(url);
                InputStream is = downloadInfo.getFile();

                File filename;
                if (savePath.equals("")){
                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                    File file = new File(root + "/tvbox/Download");
                    if (!file.exists())
                        file.mkdirs();

                    filename = new File(file, FileUtils.getFileName(url));
                    Log.e("wxwz","FileName:" + filename.getAbsolutePath());
                }else {
                    filename = new File(savePath);
                }


                try {
                    // 1K的数据缓冲
                    byte[] bs = new byte[1024];
                    // 读取到的数据长度
                    int len;
                    int hasRead = 0;//已经读取了多少
                    long total = downloadInfo.getFileSize();
                    Message msg = new Message();
                    msg.what = 3;
                    msg.obj =  FileUtils.getFileSize(total);
                    updateDialoghandler.sendMessage(msg);

                    // 输出的文件流
                    OutputStream os = new FileOutputStream(filename);
                    downloadDialog.setDownloadProgress(0);
                    downloadDialog.setDownloadProgressMax(100);
                    // 开始读取

                    while ((len = is.read(bs)) != -1) {
                        os.write(bs, 0, len);
                        hasRead += len;
                        downloadDialog.setDownloadProgress((int) (hasRead * 1.0f / total * 100));
                        Message per = new Message();
                        per.what = 4;
                        per.arg1 = (int) (hasRead * 1.0f / total * 100);
                        updateDialoghandler.sendMessage(per);

                    }
                    // 完毕，关闭所有链接
                    downloadDialog.setDownloadProgress(100);
                    os.close();
                    is.close();
                    Message msg1 = new Message();
                    isDone = true;
                    msg1.what = 1;
                    updateDialoghandler.sendMessage(msg1);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 2;
                    isDone = false;
                    updateDialoghandler.sendMessage(msg);
                }
            }
        };
        thread.start();

    }

    private static Handler updateDialoghandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 1:
                    downloadDialog.setDownloadResult("下载成功！");
                    downloadDialog.setIsDone(true);
                    downloadDialog.setDownloadPercent(100);
                    break;
                case 2:
                    downloadDialog.setDownloadResult("下载失败！");
                    downloadDialog.setIsDone(false);
                    break;
                case 3:
                    downloadDialog.setDownloadResult("下载中," + "文件大小：" + msg.obj);
                    break;
                case 4:
                    downloadDialog.setDownloadPercent(msg.arg1);
                    break;
            }
        }
    };

    public static void downloadError(Activity activity){
        Toast.makeText(activity, "当前文件无法下载！", Toast.LENGTH_SHORT).show();
    }

    public static void playWebMusic(Activity activity, DriveFolderFile currentDrive, DriveFolderFile selectedItem){
        JsonObject config = currentDrive.getConfig();
        String targetPath = selectedItem.getAccessingPathStr() + selectedItem.name;
        MusicDialog musicDialog = new MusicDialog(activity);
        musicDialog.playSong(activity,config.get("url").getAsString() + targetPath,viewModelD,selectedItem);
        musicDialog.show();
    }




}
