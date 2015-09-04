package client;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FontsWindow extends JDialog implements ActionListener {
    private JPanel mainPanel;
    private JButton okBtn;
    private JButton cnclBtn;
    private JButton chngFrstFntBtn;
    private JButton chngScndFntBtn;
    private JLabel frstFntLbl;
    private JLabel scndFntLbl;
    private final FontChooser fntChsr = new FontChooser(this);
    private SimpleAttributeSet frstAttr;
    private SimpleAttributeSet localFrstAttr;
    private SimpleAttributeSet scndAttr;
    private SimpleAttributeSet localScndAttr;

    public FontsWindow(Frame parent, SimpleAttributeSet frstAttr, SimpleAttributeSet scndAttr) {
        super(parent, "Fonts Chooser", true);
        setAttrs(frstAttr, scndAttr);

        initWnd(parent);
        initBtns();
    }

    public void setAttrs(SimpleAttributeSet frstAttr, SimpleAttributeSet scndAttr) {
        this.frstAttr = frstAttr;
        this.scndAttr = scndAttr;
        localFrstAttr = (SimpleAttributeSet)frstAttr.clone();
        localScndAttr = (SimpleAttributeSet)scndAttr.clone();

        initLbls();
    }

    private void initLbls() {
        frstFntLbl.setFont(makeFont(localFrstAttr));
        scndFntLbl.setFont(makeFont(localScndAttr));
    }

    public static Font makeFont(final SimpleAttributeSet attr) {
        final String name = StyleConstants.getFontFamily(attr);
        final boolean bold = StyleConstants.isBold(attr);
        final boolean ital = StyleConstants.isItalic(attr);
        final int size = StyleConstants.getFontSize(attr);

        return new Font(name, (bold ? Font.BOLD : 0) + (ital ? Font.ITALIC : 0), size);
    }

    private void initWnd(Frame parent) {
        add(mainPanel);
        setSize(new Dimension(640, 480));
        setLocationRelativeTo(parent);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeAndCancel();
            }
        });
    }

    private void initBtns() {
        cnclBtn.addActionListener(this);
        chngFrstFntBtn.addActionListener(this);
        chngScndFntBtn.addActionListener(this);
    }

    public void closeAndSave() {
        saveChanges(frstAttr, localFrstAttr);
        saveChanges(scndAttr, localScndAttr);
        setVisible(false);
    }

    private void saveChanges(SimpleAttributeSet attr, final SimpleAttributeSet localAttr) {
        StyleConstants.setFontFamily(attr, StyleConstants.getFontFamily(localAttr));
        StyleConstants.setFontSize(attr, StyleConstants.getFontSize(localAttr));
        StyleConstants.setBold(attr, StyleConstants.isBold(localAttr));
        StyleConstants.setItalic(attr, StyleConstants.isItalic(localAttr));
    }

    private void closeAndCancel() {
        setVisible(false);
    }

    private void chngFnt(SimpleAttributeSet attr, JLabel fntLbl) {
        fntChsr.setValues(attr, fntLbl);
        fntChsr.setVisible(true);
    }

    public JButton getOkBtn() {
        return okBtn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (cnclBtn == e.getSource())
            closeAndCancel();
        else if (chngFrstFntBtn == e.getSource())
            chngFnt(localFrstAttr, frstFntLbl);
        else if (chngScndFntBtn == e.getSource())
            chngFnt(localScndAttr, scndFntLbl);
    }
}