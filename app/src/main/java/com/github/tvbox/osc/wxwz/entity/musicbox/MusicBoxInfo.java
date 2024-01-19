package com.github.tvbox.osc.wxwz.entity.musicbox;

public class MusicBoxInfo {
    private String name;
    private String artist;
    private long rid;
    private String pic;
    private boolean iswyy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getRid() {
        return rid;
    }

    public void setRid(long rid) {
        this.rid = rid;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public boolean isIswyy() {
        return iswyy;
    }

    public void setIswyy(boolean iswyy) {
        this.iswyy = iswyy;
    }
}
