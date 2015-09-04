package client;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FontChooser extends JDialog implements ActionListener {
    private JPanel mainPanel;
    private JButton cnclBtn;
    private JButton okBtn;
    private JComboBox<String> nameCmbBox;
    private JCheckBox boldChckBx;
    private JCheckBox italicChckBx;
    private JLabel previewLbl;
    private JTextField textField;
    private JLabel lblToChange;
    private SimpleAttributeSet attr;
    private Font fnt;

    public FontChooser(Dialog parent) {
        super(parent, "Font Chooser", true);

        initWnd(parent);
        addActionListeners();

    }

    private void initSizeTxtFld() {
        textField= new IntTextField();
    }

    private void addActionListeners() {
        nameCmbBox.addActionListener(this);
        italicChckBx.addActionListener(this);
        boldChckBx.addActionListener(this);
        textField.addActionListener(this);
        okBtn.addActionListener(this);
        cnclBtn.addActionListener(this);
    }

    private void initWnd(Dialog parent) {
        add(mainPanel);
        setSize(new Dimension(720, 320));
        setLocationRelativeTo(parent);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeAndCancel();
            }
        });
    }

    public void setValues(SimpleAttributeSet attr, JLabel lblToChange) {
        this.attr = attr;
        this.lblToChange = lblToChange;
        textField.setText(String.valueOf(StyleConstants.getFontSize(attr)));
        nameCmbBox.setSelectedItem(StyleConstants.getFontFamily(attr));
        boldChckBx.setSelected(StyleConstants.isBold(attr));
        italicChckBx.setSelected(StyleConstants.isItalic(attr));
        previewLbl.setFont(FontsWindow.makeFont(attr));
    }

    private void changeAttr() {
        int size = 0;
        final String sizeStr = textField.getText();
        if (!"".equals(sizeStr))
            size = Integer.parseInt(sizeStr);

        StyleConstants.setFontFamily(attr, (String)nameCmbBox.getSelectedItem());
        StyleConstants.setFontSize(attr, size);
        StyleConstants.setBold(attr, boldChckBx.isSelected());
        StyleConstants.setItalic(attr, italicChckBx.isSelected());
    }

    private void onCnclBtn() {
        closeAndCancel();
    }

    private void changeLocalFont() {
        int size = 0;
        final String sizeStr = textField.getText();
        if (!"".equals(sizeStr))
            size = Integer.parseInt(sizeStr);
        fnt = new Font((String)nameCmbBox.getSelectedItem(), (boldChckBx.isSelected() ? Font.BOLD : 0)
                + (italicChckBx.isSelected() ? Font.ITALIC : 0), size);

        previewLbl.setFont(fnt);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (nameCmbBox == e.getSource() || boldChckBx == e.getSource() || italicChckBx == e.getSource()
                || textField == e.getSource())
            changeLocalFont();
        else if (okBtn == e.getSource())
            onOkBtn();
        else if (cnclBtn == e.getSource())
            onCnclBtn();
    }

    private void onOkBtn() {
        if (fnt != null) {
            changeAttr();
            lblToChange.setFont(fnt);
        }
        setVisible(false);
    }

    private void closeAndCancel() {
        setVisible(false);
    }

    private void createUIComponents() {
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        nameCmbBox = new JComboBox<>(ge.getAvailableFontFamilyNames());
        initSizeTxtFld();
    }
}