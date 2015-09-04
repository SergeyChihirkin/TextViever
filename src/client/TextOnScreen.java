package client;

import java.util.LinkedList;

public class TextOnScreen {
    private LinkedList<StringOnScreen> strings = new LinkedList<>();
    private int frstStrNumber, lastStrNumber;

    public LinkedList<StringOnScreen> getStrings() {
        return strings;
    }

    public int getFrstStrNumber() {
        return frstStrNumber;
    }

    public void setFrstStrNumber(int frstStrNumber) {
        this.frstStrNumber = frstStrNumber;
    }

    public int getLastStrNumber() {
        return lastStrNumber;
    }

    public void setLastStrNumber(int lastStrNumber) {
        this.lastStrNumber = lastStrNumber;
    }
}
