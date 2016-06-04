package org.mbi.nussinovrna.converters;

import lombok.NonNull;
import org.mbi.nussinovrna.rna.RnaNucleotide;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;
import org.mbi.nussinovrna.rna.RnaSequence;

import java.util.Optional;
import java.util.stream.IntStream;

/* TODO: Refactor */
public class CtConverter {
    public static String toCtFormat(@NonNull final RnaSecondaryStruct rnaSecondaryStruct)  {
        final StringBuilder stringBuilder = new StringBuilder();

        final RnaSequence rnaSequence = rnaSecondaryStruct.getRnaSequence();

        final String[] ctEntries = new String[rnaSequence.getLength()];

        stringBuilder.append(String.format("%d\n", rnaSequence.getLength()));

        IntStream.range(0, rnaSequence.getLength()).forEach(i -> {
            if(ctEntries[i] == null) {

                final long follower = (i + 2 >= rnaSequence.getLength() ? 0 : i + 2);

                ctEntries[i] =  toCtEntry(i + 1, rnaSequence.getAsList().get(i), i, follower, rnaSecondaryStruct.getSecondaryStructMap().get(i));

                Optional.ofNullable(rnaSecondaryStruct.getSecondaryStructMap().get(i)).ifPresent(relatedPair -> {
                    ctEntries[relatedPair] = toCtEntry(relatedPair + 1, rnaSequence.getAsList().get(relatedPair), relatedPair,
                            relatedPair + 2 > rnaSequence.getLength() ? 0 : relatedPair + 2, i);
                });
            }
            stringBuilder.append(ctEntries[i]);
        });

        return stringBuilder.toString();
    }

    private static String toCtEntry(final long entryNumber, final RnaNucleotide nucleitide,
                                    final long preceder, final long follower,
                                    final Integer pair) {

        return String.format("%d %s %d %d %d %d\n", entryNumber, nucleitide,
                preceder, follower,
                Optional.ofNullable(pair).map(nucleitidePair -> nucleitidePair + 1).orElse(0), entryNumber);
    }
}
