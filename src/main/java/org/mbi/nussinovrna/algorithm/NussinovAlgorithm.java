package org.mbi.nussinovrna.algorithm;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.mbi.nussinovrna.UnorderedPair;
import org.mbi.nussinovrna.rna.RnaNucleotide;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;
import org.mbi.nussinovrna.rna.RnaSequence;

import java.util.List;
import java.util.stream.IntStream;

import static org.mbi.nussinovrna.rna.RnaNucleotide.*;

public final class NussinovAlgorithm {

    private final int[][] nussinovMatrix;
    private final RnaSequence rnaSequence;
    private final int rnaSequenceLength;

    private final ImmutableMap<UnorderedPair<RnaNucleotide>, Integer> NUCLEOTIDE_PAIRS_MAPPING =
            new ImmutableMap.Builder<UnorderedPair<RnaNucleotide>, Integer>()
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


    @Getter
    private final RnaSecondaryStruct rnaSecondaryStruct;

    public NussinovAlgorithm(final RnaSequence rnaSequence) {
        this.rnaSequence = rnaSequence;
        this.rnaSequenceLength = rnaSequence.getLength();

        this.nussinovMatrix = new int[rnaSequenceLength][rnaSequenceLength];

        this.rnaSecondaryStruct = applyAlgorithm();
    }


    private RnaSecondaryStruct applyAlgorithm() {
        createNussinovMatrix();

        return new RnaSecondaryStruct();
    }

    private void createNussinovMatrix() {

        final List<RnaNucleotide> rnaSequenceAsList = rnaSequence.getAsList();

        IntStream.range(1, rnaSequenceLength).forEach(i -> {
            IntStream.range(i, rnaSequenceLength).forEach(j -> {
                final int n = j - i;

                final int case1 = nussinovMatrix[n + 1][j - 1] +
                        NUCLEOTIDE_PAIRS_MAPPING.get(UnorderedPair.of(rnaSequenceAsList.get(n), rnaSequenceAsList.get(j)));

                final int case2 = nussinovMatrix[n + 1][j];
                final int case3 = nussinovMatrix[n][j - 1];

                if(n + 3 <= j) {

                    int tmp = 0;

                    for(int k = n + 1; k < j; k++) {
                        if((nussinovMatrix[n][k] + nussinovMatrix[k + 1][j]) > tmp) {
                            tmp = nussinovMatrix[n][k] + nussinovMatrix[k + 1][j];
                        }

                        nussinovMatrix[n][j] = Math.max(
                                Math.max(case1, case2),
                                Math.max(case3, tmp)
                        );

                    }
                } else {
                    nussinovMatrix[n][j] = Math.max(Math.max(case1, case2), case3);
                }
            });
        });
    }

}
