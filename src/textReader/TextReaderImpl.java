package textReader;

import data.TextStorage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class TextReaderImpl implements TextReader {
    public static final Charset CHARSET = Charset.forName("UTF-8");
    private final int BUFFER_SIZE = 1048576; //10mb

    private RandomAccessFile raf;
    private FileChannel channel;

    private CharBuffer chunkChrBuf;
    private long bufferSize;
    private long fileLength;

    private ByteArrayOutputStream baos;


    @Override
    public TextStorage getText(File file) throws IOException {
        baos = new ByteArrayOutputStream();
        setFile(file);
        TextStorage textStorage = new TextStorage();
        try {
            if (fileLength == 0)
                return textStorage;

            prepareChunk();

            int bufPos = 0;
            char lastSeparator = '\0';
            boolean isWaitingForSepSymb = false;
            while (bufPos < chunkChrBuf.length()) {
                char c = chunkChrBuf.get(bufPos);

                if (isSepSymbol(c)) {
                    if (isWaitingForSepSymb && c!= lastSeparator) {
                        isWaitingForSepSymb = false;
                    } else {
                        String str = new String(baos.toByteArray(), CHARSET);
                        textStorage.addString(str);
                        baos.reset();
                        isWaitingForSepSymb = true;
                        lastSeparator = c;
                    }
                } else
                    baos.write(c);

                bufPos++;
            }

            String str = new String(baos.toByteArray(), CHARSET);
            textStorage.addString(str);
        } finally {
            close();
        }

        return textStorage;
    }

    private boolean isSepSymbol(char c) {
        return c == '\n' || c == '\r';
    }

    private void prepareChunk() {
        bufferSize = Math.min(BUFFER_SIZE, fileLength);

        try {
            final ByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, bufferSize);
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
}
