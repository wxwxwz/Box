package com.github.tvbox.osc.wxwz.entity;

public class Theme {
    private int colorId;
    private String colorName;
    private int color;

    public Theme(int colorId, String colorName, int color) {
        this.colorId = colorId;
        this.colorName = colorName;
        this.color = color;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
