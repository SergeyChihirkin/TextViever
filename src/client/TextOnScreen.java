package client;

import java.util.LinkedList;

public class TextOnScreen {
    private LinkedList<StringOnScreen> frstChunkStrings = new LinkedList<>();
    private LinkedList<StringOnScreen> scndChunkStrings = new LinkedList<>();
    private int frstStrNumber, lastStrNumber;
    private int frstChunkNum = 0;


    public LinkedList<StringOnScreen> getFrstChunkStrings() {
        return frstChunkStrings;
    }

    public LinkedList<StringOnScreen> getScndChunkStrings() {
        return scndChunkStrings;
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

    public int getFrstChunkNum() {
        return frstChunkNum;
    }

    public void nextChunk(LinkedList<StringOnScreen> strings) {
        frstStrNumber -= frstChunkStrings.size();
        frstChunkStrings = scndChunkStrings;
        scndChunkStrings = strings;
        frstChunkNum++;
    }

    public void previousChunk(LinkedList<StringOnScreen> strings) {
        scndChunkStrings = frstChunkStrings;
        frstChunkStrings = strings;
        frstChunkNum--;
    }
}
