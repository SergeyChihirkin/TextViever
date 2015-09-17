package client;

import java.io.Serializable;

public class StrElement implements Serializable {
    private ElementType elementType;
    private String str;
    private int numOfFnt;

    public StrElement(ElementType elementType, String str, int numOfFnt) {
        this.elementType = elementType;
        this.str = str;
        this.numOfFnt = numOfFnt;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public String getStr() {
        return str;
    }

    public int getNumOfFnt() {
        return numOfFnt;
    }
}

enum ElementType implements Serializable {
    GROUP, SPACE
}
