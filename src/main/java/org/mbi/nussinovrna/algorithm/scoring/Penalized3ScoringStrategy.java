package org.mbi.nussinovrna.algorithm.scoring;

import com.google.common.collect.ImmutableMap;
import org.mbi.nussinovrna.UnorderedPair;
import org.mbi.nussinovrna.rna.RnaNucleotide;

import java.util.Map;

import static org.mbi.nussinovrna.rna.RnaNucleotide.*;
import static org.mbi.nussinovrna.rna.RnaNucleotide.G;
import static org.mbi.nussinovrna.rna.RnaNucleotide.U;

public class Penalized3ScoringStrategy implements EnergyScoringStrategy {
    @Override
    public String getStrategyName() {
        return "Penalize Mismatch (-3)";
    }

    @Override
    public Map<UnorderedPair<RnaNucleotide>, Integer> getScoringStrategy() {
        return new ImmutableMap.Builder<UnorderedPair<RnaNucleotide>, Integer>()
                .put(UnorderedPair.of(A, A), 1)
                .put(UnorderedPair.of(A, C), -3)
                .put(UnorderedPair.of(A, G), -3)
                .put(UnorderedPair.of(A, U), -3)
                .put(UnorderedPair.of(C, C), 1)
                .put(UnorderedPair.of(C, G), -3)
                .put(UnorderedPair.of(C, U), -3)
                .put(UnorderedPair.of(G, G), 1)
                .put(UnorderedPair.of(G, U), -3)
                .put(UnorderedPair.of(U, U), 1)
                .build();
    }
}
