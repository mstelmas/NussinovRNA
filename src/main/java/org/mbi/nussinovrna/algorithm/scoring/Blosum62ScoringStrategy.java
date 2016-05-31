package org.mbi.nussinovrna.algorithm.scoring;

import com.google.common.collect.ImmutableMap;
import org.mbi.nussinovrna.UnorderedPair;
import org.mbi.nussinovrna.rna.RnaNucleotide;

import java.util.Map;

import static org.mbi.nussinovrna.rna.RnaNucleotide.*;

public class Blosum62ScoringStrategy implements EnergyScoringStrategy {
    @Override
    public String getStrategyName() {
        return "Blosum62";
    }

    @Override
    public Map<UnorderedPair<RnaNucleotide>, Integer> getScoringStrategy() {
        return new ImmutableMap.Builder<UnorderedPair<RnaNucleotide>, Integer>()
                .put(UnorderedPair.of(A, A), 4)
                .put(UnorderedPair.of(A, C), 0)
                .put(UnorderedPair.of(A, G), 0)
                .put(UnorderedPair.of(C, C), 9)
                .put(UnorderedPair.of(C, G), -3)
                .put(UnorderedPair.of(G, G), 6)
                .build();
    }
}
