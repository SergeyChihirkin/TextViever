package client;

import java.awt.*;

public class WindowInformation {
    private int height, width, tabWidth;
    private FontMetrics frstFntMetrics, scndFntMetrics;

    public WindowInformation(int height, int width, int tabWidth, FontMetrics frstFntMetrics,
                             FontMetrics scndFntMetrics) {
        this.height = height;
        this.width = width;
        this.tabWidth = tabWidth;
        this.frstFntMetrics = frstFntMetrics;
        this.scndFntMetrics = scndFntMetrics;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public FontMetrics getFrstFntMetrics() {
        return frstFntMetrics;
    }

    public FontMetrics getScndFntMetrics() {
        return scndFntMetrics;
    }
}
