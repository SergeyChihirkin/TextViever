package client;

import data.TextStorage;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;

public class TextOnScreenManager {
    private final Charset CHARSET = Charset.forName("UTF-8");
    private final int MAX_FNT_MTRCS_INF = 4;

    private int windowHeight, windowWidth, tabWidth;
    private FontMetrics frstFntMetrics, scndFntMetrics;

    private int curStrWdth;
    private LinkedList<String> strElements;
    private int strFrstWordFntMetricsInf;
    private int curStrHght;

    private State state;
    private int curWordFntMetricsInf;
    private FontMetrics curWordFntMetrics;
    private int wrdHght, curWrdWdth;
    private ByteArrayOutputStream baos;


    public TextOnScreenManager(WindowInformation windowInformation) {
        frstFntMetrics = windowInformation.getFrstFntMetrics();
        scndFntMetrics = windowInformation.getScndFntMetrics();
        windowHeight = windowInformation.getHeight();
        windowWidth = windowInformation.getWidth();
        tabWidth = windowInformation.getTabWidth();
    }

    TextOnScreen createTextOnScreen(TextStorage textStorage) {
        init();

        TextOnScreen textOnScreen = new TextOnScreen();

        for (String stringOfText : textStorage.getStrings()) {
            LinkedList<StringOnScreen> stringsOnScreen = getStringsOnScreen(stringOfText);
            textOnScreen.getStrings().addAll(stringsOnScreen);
        }

        addFrstAndLastStrNumbrs(0, textOnScreen);

        return textOnScreen;
    }

    private void addFrstAndLastStrNumbrs(int frstStrNum, TextOnScreen textOnScreen) {
        textOnScreen.setFrstStrNumber(frstStrNum);
        int height = 0;
        int i = frstStrNum;
        while (height < windowHeight && i < textOnScreen.getStrings().size()) {
            height += textOnScreen.getStrings().get(i).getHeightOfStr();
            i++;
        }

        final int lastStrNumber = i == frstStrNum ? i : i - 1;
        textOnScreen.setLastStrNumber(lastStrNumber);
    }

    private void init() {
        curWordFntMetricsInf = 0;
        strFrstWordFntMetricsInf = 0;
        curWordFntMetrics = frstFntMetrics;
        curStrHght = curWordFntMetrics.getHeight();
        curWrdWdth = 0;
        wrdHght = 0;
        state = State.BEGIN;
        baos = new ByteArrayOutputStream();
    }

    private LinkedList<StringOnScreen> getStringsOnScreen(String stringOfText) {
        LinkedList<StringOnScreen> stringsOnScreen = new LinkedList<>();

        if (stringOfText.length() == 0) {
            strFrstWordFntMetricsInf = curWordFntMetricsInf;
            stringsOnScreen.add(createStringOnScreenAndResetFields());
            return stringsOnScreen;
        }

        resetFields();

        for (int i = 0; i < stringOfText.length(); i++) {
            char c = stringOfText.charAt(i);
            boolean isStringEnded = putChar(c);
            if (isStringEnded)
                stringsOnScreen.add(createStringOnScreenAndResetFields());
        }

        if (state == State.LINE_PREFIX && stringsOnScreen.size() == 0) {
            strFrstWordFntMetricsInf = curWordFntMetricsInf;
            stringsOnScreen.add(createStringOnScreenAndResetFields());
            state = State.BEGIN;
            return stringsOnScreen;
        }

        if (baos.size() != 0) {
            String wordFromBaos = getWordFromBaos();
            strElements.add(wordFromBaos);
            if (state == State.GET_FIRST_WORD)
                strFrstWordFntMetricsInf = curWordFntMetricsInf;
            stringsOnScreen.add(createStringOnScreenAndResetFields());
            if (!wordFromBaos.contains(" ") && !wordFromBaos.contains("\t"))
                nextFntMetrics();
            state = State.BEGIN;
        }

        return stringsOnScreen;
    }

    private StringOnScreen createStringOnScreenAndResetFields() {
        final StringOnScreen stringOnScreen = new StringOnScreen(strElements, curStrHght, strFrstWordFntMetricsInf);
        resetFields();
        return stringOnScreen;
    }

    private void resetFields() {
        strElements = new LinkedList<>();
        curStrWdth = 0;
        curStrHght = curWordFntMetrics.getHeight();
    }

    private boolean putChar(char c) {
        switch (state) {
            case BEGIN:
                return begin(c);

            case LINE_PREFIX:
                return linePreffix(c);

            case GET_FIRST_WORD:
                return getFirstWord(c);

            case WAIT_FOR_NEXT_WORD:
                return waitForNextWord(c);

            case GET_WORD:
                return getWord(c);

            default:
                throw new IllegalStateException();
        }
    }

    private boolean begin(char c) {
        if (isBlankSymbol(c)) {
            state = State.LINE_PREFIX;
            return linePreffix(c);
        }

        state = State.GET_FIRST_WORD;
        return getFirstWord(c);
    }

    private boolean linePreffix(char c) {
        if (!isBlankSymbol(c)) {
            state = State.GET_FIRST_WORD;
            return getFirstWord(c);
        } else
            return false;
    }

    private boolean getFirstWord(char c) {
        if (isBlankSymbol(c)) {
            state = State.WAIT_FOR_NEXT_WORD;
            strElements.add(getWordFromBaos());
            strFrstWordFntMetricsInf = curWordFntMetricsInf;
            nextFntMetrics();
            return waitForNextWord(c);
        }

        int charWidth = findCharWidth(c);
        if (isNotFitWindow(charWidth)) {
            strElements.add(getWordFromBaos());
            addCharToWord(c, charWidth);
            state = State.GET_FIRST_WORD;
            strFrstWordFntMetricsInf = curWordFntMetricsInf;
            return true;
        }

        addCharToWord(c, charWidth);

        return false;
    }

    private boolean waitForNextWord(char c) {
        int charWidth = findCharWidth(c);
        if (isNotFitWindow(charWidth)) {
            strElements.add(getWordFromBaos());
            if (!isBlankSymbol(c)) {
                addCharToWord(c, charWidth);
                state = State.GET_FIRST_WORD;
            } else
                state = State.BEGIN;

            return true;
        }

        if (!isBlankSymbol(c)) {
            strElements.add(getWordFromBaos());
            state = State.GET_WORD;
            return getWord(c);
        }

        addCharToWord(c, charWidth);

        return false;
    }

    private boolean getWord(char c) {
        int charWidth = findCharWidth(c);
        if (isNotFitWindow(charWidth)) {
            if (isBlankSymbol(c)) {
                strElements.add(getWordFromBaos());
                nextFntMetrics();
                state = State.BEGIN;
            } else {
                addCharToWord(c, charWidth);
                state = State.GET_FIRST_WORD;
            }

            return true;
        }

        if (isBlankSymbol(c)) {
            strElements.add(getWordFromBaos());
            state = State.WAIT_FOR_NEXT_WORD;
            nextFntMetrics();
            return waitForNextWord(c);
        }

        addCharToWord(c, charWidth);

        return false;
    }

    private void nextFntMetrics() {
        curWordFntMetricsInf = (curWordFntMetricsInf + 1) % MAX_FNT_MTRCS_INF;

        curWordFntMetrics = curWordFntMetricsInf < 2 ? frstFntMetrics : scndFntMetrics;
    }

    private void addCharToWord(char c, int charWidth) {
        wrdHght = curWordFntMetrics.getHeight();
        curWrdWdth += charWidth;
        baos.write(c);
    }

    private String getWordFromBaos() {
        String word = new String(baos.toByteArray(), CHARSET);
        baos.reset();
        curStrWdth += curWrdWdth;
        curWrdWdth = 0;
        curStrHght = Math.max(curStrHght, wrdHght);

        return word;
    }

    private int findCharWidth(char c) {
        if (c == '\t') {
            final int offset = curStrWdth + curWrdWdth;
            return (offset / tabWidth + 1) * tabWidth - offset;
        }

        return curWordFntMetrics.charWidth(c);
    }

    private boolean isBlankSymbol(char c) {
        return c == ' ' || c == '\t';
    }

    private boolean isNotFitWindow(int charWidth) {
        return windowWidth <= curStrWdth + curWrdWdth + charWidth;
    }

    public void nextString(TextOnScreen textOnScreen) {
        int frstStrNumber = textOnScreen.getFrstStrNumber();
        if (frstStrNumber != textOnScreen.getLastStrNumber()) {
            frstStrNumber++;
            addFrstAndLastStrNumbrs(frstStrNumber, textOnScreen);
        }
    }

    public void previousString(TextOnScreen textOnScreen) {
        int frstStrNumber = textOnScreen.getFrstStrNumber();
        if (frstStrNumber != 0) {
            frstStrNumber--;
            addFrstAndLastStrNumbrs(frstStrNumber, textOnScreen);
        }
    }

    public void nextPage(TextOnScreen textOnScreen) {
        addFrstAndLastStrNumbrs(textOnScreen.getLastStrNumber(), textOnScreen);
    }

    public void previousPage(TextOnScreen textOnScreen) {
        int oldFrstStrNum = textOnScreen.getFrstStrNumber();

        if (oldFrstStrNum == textOnScreen.getLastStrNumber()) {
            previousString(textOnScreen);
            oldFrstStrNum = textOnScreen.getFrstStrNumber();
        }

        while (oldFrstStrNum < textOnScreen.getLastStrNumber() && textOnScreen.getFrstStrNumber() != 0)
            previousString(textOnScreen);
    }

    static enum State {
        BEGIN, GET_FIRST_WORD, WAIT_FOR_NEXT_WORD, GET_WORD, LINE_PREFIX
    }
}
