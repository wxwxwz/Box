package com.github.tvbox.osc.wxwz.ui.activity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.wxwz.entity.musicbox.MusicBoxInfo;
import com.github.tvbox.osc.wxwz.entity.musicbox.SearchTip;
import com.github.tvbox.osc.wxwz.entity.musicbox.SearchTipData;
import com.github.tvbox.osc.wxwz.entity.musicbox.SearchTipResponse;
import com.github.tvbox.osc.wxwz.network.MusicBoxApi;
import com.github.tvbox.osc.wxwz.ui.adapter.MusicBoxAdapter;
import com.github.tvbox.osc.wxwz.ui.dialog.MusicDialog;
import com.github.tvbox.osc.wxwz.util.TaskQueueJava;
import com.github.tvbox.osc.wxwz.view.MediaCenterImageViewButton;
import com.owen.tvrecyclerview.widget.TvRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends BaseActivity {
    private com.owen.tvrecyclerview.widget.TvRecyclerView mGridViewMusicList;
    private List<MusicBoxInfo> musicBoxInfoList = new ArrayList<>();
    private List<String> searchTipData = new ArrayList<>();
    private TaskQueueJava taskQueueJava;
    private MusicBoxAdapter musicBoxAdapter;
    private ArrayAdapter<String> searchTipAdapter;
    private com.github.tvbox.osc.wxwz.view.MediaCenterImageViewButton ivSearch;
    private AutoCompleteTextView atv;
    private MusicDialog musicDialog;
    private boolean isMore = true;
    private int loadPos=0;
    private android.widget.LinearLayout llLoad;
    private android.widget.ProgressBar pbLoad;
    private android.widget.LinearLayout llEmpty;


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent event) {
        if (event.type == RefreshEvent.TYPE_MUSIC_UPDATE) {
            List<MusicBoxInfo> boxInfoList = (List<MusicBoxInfo>)event.obj;
            musicBoxInfoList.addAll(boxInfoList);
            if (boxInfoList.size()<30){
                musicBoxAdapter.loadMoreEnd();
                isMore=false;
            }else {
                musicBoxAdapter.loadMoreComplete();
                isMore=true;
            }
            musicBoxAdapter.notifyDataSetChanged();
            if (musicBoxInfoList.size()!=0){
                mGridViewMusicList.smoothScrollToPosition(musicBoxInfoList.size() - boxInfoList.size());
                hideload();
            }else {
                showEmptyUI();
            }

            Log.i("wxwz","更新,list=" + musicBoxAdapter.getData().size() + ",lisR=" + musicBoxInfoList.size());
        }
        if (event.type ==RefreshEvent.TYPE_MUSIC_SEARCH_TIP){
            searchTipData.clear();
            SearchTipResponse searchTipResponse = (SearchTipResponse) event.obj;
            if (searchTipResponse.getData()!=null){
                for (SearchTip tip:searchTipResponse.getData()){
                    if (tip.getLableName().isEmpty()){
                        for (SearchTipData data:tip.getRecordDatas()){
                            searchTipData.add(data.getHintInfo());
                        }
                        Log.i("wxwz","列表已更新" + searchTipData.size());
                    }
                }
            }
            searchTipAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_music;
    }

    @Override
    protected void init() {
        initView();
        initEvent();
    }

    private void initEvent() {
        EventBus.getDefault().register(this);
        taskQueueJava = new TaskQueueJava();
        mGridViewMusicList.setHasFixedSize(true);
        mGridViewMusicList.setLayoutManager(new LinearLayoutManager(this.mContext));
        musicBoxAdapter = new MusicBoxAdapter(musicBoxInfoList);

        mGridViewMusicList.setAdapter(musicBoxAdapter);
        searchTipData.add("halo");
        searchTipAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, searchTipData);
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoad();
                taskQueueJava.addTask(()->{
                    musicBoxInfoList.clear();
                    loadPos = 1;
                    MusicBoxApi.searchMusic(atv.getText().toString().trim(),1);
                });
            }
        });

        musicBoxAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                musicDialog = new MusicDialog(MusicActivity.this);
                musicDialog.setPlayMode(musicDialog.PLAY_ONLINE_WYY);
                musicDialog.setWYYList(musicBoxInfoList);
                musicDialog.playPos(position);
                musicDialog.show();
            }
        });
        musicBoxAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                if (isMore){
                    loadPos++;
                    taskQueueJava.addTask(()->{
                        MusicBoxApi.searchMusic(atv.getText().toString().trim(),loadPos);
                    });
                }else {
                    musicBoxAdapter.loadMoreEnd();
                }
            }
        });
        atv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                taskQueueJava.addTask(()->{
                    //MusicBoxApi.searchTip(charSequence.toString().trim());
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initView() {
        mGridViewMusicList = (TvRecyclerView) findViewById(R.id.mGridViewMusicList);
        ivSearch = (MediaCenterImageViewButton) findViewById(R.id.iv_search);
        atv = findViewById(R.id.atv);
        llLoad =  findViewById(R.id.ll_load);
        pbLoad =  findViewById(R.id.pb_load);
        llEmpty =  findViewById(R.id.ll_empty);
    }

    private void showLoad(){
        pbLoad.setVisibility(View.VISIBLE);
        llEmpty.setVisibility(View.GONE);
        llLoad.setVisibility(View.VISIBLE);
    }
    private void showEmptyUI(){
        pbLoad.setVisibility(View.GONE);
        llEmpty.setVisibility(View.VISIBLE);
        llLoad.setVisibility(View.VISIBLE);
    }
    private void hideload(){
        llLoad.setVisibility(View.GONE);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        taskQueueJava.stop();
    }
}
