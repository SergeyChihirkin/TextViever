package client;

import java.util.LinkedList;

public class TextOnScreen {
    private LinkedList<StringOnScreen> strings = new LinkedList<>();
    private int frstStrNumber, lastStrNumber;


    public LinkedList<StringOnScreen> getStrings() {
        return strings;
    }

    public void setFrstStrNumber(int frstStrNumber) {
        this.frstStrNumber = frstStrNumber;
    }

    public void setLastStrNumber(int lastStrNumber) {
        this.lastStrNumber = lastStrNumber;
    }

    public int getFrstStrNumber() {
        return frstStrNumber;
    }

    public int getLastStrNumber() {
        return lastStrNumber;
    }
}
