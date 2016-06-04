package org.mbi.nussinovrna;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mbi.nussinovrna.converters.BpseqConverter;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;
import org.mbi.nussinovrna.rna.RnaSequence;

import static org.assertj.core.api.Assertions.assertThat;

public class BpseqConverterTest {
    private final RnaSecondaryStruct rnaSecondaryStruct = RnaSecondaryStruct.builder()
            .rnaSequence(RnaSequence.of("GGCAUUCC"))
            .secondaryStructMap(
                    new ImmutableMap.Builder<Integer, Integer>()
                            .put(0, 7)
                            .put(1, 6)
                            .build()
            )
            .nussinovMap(null)
            .build();

    @Test
    public void shouldConvertToBPSeqProperly() {
        final String bpseqFormat = BpseqConverter.toBpseqFormat(rnaSecondaryStruct);
        assertThat(bpseqFormat).isEqualTo(
                        "1 G 8\n" +
                        "2 G 7\n" +
                        "3 C 0\n" +
                        "4 A 0\n" +
                        "5 U 0\n" +
                        "6 U 0\n" +
                        "7 C 2\n" +
                        "8 C 1\n"
        );
    }
}
