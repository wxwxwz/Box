package com.github.tvbox.osc.wxwz.network;

import android.util.Log;

import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.wxwz.entity.musicbox.MusicBoxInfo;
import com.github.tvbox.osc.wxwz.entity.musicbox.SearchTip;
import com.github.tvbox.osc.wxwz.entity.musicbox.SearchTipResponse;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MusicBoxApi {
    private static String baseUrl = "https://kwapi-api-iobiovqpvk.cn-beijing.fcapp.run";
    public static List<MusicBoxInfo> searchMusic(String key,int page){
        List<MusicBoxInfo> musicBoxInfoList = new ArrayList<>();
        OkGo.<List<MusicBoxInfo>>get(baseUrl + "/wyy/search?key=" + key + "&pn=" + page)
                .execute(new AbsCallback<List<MusicBoxInfo>>() {
                    @Override
                    public void onSuccess(Response<List<MusicBoxInfo>> response) {
                        if (response.isSuccessful()){
                            if (response.body()!=null){
                                musicBoxInfoList.addAll(response.body());
                                Log.i("wxwz","name=" + response.body().size());
                                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_MUSIC_UPDATE,musicBoxInfoList));
                            }else {
                                Log.i("wxwz","失败null" );
                            }

                        }else {Log.i("wxwz","失败s" );}

                    }

                    @Override
                    public void onError(Response<List<MusicBoxInfo>> response) {
                        super.onError(response);
                        Log.i("wxwz","失败e" );
                    }

                    @Override
                    public List<MusicBoxInfo> convertResponse(okhttp3.Response response) throws Throwable {
                        List<MusicBoxInfo> list = new ArrayList<>();

                        if (response.body()!=null){
                            String json = response.body().string();
                            TypeToken<List<MusicBoxInfo>> typeToken = new TypeToken<List<MusicBoxInfo>>() {};
                            list = new Gson().fromJson(json, typeToken.getType());
                        }
                        return list;
                    }
                });
        return musicBoxInfoList;
    }

    public static String getMusicUrl(long rid){
        return baseUrl + "/wyy/mp3?rid=" + rid;
    }

    public static String getMusicLrc(long rid){
        return baseUrl + "/wyy/lrc?rid=" + rid;
    }

    public static void searchTip(String key){
        if (key.length()==0){
            return;
        }
        OkGo.<SearchTipResponse>get("https://searchtip.kugou.com/getSearchTip?" + "MusicTipCount=10" + "&keyword=" + key + "&page=1&pagesize=10&userid=-1&platform=WebFilter&filter=2&iscorrection=1&privilege_filter=0" )
                .execute(new AbsCallback<SearchTipResponse>() {
                    @Override
                    public void onSuccess(Response<SearchTipResponse> response) {
                        if (response.body()!=null){
                            EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_MUSIC_SEARCH_TIP,response.body()));
                            Log.e("wxwz","成功");
                        }
                    }

                    @Override
                    public SearchTipResponse convertResponse(okhttp3.Response response) throws Throwable {
                        SearchTipResponse searchTipResponse = null;

                        if (response.body()!=null){
                            searchTipResponse = new Gson().fromJson(response.body().string(), SearchTipResponse.class);
                            Log.e("wxwz","tip=" + searchTipResponse.toString());
                        }

                        return searchTipResponse;
                    }
                });
    }
}
