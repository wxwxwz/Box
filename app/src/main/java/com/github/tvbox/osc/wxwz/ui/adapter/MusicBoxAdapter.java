package com.github.tvbox.osc.wxwz.ui.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.ImgUtil;
import com.github.tvbox.osc.wxwz.entity.musicbox.MusicBoxInfo;

import java.util.List;

public class MusicBoxAdapter extends BaseQuickAdapter<MusicBoxInfo, BaseViewHolder> {
    public MusicBoxAdapter(@Nullable List<MusicBoxInfo> data) {
        super(R.layout.item_musicbox,data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, MusicBoxInfo item) {
        helper.setText(R.id.tvName,item.getName() + " - " + item.getArtist());
        ImgUtil.load(item.getPic(),helper.getView(R.id.ivPic));
    }
}
