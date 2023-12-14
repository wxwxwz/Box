package com.github.tvbox.osc.wxwz.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.ImgUtil;
import com.github.tvbox.osc.wxwz.entity.Theme;
import com.github.tvbox.osc.wxwz.entity.Wallpaper;
import com.github.tvbox.osc.wxwz.util.DownloadDriveUtils;

import java.io.File;
import java.util.ArrayList;

public class ThemeWallpaperAdapter extends BaseQuickAdapter<Wallpaper, BaseViewHolder> {
    private Context context;
    public ThemeWallpaperAdapter() {
        super(R.layout.item_theme_wallpaper,new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, Wallpaper item) {
        ImageView wallpaper = helper.getView(R.id.theme_wallpaper_item);
        TextView wallpaperName = helper.getView(R.id.theme_wallpaper_name);

        if (item.getType()==0){
            //Picasso.get().load(item.getPath()).into(new File(context.getFilesDir().getAbsolutePath(), "wp"));
            ImgUtil.load(item.getPath(),wallpaper);

            Log.e("wxwz","类型:" + item.getType()+"**" +item.getPath());
        }else if (item.getType()==1){
            ImgUtil.load(item.getResPath(),wallpaper);
            Log.e("wxwz","类型:" + item.getType() + "**" +item.getResPath());
        }else if (item.getType()==2){
            ImgUtil.load(item.getFile().getPath(),wallpaper);
            Log.e("wxwz","类型:" + item.getType());
        }

        wallpaperName.setText(item.getName());

    }
}
