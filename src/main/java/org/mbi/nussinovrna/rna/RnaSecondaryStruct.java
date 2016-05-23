package org.mbi.nussinovrna.rna;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Builder
@RequiredArgsConstructor
public class RnaSecondaryStruct {
    @Getter private final RnaSequence rnaSequence;
    @Getter private final Map<Integer, Integer> secondaryStructMap;
    @Getter private final int[][] nussinovMap;


    public String getNussinovMatrixAsString() {
        final StringBuilder nussinovMatrixBuilder = new StringBuilder();

        Optional.ofNullable(nussinovMap).ifPresent(nussinovMatrix -> {
            final int matrixSize = nussinovMatrix.length;

            IntStream.range(0, matrixSize).forEach(i -> {

                final StringBuilder nussinovRowBuilder = new StringBuilder();
                nussinovRowBuilder.append("\t|");

                IntStream.range(0, matrixSize).forEach(j -> {
                    final int entryLength = String.valueOf(nussinovMatrix[i][j]).trim().length();

                    nussinovRowBuilder.append(nussinovMatrix[i][j]);

                    if(entryLength == 1) {
                        nussinovRowBuilder.append("   ");
                    } else {
                        nussinovRowBuilder.append("  ");
                    }
                });

                nussinovRowBuilder.append("|\n");
                nussinovMatrixBuilder.append(nussinovRowBuilder);
            });

        });

        return nussinovMatrixBuilder.toString();
    }
}
