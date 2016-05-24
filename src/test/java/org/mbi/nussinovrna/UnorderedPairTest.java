package org.mbi.nussinovrna;

import org.junit.Test;
import org.mbi.nussinovrna.rna.RnaNucleotide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mbi.nussinovrna.rna.RnaNucleotide.*;

public class UnorderedPairTest {

    @Test
    public void shouldCompareProperlyTwoUPairsOfRnaNucleotides() {
        final UnorderedPair<RnaNucleotide> nucleotideUnorderedPair1 = UnorderedPair.of(A, C);
        final UnorderedPair<RnaNucleotide> nucleotideUnorderedPair2 = UnorderedPair.of(A, C);
        final UnorderedPair<RnaNucleotide> nucleotideUnorderedPair3 = UnorderedPair.of(C, A);

        assertThat(nucleotideUnorderedPair1).isEqualTo(nucleotideUnorderedPair2);
        assertThat(nucleotideUnorderedPair1).isEqualTo(nucleotideUnorderedPair3);
    }
}
