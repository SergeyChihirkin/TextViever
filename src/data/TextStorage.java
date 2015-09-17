package data;

import java.util.LinkedList;

public class TextStorage {
    private LinkedList<String> strings = new LinkedList<>();

    public void addString(String str) {
        strings.add(str);
    }

    public LinkedList<String> getStrings() {
        return strings;
    }

    public void setStrings(LinkedList<String> strings) {
        this.strings = strings;
    }
}
