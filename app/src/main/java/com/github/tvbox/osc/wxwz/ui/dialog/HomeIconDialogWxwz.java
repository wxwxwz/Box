package com.github.tvbox.osc.wxwz.ui.dialog;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ui.dialog.BaseDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;

/**
 * 描述
 *
 * @author pj567
 * @since 2020/12/27
 */
public class HomeIconDialogWxwz extends BaseDialog {
    private final TextView tvHomeSearch;
    private final TextView tvHomeMenu;
    private LinearLayout llSearchR;
    private TextView tvSearchR;

    public HomeIconDialogWxwz(@NonNull @NotNull Context context) {
        super(context);
        setContentView(R.layout.dialog_homeoption);
        setCanceledOnTouchOutside(true);
        tvHomeSearch = findViewById(R.id.tvHomeSearch);
        tvHomeSearch.setText(Hawk.get(HawkConfig.HOME_REC_STYLE, false) ? "瀑布流" : "单行");
        //tvHomeSearch.setText(Hawk.get(HawkConfig.HOME_SEARCH_POSITION, true) ? "上方" : "下方");
        tvHomeMenu = findViewById(R.id.tvHomeMenu);
        tvHomeMenu.setText(Hawk.get(HawkConfig.HOME_MENU_POSITION, true) ? "上方" : "下方");
        tvSearchR = (TextView) findViewById(R.id.tvSearchR);
        tvSearchR.setText(Hawk.get(HawkConfig.HOME_SEARCH_POSITION, true) ? "上方" : "下方");

        findViewById(R.id.llSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                /*Hawk.put(HawkConfig.HOME_SEARCH_POSITION, !Hawk.get(HawkConfig.HOME_SEARCH_POSITION, true));
                tvHomeSearch.setText(Hawk.get(HawkConfig.HOME_SEARCH_POSITION, true) ? "上方" : "下方");*/
                Hawk.put(HawkConfig.HOME_REC_STYLE, !Hawk.get(HawkConfig.HOME_REC_STYLE, false));
                tvHomeSearch.setText(Hawk.get(HawkConfig.HOME_REC_STYLE, false) ? "多行" : "单行");
            }
        });
        findViewById(R.id.llMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                Hawk.put(HawkConfig.HOME_MENU_POSITION, !Hawk.get(HawkConfig.HOME_MENU_POSITION, true));
                tvHomeMenu.setText(Hawk.get(HawkConfig.HOME_MENU_POSITION, true) ? "上方" : "下方");
            }
        });
        findViewById(R.id.llSearchR).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                Hawk.put(HawkConfig.HOME_SEARCH_POSITION, !Hawk.get(HawkConfig.HOME_SEARCH_POSITION, true));
                tvSearchR.setText(Hawk.get(HawkConfig.HOME_MENU_POSITION, true) ? "上方" : "下方");
            }
        });
    }
}