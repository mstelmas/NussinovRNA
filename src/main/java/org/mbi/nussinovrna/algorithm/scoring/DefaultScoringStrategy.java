package org.mbi.nussinovrna.algorithm.scoring;

import com.google.common.collect.ImmutableMap;
import org.mbi.nussinovrna.UnorderedPair;
import org.mbi.nussinovrna.rna.RnaNucleotide;

import java.util.Map;

import static org.mbi.nussinovrna.rna.RnaNucleotide.*;
import static org.mbi.nussinovrna.rna.RnaNucleotide.G;
import static org.mbi.nussinovrna.rna.RnaNucleotide.U;

public class DefaultScoringStrategy implements EnergyScoringStrategy {
    @Override
    public String getStrategyName() {
        return "Default";
    }

    @Override
    public Map<UnorderedPair<RnaNucleotide>, Integer> getScoringStrategy() {
        return new ImmutableMap.Builder<UnorderedPair<RnaNucleotide>, Integer>()
                        .put(UnorderedPair.of(A, A), 0)
                        .put(UnorderedPair.of(A, C), 0)
                        .put(UnorderedPair.of(A, G), 0)
                        .put(UnorderedPair.of(A, U), 1)
                        .put(UnorderedPair.of(C, C), 0)
                        .put(UnorderedPair.of(C, G), 1)
                        .put(UnorderedPair.of(C, U), 0)
                        .put(UnorderedPair.of(G, G), 0)
                        .put(UnorderedPair.of(G, U), 0)
                        .put(UnorderedPair.of(U, U), 0)
                        .build();
    }
}
