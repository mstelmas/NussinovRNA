package org.mbi.nussinovrna;

import org.mbi.nussinovrna.algorithm.NussinovAlgorithm;
import org.mbi.nussinovrna.algorithm.SecondaryStructAlgorithm;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;
import org.mbi.nussinovrna.rna.RnaSequence;

import javax.swing.*;

public class App extends JFrame {

    private final static String APP_TITLE = "Nussinov Algorithm";

    private final JButton helloButton = new JButton("Click Me!");

    private App() {

        final RnaSequence rnaSequence = RnaSequence.of("GCACGACG");
        final NussinovAlgorithm nussinovAlgorithm = new NussinovAlgorithm(rnaSequence);
        final RnaSecondaryStruct rnaSecondaryStruct = nussinovAlgorithm.getRnaSecondaryStruct();


        helloButton.addActionListener(actionListener -> {
            JOptionPane.showMessageDialog(this, "Hello Nussinov!", "Hello Nussinov!", JOptionPane.INFORMATION_MESSAGE);
        });

        this.getContentPane().add(helloButton);

        this.setTitle(APP_TITLE);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
