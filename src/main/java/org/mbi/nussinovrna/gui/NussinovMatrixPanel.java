package org.mbi.nussinovrna.gui;

import lombok.RequiredArgsConstructor;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.stream.IntStream;

public class NussinovMatrixPanel extends JPanel {

    private final NussinovMatrixGrid nussinovMatrixGrid;

    private final int PANEL_PADDING;

    public NussinovMatrixPanel(final NussinovMatrixGrid nussinovMatrixGrid) {
        this.nussinovMatrixGrid = nussinovMatrixGrid;
        this.PANEL_PADDING =  nussinovMatrixGrid.getCellSize() / 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Optional.ofNullable(nussinovMatrixGrid.getAreaSize()).ifPresent(areaSize -> {
            Optional.ofNullable(nussinovMatrixGrid.getRnaSecondaryStruct()).ifPresent(rnaSecondaryStruct -> {
                final int currentCellSize = nussinovMatrixGrid.getCellSize();
                final int currentCellCount = nussinovMatrixGrid.getNussinovMatrixSize();

                IntStream.range(0, currentCellCount + 2).forEach(cell -> {
                    g.drawLine(cell * currentCellSize + PANEL_PADDING, PANEL_PADDING, cell * currentCellSize + PANEL_PADDING, currentCellSize + PANEL_PADDING);
                    g.drawLine(PANEL_PADDING, cell * currentCellSize + PANEL_PADDING, currentCellSize + PANEL_PADDING, cell * currentCellSize + PANEL_PADDING);
                });

                g.drawLine(PANEL_PADDING, PANEL_PADDING, areaSize.width + currentCellSize + PANEL_PADDING, PANEL_PADDING);
                g.drawLine(PANEL_PADDING, currentCellSize + PANEL_PADDING, areaSize.width + currentCellSize + PANEL_PADDING, currentCellSize + PANEL_PADDING);
                g.drawLine(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, areaSize.height + currentCellSize + PANEL_PADDING);
                g.drawLine(currentCellSize + PANEL_PADDING, PANEL_PADDING, currentCellSize + PANEL_PADDING, areaSize.width + currentCellSize + PANEL_PADDING);


                final FontMetrics fontMetrics = getFontMetrics(nussinovMatrixGrid.getCurrentFont());

                g.setFont(nussinovMatrixGrid.getCurrentFont());
                g.setColor(NussinovMatrixGridConst.MATRIX_VALUE_COLOR);

                IntStream.range(0, currentCellCount).forEach(cell -> {
                    final String cellValue = String.valueOf(rnaSecondaryStruct.getRnaSequence().getAsString().charAt(cell));
                    final int adjustedFontWidth = fontMetrics.stringWidth(cellValue);
                    final int adjustedFontHeight = fontMetrics.getAscent();

                    g.drawString(
                            cellValue,
                            (cell + 1) * currentCellSize + PANEL_PADDING + currentCellSize / 2 - (adjustedFontWidth / 2),
                            adjustedFontHeight * 3 / 2 + PANEL_PADDING
                    );

                    g.drawString(
                            cellValue,
                            adjustedFontWidth + PANEL_PADDING,
                            (cell + 1) * currentCellSize + PANEL_PADDING + adjustedFontHeight * 3 / 2
                    );
                });


                final BufferedImage nussinovMatrixImgBuffer = new BufferedImage(
                        nussinovMatrixGrid.getAreaSize().width,
                        nussinovMatrixGrid.getAreaSize().height,
                        BufferedImage.TYPE_INT_ARGB
                );

                nussinovMatrixGrid.paintComponent(nussinovMatrixImgBuffer.createGraphics());

                g.drawImage(nussinovMatrixImgBuffer, currentCellSize + PANEL_PADDING, currentCellSize + PANEL_PADDING, this);
            });
        });
    }


    public void setRnaSecondaryStruct(final RnaSecondaryStruct rnaSecondaryStruct) {
        nussinovMatrixGrid.setRnaSecondaryStruct(rnaSecondaryStruct);
        repaint();
    }

    public void setZoomLevel(final int zoomLevel) {
        nussinovMatrixGrid.setZoomLevel(zoomLevel);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {

        final Dimension nussinovMatrixGridPreferredSize = nussinovMatrixGrid.getPreferredSize();

        return new Dimension(
                nussinovMatrixGridPreferredSize.width + PANEL_PADDING,
                nussinovMatrixGridPreferredSize.height + PANEL_PADDING
        );
    }
}
