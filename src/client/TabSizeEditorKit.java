package client;

import javax.swing.text.*;

public class TabSizeEditorKit extends StyledEditorKit {
    private int tabWidth;

    public TabSizeEditorKit(int tabWidth) {
        this.tabWidth = tabWidth;
    }

    public ViewFactory getViewFactory() {
        return new MyViewFactory(tabWidth);
    }

    static class MyViewFactory implements ViewFactory {
        private int tabWidth;

        MyViewFactory(int tabWidth) {
            this.tabWidth = tabWidth;
        }

        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        return new LabelView(elem);
                    case AbstractDocument.ParagraphElementName:
                        return new CustomTabParagraphView(elem, tabWidth);
                    case AbstractDocument.SectionElementName:
                        return new BoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);
                    case StyleConstants.IconElementName:
                        return new IconView(elem);
                }
            }

            return new LabelView(elem);
        }
    }

    static class CustomTabParagraphView extends ParagraphView {
        private int tabWidth;

        public CustomTabParagraphView(Element elem, int tabWidth) {
            super(elem);
            this.tabWidth = tabWidth;
        }

        public float nextTabStop(float x, int tabOffset) {
            TabSet tabs = getTabSet();
            if (tabs == null) {
                return (getTabBase() + (((int)x / tabWidth + 1) * tabWidth));
            }

            return super.nextTabStop(x, tabOffset);
        }
    }
}