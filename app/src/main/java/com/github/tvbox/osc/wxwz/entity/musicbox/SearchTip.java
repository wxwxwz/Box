package com.github.tvbox.osc.wxwz.entity.musicbox;

import java.util.List;

public class SearchTip {
    private int RecordCount;
    private String LableName;
    private List<SearchTipData> RecordDatas;


    public int getRecordCount() {
        return RecordCount;
    }

    public void setRecordCount(int recordCount) {
        RecordCount = recordCount;
    }

    public String getLableName() {
        return LableName;
    }

    public void setLableName(String lableName) {
        LableName = lableName;
    }

    public List<SearchTipData> getRecordDatas() {
        return RecordDatas;
    }

    public void setRecordDatas(List<SearchTipData> recordDatas) {
        RecordDatas = recordDatas;
    }

    @Override
    public String toString() {
        return "SearchTip{" +
                "RecordCount=" + RecordCount +
                ", LableName='" + LableName + '\'' +
                ", RecordDatas=" + RecordDatas +
                '}';
    }
}
