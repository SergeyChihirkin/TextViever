package client;

import data.TextStorage;
import sun.misc.Cleaner;
import textReader.TextReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.LinkedList;

public class MainPanel extends ComponentAdapter implements ActionListener, KeyListener {
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
    private boolean isFileLong;
    private File bufFile;
    private LinkedList<Long> chunkPositions;
    private TextPositionManager textPositionManager;


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

    private void addFileFilter() {
        final FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
        fc.setFileFilter(filter);
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

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            getTextByFile(fc.getSelectedFile());
            redraw();
        }
    }

    private void getTextByFile(File selectedFile) {
        deleteBufFileIfExists();
        file = selectedFile;

        try {
            isFileLong = reader.isFileLong(file);
            if (!isFileLong) {
                reader.clearPosParams();
                textStorage = reader.getText(file);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Can't read from that file", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void deleteBufFileIfExists() {
        try {
            if (bufFile != null && bufFile.exists()) {
                Files.delete(bufFile.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printText() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textPane.setText("");

                if (textOnScreen.getFrstChunkStrings().size() == 0)
                    return;

                final StyledDocument doc = textPane.getStyledDocument();
                ((DefaultCaret)textPane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

                try {
                    final LinkedList<StringOnScreen> frstChunkStrings = textOnScreen.getFrstChunkStrings();
                    final int lastStrInFrstChunk = Math.min(frstChunkStrings.size() - 1,
                            textOnScreen.getLastStrNumber());
                    for (int i = textOnScreen.getFrstStrNumber(); i <= lastStrInFrstChunk; i++)
                        printString(doc, i, frstChunkStrings);
                    final LinkedList<StringOnScreen> scndChunkStrings = textOnScreen.getScndChunkStrings();
                    int scndChunkStart = textOnScreen.getFrstStrNumber() - frstChunkStrings.size();
                    scndChunkStart = scndChunkStart < 0 ? 0 : scndChunkStart;
                    for (int i = scndChunkStart; i < textOnScreen.getLastStrNumber() - frstChunkStrings.size() + 1; i++)
                        printString(doc, i, scndChunkStrings);
                } catch (BadLocationException e) {
                    JOptionPane.showMessageDialog(frame, "Can't print text", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
    }

    private void printString(StyledDocument doc, int i, LinkedList<StringOnScreen> strings)
            throws BadLocationException {
        final StringOnScreen stringOnScreen = strings.get(i);
        final LinkedList<StrElement> strElements = stringOnScreen.getStrElements();
        for (StrElement strElement : strElements) {
            doc.insertString(doc.getLength(), strElement.getStr(), getStyle(strElement.getNumOfFnt()));
        }
        doc.insertString(doc.getLength(), "\n", findStyle(stringOnScreen.getHeightOfStr()));
    }

    private AttributeSet findStyle(int heightOfStr) {
        if (getFntMetrics(frstAttr).getHeight() == heightOfStr)
            return getStyle(0);

        return getStyle(1);
    }

    private Style getStyle(int numOfFnt) {
        return styles[numOfFnt];
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
        return new WindowInformation(txtScrollPane.getWidth(), TAB_WIDTH, getFntMetrics(frstAttr),
                getFntMetrics(scndAttr));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (file == null)
            return;

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            textPositionManager.nextString(textOnScreen);
            printText();
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            textPositionManager.previousString(textOnScreen);
            printText();
        } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            textPositionManager.nextPage(textOnScreen);
            printText();
        } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            textPositionManager.previousPage(textOnScreen);
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

    private void addTextToFile(ObjectOutputStream oos) {
        try {
            oos.writeObject(textOnScreen.getFrstChunkStrings());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Can't write to that file", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void readAndStoreLongFile() {
        try {
            bufFile = new File(file.toString().concat("~"));
            deleteBufFileIfExists();
            bufFile.createNewFile();

            try (RandomAccessFile raf = new RandomAccessFile(bufFile, "rw"); FileChannel channel = raf.getChannel()) {
                textOnScreenManager = new TextOnScreenManager(createWindowInformation());
                reader.clearPosParams();
                chunkPositions = new LinkedList<>();

                while (!reader.isFileEnded()) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                        textStorage = reader.getText(file);
                        textOnScreen = textOnScreenManager.createTextOnScreen(textStorage);

                        addTextToFile(oos);

                        final long chunkStart = chunkPositions.size() == 0 ? 0 : chunkPositions.getLast();
                        final long chunkEnd = baos.toByteArray().length + chunkStart;
                        chunkPositions.add(chunkEnd);

                        final MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE,
                                chunkStart, chunkEnd);

                        try {
                            mappedByteBuffer.put(baos.toByteArray());
                            mappedByteBuffer.force();
                        } finally {
                            Cleaner cleaner = ((sun.nio.ch.DirectBuffer) mappedByteBuffer).cleaner();
                            if (cleaner != null) {
                                cleaner.clean();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void redraw() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!isFileLong) {
                    textOnScreenManager = new TextOnScreenManager(createWindowInformation());
                    textOnScreen = textOnScreenManager.createTextOnScreen(textStorage);
                    textPositionManager = new TextPositionManager(txtScrollPane.getHeight());
                } else {
                    if (!reader.isFileEnded())
                        readAndStoreLongFile();

                    textPositionManager = new TextPositionManager(txtScrollPane.getHeight(), bufFile, chunkPositions);
                    textOnScreen = textPositionManager.getTextOnScreenFromFile();
                }
                textPositionManager.addFrstAndLastStrNumbrs(0, textOnScreen);
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