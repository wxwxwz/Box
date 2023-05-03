package com.github.tvbox.osc.wxwz.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.provider.SyncStateContract;


import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.github.tvbox.osc.ui.dialog.TipDialog;

import java.io.File;

public class ApkUtils {
    public static TipDialog dialog = null;
    public static void startInstallN(Activity context,String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", new File(filePath));
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }


    public static void startInstallO(Activity context,String filePath) {
        boolean isGranted = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            isGranted = context.getPackageManager().canRequestPackageInstalls();
        }else {
            isGranted = true;
        }

        if (isGranted) {
            startInstallN(context,filePath);
            return;
        }

        dialog = new TipDialog(context, "安装应用需要打开未知来源权限，请去设置中开启权限", "确定", "取消", new TipDialog.OnListener() {
            @Override
            public void left() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                context.startActivityForResult(intent, 0);
                dialog.dismiss();
            }

            @Override
            public void right() {
                dialog.dismiss();
            }

            @Override
            public void cancel() {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


}
