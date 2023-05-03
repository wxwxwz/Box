package com.github.tvbox.osc.wxwz.ui.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.DriveFolderFile;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter;
import com.github.tvbox.osc.ui.dialog.ApiHistoryDialog;
import com.github.tvbox.osc.ui.dialog.BaseDialog;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.StorageDriveType;
import com.github.tvbox.osc.wxwz.ui.adapter.ThemeColorAdapter;
import com.github.tvbox.osc.wxwz.ui.adapter.ThemeWallpaperAdapter;
import com.github.tvbox.osc.wxwz.util.FileUtils;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

public class ThemeDialog extends BaseDialog {
    private TextView themeTitle;
    private TvRecyclerView themeColor;
    private TvRecyclerView themeWallpaper;
    private TextView leftBtn;
    private TextView rightBtn;
    public OnListener onListener;
    private EditText themeInput;
    private LinearLayout themeOpen;
    private LinearLayout themeHistory;

    public ThemeDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_theme);
        setCanceledOnTouchOutside(false);
        initView();
        initEvent();
    }

    private void initEvent() {
        themeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        themeOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooserDialog dialog = new ChooserDialog(getContext(),R.style.FileChooserStyle);
                dialog
                        .withStringResources("选择一个壁纸", "确定", "取消")
                        .titleFollowsDir(true)
                        .displayPath(true)
                        .enableDpad(true)
                        .withFilterRegex(false, true,".*\\.(jpe?g|png)")
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String dir, File dirFile) {
                                String absPath = dirFile.getAbsolutePath();
                                themeInput.setText(absPath);
                            }
                        }).show();
            }
        });
        themeHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> history = Hawk.get(HawkConfig.THEME_WALLPAPER_URL_HISTORY, new ArrayList<String>());
                if (history.isEmpty())
                    return;
                String current = Hawk.get(HawkConfig.THEME_WALLPAPER_URL, "");
                int idx = 0;
                if (history.contains(current))
                    idx = history.indexOf(current);
                ApiHistoryDialog dialog = new ApiHistoryDialog(getContext());
                dialog.setTip(HomeActivity.getRes().getString(R.string.dia_history_list));
                dialog.setAdapter(new ApiHistoryDialogAdapter.SelectDialogInterface() {
                    @Override
                    public void click(String value) {
                        themeInput.setText(value);
                        dialog.dismiss();
                    }

                    @Override
                    public void del(String value, ArrayList<String> data) {
                        Hawk.put(HawkConfig.THEME_WALLPAPER_URL_HISTORY, data);
                    }
                }, history, idx);
                dialog.show();

            }
        });
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onListener != null) {
                    onListener.left();
                }

            }
        });
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onListener != null) {
                    onListener.right();
                }

            }
        });

    }

    private void initView() {
        themeTitle = (TextView) findViewById(R.id.theme_title);
        themeColor = (TvRecyclerView) findViewById(R.id.theme_color);
        themeWallpaper = (TvRecyclerView) findViewById(R.id.theme_wallpaper);
        leftBtn = (TextView) findViewById(R.id.leftBtn);
        rightBtn = (TextView) findViewById(R.id.rightBtn);
        themeInput = (EditText) findViewById(R.id.theme_input);
        themeOpen = (LinearLayout) findViewById(R.id.theme_open);
        themeHistory = (LinearLayout) findViewById(R.id.theme_history);
    }

    public void setThemeAdapter(ThemeColorAdapter themeAdapter,int pos) {
        themeColor.setAdapter(themeAdapter);
        themeColor.setSelectedPosition(pos);
        themeColor.post(new Runnable() {
            @Override
            public void run() {
                themeColor.scrollToPosition(pos);
                themeColor.setSelectionWithSmooth(pos);
            }
        });
    }

    public void setThemeWallpaperAdapter(ThemeWallpaperAdapter themeWallpaperAdapter,int pos) {
        themeWallpaper.setAdapter(themeWallpaperAdapter);
    }

    public void setEditText(String input){
        themeInput.setText(input);
    }

    public String getEditText(){
        return themeInput.getText().toString();
    }

    public void setOnButtonClickListner(OnListener listener) {
        this.onListener = listener;
    }

    public interface OnListener {
        void left();

        void right();

    }
}
