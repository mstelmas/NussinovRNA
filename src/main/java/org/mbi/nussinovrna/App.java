package org.mbi.nussinovrna;

import com.google.common.collect.ImmutableList;
import fr.orsay.lri.varna.VARNAPanel;
import fr.orsay.lri.varna.models.export.SwingGraphics;
import fr.orsay.lri.varna.models.export.VueVARNAGraphics;
import javaslang.control.Match;
import javaslang.control.Try;
import lombok.extern.java.Log;
import net.miginfocom.swing.MigLayout;
import org.mbi.nussinovrna.algorithm.NussinovAlgorithm;
import org.mbi.nussinovrna.algorithm.scoring.*;
import org.mbi.nussinovrna.gui.EnergyScorePanel;
import org.mbi.nussinovrna.gui.NussinovMatrixGrid;
import org.mbi.nussinovrna.gui.NussinovMatrixPanel;
import org.mbi.nussinovrna.rna.RnaSequence;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.logging.Level;

@Log
public class App extends JFrame {

    private final static String APP_TITLE = "Nussinov Algorithm";

    private final JPanel framePanel = new JPanel(new MigLayout("fill"));

    private JPanel leftPanel;

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

    private final JPanel rnaSequenceAreaPanel = new JPanel();
    private final TitledBorder rnaSequenceAreaPanelBorder = BorderFactory.createTitledBorder("RNA Sequence");
    private final JTextArea rnaSequenceTextArea = new JTextArea(10, 35);
    private final JScrollPane rnaSequenceTextScrollPane = new JScrollPane(rnaSequenceTextArea);
    private final JButton rnaSequenceCalculateButton = new JButton("Calculate");
    private final JButton rnaSequenceClearButton = new JButton("Clear");

    private final JButton saveRnaImageButton = new JButton("Save to file");

    private final JLabel viennaLabel = new JLabel("Vienna format: ");
    private final JLabel viennaFormatLabel = new JLabel();

    private JTabbedPane nussinovTabbedPane;


    private final NussinovMatrixPanel nussinovMatrixPanel = new NussinovMatrixPanel(new NussinovMatrixGrid());
    private final JScrollPane nussinovMatrixScrollPane = new JScrollPane(nussinovMatrixPanel);

    private final EnergyScorePanel energyScorePanel = new EnergyScorePanel(PREDEFINED_ENERGY_SCORINGS.get(0));
    private final TitledBorder energyScorePanelBorder = BorderFactory.createTitledBorder("Energy Scores");

    private final JComboBox<EnergyScoringStrategy> energyScoringStrategyJComboBox = new JComboBox<>(
            new DefaultComboBoxModel(PREDEFINED_ENERGY_SCORINGS.toArray())
    );

    private JMenuBar menuBar;

    private final JMenu fileMenu = new JMenu("File");

    private final JMenuItem exitMenuItem = new JMenuItem("Exit");
    private final JMenuItem openMenuItem = new JMenuItem("Open");
    private final JMenuItem saveMenuItem = new JMenuItem("Save");

    private final JFileChooser rnaSequenceFileChooser = new JFileChooser();

    private final VARNAPanel varnaPanel = new VARNAPanel();

    private App() {

        Try.run(() -> {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }).onFailure((e) -> {
            log.info("Using default Look and Feel manager...");
        });

        buildGui();

        this.setTitle(APP_TITLE);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationByPlatform(true);
        this.pack();
        this.setVisible(true);
    }

    private void buildGui() {

        menuBar = buildMenuBar();

        nussinovTabbedPane = buildNussinovTabbedPanel();


        leftPanel = buildLeftPanel();

        rnaSequenceTextArea.setLineWrap(true);
        // for debugging
        rnaSequenceTextArea.setText("GCACGACG");

        rnaSequenceAreaPanel.setLayout(new BorderLayout());
        rnaSequenceAreaPanel.add(rnaSequenceTextScrollPane, BorderLayout.CENTER);
        rnaSequenceAreaPanel.setBorder(rnaSequenceAreaPanelBorder);

        rnaSequenceCalculateButton.addActionListener(calculateButtonActionListener);
        rnaSequenceClearButton.addActionListener(clearButtonActionListener);
        saveRnaImageButton.addActionListener(saveVisulatizationToFileListner);

        nussinovMatrixPanel.setAutoscrolls(true);


        framePanel.add(leftPanel, "dock west");
        framePanel.add(nussinovTabbedPane, "dock center");

        this.setJMenuBar(menuBar);
        this.getContentPane().add(framePanel);
    }

    private JPanel buildLeftPanel() {
        final JPanel leftPanel = new JPanel();

        leftPanel.setLayout(new MigLayout());

        leftPanel.add(rnaSequenceAreaPanel, "wrap, push, grow");

        energyScoringStrategyJComboBox.setRenderer(energyScoresComboBoxRenderer);
        energyScoringStrategyJComboBox.addActionListener(energyScoresComboBoxListener);

        energyScorePanel.setBorder(energyScorePanelBorder);

        leftPanel.add(energyScoringStrategyJComboBox, "wrap");

        leftPanel.add(energyScorePanel, "wrap, push, grow");

        leftPanel.add(rnaSequenceClearButton, "split");
        leftPanel.add(rnaSequenceCalculateButton);

        leftPanel.add(saveRnaImageButton);

        return leftPanel;
    }

    private final ListCellRenderer energyScoresComboBoxRenderer = new DefaultListCellRenderer() {

        @Override
        public Component getListCellRendererComponent(final JList<?> list, final Object value,
                                                      final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if(value instanceof EnergyScoringStrategy) {
                setText(((EnergyScoringStrategy)value).getStrategyName());
            }

            return this;
        }
    };

    private final ActionListener energyScoresComboBoxListener = (actionEvent) -> {
        if(actionEvent.getSource() instanceof JComboBox) {
            final JComboBox comboBoxSource = (JComboBox) actionEvent.getSource();

            if(comboBoxSource.getSelectedItem() instanceof EnergyScoringStrategy) {
                final EnergyScoringStrategy selectedScoringStrategy = (EnergyScoringStrategy) comboBoxSource.getSelectedItem();

                log.log(Level.INFO, String.format("Loading selected Scoring Strategy: %s", selectedScoringStrategy.getStrategyName()));

                energyScorePanel.loadEnergyScores(selectedScoringStrategy);

            } else {
                log.warning("Invalid selected item type (no EnergyScoringStrategy) ...?!");
            }
        }
    };

    private JPanel buildPredictedSecondaryStructurePanel() {
        final JPanel nussinovPredictedStructurePanel = new JPanel();

        nussinovPredictedStructurePanel.setLayout(new MigLayout());

        viennaLabel.setLabelFor(viennaFormatLabel);

        nussinovPredictedStructurePanel.add(viennaLabel);
        nussinovPredictedStructurePanel.add(viennaFormatLabel);

        return nussinovPredictedStructurePanel;
    }

    private JTabbedPane buildNussinovTabbedPanel() {
        final JTabbedPane nussinovTabbedPane = new JTabbedPane();

        nussinovTabbedPane.add("Drawing", varnaPanel);
        nussinovTabbedPane.addTab("RNA Secondary Structure", buildPredictedSecondaryStructurePanel());
        nussinovTabbedPane.addTab("Nussinov Matrix", nussinovMatrixScrollPane);

        return nussinovTabbedPane;
    }



    private JMenuBar buildMenuBar() {

        final JMenuBar menuBar = new JMenuBar();

        openMenuItem.addActionListener(openMenuItemActionListener);
        fileMenu.add(openMenuItem);

        saveMenuItem.addActionListener(saveMenuItemActionListener);
        fileMenu.add(saveMenuItem);

        fileMenu.addSeparator();

        exitMenuItem.addActionListener(exitMenuItemActionListener);
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        return menuBar;
    }

    // tmp
    private ActionListener saveVisulatizationToFileListner = actionEvent -> {
        final BufferedImage bufferedImage = new BufferedImage(
                varnaPanel.getInnerWidth(),
                varnaPanel.getInnerHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        final Graphics2D graphics = bufferedImage.createGraphics();


        final VueVARNAGraphics vueVARNAGraphics = new SwingGraphics(graphics);

        final Color currentColor = vueVARNAGraphics.getColor();

        vueVARNAGraphics.setColor(Color.WHITE);

        vueVARNAGraphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        vueVARNAGraphics.setColor(currentColor);


        final Rectangle2D.Double rectange = new Rectangle2D.Double(
                0, 0, bufferedImage.getWidth(), bufferedImage.getHeight()
        );

        varnaPanel.renderRNA(vueVARNAGraphics, rectange, true, true);

        graphics.dispose();

        final File f = new File("savedImage.png");

        Try.run(() -> ImageIO.write(bufferedImage, "png", f))
                .onFailure((e) -> JOptionPane.showMessageDialog(this, e.getMessage(), "Could not save RNA image", JOptionPane.ERROR_MESSAGE));


    };

    private ActionListener calculateButtonActionListener = actionEvent -> {
        Try.of (() ->
                Optional.ofNullable(rnaSequenceTextArea.getText())
                    .map(RnaSequence::of)
                    .map(rnaSequence -> new NussinovAlgorithm(rnaSequence, energyScorePanel.getCurrentEnergyScores()))
                    .map(NussinovAlgorithm::getRnaSecondaryStruct)
                    .get()
        ).onFailure(e -> {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid RNA sequence", JOptionPane.ERROR_MESSAGE);
            rnaSequenceTextArea.selectAll();
            rnaSequenceTextArea.requestFocus();
        }).onSuccess(predictedSecondaryStructure -> {

            final String viennaFormat = ViennaConverter.toViennaFormat(predictedSecondaryStructure);

            nussinovMatrixPanel.setRnaSecondaryStruct(predictedSecondaryStructure);
            viennaFormatLabel.setText(viennaFormat);
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
        viennaFormatLabel.setText("");
        nussinovMatrixPanel.setRnaSecondaryStruct(null);
    };

    private ActionListener exitMenuItemActionListener = actionEvent -> {
        // TODO: see if there is anything to save before exiting
        System.exit(0);
    };

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

    private ActionListener saveMenuItemActionListener = actionEvent -> {
        JOptionPane.showMessageDialog(this, "Not implemented!", "Not implemented", JOptionPane.INFORMATION_MESSAGE);
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
