package org.mbi.nussinovrna.gui;

import lombok.RequiredArgsConstructor;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class NussinovMatrixPanel extends JPanel {

    private final NussinovMatrixGrid nussinovMatrixGrid;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Optional.ofNullable(nussinovMatrixGrid.getAreaSize()).ifPresent(areaSize -> {
            Optional.ofNullable(nussinovMatrixGrid.getRnaSecondaryStruct()).ifPresent(rnaSecondaryStruct -> {
                final int currentCellSize = nussinovMatrixGrid.getCellSize();
                final int currentCellCount = nussinovMatrixGrid.getNussinovMatrixSize();

                IntStream.range(0, currentCellCount + 2).forEach(cell -> {
                    g.drawLine(cell * currentCellSize, 0, cell * currentCellSize, currentCellSize);
                    g.drawLine(0, cell * currentCellSize, currentCellSize, cell * currentCellSize);
                });

                g.drawLine(0, 0, areaSize.width + currentCellSize, 0);
                g.drawLine(0, currentCellSize, areaSize.width + currentCellSize, currentCellSize);
                g.drawLine(0, 0, 0, areaSize.height + currentCellSize);
                g.drawLine(currentCellSize, 0, currentCellSize, areaSize.width + currentCellSize);


                final FontMetrics fontMetrics = getFontMetrics(getFont());

                g.setFont(getFont());
                g.setColor(NussinovMatrixGridConst.MATRIX_VALUE_COLOR);

                IntStream.range(0, currentCellCount).forEach(cell -> {
                    final String cellValue = String.valueOf(rnaSecondaryStruct.getRnaSequence().getAsString().charAt(cell));
                    final int adjustedFontWidth = fontMetrics.stringWidth(cellValue);

                    g.drawString(
                            cellValue,
                            (cell + 1) * currentCellSize + currentCellSize / 2 - (adjustedFontWidth / 2),
                            2 * fontMetrics.getAscent()
                    );

                    g.drawString(
                            cellValue,
                            fontMetrics.getAscent(),
                            (cell + 1) * currentCellSize + currentCellSize / 2
                    );
                });


                final BufferedImage nussinovMatrixImgBuffer = new BufferedImage(
                        nussinovMatrixGrid.getAreaSize().width,
                        nussinovMatrixGrid.getAreaSize().height,
                        BufferedImage.TYPE_INT_ARGB
                );

                nussinovMatrixGrid.paintComponent(nussinovMatrixImgBuffer.createGraphics());

                g.drawImage(nussinovMatrixImgBuffer, currentCellSize - 1, currentCellSize - 1, this);
            });
        });
    }


    public void setRnaSecondaryStruct(final RnaSecondaryStruct rnaSecondaryStruct) {
        nussinovMatrixGrid.setRnaSecondaryStruct(rnaSecondaryStruct);
        repaint();
    }
}
