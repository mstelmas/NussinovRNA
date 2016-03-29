package org.mbi.nussinovrna.algorithm;

import org.mbi.nussinovrna.rna.RnaSecondaryStruct;
import org.mbi.nussinovrna.rna.RnaSequence;

public interface SecondaryStructAlgorithm {
    RnaSecondaryStruct execute(final RnaSequence rnaSequence);
}
