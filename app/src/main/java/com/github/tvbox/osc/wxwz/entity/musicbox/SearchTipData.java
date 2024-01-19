package com.github.tvbox.osc.wxwz.entity.musicbox;

import java.util.List;

public class SearchTipData {
    private List<String> tags;
    private String Use;
    private int IsRadio;
    private List<String> tags_v2;
    private int IsKlist;
    private String HintInfo;
    private int MatchCount;
    private long Hot;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getUse() {
        return Use;
    }

    public void setUse(String use) {
        Use = use;
    }

    public int getIsRadio() {
        return IsRadio;
    }

    public void setIsRadio(int isRadio) {
        IsRadio = isRadio;
    }

    public List<String> getTags_v2() {
        return tags_v2;
    }

    public void setTags_v2(List<String> tags_v2) {
        this.tags_v2 = tags_v2;
    }

    public int getIsKlist() {
        return IsKlist;
    }

    public void setIsKlist(int isKlist) {
        IsKlist = isKlist;
    }

    public String getHintInfo() {
        return HintInfo;
    }

    public void setHintInfo(String hintInfo) {
        HintInfo = hintInfo;
    }

    public int getMatchCount() {
        return MatchCount;
    }

    public void setMatchCount(int matchCount) {
        MatchCount = matchCount;
    }

    public long getHot() {
        return Hot;
    }

    public void setHot(long hot) {
        Hot = hot;
    }

    @Override
    public String toString() {
        return "SearchTipData{" +
                "tags=" + tags +
                ", Use='" + Use + '\'' +
                ", IsRadio=" + IsRadio +
                ", tags_v2=" + tags_v2 +
                ", IsKlist=" + IsKlist +
                ", HintInfo='" + HintInfo + '\'' +
                ", MatchCount=" + MatchCount +
                ", Hot=" + Hot +
                '}';
    }
}


