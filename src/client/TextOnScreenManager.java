package client;

import data.TextStorage;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;

public class TextOnScreenManager {
    private final Charset CHARSET = Charset.forName("UTF-8");
    private final int MAX_WORD_IN_GROUP_NUM = 2;
    private final int MAX_FNT_METRICS_NUM = 2;

    private int windowWidth, tabWidth;
    private FontMetrics frstFntMetrics, scndFntMetrics;

    private int curStrWdth;
    private LinkedList<StrElement> strElements;
    private int curStrHght;

    private State state;
    private int curWordInGroupNum = 0;
    private FontMetrics curWordFntMetrics;
    private int curWordFntMetricsNum = 0;
    private int elHght, elWdth;
    private int wordHght, wordWdth;
    private ByteArrayOutputStream elBaos;
    private ByteArrayOutputStream wordBaos;


    public TextOnScreenManager(WindowInformation windowInformation) {
        frstFntMetrics = windowInformation.getFrstFntMetrics();
        scndFntMetrics = windowInformation.getScndFntMetrics();
        windowWidth = windowInformation.getWidth();
        tabWidth = windowInformation.getTabWidth();
    }

    TextOnScreen createTextOnScreen(TextStorage textStorage) {
        init();

        final TextOnScreen textOnScreen = new TextOnScreen();

        for (String stringOfText : textStorage.getStrings()) {
            LinkedList<StringOnScreen> stringsOnScreen = getStringsOnScreen(stringOfText);
            textOnScreen.getFrstChunkStrings().addAll(stringsOnScreen);
        }

        return textOnScreen;
    }

    private void init() {
        curWordFntMetrics = frstFntMetrics;
        curStrHght = curWordFntMetrics.getHeight();
        elWdth = 0;
        elHght = 0;
        state = State.BEGIN;
        elBaos = new ByteArrayOutputStream();
        wordBaos = new ByteArrayOutputStream();
    }

    private LinkedList<StringOnScreen> getStringsOnScreen(String stringOfText) {
        final LinkedList<StringOnScreen> stringsOnScreen = new LinkedList<>();

        if (stringOfText.length() == 0) {
            stringsOnScreen.add(createStringOnScreenAndResetFields());
            return stringsOnScreen;
        }

        resetFields();

        for (int i = 0; i < stringOfText.length(); i++) {
            final char c = stringOfText.charAt(i);
            final boolean isStringEnded = putChar(c);
            if (isStringEnded)
                stringsOnScreen.add(createStringOnScreenAndResetFields());
        }

        if (state == State.LINE_PREFIX && stringsOnScreen.size() == 0) {
            stringsOnScreen.add(createStringOnScreenAndResetFields());
            state = State.BEGIN;
            return stringsOnScreen;
        }

        if (elBaos.size() != 0 || wordBaos.size() != 0) {
            if (wordBaos.size() != 0) {
                addWordToElBaos();
                strElements.add(new StrElement(ElementType.GROUP, getElementFromBaos(), curWordFntMetricsNum));
                nextWordNum();
            } else
                strElements.add(new StrElement(ElementType.GROUP, getElementFromBaos(), curWordFntMetricsNum));
            stringsOnScreen.add(createStringOnScreenAndResetFields());
//            if (!elementFromBaos.startsWith(" ") && !elementFromBaos.startsWith("\t"))
//                nextWordNum();
            state = State.BEGIN;
        }

        return stringsOnScreen;
    }

    private StringOnScreen createStringOnScreenAndResetFields() {
        final StringOnScreen stringOnScreen = new StringOnScreen(strElements, curStrHght);
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
            addWordToElBaos();
            if (curWordInGroupNum == MAX_WORD_IN_GROUP_NUM - 1)
                strElements.add(new StrElement(ElementType.GROUP, getElementFromBaos(), curWordFntMetricsNum));
            nextWordNum();
            return waitForNextWord(c);
        }

        int charWidth = findCharWidth(c);
        if (isNotFitWindow(charWidth)) {
            addWordToElBaos();
            strElements.add(new StrElement(ElementType.GROUP, getElementFromBaos(), curWordFntMetricsNum));
            addCharToWord(c, charWidth);
            state = State.GET_FIRST_WORD;
            return true;
        }

        addCharToWord(c, charWidth);

        return false;
    }

    private boolean waitForNextWord(char c) {
        final int charWidth = findCharWidth(c);
        if (isNotFitWindow(charWidth)) {
            final ElementType elementType = curWordInGroupNum == 0 ? ElementType.SPACE : ElementType.GROUP;
            strElements.add(new StrElement(elementType, getElementFromBaos(), curWordFntMetricsNum));
            if (!isBlankSymbol(c)) {
                addCharToElement(c, charWidth);
                state = State.GET_FIRST_WORD;
            } else
                state = State.BEGIN;

            return true;
        }

        if (!isBlankSymbol(c)) {
            if (curWordInGroupNum == 0)
                strElements.add(new StrElement(ElementType.SPACE, getElementFromBaos(), curWordFntMetricsNum));
            state = State.GET_WORD;
            return getWord(c);
        }

        addCharToElement(c, charWidth);

        return false;
    }

    private boolean getWord(char c) {
        final int charWidth = findCharWidth(c);
        if (isNotFitWindow(charWidth)) {
            if (isBlankSymbol(c)) {
                addWordToElBaos();
                strElements.add(new StrElement(ElementType.GROUP, getElementFromBaos(), curWordFntMetricsNum));
                nextWordNum();
                state = State.BEGIN;
            } else {
                if (curWordInGroupNum != 0)
                    strElements.add(new StrElement(ElementType.GROUP, getElementFromBaos(), curWordFntMetricsNum));

                addCharToWord(c, charWidth);
                state = State.GET_FIRST_WORD;
            }

            return true;
        }

        if (isBlankSymbol(c)) {
            addWordToElBaos();
            if (curWordInGroupNum == MAX_WORD_IN_GROUP_NUM - 1)
                strElements.add(new StrElement(ElementType.GROUP, getElementFromBaos(), curWordFntMetricsNum));
            nextWordNum();
            state = State.WAIT_FOR_NEXT_WORD;
            return waitForNextWord(c);
        }

        addCharToWord(c, charWidth);

        return false;
    }

    private void nextWordNum() {
        curWordInGroupNum = (curWordInGroupNum + 1) % MAX_WORD_IN_GROUP_NUM;

        if (curWordInGroupNum == 0) {
            curWordFntMetricsNum = (curWordFntMetricsNum + 1) % MAX_FNT_METRICS_NUM;
            curWordFntMetrics = curWordFntMetricsNum == 0 ? frstFntMetrics : scndFntMetrics;
        }
    }

    private void addCharToElement(char c, int charWidth) {
        elHght = Math.max(elHght, curWordFntMetrics.getHeight()) ;
        elWdth += charWidth;
        elBaos.write(c);
    }

    private void addCharToWord(char c, int charWidth) {
        wordHght = curWordFntMetrics.getHeight();
        wordWdth += charWidth;
        wordBaos.write(c);
    }

    private void addWordToElBaos() {
        try {
            elBaos.write(wordBaos.toByteArray());
            wordBaos.reset();
            elHght = Math.max(elHght, wordHght);
            wordHght = 0;
            elWdth += wordWdth;
            wordWdth = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getElementFromBaos() {
        final String word = new String(elBaos.toByteArray(), CHARSET);
        elBaos.reset();
        curStrWdth += elWdth;
        elWdth = 0;
        curStrHght = Math.max(curStrHght, elHght);

        return word;
    }

    private int findCharWidth(char c) {
        if (c == '\t') {
            final int offset = curStrWdth + elWdth;
            return (offset / tabWidth + 1) * tabWidth - offset;
        }

        return curWordFntMetrics.charWidth(c);
    }

    private boolean isBlankSymbol(char c) {
        return c == ' ' || c == '\t';
    }

    private boolean isNotFitWindow(int charWidth) {
        return windowWidth <= curStrWdth + elWdth + wordWdth + charWidth;
    }

    static enum State {
        BEGIN, GET_FIRST_WORD, WAIT_FOR_NEXT_WORD, GET_WORD, LINE_PREFIX
    }
}
