package com.github.tvbox.osc.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.AbsXml;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.event.ServerEvent;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.ui.adapter.PinyinAdapter;
import com.github.tvbox.osc.ui.adapter.SearchAdapter;
import com.github.tvbox.osc.ui.dialog.RemoteDialog;
import com.github.tvbox.osc.ui.dialog.SearchCheckboxDialog;
import com.github.tvbox.osc.ui.tv.QRCodeGen;
import com.github.tvbox.osc.ui.tv.widget.SearchKeyboard;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.SearchHelper;
import com.github.tvbox.osc.util.js.JSEngine;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.github.tvbox.osc.wxwz.entity.GameLabel;
import com.github.tvbox.osc.wxwz.inface.IOnItemClickListener;
import com.github.tvbox.osc.wxwz.inface.IOnItemLongClickListener;
import com.github.tvbox.osc.wxwz.util.SharedPreferencesUtil;
import com.github.tvbox.osc.wxwz.view.LabelListView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class SearchActivity extends BaseActivity {
    private LinearLayout llLayout;
    private TvRecyclerView mGridView;
    private TvRecyclerView mGridViewWord;
    SourceViewModel sourceViewModel;
    private EditText etSearch;
    private ImageView tvSearch;
    private ImageView tvClear;
    private ImageView tvBackspace;
    private ImageView tvRemoteSearch;
    private SearchKeyboard keyboard;
    private TextView tvAddress;
    private ImageView ivQRCode;
    private SearchAdapter searchAdapter;
    private PinyinAdapter wordAdapter;
    private String searchTitle = "";
    private ImageView tvSearchCheckbox;
    private static HashMap<String, String> mCheckSources = null;
    private SearchCheckboxDialog mSearchCheckboxDialog = null;
    private LabelListView mLabelListView;
    private ArrayList<GameLabel> labelList = new ArrayList<GameLabel>();

    private int maxSaveHistory = 20;
    private String saveSP = "SearchActivity";
    private String saveSPIsNull = "isHistoryNull";

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_search;
    }

    @Override
    protected void init() {
        initView();
        initViewModel();
        initData();
    }

    private List<Runnable> pauseRunnable = null;

    @Override
    protected void onResume() {
        super.onResume();
        if (pauseRunnable != null && pauseRunnable.size() > 0) {
            searchExecutorService = Executors.newFixedThreadPool(5);
            allRunCount.set(pauseRunnable.size());
            for (Runnable runnable : pauseRunnable) {
                searchExecutorService.execute(runnable);
            }
            pauseRunnable.clear();
            pauseRunnable = null;
        }
    }

    private boolean isKeyboardHidden() {
        final View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        return rootView.getBottom() == r.bottom;
    }

    private void initView() {
        EventBus.getDefault().register(this);
        llLayout = findViewById(R.id.llLayout);
        etSearch = findViewById(R.id.etSearch);
        tvSearch = findViewById(R.id.tvSearch);
        tvSearchCheckbox = findViewById(R.id.tvSearchCheckbox);
        tvClear = findViewById(R.id.tvClear);
        tvAddress = findViewById(R.id.tvAddress);
        ivQRCode = findViewById(R.id.ivQRCode);
        mGridView = findViewById(R.id.mGridView);
        tvBackspace = findViewById(R.id.tv_backspace);
        tvRemoteSearch = findViewById(R.id.tvRemoteSearch);
        keyboard = findViewById(R.id.keyBoardRoot);
        tvSearch.setOnFocusChangeListener(focusChangeListener);
        tvRemoteSearch.setOnFocusChangeListener(focusChangeListener);
        tvClear.setOnFocusChangeListener(focusChangeListener);
        tvBackspace.setOnFocusChangeListener(focusChangeListener);
        tvSearchCheckbox.setOnFocusChangeListener(focusChangeListener);

        mGridViewWord = findViewById(R.id.mGridViewWord);
        mGridViewWord.setHasFixedSize(true);
        mGridViewWord.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        wordAdapter = new PinyinAdapter();
        mGridViewWord.setAdapter(wordAdapter);
        //history
        initDatas();
        mLabelListView = findViewById(R.id.mGridViewSHistory);
        mLabelListView.setSize(14);
        mLabelListView.setData(labelList);
        mLabelListView.setOnClickListener(new IOnItemClickListener() {

            @Override
            public void onClick(String name, int position) {
                etSearch.setText(name);
                etSearch.setSelection(etSearch.getText().length());
                search(name);
                mLabelListView.setData(labelList);
            }
        });

        mLabelListView.setOnLongClickListener(new IOnItemLongClickListener() {
            @Override
            public void onLongClick(String name, int position) {
                delHistoryData(position + 1);
            }
        });
        // lite
        if (Hawk.get(HawkConfig.SEARCH_VIEW, 0) == 0)
            mGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
            // with preview
        else
            mGridView.setLayoutManager(new V7GridLayoutManager(this.mContext, 3));
        searchAdapter = new SearchAdapter();
        mGridView.setAdapter(searchAdapter);
        // Allow Dpad Key switch to other focus
        etSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (isKeyboardHidden()) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                    .showSoftInput(etSearch, 0);
                            return false;
                        }
                    } else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        int len = etSearch.getText().length();
                        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                            // Avoid show ime keyboard bug
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                            etSearch.focusSearch(View.FOCUS_DOWN).requestFocus();
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && (len == 0 || etSearch.getSelectionStart() == len)) {
                            etSearch.focusSearch(View.FOCUS_RIGHT).requestFocus();
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        wordAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                search(wordAdapter.getItem(position));
            }
        });
        mGridView.setHasFixedSize(true);
        // lite
        if (Hawk.get(HawkConfig.SEARCH_VIEW, 0) == 0)
            mGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
            // with preview
        else
            mGridView.setLayoutManager(new V7GridLayoutManager(this.mContext, 3));
        searchAdapter = new SearchAdapter();
        mGridView.setAdapter(searchAdapter);
        searchAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                Movie.Video video = searchAdapter.getData().get(position);
                if (video != null) {
                    try {
                        if (searchExecutorService != null) {
                            pauseRunnable = searchExecutorService.shutdownNow();
                            searchExecutorService = null;
                            JSEngine.getInstance().stopAll();
                        }
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("id", video.id);
                    bundle.putString("sourceKey", video.sourceKey);
                    jumpActivity(DetailActivity.class, bundle);
                }
            }
        });
        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                String wd = etSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(wd)) {
                    search(wd);
                } else {
                    Toast.makeText(mContext, getString(R.string.search_input), Toast.LENGTH_SHORT).show();
                }
            }
        });
        tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                etSearch.setText("");
            }
        });
        tvBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etSearch.getText().toString().trim();
                if (text.length() > 0) {
                    text = text.substring(0, text.length() - 1);
                    etSearch.setText(text);
                    etSearch.setSelection(text.length());
                }
                if (text.length() > 0) {
                    loadRec(text);
                }
            }
        });
        tvRemoteSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoteDialog remoteDialog = new RemoteDialog(mContext);
                remoteDialog.show();
            }
        });
        keyboard.setOnSearchKeyListener(new SearchKeyboard.OnSearchKeyListener() {
            @Override
            public void onSearchKey(int pos, String key) {
                if (pos >= 0) {
                    String text = etSearch.getText().toString().trim();
                    text += key;
                    etSearch.setText(text);
                    if (text.length() > 0) {
                        loadRec(text);
                    }
                } /*else if (pos == 1) {
                    String text = etSearch.getText().toString().trim();
                    if (text.length() > 0) {
                        text = text.substring(0, text.length() - 1);
                        etSearch.setText(text);
                    }
                    if (text.length() > 0) {
                        loadRec(text);
                    }
                } else if (pos == 0) {
                    RemoteDialog remoteDialog = new RemoteDialog(mContext);
                    remoteDialog.show();
                }*/
            }
        });
        tvSearchCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSearchCheckboxDialog == null) {
                    List<SourceBean> allSourceBean = ApiConfig.get().getSourceBeanList();
                    List<SourceBean> searchAbleSource = new ArrayList<>();
                    for (SourceBean sourceBean : allSourceBean) {
                        if (sourceBean.isSearchable()) {
                            searchAbleSource.add(sourceBean);
                        }
                    }
                    mSearchCheckboxDialog = new SearchCheckboxDialog(SearchActivity.this, searchAbleSource, mCheckSources);
                }
                mSearchCheckboxDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
                mSearchCheckboxDialog.show();
            }
        });
        setLoadSir(llLayout);
    }

    private void initViewModel() {
        sourceViewModel = new ViewModelProvider(this).get(SourceViewModel.class);
    }

    private void initDatas() {


        if (SharedPreferencesUtil.readByBool(this,saveSP,saveSPIsNull,true)){
            //初始化数量
            GameLabel label = new GameLabel();
            label.name = "0";
            label.textColor = "FFFFFF";
            labelList.add(0,label);
            SharedPreferencesUtil.SaveStr(this,saveSP,"history",labelList);
            SharedPreferencesUtil.SaveBool(this,saveSP,saveSPIsNull,false);
        }else {
            labelList = SharedPreferencesUtil.readByStr(this,saveSP,"history");
            if (labelList.size()==0){
                //初始化数量
                GameLabel label = new GameLabel();
                label.name = "0";
                label.textColor = "FFFFFF";
                labelList.add(0,label);
                SharedPreferencesUtil.SaveStr(this,saveSP,"history",labelList);
            }
        }


    }

    private void addHistoryData(String name){
        GameLabel label = new GameLabel();
        label.name = name;
        label.textColor = "FFFFFF";
        //历史记录最大值

        if (labelList.size() > (int)Hawk.get(HawkConfig.HOME_NUM,20)){
            delHistoryData(labelList.size() - 1);
        }
        if (labelList.size()!=0){
            for (int i = 1;i < labelList.size();i++){
                if (labelList.get(i).name.contains(name)){
                    delHistoryData(i);
                    labelList.add(1,label);
                    SharedPreferencesUtil.SaveStr(this,saveSP,"history",labelList);
                    return;
                }
            }
        }

        labelList.add(1,label);
        //Hawk.put(HawkConfig.SEARCH_HISTORY,labelList);
        SharedPreferencesUtil.SaveStr(this,saveSP,"history",labelList);
        mLabelListView.setData(labelList);
    }

    private void delHistoryData(int item){
        labelList.remove(item);
        SharedPreferencesUtil.SaveStr(this,saveSP,"history",labelList);
        mLabelListView.setData(labelList);
    }

    private final View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
                v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            else
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
        }
    };

    /**
     * 拼音联想
     */
    private void loadRec(String key) {
        OkGo.get("https://tv.aiseet.atianqi.com/i-tvbin/qtv_video/search/get_search_smart_box")
                .params("format", "json")
                .params("page_num", 0)
                .params("page_size", 50) //随便改
                .params("key", key)
                .execute(new AbsCallback() {
                    @Override
                    public void onSuccess(Response response) {
                        try {
                            ArrayList hots = new ArrayList<>();
                            String result = (String) response.body();
                            Gson gson = new Gson();
                            JsonElement json = gson.fromJson(result, JsonElement.class);
                            JsonArray groupDataArr = json.getAsJsonObject()
                                    .get("data").getAsJsonObject()
                                    .get("search_data").getAsJsonObject()
                                    .get("vecGroupData").getAsJsonArray()
                                    .get(0).getAsJsonObject()
                                    .get("group_data").getAsJsonArray();
                            for (JsonElement groupDataElement : groupDataArr) {
                                JsonObject groupData = groupDataElement.getAsJsonObject();
                                String keywordTxt = groupData.getAsJsonObject("dtReportInfo")
                                        .getAsJsonObject("reportData")
                                        .get("keyword_txt").getAsString();
                                hots.add(keywordTxt.trim());
                            }
                            wordAdapter.setNewData(hots);
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return response.body().string();
                    }
                });
    }

    private void initData() {
        refreshQRCode();
        initCheckedSourcesForSearch();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("title")) {
            String title = intent.getStringExtra("title");
            showLoading();
            search(title);
        }
        // 加载热词
        loadHotSearch();
    }

    //load hot search
    private void loadHotSearch() {
        OkGo.<String>get("https://node.video.qq.com/x/api/hot_search")
                .params("channdlId", "0")
                .params("_", System.currentTimeMillis())
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            ArrayList<String> hots = new ArrayList<>();
                            JsonObject mapResult = JsonParser.parseString(response.body())
                                    .getAsJsonObject()
                                    .get("data").getAsJsonObject()
                                    .get("mapResult").getAsJsonObject();
                            List<String> groupIndex = Arrays.asList("0", "1", "2", "3", "5");
                            for (String index : groupIndex) {
                                JsonArray itemList = mapResult.get(index).getAsJsonObject()
                                        .get("listInfo").getAsJsonArray();
                                for (JsonElement ele : itemList) {
                                    JsonObject obj = (JsonObject) ele;
                                    String hotKey = obj.get("title").getAsString().trim().replaceAll("<|>|《|》|-", "").split(" ")[0];
                                    if (!hots.contains(hotKey))
                                        hots.add(hotKey);
                                }
                            }

                            wordAdapter.setNewData(hots);
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return response.body().string();
                    }
                });
    }

    private void refreshQRCode() {
        String address = ControlManager.get().getAddress(false);
        tvAddress.setText(String.format("远程搜索使用手机/电脑扫描下面二维码或者直接浏览器访问地址\n%s", address));
        ivQRCode.setImageBitmap(QRCodeGen.generateBitmap(address, 300, 300));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void server(ServerEvent event) {
        if (event.type == ServerEvent.SERVER_SEARCH) {
            String title = (String) event.obj;
            showLoading();
            search(title);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent event) {
        if (event.type == RefreshEvent.TYPE_SEARCH_RESULT) {
            try {
                searchData(event.obj == null ? null : (AbsXml) event.obj);
            } catch (Exception e) {
                searchData(null);
            }
        }
    }

    private void initCheckedSourcesForSearch() {
        mCheckSources = SearchHelper.getSourcesForSearch();
    }

    public static void setCheckedSourcesForSearch(HashMap<String, String> checkedSources) {
        mCheckSources = checkedSources;
    }

    private void search(String title) {
        cancel();
        showLoading();
        this.searchTitle = title;
        mGridView.setVisibility(View.INVISIBLE);
        searchAdapter.setNewData(new ArrayList<>());
        searchResult();
        addHistoryData(title);
    }

    private ExecutorService searchExecutorService = null;
    private final AtomicInteger allRunCount = new AtomicInteger(0);

    private void searchResult() {
        try {
            if (searchExecutorService != null) {
                searchExecutorService.shutdownNow();
                searchExecutorService = null;
                JSEngine.getInstance().stopAll();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            searchAdapter.setNewData(new ArrayList<>());
            allRunCount.set(0);
        }
        searchExecutorService = Executors.newFixedThreadPool(5);
        List<SourceBean> searchRequestList = new ArrayList<>();
        searchRequestList.addAll(ApiConfig.get().getSourceBeanList());
        SourceBean home = ApiConfig.get().getHomeSourceBean();
        searchRequestList.remove(home);
        searchRequestList.add(0, home);

        ArrayList<String> siteKey = new ArrayList<>();
        for (SourceBean bean : searchRequestList) {
            if (!bean.isSearchable()) {
                continue;
            }
            if (mCheckSources != null && !mCheckSources.containsKey(bean.getKey())) {
                continue;
            }
            siteKey.add(bean.getKey());
            allRunCount.incrementAndGet();
        }
        if (siteKey.size() <= 0) {
            Toast.makeText(mContext, getString(R.string.search_site), Toast.LENGTH_SHORT).show();
            showEmpty();
            return;
        }
        for (String key : siteKey) {
            searchExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    sourceViewModel.getSearch(key, searchTitle);
                }
            });
        }
    }

    private boolean matchSearchResult(String name, String searchTitle) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(searchTitle)) return false;
        searchTitle = searchTitle.trim();
        String[] arr = searchTitle.split("\\s+");
        int matchNum = 0;
        for (String one : arr) {
            if (name.contains(one)) matchNum++;
        }
        return matchNum == arr.length;
    }

    private void searchData(AbsXml absXml) {
        if (absXml != null && absXml.movie != null && absXml.movie.videoList != null && absXml.movie.videoList.size() > 0) {
            List<Movie.Video> data = new ArrayList<>();
            for (Movie.Video video : absXml.movie.videoList) {
                data.add(video);
            }
            if (searchAdapter.getData().size() > 0) {
                searchAdapter.addData(data);
            } else {
                showSuccess();
                mGridView.setVisibility(View.VISIBLE);
                searchAdapter.setNewData(data);
            }
        }

        int count = allRunCount.decrementAndGet();
        if (count <= 0) {
            if (searchAdapter.getData().size() <= 0) {
                showEmpty();
            }
            cancel();
        }
    }

    private void cancel() {
        OkGo.getInstance().cancelTag("search");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancel();
        try {
            if (searchExecutorService != null) {
                searchExecutorService.shutdownNow();
                searchExecutorService = null;
                JSEngine.getInstance().stopAll();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
    }
}