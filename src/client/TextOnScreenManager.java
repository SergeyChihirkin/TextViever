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

        return textOnScreen;
    }

    private void init() {
        curWordFntMetricsInf = 0;
        strFrstWordFntMetricsInf = 0;
        curWordFntMetrics = frstFntMetrics;
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
        final StringOnScreen stringOnScreen = new StringOnScreen(strElements, 0, strFrstWordFntMetricsInf);
        resetFields();
        return stringOnScreen;
    }

    private void resetFields() {
        strElements = new LinkedList<>();
        curStrWdth = 0;
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

    static enum State {
        BEGIN, GET_FIRST_WORD, WAIT_FOR_NEXT_WORD, GET_WORD, LINE_PREFIX
    }
}
