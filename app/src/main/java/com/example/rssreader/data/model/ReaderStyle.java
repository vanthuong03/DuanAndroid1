package com.example.rssreader.data.model;

import android.graphics.Color;

import java.io.Serializable;

public class ReaderStyle implements Serializable {

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SEPIA = 2;

    public static final int FONT_SIZE_SMALL = 0;   // 14sp
    public static final int FONT_SIZE_MEDIUM = 1;  // 16sp
    public static final int FONT_SIZE_LARGE = 2;   // 19sp
    public static final int FONT_SIZE_XLARGE = 3;  // 22sp

    private int fontSize;
    private int theme;

    public ReaderStyle() {
        this.fontSize = FONT_SIZE_MEDIUM;
        this.theme = THEME_LIGHT;
    }

    public ReaderStyle(int fontSize, int theme) {
        this.fontSize = fontSize;
        this.theme = theme;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }

    public float getFontSizeInSp() {
        switch (fontSize) {
            case FONT_SIZE_SMALL: return 14f;
            case FONT_SIZE_LARGE: return 19f;
            case FONT_SIZE_XLARGE: return 22f;
            case FONT_SIZE_MEDIUM:
            default: return 16f;
        }
    }

    public int getBackgroundColor() {
        switch (theme) {
            case THEME_DARK: return Color.parseColor("#121212");
            case THEME_SEPIA: return Color.parseColor("#F4ECD8");
            case THEME_LIGHT:
            default: return Color.parseColor("#FFFFFF");
        }
    }

    public int getTextColor() {
        switch (theme) {
            case THEME_DARK: return Color.parseColor("#E0E0E0");
            case THEME_SEPIA: return Color.parseColor("#5B4636");
            case THEME_LIGHT:
            default: return Color.parseColor("#202124");
        }
    }

    public int getSecondaryTextColor() {
        switch (theme) {
            case THEME_DARK: return Color.parseColor("#9E9E9E");
            case THEME_SEPIA: return Color.parseColor("#8C7662");
            case THEME_LIGHT:
            default: return Color.parseColor("#5F6368");
        }
    }
}
