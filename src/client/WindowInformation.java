package client;

import java.awt.*;

public class WindowInformation {
    private int width, tabWidth;
    private FontMetrics frstFntMetrics, scndFntMetrics;

    public WindowInformation(int width, int tabWidth, FontMetrics frstFntMetrics,
                             FontMetrics scndFntMetrics) {
        this.width = width;
        this.tabWidth = tabWidth;
        this.frstFntMetrics = frstFntMetrics;
        this.scndFntMetrics = scndFntMetrics;
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
