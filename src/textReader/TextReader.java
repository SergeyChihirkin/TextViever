package textReader;

import data.TextStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface TextReader {

    public TextStorage getText(File file) throws IOException;

    public boolean isFileLong(File file) throws IOException;

    public boolean isFileEnded();

    public void clearPosParams();
}
