package org.mbi.nussinovrna.algorithm;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.mbi.nussinovrna.UnorderedPair;
import org.mbi.nussinovrna.rna.RnaNucleotide;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;
import org.mbi.nussinovrna.rna.RnaSequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final Map<Integer, Integer> pairs = new HashMap<>();

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
        traceback(0, rnaSequenceLength - 1);

        return RnaSecondaryStruct.builder()
                .rnaSequence(rnaSequence)
                .secondaryStructMap(pairs)
                .nussinovMap(nussinovMatrix)
                .build();
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

    /*
        Traceback through the calculated matrix starting from @(iPos, jPos) position to find
        one of the maximally base paired structures.
     */
    private void traceback(final int iPos, final int jPos) {

        final List<RnaNucleotide> rnaSequenceAsList = rnaSequence.getAsList();

        if(iPos < jPos) {
            if (nussinovMatrix[iPos][jPos] == nussinovMatrix[iPos + 1][jPos]) {
                traceback(iPos + 1, jPos);
            } else if (nussinovMatrix[iPos][jPos] == nussinovMatrix[iPos][jPos - 1]) {
                traceback(iPos, jPos - 1);
            } else if (nussinovMatrix[iPos][jPos] == nussinovMatrix[iPos + 1][jPos - 1] +
                    NUCLEOTIDE_PAIRS_MAPPING.get(UnorderedPair.of(rnaSequenceAsList.get(iPos), rnaSequenceAsList.get(jPos)))) {
                pairs.put(iPos, jPos);
                traceback(iPos + 1, jPos - 1);
            } else {
                for (int k = iPos + 1; k < jPos; k++) {
                    if (nussinovMatrix[iPos][jPos] == nussinovMatrix[iPos][k] + nussinovMatrix[k + 1][jPos]) {
                        traceback(iPos, k);
                        traceback(k + 1, jPos);
                        break;
                    }
                }
            }
        }
    }
}
