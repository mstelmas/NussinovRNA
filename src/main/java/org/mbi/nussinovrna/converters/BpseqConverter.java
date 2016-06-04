package org.mbi.nussinovrna.converters;

import lombok.NonNull;
import org.mbi.nussinovrna.rna.RnaNucleotide;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;
import org.mbi.nussinovrna.rna.RnaSequence;

import java.util.Optional;
import java.util.stream.IntStream;

/* TODO: Refactor to Java 8 */
/* TODO: Make more readable */
public class BpseqConverter {
    public static String toBpseqFormat(@NonNull final RnaSecondaryStruct rnaSecondaryStruct) {
        final StringBuilder stringBuilder = new StringBuilder();

        final RnaSequence rnaSequence = rnaSecondaryStruct.getRnaSequence();

        final String[] bseqEntries = new String[rnaSequence.getLength()];

        IntStream.range(0, rnaSequence.getLength()).forEach(i -> {
            if(bseqEntries[i] == null) {

                bseqEntries[i] =  toBpseqEntry(i + 1, rnaSequence.getAsList().get(i), rnaSecondaryStruct.getSecondaryStructMap().get(i));

                Optional.ofNullable(rnaSecondaryStruct.getSecondaryStructMap().get(i)).ifPresent(relatedPair -> {
                    bseqEntries[relatedPair] = toBpseqEntry(relatedPair + 1, rnaSequence.getAsList().get(relatedPair), i);
                });
            }
            stringBuilder.append(bseqEntries[i]);
        });

        return stringBuilder.toString();
    }

    private static String toBpseqEntry(final long entryNumber, final RnaNucleotide nucleitide, final Integer pair) {
        return String.format("%d %s %d\n", entryNumber, nucleitide, Optional.ofNullable(pair).map(nucleitidePair -> nucleitidePair + 1).orElse(0));
    }

}
