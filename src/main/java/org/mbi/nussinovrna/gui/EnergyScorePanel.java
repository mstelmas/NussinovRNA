package org.mbi.nussinovrna.gui;

import javaslang.control.Try;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.mbi.nussinovrna.NumericDocumentFilter;
import org.mbi.nussinovrna.UnorderedPair;
import org.mbi.nussinovrna.rna.RnaNucleotide;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log
public class EnergyScorePanel extends JPanel {

    private final RnaNucleotide[] NUCLEOTIDES = RnaNucleotide.values();

    private final int NUCLEOTIDES_SIZE = NUCLEOTIDES.length;

    private final GridLayout energyScorePanelLayout = new GridLayout(NUCLEOTIDES_SIZE + 1, NUCLEOTIDES_SIZE  + 1);

    private final JLabel[] horizontalNucleotidesLabels = new JLabel[NUCLEOTIDES_SIZE];
    private final JLabel[] verticalNucleotidesLabels = new JLabel[NUCLEOTIDES_SIZE];

    private final Map<UnorderedPair<RnaNucleotide>, JTextField> energyScoresMap = new HashMap<>();

    private final DocumentFilter numericDocumentFilter = new NumericDocumentFilter();

    public EnergyScorePanel(final Map<UnorderedPair<RnaNucleotide>, Integer> defaultEnergyScores) {

        setLayout(energyScorePanelLayout);
        setFocusable(true);

        buildGUI();

        loadEnergyScores(defaultEnergyScores);
    }


    private Function<JTextField, Integer> toEnergyScoreValue = jTextField ->
            Try.of(() -> Optional.ofNullable(jTextField.getText()).map(Integer::valueOf).orElse(0))
                    .getOrElse(0);

    public Map<UnorderedPair<RnaNucleotide>, Integer> getCurrentEnergyScores() {
        return energyScoresMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, k -> toEnergyScoreValue.apply(k.getValue())));
    }


    // TODO: validation!!!
    private void loadEnergyScores(final Map<UnorderedPair<RnaNucleotide>, Integer> defaultEnergyScores) {

        energyScoresMap.entrySet().stream().forEach(energyScoresMapEntrySet -> {

            final Optional<Integer> energyValue = Optional.ofNullable(defaultEnergyScores.get(energyScoresMapEntrySet.getKey()));

            if(!energyValue.isPresent()) {
                log.warning("Energy has no value...defaulting to 0");
            }

            energyScoresMapEntrySet.getValue().setText(
                    energyValue.map(val -> Integer.toString(val)).orElse("0")
            );
        });

    }

    // TODO: Potentially risky?!
    private final FocusListener energyTextFieldFocusListener = new FocusListener() {

        private Optional<String> currentEnergyTextFieldValue = Optional.empty();

        @Override
        public void focusGained(final FocusEvent focusEvent) {
            if(focusEvent.getComponent() instanceof JTextField) {
                currentEnergyTextFieldValue = Optional.of(((JTextField)focusEvent.getComponent()).getText());
            }
        }

        @Override
        public void focusLost(final FocusEvent focusEvent) {
            if (!focusEvent.isTemporary() && focusEvent.getComponent() instanceof JTextField) {

                final JTextField energyValueTextField = (JTextField) focusEvent.getComponent();
                final String energyValue = energyValueTextField.getText();

                if(StringUtils.isBlank(energyValue)) {
                    if(!currentEnergyTextFieldValue.isPresent()) {
                        log.warning("Cannot restore previous value for energy text field?! Setting to 0");
                        energyValueTextField.setText("0");
                    } else {
                        energyValueTextField.setText(currentEnergyTextFieldValue.get());
                        currentEnergyTextFieldValue = Optional.empty();
                    }
                }
            }
        }
    };



    private Supplier<JTextField> textFieldWithNumericFilter = () -> {
        final JTextField jTextField = new JTextField();
        ((AbstractDocument)jTextField.getDocument()).setDocumentFilter(numericDocumentFilter);
        jTextField.addFocusListener(energyTextFieldFocusListener);
        return jTextField;
    };

    private void buildGUI() {

        add(new JLabel());

        IntStream.range(0, NUCLEOTIDES_SIZE).forEach(i -> {
            horizontalNucleotidesLabels[i] = new JLabel(NUCLEOTIDES[i].name());
            verticalNucleotidesLabels[i] = new JLabel(NUCLEOTIDES[i].name());

            horizontalNucleotidesLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            verticalNucleotidesLabels[i].setHorizontalAlignment(SwingConstants.CENTER);

            add(horizontalNucleotidesLabels[i]);
        });

        IntStream.range(0, NUCLEOTIDES_SIZE).forEach(j -> {

            add(verticalNucleotidesLabels[j]);

            IntStream.rangeClosed(0, j).forEach(i -> {
                final JTextField nucleotideTextField = textFieldWithNumericFilter.get();
                nucleotideTextField.setHorizontalAlignment(SwingConstants.CENTER);
                add(nucleotideTextField);

                energyScoresMap.put(
                        UnorderedPair.of(NUCLEOTIDES[i], NUCLEOTIDES[j]),
                        nucleotideTextField
                );

            });

            IntStream.range(j + 1, NUCLEOTIDES_SIZE).forEach(k -> add(new JLabel()));
        });
    }
}
