package org.mbi.nussinovrna.gui;

import javax.swing.*;
import java.awt.*;
import java.util.stream.IntStream;

public class EnergyScorePanel extends JPanel {

    private final String NUCLEOTIDES = "ACGU";
    private final int NUCLEOTIDES_SIZE = NUCLEOTIDES.length();
    private final int REQUIRED_EDITABLE_CELLS = (NUCLEOTIDES_SIZE * NUCLEOTIDES_SIZE + NUCLEOTIDES_SIZE) / 2;

    private final GridLayout energyScorePanelLayout = new GridLayout(NUCLEOTIDES_SIZE + 1, NUCLEOTIDES_SIZE  + 1);

    private final JLabel[] horizontalNucleotidesLabels = new JLabel[NUCLEOTIDES_SIZE];
    private final JLabel[] verticalNucleotidesLabels = new JLabel[NUCLEOTIDES_SIZE];
    private final JTextField[] energyScoreTextFields = new JTextField[REQUIRED_EDITABLE_CELLS];


    public EnergyScorePanel() {

        setLayout(energyScorePanelLayout);

        buildGUI();

        for(int i = 0; i < REQUIRED_EDITABLE_CELLS; i++) {
            energyScoreTextFields[i].setText("0");
        }
    }

    private void buildGUI() {

        add(new JLabel());

        IntStream.range(0, NUCLEOTIDES_SIZE).forEach(i -> {
            horizontalNucleotidesLabels[i] = new JLabel(NUCLEOTIDES.substring(i, i + 1));
            verticalNucleotidesLabels[i] = new JLabel(NUCLEOTIDES.substring(i, i + 1));

            horizontalNucleotidesLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            verticalNucleotidesLabels[i].setHorizontalAlignment(SwingConstants.CENTER);

            add(horizontalNucleotidesLabels[i]);
        });

        int count = 0;

        for(int j = 0; j < NUCLEOTIDES_SIZE; j++) {

            add(verticalNucleotidesLabels[j]);

            for(int i = 0; i <= j; i++) {
                energyScoreTextFields[count] = new JTextField();
                energyScoreTextFields[count].setHorizontalAlignment(SwingConstants.CENTER);
                add(energyScoreTextFields[count]);
                count++;
            }

            for(int k = j + 1; k < NUCLEOTIDES_SIZE; k++) {
                add(new JLabel());
            }
        }
    }
}
