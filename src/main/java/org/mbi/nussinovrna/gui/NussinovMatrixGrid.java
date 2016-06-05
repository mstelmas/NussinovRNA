package org.mbi.nussinovrna.gui;


import lombok.Getter;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.stream.IntStream;

public final class NussinovMatrixGrid extends JComponent {

    private final static int DEFAULT_CELL_SIZE = 40;
    private final static int DEFAULT_ZOOM_LEVEL = 75;
    private final static int DEFAULT_FONT_SIZE = 18;
    private final static Font DEFAULT_FONT =
            new Font(null, Font.PLAIN, (int) (DEFAULT_FONT_SIZE * DEFAULT_ZOOM_LEVEL / 100));

    @Getter private int cellSize = (int) (DEFAULT_CELL_SIZE * DEFAULT_ZOOM_LEVEL / 100);
    @Getter private double zoomLevel = DEFAULT_ZOOM_LEVEL / 100;
    @Getter private Font currentFont = DEFAULT_FONT;

    private NussinovMatrixCell nussinovMatrixCells[][];

    @Getter private int nussinovMatrixSize;

    @Getter private Dimension areaSize = null;

    @Getter private RnaSecondaryStruct rnaSecondaryStruct;

    public NussinovMatrixGrid() {
        setFont(currentFont);
    }

    private void createNussinovMatrix(final int nussinovMatrixSize, final int[][] nussinovMatrixData) {
        this.nussinovMatrixSize = nussinovMatrixSize;
        this.nussinovMatrixCells = new NussinovMatrixCell[nussinovMatrixSize][nussinovMatrixSize];

        IntStream.range(0, nussinovMatrixSize).forEach(i -> {
            IntStream.range(0, nussinovMatrixSize).forEach(j -> {
                nussinovMatrixCells[i][j] = new NussinovMatrixCell(i, j);
                Optional.ofNullable(nussinovMatrixData).ifPresent(nussinovMatrix -> {
                    nussinovMatrixCells[i][j].setValue(String.valueOf(nussinovMatrix[i][j]));
                });
            });
        });
    }

    public void setRnaSecondaryStruct(final RnaSecondaryStruct rnaSecondaryStruct) {

        this.rnaSecondaryStruct = rnaSecondaryStruct;

        if(rnaSecondaryStruct != null) {
            createNussinovMatrix(rnaSecondaryStruct.getNussinovMap().length, rnaSecondaryStruct.getNussinovMap());
        } else {
            createNussinovMatrix(0, null);
        }

//        Optional.ofNullable(rnaSecondaryStruct).ifPresent(rnaSecondaryStruct1 -> {
//            createNussinovMatrix(rnaSecondaryStruct1.getNussinovMap().length, rnaSecondaryStruct1.getNussinovMap());
////            calculateAreaSize();
//        });

        calculateAreaSize();

    }



@Override public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Optional.ofNullable(rnaSecondaryStruct).ifPresent((rnaSecondaryStruct1) -> {

            if (areaSize == null) {
                calculateAreaSize();
            }

            drawGrid(g);
        });
    }


    protected void calculateAreaSize() {
        areaSize = new Dimension();

        areaSize.setSize(
                nussinovMatrixSize * cellSize,
                nussinovMatrixSize * cellSize
        );
    }

    protected void drawGrid(final Graphics g) {
        /* background */
        g.setColor(NussinovMatrixGridConst.MATRIX_BACKGROUND_COLOR);
        g.fillRect(0, 0, areaSize.width + 1, areaSize.height + 1);

        /* nussinov matrix grid lines */
        g.setColor(NussinovMatrixGridConst.MATRIX_COLOR);

        IntStream.range(0, (nussinovMatrixSize + 1)).forEach(i -> {
            g.drawLine(i * cellSize, 0, i * cellSize, areaSize.height);
            g.drawLine(0, i * cellSize, areaSize.width, i * cellSize);
        });

        /* fill unimportant cells in Nussinov matrix */
        if(nussinovMatrixSize >= 2) {

            IntStream.range(2, nussinovMatrixSize).forEach(row ->
                IntStream.range(0, (row - 1)).forEach(col ->
                    colorCellAt(g, col, row, NussinovMatrixGridConst.MATRIX_CELL_UNUSED)
                )
            );
        }

        drawValues(g);
    }

    protected void drawValues(final Graphics g) {

        final FontMetrics fontMetrics = getFontMetrics(currentFont);

        final int adjustedFontHeight = fontMetrics.getAscent() / 2;

        g.setFont(currentFont);
        g.setColor(NussinovMatrixGridConst.MATRIX_VALUE_COLOR);

        rnaSecondaryStruct.getSecondaryStructMap().entrySet().stream().forEach(entrySet -> {
            colorCellAt(g, entrySet.getValue(), entrySet.getKey(), NussinovMatrixGridConst.MATRIX_CELL_PAIRED);
        });

        IntStream.range(0, nussinovMatrixSize).forEach(row ->
            IntStream.range(0, nussinovMatrixSize).forEach(col ->
                Optional.ofNullable(nussinovMatrixCells[row][col].getValue())
                        .ifPresent(cellValue -> {
                            final int adjustedFontWidth = fontMetrics.stringWidth(cellValue);

                            g.drawString(
                                    cellValue,
                                    col * cellSize + cellSize / 2 - (adjustedFontWidth / 2),
                                    row * cellSize + cellSize / 2 + adjustedFontHeight
                            );
                        })
            )
        );
    }

    private void colorCellAt(final Graphics g, final int row, final int column, final Color cellColor) {
        final Color currentColor = g.getColor();

        g.setColor(cellColor);

        g.fillRect(row * cellSize + 1, column * cellSize + 1, cellSize, cellSize);

        g.setColor(currentColor);
    }

    public void setZoomLevel(final int zoomLevel) {
        this.zoomLevel = zoomLevel;

        cellSize =  (DEFAULT_CELL_SIZE * zoomLevel / 100);

        currentFont = new Font(null, Font.PLAIN, (DEFAULT_FONT_SIZE * zoomLevel) / 100);
        setFont(currentFont);

        areaSize = null;
    }

    @Override
    public Dimension getPreferredSize() {
        if(areaSize == null) {
            calculateAreaSize();
        }

        return areaSize;
    }
}
