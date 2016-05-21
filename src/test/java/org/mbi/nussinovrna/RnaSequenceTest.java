package org.mbi.nussinovrna;

import org.junit.Test;
import org.mbi.nussinovrna.rna.RnaNucleotide;
import org.mbi.nussinovrna.rna.RnaSequence;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mbi.nussinovrna.rna.RnaNucleotide.*;

public class RnaSequenceTest {

    private static final String VALID_TEST_SEQUENCE = "AGUCGUACCA";
    private static final List<RnaNucleotide> VALID_TEST_SEQUENCE_LIST =
            Arrays.asList(A, G, U, C, G, U, A, C, C, A);

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowRnaSequenceWithInvalidNucleotideName() {
        RnaSequence.of("AGUCGUATCG");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowCreationOfEmptyRnaSequence() {
        RnaSequence.of("");
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotAllowCreationOfNullRnaSequence() {
        RnaSequence.of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowProperRnaSequenceOfOddLengthName() {
        RnaSequence.of("AGUCGUACC");
    }

    @Test
    public void shouldCreateProperRnaSequence() {
        RnaSequence.of("AGUCGUACCA");
    }

    @Test
    public void shouldReturnAProperListOfNucleotides() {
        final RnaSequence rnaSequence = RnaSequence.of(VALID_TEST_SEQUENCE);

        assertThat( rnaSequence.getAsList())
                .hasSize(VALID_TEST_SEQUENCE.length())
                .containsExactlyElementsOf(VALID_TEST_SEQUENCE_LIST);

    }
}
