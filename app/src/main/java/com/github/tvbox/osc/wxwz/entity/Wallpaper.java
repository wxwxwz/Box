package com.github.tvbox.osc.wxwz.entity;

import java.io.File;

public class Wallpaper {
    private String name;
    private String path;
    private int ResPath;
    private File file;
    /**
     * type = 0 url
     * type = 1 R.drawale
     * type = 2 File
     */
    private int type=0;

    public Wallpaper(String name, String path, int type) {
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public Wallpaper(String name, int resPath, int type) {
        this.name = name;
        this.ResPath = resPath;
        this.type = type;
    }

    public Wallpaper(String name, File file, int type) {
        this.name = name;
        this.file = file;
        this.type = type;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResPath() {
        return ResPath;
    }

    public void setResPath(int resPath) {
        ResPath = resPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
