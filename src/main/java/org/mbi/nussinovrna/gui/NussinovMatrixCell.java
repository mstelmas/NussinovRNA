package org.mbi.nussinovrna.gui;

import lombok.Data;

@Data
public class NussinovMatrixCell {
    protected final int columnNumber;
    protected final int rowNumber;
    protected String value;
}
