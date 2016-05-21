package org.mbi.nussinovrna.algorithm;

import org.mbi.nussinovrna.rna.RnaSecondaryStruct;
import org.mbi.nussinovrna.rna.RnaSequence;

public final class NussinovAlgorithm implements org.mbi.nussinovrna.algorithm.SecondaryStructAlgorithm {

    @Override
    public RnaSecondaryStruct execute(final RnaSequence rnaSequence) {
        return new RnaSecondaryStruct();
    }
}