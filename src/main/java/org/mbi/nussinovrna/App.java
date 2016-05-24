package org.mbi.nussinovrna;

import javaslang.control.Try;
import org.mbi.nussinovrna.algorithm.NussinovAlgorithm;
import org.mbi.nussinovrna.gui.NussinovMatrixGrid;
import org.mbi.nussinovrna.gui.NussinovMatrixPanel;
import org.mbi.nussinovrna.rna.RnaSequence;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

public class App extends JFrame {

    private final static String APP_TITLE = "Nussinov Algorithm";

    private final JPanel framePanel = new JPanel(new BorderLayout());

    private final JPanel rnaSequencePanel = new JPanel(new BorderLayout());
    private final JPanel rnaSequenceAreaPanel = new JPanel();
    private final TitledBorder rnaSequenceAreaPanelBorder = BorderFactory.createTitledBorder("RNA Sequence");
    private final JPanel rnaSequenceButtonsPanel = new JPanel(new BorderLayout());
    private final JTextArea rnaSequenceTextArea = new JTextArea(20, 35);
    private final JButton rnaSequenceCalculateButton = new JButton("Calculate");
    private final JButton rnaSequenceClearButton = new JButton("Clear");

    private JTabbedPane nussinovTabbedPane;
    private final TitledBorder secondaryStructurePanelBorder = BorderFactory.createTitledBorder("RNA Secondary Structure");


    private final NussinovMatrixPanel nussinovMatrixPanel = new NussinovMatrixPanel(new NussinovMatrixGrid());
    private final JScrollPane nussinovMatrixScrollPane = new JScrollPane(nussinovMatrixPanel);

    private final JPanel nussinovPredictedStructurePanel = new JPanel();


    private JMenuBar menuBar;

    private final JMenu fileMenu = new JMenu("File");

    private final JMenuItem exitMenuItem = new JMenuItem("Exit");
    private final JMenuItem openMenuItem = new JMenuItem("Open");
    private final JMenuItem saveMenuItem = new JMenuItem("Save");

    private final JFileChooser rnaSequenceFileChooser = new JFileChooser();

    private App() {

        buildGui();

        this.setTitle(APP_TITLE);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    private void buildGui() {

        menuBar = buildMenuBar();

        nussinovTabbedPane = buildNussinovTabbedPanel();


        rnaSequenceTextArea.setLineWrap(true);
        // for debugging
        rnaSequenceTextArea.setText("GCACGACG");


        rnaSequenceAreaPanel.add(rnaSequenceTextArea);
        rnaSequenceAreaPanel.setBorder(rnaSequenceAreaPanelBorder);

        rnaSequenceButtonsPanel.add(rnaSequenceCalculateButton, BorderLayout.NORTH);
        rnaSequenceButtonsPanel.add(rnaSequenceClearButton, BorderLayout.SOUTH);

        rnaSequencePanel.add(rnaSequenceAreaPanel, BorderLayout.NORTH);
        rnaSequencePanel.add(rnaSequenceButtonsPanel, BorderLayout.SOUTH);

        rnaSequenceCalculateButton.addActionListener(calculateButtonActionListener);
        rnaSequenceClearButton.addActionListener(clearButtonActionListener);


        nussinovMatrixPanel.setAutoscrolls(true);


        framePanel.add(rnaSequencePanel, BorderLayout.WEST);
        nussinovMatrixPanel.setPreferredSize(new Dimension(400, 400));
        nussinovMatrixScrollPane.setViewportView(nussinovMatrixPanel);
        nussinovMatrixScrollPane.setPreferredSize(new Dimension(100, 100));
        framePanel.add(nussinovTabbedPane, BorderLayout.CENTER);
//        framePanel.add(nussinovMatrixScrollPane, BorderLayout.CENTER);

        this.setJMenuBar(menuBar);
        this.getContentPane().add(framePanel);
    }

    private JTabbedPane buildNussinovTabbedPanel() {
        final JTabbedPane nussinovTabbedPane = new JTabbedPane();
        nussinovTabbedPane.setBorder(secondaryStructurePanelBorder);


//        nussinovMatrixScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//        nussinovMatrixScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
//
        nussinovTabbedPane.addTab("Nussinov Matrix", nussinovMatrixScrollPane);
        nussinovTabbedPane.addTab("Secondary Structure", nussinovPredictedStructurePanel);

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

    private ActionListener calculateButtonActionListener = actionEvent -> {
        Try.of (() ->
                Optional.ofNullable(rnaSequenceTextArea.getText())
                    .map(RnaSequence::of)
                    .map(NussinovAlgorithm::new)
                    .map(NussinovAlgorithm::getRnaSecondaryStruct)
                    .get()
        ).onFailure(e -> {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid RNA sequence", JOptionPane.ERROR_MESSAGE);
            rnaSequenceTextArea.selectAll();
            rnaSequenceTextArea.requestFocus();
        }).onSuccess(nussinovMatrixPanel::setRnaSecondaryStruct);
    };

    private ActionListener clearButtonActionListener = actionEvent -> {
        rnaSequenceTextArea.setText("");
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
