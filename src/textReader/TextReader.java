package textReader;

import data.TextStorage;

import java.io.File;
import java.io.IOException;

public interface TextReader {

    public TextStorage getText(File file) throws IOException;
}
