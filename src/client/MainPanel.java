package client;

import data.TextStorage;
import textReader.TextReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;

public class MainPanel extends ComponentAdapter implements ActionListener, KeyListener {
    private final int MAX_FNT_MTRCS_INF = 4;
    private final int NUM_OF_STYLES = 2;
    private final int TAB_WIDTH = 72;

    private JFrame frame;
    private JPanel mainPanel;
    private JButton opnBtn;
    private JTextPane textPane;
    private JButton sltFntBtn;
    private JScrollPane txtScrollPane;
    private JFileChooser fc = new JFileChooser();
    private FontsWindow fontsWindow;
    private JButton fntOkBtn;

    private SimpleAttributeSet frstAttr = new SimpleAttributeSet();
    private SimpleAttributeSet scndAttr = new SimpleAttributeSet();
    private Style first;
    private Style second;
    private Style styles[];

    private File file;
    private TextReader reader;
    private TextOnScreen textOnScreen;
    private TextStorage textStorage;
    private TextOnScreenManager textOnScreenManager;

    private int curWordFntMetricsInf;


    public MainPanel(JFrame frame, TextReader reader) {
        this.frame = frame;
        this.reader = reader;
        initAttributeSets();
        opnBtn.addActionListener(this);
        sltFntBtn.addActionListener(this);
        addStylesToDocument();
        txtScrollPane.addKeyListener(this);
        textPane.addKeyListener(this);
        frame.addComponentListener(this);
        addFileFilter();
    }

    private void addFileFilter() {
        final FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
        fc.setFileFilter(filter);
    }

    public MainPanel(String fileName, JFrame frame, TextReader textReaderNio) {
        this(frame, textReaderNio);

        final File f = new File(fileName);
        if (!f.exists() || !f.isFile()) {
            JOptionPane.showMessageDialog(frame, "File doesn't exist.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            final String s = Files.probeContentType(f.toPath());
            if (s == null || !s.equals("text/plain")) {
                JOptionPane.showMessageDialog(frame, "Wrong file format.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (IOException ignore) {}

        getTextByFile(f);
    }

    private void initAttributeSets() {
        final String fntName = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()[0];
        initAttrSet(fntName, frstAttr, 42, true, false);
        initAttrSet(fntName, scndAttr, 21, false, true);
    }

    private void initAttrSet(String fntName, SimpleAttributeSet attr, int size, boolean bold, boolean italic) {
        StyleConstants.setFontFamily(attr, fntName);
        StyleConstants.setFontSize(attr, size);
        StyleConstants.setBold(attr, bold);
        StyleConstants.setItalic(attr, italic);
    }

    private FontMetrics getFntMetrics(SimpleAttributeSet attr) {
        final Font fnt = FontsWindow.makeFont(attr);

        return textPane.getFontMetrics(fnt);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void onSlctFntBtn() {
        if (fontsWindow == null) {
            fontsWindow = new FontsWindow(frame, frstAttr, scndAttr);
            fntOkBtn = fontsWindow.getOkBtn();
            fntOkBtn.addActionListener(this);
        } else
            fontsWindow.setAttrs(frstAttr, scndAttr);

        fontsWindow.setVisible(true);
    }

    private void onOpnBtn() {
        final int returnVal = fc.showOpenDialog(mainPanel);

        if (returnVal == JFileChooser.APPROVE_OPTION)
            getTextByFile(fc.getSelectedFile());
    }

    private void getTextByFile(File selectedFile) {
        file = selectedFile;

        try {
            textStorage = reader.getText(file);
            redraw();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Can't read from that file", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void printText() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textPane.setText("");

                if (textOnScreen.getStrings().size() == 0)
                    return;

                final StyledDocument doc = textPane.getStyledDocument();
                ((DefaultCaret)textPane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

                try {
                    for (int i = textOnScreen.getFrstStrNumber(); i <= textOnScreen.getLastStrNumber(); i++) {
                        final StringOnScreen stringOnScreen = textOnScreen.getStrings().get(i);
                        curWordFntMetricsInf = stringOnScreen.getStrFrstWordFntMetricsInf();
                        final LinkedList<String> elements = stringOnScreen.getElements();
                        for (int j = 0; j < elements.size(); j += 2) {
                            doc.insertString(doc.getLength(), elements.get(j), getStyle());
                            curWordFntMetricsInf = (curWordFntMetricsInf + 1) % MAX_FNT_MTRCS_INF;
                            if (j + 1 < elements.size())
                                doc.insertString(doc.getLength(), elements.get(j + 1), getStyle());
                        }
                        doc.insertString(doc.getLength(), "\n", getStyle());
                    }
                } catch (BadLocationException e) {
                    JOptionPane.showMessageDialog(frame, "Can't print text", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }

            }
        });
    }

    private Style getStyle() {
        final int numOfStyle = curWordFntMetricsInf < 2 ? 0 : 1;
        return styles[numOfStyle];
    }

    private void addStylesToDocument() {
        final StyledDocument doc = textPane.getStyledDocument();
        doc.putProperty(PlainDocument.tabSizeAttribute, TAB_WIDTH);

        final Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        first = doc.addStyle("first", def);
        setFontAttrs(first, frstAttr);

        second = doc.addStyle("scnd", def);
        setFontAttrs(second, scndAttr);

        styles = new Style[NUM_OF_STYLES];
        styles[0] = first;
        styles[1] = second;
    }

    private void setFontAttrs(Style style, SimpleAttributeSet attr) {
        StyleConstants.setFontFamily(style, StyleConstants.getFontFamily(attr));
        StyleConstants.setBold(style, StyleConstants.isBold(attr));
        StyleConstants.setItalic(style, StyleConstants.isItalic(attr));
        StyleConstants.setFontSize(style, StyleConstants.getFontSize(attr));
    }

    private void createUIComponents() {
        textPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }
        };
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (opnBtn == e.getSource())
            onOpnBtn();
        else if (sltFntBtn == e.getSource())
            onSlctFntBtn();
        else if (fntOkBtn != null && fntOkBtn == e.getSource()) {
            setFontAttrs(first, frstAttr);
            setFontAttrs(second, scndAttr);

            fontsWindow.closeAndSave();
            addStylesToDocument();

            redraw();
        }
    }

    private WindowInformation createWindowInformation() {
        return new WindowInformation(txtScrollPane.getHeight(), txtScrollPane.getWidth(), TAB_WIDTH,
                getFntMetrics(frstAttr), getFntMetrics(scndAttr));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (file == null)
            return;

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            textOnScreenManager.nextString(textOnScreen);
            printText();
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            textOnScreenManager.previousString(textOnScreen);
            printText();
        } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            textOnScreenManager.nextPage(textOnScreen);
            printText();
        } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            textOnScreenManager.previousPage(textOnScreen);
            printText();
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (e.getSource() == frame) {
            textPane.setText("");
            redraw();
        }
    }

    private void redraw() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textOnScreenManager = new TextOnScreenManager(createWindowInformation());
                textOnScreen = textOnScreenManager.createTextOnScreen(textStorage);
                printText();
            }
        });
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}