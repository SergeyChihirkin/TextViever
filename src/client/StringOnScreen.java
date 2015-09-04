package client;

import java.util.LinkedList;

public class StringOnScreen {
    private LinkedList<String> elements;
    private int heightOfStr;
    private int strFrstWordFntMetricsInf;

    public StringOnScreen(LinkedList<String> elements, int heightOfStr, int strFrstWordFntMetricsInf) {
        this.elements = elements;
        this.heightOfStr = heightOfStr;
        this.strFrstWordFntMetricsInf = strFrstWordFntMetricsInf;
    }

    public LinkedList<String> getElements() {
        return elements;
    }

    public int getStrFrstWordFntMetricsInf() {
        return strFrstWordFntMetricsInf;
    }
}
