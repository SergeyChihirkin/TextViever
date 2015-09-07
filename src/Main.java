import client.MainPanel;
import textReader.TextReaderImpl;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        final JFrame frame = new JFrame("TextViewer");
        frame.setLayout(new BorderLayout());
        frame.setMinimumSize(new Dimension(70, 210));

        final MainPanel mainPanel;
        String fileName;
        if (args.length != 0) {
            fileName = args[0];
            mainPanel = new MainPanel(fileName, frame, new TextReaderImpl());
        } else
            mainPanel = new MainPanel(frame, new TextReaderImpl());

        frame.getContentPane().add(mainPanel.getMainPanel());

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setSize(new Dimension(1024, 720));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
