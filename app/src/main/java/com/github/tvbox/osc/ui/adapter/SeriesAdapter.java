package com.github.tvbox.osc.ui.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.VodInfo;

import java.util.ArrayList;

/**
 * @author pj567
 * @date :2020/12/22
 * @description:
 */
public class SeriesAdapter extends BaseQuickAdapter<VodInfo.VodSeries, BaseViewHolder> {
    public SeriesAdapter() {
        super(R.layout.item_series, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, VodInfo.VodSeries item) {
        FrameLayout root = helper.getView(R.id.item_series_root);
        TextView tvSeries = helper.getView(R.id.tvSeries);
        root.setOnFocusChangeListener(focusChangeListener);
        if (item.selected) {
            // takagen99: Added Theme Color
//            tvSeries.setTextColor(mContext.getResources().getColor(R.color.color_theme));
            tvSeries.setTextColor(((BaseActivity) mContext).getThemeColor());
        } else {
            tvSeries.setTextColor(Color.WHITE);
        }
        helper.setText(R.id.tvSeries, item.name);
    }

    private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View itemView, boolean hasFocus) {
                if (hasFocus)
                    itemView.animate().scaleX(1.01f).scaleY(1.01f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
                else
                    itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();

        }
    };
}