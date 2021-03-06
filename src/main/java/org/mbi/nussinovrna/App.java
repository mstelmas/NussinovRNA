package org.mbi.nussinovrna;

import com.google.common.collect.ImmutableList;
import fr.orsay.lri.varna.VARNAPanel;
import fr.orsay.lri.varna.applications.FileNameExtensionFilter;
import fr.orsay.lri.varna.models.export.SwingGraphics;
import fr.orsay.lri.varna.models.export.VueVARNAGraphics;
import javaslang.control.Match;
import javaslang.control.Try;
import lombok.NonNull;
import lombok.extern.java.Log;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mbi.nussinovrna.algorithm.NussinovAlgorithm;
import org.mbi.nussinovrna.algorithm.scoring.*;
import org.mbi.nussinovrna.converters.BpseqConverter;
import org.mbi.nussinovrna.converters.CtConverter;
import org.mbi.nussinovrna.converters.ViennaConverter;
import org.mbi.nussinovrna.gui.EnergyScorePanel;
import org.mbi.nussinovrna.gui.NussinovMatrixGrid;
import org.mbi.nussinovrna.gui.NussinovMatrixPanel;
import org.mbi.nussinovrna.gui.documentFilters.PositiveNumericDocumentFilter;
import org.mbi.nussinovrna.rna.RnaSequence;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;


@Log
public class App extends JFrame {

    private final static String APP_TITLE = "Nussinov Algorithm (Bioinformatics)";
    private final static String PREFERRED_LOOK_AND_FEEL = "Nimbus";

    private static final ImmutableList<EnergyScoringStrategy> PREDEFINED_ENERGY_SCORINGS =
            new ImmutableList.Builder<EnergyScoringStrategy>()
                    .add(new DefaultScoringStrategy())
                    .add(new Pam250ScoringStrategy())
                    .add(new Blosum62ScoringStrategy())
                    .add(new Vtml160ScoringStrategy())
                    .add(new Penalized1ScoringStrategy())
                    .add(new Penalized2ScoringStrategy())
                    .add(new Penalized3ScoringStrategy())
                    .build();

    private final JPanel framePanel = new JPanel(new MigLayout("fill"));

    private final JPanel rnaSequenceAreaPanel = new JPanel();
    private final TitledBorder rnaSequenceAreaPanelBorder = BorderFactory.createTitledBorder("RNA Sequence");
    private final JTextArea rnaSequenceTextArea = new JTextArea(10, 35);
    private final JScrollPane rnaSequenceTextScrollPane = new JScrollPane(rnaSequenceTextArea);
    private final JButton rnaSequenceCalculateButton = new JButton("Calculate");
    private final JButton rnaSequenceClearButton = new JButton("Clear");
    private final JTextField rnaSequenceMinLoopSizeTextField = new JTextField(3);
    private final JLabel rnaSequenceMinLoopSizeLabel = new JLabel("Minimum size of a loop: ");

    /* Secondary Structure Formats */
    private final JLabel viennaLabel = new JLabel("Vienna format: ");
    private final JTextField viennaFormatTextField = new JTextField(15);
    private final JScrollBar viennaFormatScrollBar = new JScrollBar(Adjustable.HORIZONTAL);
    private final BoundedRangeModel viennaFormatBoundedRangeModel = viennaFormatTextField.getHorizontalVisibility();
    private final JButton viennaExportButton = new JButton("Export to file");
    private final JPanel viennaPanel = new JPanel(new MigLayout());

    private final JLabel bpseqLabel = new JLabel("BPSEQ format: ");
    private final JTextArea bpseqFormatTextArea = new JTextArea(20, 8);
    private final JButton bpseqExportButton = new JButton("Export to file");
    private final JPanel bpseqPanel = new JPanel(new MigLayout());

    private final JLabel ctLabel = new JLabel("CT Format: ");
    private final JTextArea ctFormatTextArea = new JTextArea(20, 15);
    private final JButton ctExportButton = new JButton("Export to file");
    private final JPanel ctPanel = new JPanel(new MigLayout());

    private final NussinovMatrixGrid nussinovMatrixGrid = new NussinovMatrixGrid();
    private final NussinovMatrixPanel nussinovMatrixPanel = new NussinovMatrixPanel(nussinovMatrixGrid);

    private final JScrollPane nussinovMatrixScrollPane = new JScrollPane(nussinovMatrixPanel);

    // Zoom
    private final JLabel zoomLevelLabel = new JLabel("Zoom level: ");
    private final JPanel zoomLevelPanel = new JPanel(new MigLayout());
    private final JButton setZoomLevelButton = new JButton("Zoom!");
    private final JTextField setZoomLevelTextField = new JTextField(6);
    private final JButton saveNussinovMatrixButton = new JButton("Export to file");

    private final JPanel nussinovMatrixTopPanel = new JPanel(new BorderLayout());

    private final EnergyScorePanel energyScorePanel = new EnergyScorePanel(PREDEFINED_ENERGY_SCORINGS.get(0));
    private final TitledBorder energyScorePanelBorder = BorderFactory.createTitledBorder("Energy Scores");
    private final JLabel energyScoresStrategyLabel = new JLabel("Energy scoring strategy: ");

    private final JComboBox<EnergyScoringStrategy> energyScoringStrategyJComboBox = new JComboBox(
            new DefaultComboBoxModel<>(PREDEFINED_ENERGY_SCORINGS.toArray())
    );


    private final JMenu fileMenu = new JMenu("File");
    private final JMenu sequenceMenu = new JMenu("Sequence");

    private final JMenuItem exitMenuItem = new JMenuItem("Exit");

    private final JMenuItem importSequenceMenuItem = new JMenuItem("Import");
    private final JMenuItem exportSequenceMenuItem = new JMenuItem("Export");

    private final JFileChooser rnaSequenceFileChooser = new JFileChooser();

    private final VARNAPanel varnaPanel = new VARNAPanel();

    private App() {

        setUpLookAndFeel().onFailure((e) -> log.warning("Could not load system default theme! Oh well..."));

        buildGui();

        this.setTitle(APP_TITLE);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationByPlatform(true);
        this.pack();
        this.setVisible(true);
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(App::new);
    }

    private Try<Void> setUpLookAndFeel() {
            return loadLookAndFeelTheme(PREFERRED_LOOK_AND_FEEL)
                    .onFailure((e) -> log.warning(
                            String.format("Could not load theme: %s, loading system default theme...", PREFERRED_LOOK_AND_FEEL))
                    )
                    .orElse(this::loadSystemLookAndFeel);
    }

    private Try<Void> loadLookAndFeelTheme(@NonNull final String lookAndFeelTheme) {
        return Try.run(() -> UIManager.setLookAndFeel(
                Stream.of(UIManager.getInstalledLookAndFeels())
                        .filter(lookAndFeelInfo -> lookAndFeelInfo.getName().equals(lookAndFeelTheme))
                        .findAny()
                        .map(UIManager.LookAndFeelInfo::getClassName)
                        .orElseThrow(Exception::new)
        ));
    }

    private Try<Void> loadSystemLookAndFeel() {
        return Try.run(() -> UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()));
    }

    /* The entire GUI is divided into 2 panels:
       - left : contains data related to the analyzed RNA sequence
       - right: contains predicted secondary structure related data
     */
    private void buildGui() {

        final JMenuBar menuBar = buildMenuBar();

        final JTabbedPane rightPanel = buildRightPanel();

        final JPanel leftPanel = buildLeftPanel();

        framePanel.add(leftPanel, "dock west");
        framePanel.add(rightPanel, "dock center");

        this.setJMenuBar(menuBar);
        this.getContentPane().add(framePanel);
    }

    private JPanel buildLeftPanel() {
        final JPanel leftPanel = new JPanel();

        leftPanel.setLayout(new MigLayout());


        rnaSequenceTextArea.setLineWrap(true);
        // for debugging
        rnaSequenceTextArea.setText("GCACGACG");

        rnaSequenceAreaPanel.setLayout(new BorderLayout());
        rnaSequenceAreaPanel.add(rnaSequenceTextScrollPane, BorderLayout.CENTER);
        rnaSequenceAreaPanel.setBorder(rnaSequenceAreaPanelBorder);

        rnaSequenceCalculateButton.addActionListener(calculateButtonActionListener);
        rnaSequenceClearButton.addActionListener(clearButtonActionListener);

        leftPanel.add(rnaSequenceAreaPanel, "wrap, push, grow");

        energyScoresStrategyLabel.setLabelFor(energyScoringStrategyJComboBox);

        energyScoringStrategyJComboBox.setRenderer(energyScoresComboBoxRenderer);
        energyScoringStrategyJComboBox.addActionListener(energyScoresComboBoxListener);

        energyScorePanel.setBorder(energyScorePanelBorder);

        leftPanel.add(energyScoresStrategyLabel, "split");
        leftPanel.add(energyScoringStrategyJComboBox, "wrap");

        leftPanel.add(energyScorePanel, "wrap, push, grow");

        leftPanel.add(rnaSequenceClearButton, "split");
        leftPanel.add(rnaSequenceCalculateButton);

        rnaSequenceMinLoopSizeLabel.setLabelFor(rnaSequenceMinLoopSizeTextField);
        rnaSequenceMinLoopSizeTextField.setText(String.valueOf(NussinovAlgorithm.DEFAULT_MIN_LOOP_SIZE));
        rnaSequenceMinLoopSizeTextField.setHorizontalAlignment(JTextField.CENTER);
        ((AbstractDocument)rnaSequenceMinLoopSizeTextField.getDocument()).setDocumentFilter(new PositiveNumericDocumentFilter());
        rnaSequenceMinLoopSizeTextField.addFocusListener(minLoopSizeTextFieldFocusListener);

        leftPanel.add(rnaSequenceMinLoopSizeLabel);
        leftPanel.add(rnaSequenceMinLoopSizeTextField);

        return leftPanel;
    }


    private JPanel buildPredictedSecondaryStructurePanel() {
        final JPanel nussinovPredictedStructurePanel = new JPanel();

        nussinovPredictedStructurePanel.setLayout(new MigLayout("fill"));

        viennaLabel.setLabelFor(viennaFormatTextField);
        viennaFormatTextField.setEditable(false);
        viennaFormatScrollBar.setModel(viennaFormatBoundedRangeModel);

        viennaExportButton.addActionListener(exportAsViennaFormatActionListener);
        viennaExportButton.setEnabled(false);

        viennaPanel.add(viennaLabel);
        viennaPanel.add(viennaExportButton, "wrap");
        viennaPanel.add(viennaFormatTextField, "span, pushx, grow, wrap");
        viennaPanel.add(viennaFormatScrollBar, "span, pushx, grow");

        nussinovPredictedStructurePanel.add(viennaPanel, "span, grow, pushx, wrap");

        ctExportButton.addActionListener(exportAsCtFormatActionListener);
        ctExportButton.setEnabled(false);

        ctLabel.setLabelFor(ctFormatTextArea);
        ctFormatTextArea.setEditable(false);
        ctPanel.add(ctLabel);
        ctPanel.add(ctExportButton, "wrap");
        ctPanel.add(new JScrollPane(ctFormatTextArea), "span, push, grow");

        nussinovPredictedStructurePanel.add(ctPanel, "grow");

        bpseqExportButton.addActionListener(exportAsBpseqFormatActionListener);
        bpseqExportButton.setEnabled(false);

        bpseqLabel.setLabelFor(bpseqFormatTextArea);
        bpseqFormatTextArea.setEditable(false);
        bpseqPanel.add(bpseqLabel);
        bpseqPanel.add(bpseqExportButton, "wrap");
        bpseqPanel.add(new JScrollPane(bpseqFormatTextArea), "span, push, grow");

        nussinovPredictedStructurePanel.add(bpseqPanel, "grow");

        return nussinovPredictedStructurePanel;
    }

    private JTabbedPane buildRightPanel() {
        final JTabbedPane nussinovTabbedPane = new JTabbedPane();

        nussinovMatrixPanel.setAutoscrolls(true);
        nussinovMatrixPanel.setLayout(new MigLayout("fill"));

        zoomLevelLabel.setLabelFor(setZoomLevelTextField);
        setZoomLevelButton.addActionListener(setZoomLevelActionListener);
        saveNussinovMatrixButton.addActionListener(saveNussinovMatrixActionListener);
        setZoomLevelTextField.setHorizontalAlignment(JTextField.CENTER);
        zoomLevelPanel.add(zoomLevelLabel);
        zoomLevelPanel.add(setZoomLevelTextField);
        zoomLevelPanel.add(setZoomLevelButton);
        zoomLevelPanel.add(saveNussinovMatrixButton);

        nussinovMatrixTopPanel.add(nussinovMatrixScrollPane, BorderLayout.CENTER);
        nussinovMatrixTopPanel.add(zoomLevelPanel, BorderLayout.SOUTH);

        nussinovTabbedPane.addTab("RNA Secondary Structure", buildPredictedSecondaryStructurePanel());
        nussinovTabbedPane.addTab("Nussinov Matrix", nussinovMatrixTopPanel);
        nussinovTabbedPane.addTab("Visualization (powered by VARNA)", varnaPanel);

        return nussinovTabbedPane;
    }



    private JMenuBar buildMenuBar() {

        final JMenuBar menuBar = new JMenuBar();

        importSequenceMenuItem.addActionListener(openMenuItemActionListener);
        sequenceMenu.add(importSequenceMenuItem);

        exportSequenceMenuItem.addActionListener(saveMenuItemActionListener);
        sequenceMenu.add(exportSequenceMenuItem);

        exitMenuItem.addActionListener(exitMenuItemActionListener);
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(sequenceMenu);

        return menuBar;
    }

    private ActionListener saveNussinovMatrixActionListener = actionEvent -> {

        final JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png"));
        final int saveActionResult = jFileChooser.showSaveDialog(this);


        if(saveActionResult == JFileChooser.APPROVE_OPTION && jFileChooser.getSelectedFile() != null) {
            final Dimension areaSize = nussinovMatrixGrid.getAreaSize();


            final BufferedImage bufferedImage = new BufferedImage(
                    areaSize.width,
                    areaSize.height,
                    BufferedImage.TYPE_INT_RGB
            );

            final Graphics2D graphics = bufferedImage.createGraphics();

            nussinovMatrixGrid.paintComponent(graphics);

            graphics.dispose();

            Try.run(() -> ImageIO.write(bufferedImage, "png", jFileChooser.getSelectedFile()))
                    .onFailure((e) -> JOptionPane.showMessageDialog(this, e.getMessage(), "Could not save RNA image", JOptionPane.ERROR_MESSAGE))
                    .onSuccess((e) -> JOptionPane.showMessageDialog(this, "Successfully exported Nussinov matrix to image file", "Success!", JOptionPane.INFORMATION_MESSAGE));
        }
    };

    private final ListCellRenderer energyScoresComboBoxRenderer = new DefaultListCellRenderer() {

        @Override
        public Component getListCellRendererComponent(final JList<?> list, final Object value,
                                                      final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            Match.of(value)
                    .whenType(EnergyScoringStrategy.class)
                    .thenRun(energyScoringStrategy -> setText(energyScoringStrategy.getStrategyName()));

            return this;
        }
    };

    private final ActionListener energyScoresComboBoxListener = (actionEvent) ->
            Match.of(actionEvent.getSource())
                    .whenType(JComboBox.class)
                    .then(comboBoxSource ->
                            Match.of(comboBoxSource.getSelectedItem())
                                    .whenType(EnergyScoringStrategy.class)
                                    .thenRun(energyScoringStrategy -> {
                                        log.log(Level.INFO, String.format("Loading selected Scoring Strategy: %s", energyScoringStrategy.getStrategyName()));
                                        energyScorePanel.loadEnergyScores(energyScoringStrategy);
                                    })
                    );

    private ActionListener calculateButtonActionListener = actionEvent -> {
        Try.of (() ->
                Optional.ofNullable(rnaSequenceTextArea.getText())
                    .map(StringUtils::trim)
                    .map(RnaSequence::of)
                    .map(rnaSequence -> new NussinovAlgorithm(
                            rnaSequence, energyScorePanel.getCurrentEnergyScores(), Optional.ofNullable(
                            Try.of(() -> Integer.valueOf(rnaSequenceMinLoopSizeTextField.getText().trim())).getOrElse(1)))
                    )
                    .map(NussinovAlgorithm::getRnaSecondaryStruct)
                    .get()
        ).onFailure(e -> {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid RNA sequence", JOptionPane.ERROR_MESSAGE);
            rnaSequenceTextArea.selectAll();
            rnaSequenceTextArea.requestFocus();
        }).onSuccess(predictedSecondaryStructure -> {

            final String viennaFormat = ViennaConverter.toViennaFormat(predictedSecondaryStructure);

            nussinovMatrixPanel.setRnaSecondaryStruct(predictedSecondaryStructure);
            viennaFormatTextField.setText(viennaFormat);
            bpseqFormatTextArea.setText(BpseqConverter.toBpseqFormat(predictedSecondaryStructure));
            ctFormatTextArea.setText(CtConverter.toCtFormat(predictedSecondaryStructure));

            setZoomLevelTextField.setText(Double.toString(nussinovMatrixGrid.getZoomLevel()));

            viennaExportButton.setEnabled(true);
            ctExportButton.setEnabled(true);
            bpseqExportButton.setEnabled(true);

            nussinovMatrixScrollPane.invalidate();
            repaint();

            Try.run(() -> {
                varnaPanel.drawRNA(
                        predictedSecondaryStructure.getRnaSequence().getAsString(),
                        viennaFormat
                );
            }).onFailure(e -> JOptionPane.showMessageDialog(this, e.getMessage(), "", JOptionPane.ERROR_MESSAGE));
        });
    };

    private ActionListener clearButtonActionListener = actionEvent -> {
        rnaSequenceTextArea.setText("");
        viennaFormatTextField.setText("");
        bpseqFormatTextArea.setText("");
        ctFormatTextArea.setText("");
        nussinovMatrixPanel.setRnaSecondaryStruct(null);


        viennaExportButton.setEnabled(false);
        ctExportButton.setEnabled(false);
        bpseqExportButton.setEnabled(false);

        nussinovMatrixScrollPane.invalidate();
        repaint();
    };

    private ActionListener exitMenuItemActionListener = actionEvent -> System.exit(0);

    private ActionListener openMenuItemActionListener = actionEvent -> {
        final int fileChooserDialogReturnValue = rnaSequenceFileChooser.showOpenDialog(this);

        if(fileChooserDialogReturnValue == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = rnaSequenceFileChooser.getSelectedFile();

            Optional.ofNullable(selectedFile).ifPresent(file -> {
                Try.run(() -> {
                    final String fileContent = new String(Files.readAllBytes(file.toPath())).trim();

                    final RnaSequence loadedRnaSequence = RnaSequence.of(fileContent);

                    rnaSequenceTextArea.setText(loadedRnaSequence.getAsString());
                }).onFailure(e -> {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Error reading RNA sequence from file!", JOptionPane.ERROR_MESSAGE);
                });
            });
        }
    };

    private ActionListener saveMenuItemActionListener = actionEvent ->
            Optional.ofNullable(rnaSequenceTextArea.getText())
                    .map(StringUtils::trim)
                    .ifPresent(rnaSequence ->
                        saveTextWithFileChooser(rnaSequence)
                            .onFailure(e ->
                                    JOptionPane.showMessageDialog(this, e.getMessage(), "Error exporting RNA sequence", JOptionPane.ERROR_MESSAGE)
                            )
            );

    private ActionListener setZoomLevelActionListener = actionEvent ->
        Optional.ofNullable(setZoomLevelTextField.getText())
                .ifPresent(zoomLevel -> {
                    nussinovMatrixPanel.setZoomLevel(Integer.parseInt(zoomLevel));
                    nussinovMatrixScrollPane.invalidate();
                    repaint();
                });


    private ActionListener exportAsViennaFormatActionListener = actionEvent ->
        Optional.ofNullable(viennaFormatTextField.getText()).ifPresent(viennaFormat ->
            saveTextWithFileChooser(viennaFormat)
                    .onFailure(e ->
                            JOptionPane.showMessageDialog(this, e.getMessage(), "Error exporting Vienna format", JOptionPane.ERROR_MESSAGE)
                    )
        );

    private ActionListener exportAsBpseqFormatActionListener = actionEvent ->
            Optional.ofNullable(bpseqFormatTextArea.getText()).ifPresent(bpseqFormat ->
                    saveTextWithFileChooser(bpseqFormat)
                            .onFailure(e ->
                                    JOptionPane.showMessageDialog(this, e.getMessage(), "Error exporting BPSEQ format", JOptionPane.ERROR_MESSAGE)
                            )
            );


    private ActionListener exportAsCtFormatActionListener = actionEvent ->
            Optional.ofNullable(ctFormatTextArea.getText()).ifPresent(ctFormat ->
                    saveTextWithFileChooser(ctFormat)
                            .onFailure(e ->
                                    JOptionPane.showMessageDialog(this, e.getMessage(), "Error exporting CT format", JOptionPane.ERROR_MESSAGE)
                            )
            );

    private Try<Void> saveTextWithFileChooser(@NonNull final String text) {
        final JFileChooser jFileChooser = new JFileChooser();
        final int fileChooserDialogReturnValue = jFileChooser.showSaveDialog(this);

        if(fileChooserDialogReturnValue == JFileChooser.APPROVE_OPTION && jFileChooser.getSelectedFile() != null) {
            return Try.run(() -> Files.write(jFileChooser.getSelectedFile().toPath(), text.getBytes()));
        }

        return Try.success(null);
    }

    private final FocusListener minLoopSizeTextFieldFocusListener = new FocusListener() {

        private Optional<String> currentMinLoopSizeValue = Optional.empty();

        @Override
        public void focusGained(final FocusEvent focusEvent) {
            Match.of(focusEvent.getComponent())
                    .whenType(JTextField.class)
                        .thenRun((minLoopSizeTextField -> currentMinLoopSizeValue = Optional.of(minLoopSizeTextField.getText())));
        }

        @Override
        public void focusLost(final FocusEvent focusEvent) {
            if (!focusEvent.isTemporary() && focusEvent.getComponent() instanceof JTextField) {

                final JTextField minLoopSizeTextField = (JTextField) focusEvent.getComponent();
                final String energyValue = minLoopSizeTextField.getText();

                if(StringUtils.isBlank(energyValue) || !NumberUtils.isNumber(energyValue)) {
                    if(!currentMinLoopSizeValue.isPresent()) {
                        log.warning("Cannot restore previous value for energy text field?! Setting to " + PREDEFINED_ENERGY_SCORINGS);
                        minLoopSizeTextField.setText(String.valueOf(PREDEFINED_ENERGY_SCORINGS));
                    } else {
                        minLoopSizeTextField.setText(currentMinLoopSizeValue.get());
                        currentMinLoopSizeValue = Optional.empty();
                    }
                }
            }
        }
    };

}
