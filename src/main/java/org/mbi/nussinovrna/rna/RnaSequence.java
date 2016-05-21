package org.mbi.nussinovrna.rna;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;

import static java.util.stream.Collectors.toList;

@EqualsAndHashCode
public final class RnaSequence {
    private final String sequence;

    private List<RnaNucleotide> listOfNucleitides;

    private RnaSequence(final String sequence) {
        this.sequence = sequence;
    }


    public static RnaSequence of(@NonNull final String sequence) {
        if(sequence.isEmpty()) {
            throw new IllegalArgumentException("RNASequence cannot be empty!");
        }

        if(sequence.length() % 2 != 0) {
            throw new IllegalArgumentException("RNASequence cannot be of odd length!");
        }

        validateSequenceIntegrity(sequence);

        return new RnaSequence(sequence);
    }

    public String getAsString() {
        return sequence;
    }

    public List<RnaNucleotide> getAsList() {
        if(listOfNucleitides == null) {
            listOfNucleitides = sequence.chars()
                    .mapToObj(obj -> (char)obj)
                    .map(String::valueOf)
                    .map(RnaNucleotide::valueOf)
                    .collect(toList());
        }

        return Collections.unmodifiableList(listOfNucleitides);
    }

    private static void validateSequenceIntegrity(final String sequence) {
        final long allowableCharsSequenceLength = sequence.chars().parallel().filter(VALID_RNA_CHARACTERS).count();

        if(allowableCharsSequenceLength != sequence.length()) {
            throw new IllegalArgumentException("RNASequence contains not allowable characters");
        }
    }

    private static IntPredicate VALID_RNA_CHARACTERS = rnaCharacter -> rnaCharacter == 'A' || rnaCharacter == 'G' ||
            rnaCharacter == 'U' || rnaCharacter == 'C';

}
