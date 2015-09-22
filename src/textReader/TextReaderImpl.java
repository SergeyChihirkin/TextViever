package textReader;

import data.TextStorage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;

public class TextReaderImpl implements TextReader {
    public static final Charset CHARSET = Charset.forName("UTF-8");
    private final int BUFFER_SIZE = 1048576; //10mb
//    private final int BUFFER_SIZE = 4096;

    private RandomAccessFile raf;
    private FileChannel channel;

    private CharBuffer chunkChrBuf;
    private long fileLength;
    private int bufferSize;
    private long strEnding;
    private long filePos = 0;
    private boolean fileEnded = false;

    @Override
    public void clearPosParams() {
        filePos = 0;
        fileEnded = false;
    }

    @Override
    public boolean isFileLong(File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileLength = raf.length();

            return fileLength > BUFFER_SIZE;
        }
    }

    @Override
    public TextStorage getText(File file) throws IOException {
        setFile(file);
        final TextStorage textStorage = new TextStorage();
        try {
            if (fileLength == 0)
                return textStorage;

            prepareChunk();

            String textChunk = chunkChrBuf.toString();

            String[] strs = textChunk.split("\\r\\n?|\\n");

            final LinkedList<String> strings = new LinkedList<>(Arrays.asList(strs));
            if (!fileEnded && strEnding != BUFFER_SIZE - 1)
                strings.removeLast();
            textStorage.setStrings(strings);
        } finally {
            close();
        }

        return textStorage;
    }

    private void prepareChunk() {
        if (fileLength - filePos <= BUFFER_SIZE) {
            bufferSize = (int)(fileLength - filePos);
            fileEnded = true;
        } else
            bufferSize = BUFFER_SIZE;

        try {
            final ByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, filePos, bufferSize);

            int i;
            for (i = bufferSize - 1; i >= 0; i--) {
                if (byteBuffer.get(i) == '\r' || byteBuffer.get(i) == '\n') {
                    strEnding = i;
                    break;
                }
            }
            filePos += strEnding + 1;
            chunkChrBuf = CHARSET.decode(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFile(File file) throws IOException {
        raf = new RandomAccessFile(file, "r");
        channel = raf.getChannel();
        fileLength = raf.length();
    }

    public void close() throws IOException {
        if (raf != null) {
            raf.close();
            raf = null;
        }
    }

    public boolean isFileEnded() {
        return fileEnded;
    }
}
