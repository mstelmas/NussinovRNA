package org.mbi.nussinovrna.algorithm.scoring;

import org.mbi.nussinovrna.UnorderedPair;
import org.mbi.nussinovrna.rna.RnaNucleotide;

import java.util.Map;

public interface EnergyScoringStrategy {
    String getStrategyName();
    Map<UnorderedPair<RnaNucleotide>, Integer> getScoringStrategy();
}
