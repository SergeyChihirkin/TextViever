package client;

import java.util.LinkedList;

public class StringOnScreen {
    private LinkedList<StrElement> strElements;
    private int heightOfStr;

    public StringOnScreen(LinkedList<StrElement> strElements, int heightOfStr) {
        this.strElements = strElements;
        this.heightOfStr = heightOfStr;
    }

    public LinkedList<StrElement> getStrElements() {
        return strElements;
    }

    public int getHeightOfStr() {
        return heightOfStr;
    }
}
