package client;

import sun.misc.Cleaner;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

public class TextPositionManager {
    private File bufFile;
    private LinkedList<Long> chunkPositions;
    private int windowHeight;


    public TextPositionManager(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public TextPositionManager(int windowHeight, File bufFile, LinkedList<Long> chunkPositions) {
        this(windowHeight);
        this.bufFile = bufFile;
        this.chunkPositions = chunkPositions;
    }

    private LinkedList<StringOnScreen> getStringsOnScreenFromFile(long chunkStart, long chunkEnd) {
        LinkedList<StringOnScreen> strsOnScreen = null;
        try (FileInputStream fis = new FileInputStream(bufFile); FileChannel channel = fis.getChannel()) {
            final ByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, chunkEnd);
            try {
                byteBuffer.position((int)chunkStart);
                byte[] bytes = new byte[(int)(chunkEnd - chunkStart)];
                byteBuffer.get(bytes);
                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                    try {
                        strsOnScreen = (LinkedList<StringOnScreen>)ois.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                Cleaner cleaner = ((sun.nio.ch.DirectBuffer) byteBuffer).cleaner();
                if (cleaner != null) {
                    cleaner.clean();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strsOnScreen;
    }

    void addFrstAndLastStrNumbrs(int frstStrNum, TextOnScreen textOnScreen) {
        textOnScreen.setFrstStrNumber(frstStrNum);
        int height = 0;
        int i = frstStrNum;
        int frstChunkSize = textOnScreen.getFrstChunkStrings().size();
        while (height < windowHeight && i < frstChunkSize) {
            height += textOnScreen.getFrstChunkStrings().get(i).getHeightOfStr();
            i++;
        }
        int scndChunkSize = textOnScreen.getScndChunkStrings().size();
        int chunksSize = frstChunkSize + scndChunkSize;
        while (height < windowHeight && i < chunksSize) {
            height += textOnScreen.getScndChunkStrings().get(i - frstChunkSize).getHeightOfStr();
            i++;
        }

        if (chunkPositions != null && i == chunksSize && height < windowHeight) {
            final int frstChunkNum = textOnScreen.getFrstChunkNum();
            if (frstChunkNum != chunkPositions.size() - 2) {
                textOnScreen.nextChunk(getStringsOnScreenFromFile(chunkPositions.get(frstChunkNum + 1),
                                chunkPositions.get(frstChunkNum + 2)));

                frstChunkSize = scndChunkSize;
                scndChunkSize = textOnScreen.getScndChunkStrings().size();
                chunksSize = frstChunkSize + scndChunkSize;
                i = frstChunkSize;
                while (height < windowHeight && i < chunksSize) {
                    height += textOnScreen.getScndChunkStrings().get(i - frstChunkSize).getHeightOfStr();
                    i++;
                }
            }
        }

        final int lastStrNumber = i == frstStrNum ? i : i - 1;
        textOnScreen.setLastStrNumber(lastStrNumber);
    }

    public void nextString(TextOnScreen textOnScreen) {
        int frstStrNumber = textOnScreen.getFrstStrNumber();
        if (frstStrNumber != textOnScreen.getLastStrNumber()) {
            frstStrNumber++;
            addFrstAndLastStrNumbrs(frstStrNumber, textOnScreen);
        }
    }

    public boolean previousString(TextOnScreen textOnScreen) {
        boolean isChunkChanged = false;
        int frstStrNumber = textOnScreen.getFrstStrNumber();
        if (frstStrNumber == 0) {
            final int frstChunkNum = textOnScreen.getFrstChunkNum();
            if (frstChunkNum != 0) {
                final long chunkStart = frstChunkNum - 2 < 0 ? 0 : chunkPositions.get(frstChunkNum - 2);
                final long chunkEnd = chunkPositions.get(frstChunkNum - 1);
                textOnScreen.previousChunk(getStringsOnScreenFromFile(chunkStart, chunkEnd));
                isChunkChanged = true;
                frstStrNumber = textOnScreen.getFrstChunkStrings().size() - 1;
            } else
                return false;
        } else
            frstStrNumber--;

        addFrstAndLastStrNumbrs(frstStrNumber, textOnScreen);

        return isChunkChanged;
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

        while (oldFrstStrNum < textOnScreen.getLastStrNumber() && !(textOnScreen.getFrstStrNumber() == 0
                && textOnScreen.getFrstChunkNum() == 0)) {
            if (previousString(textOnScreen))
                oldFrstStrNum += textOnScreen.getFrstChunkStrings().size();
        }
    }

    public TextOnScreen getTextOnScreenFromFile() {
        TextOnScreen textOnScreen = new TextOnScreen();
        textOnScreen.getFrstChunkStrings().addAll(getStringsOnScreenFromFile(0, chunkPositions.get(0)));
        textOnScreen.getScndChunkStrings().addAll(getStringsOnScreenFromFile(chunkPositions.get(0),
                chunkPositions.get(1)));

        return textOnScreen;
    }
}
