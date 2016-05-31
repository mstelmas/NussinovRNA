package org.mbi.nussinovrna;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

// TODO: Fix regex to allow only '-' before a digit (negative numbers)
public class NumericDocumentFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        fb.insertString(offset, string.replace("[^\\d-]", ""), attr);
//        fb.insertString(offset, string.replace("\\D+", ""), attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//        fb.replace(offset, length, text.replaceAll("\\D++", ""), attrs);
        fb.replace(offset, length, text.replaceAll("[^\\d-]", ""), attrs);
    }
}
