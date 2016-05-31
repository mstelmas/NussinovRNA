package org.mbi.nussinovrna.algorithm.scoring;


import com.google.common.collect.ImmutableMap;
import org.mbi.nussinovrna.UnorderedPair;
import org.mbi.nussinovrna.rna.RnaNucleotide;

import java.util.Map;

import static org.mbi.nussinovrna.rna.RnaNucleotide.*;

public class Vtml160ScoringStrategy implements EnergyScoringStrategy {
    @Override
    public String getStrategyName() {
        return "Vtml160";
    }

    @Override
    public Map<UnorderedPair<RnaNucleotide>, Integer> getScoringStrategy() {
        return new ImmutableMap.Builder<UnorderedPair<RnaNucleotide>, Integer>()
                .put(UnorderedPair.of(A, A), 5)
                .put(UnorderedPair.of(A, C), 1)
                .put(UnorderedPair.of(A, G), 0)
                .put(UnorderedPair.of(C, C), 13)
                .put(UnorderedPair.of(C, G), -2)
                .put(UnorderedPair.of(G, G), 8)
                .build();
    }
}
