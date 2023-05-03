package com.github.tvbox.osc.wxwz.ui.adapter;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.wxwz.entity.Theme;

import java.util.ArrayList;

public class ThemeColorAdapter extends BaseQuickAdapter<Theme, BaseViewHolder> {
    public ThemeColorAdapter() {
        super(R.layout.item_theme_color,new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, Theme item) {
        LinearLayout root = helper.getView(R.id.theme_color_root);
        ImageView color = helper.getView(R.id.theme_color);
        TextView colorName = helper.getView(R.id.theme_color_name);
        color.setColorFilter(item.getColor());
        colorName.setText(item.getColorName());
    }


}
