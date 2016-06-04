package org.mbi.nussinovrna;

import com.google.common.collect.ImmutableMap;
import com.sun.corba.se.impl.encoding.CodeSetConversion;
import org.junit.Test;
import org.mbi.nussinovrna.converters.BpseqConverter;
import org.mbi.nussinovrna.converters.CtConverter;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;
import org.mbi.nussinovrna.rna.RnaSequence;

import static org.assertj.core.api.Assertions.assertThat;

public class CtConverterTest {
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
    public void shouldConvertToCTProperly() {
        final String ctFormat = CtConverter.toCtFormat(rnaSecondaryStruct);
        assertThat(ctFormat).isEqualTo(
                        "8\n" +
                        "1 G 0 2 8 1\n" +
                        "2 G 1 3 7 2\n" +
                        "3 C 2 4 0 3\n" +
                        "4 A 3 5 0 4\n" +
                        "5 U 4 6 0 5\n" +
                        "6 U 5 7 0 6\n" +
                        "7 C 6 8 2 7\n" +
                        "8 C 7 0 1 8\n"
        );
    }
}
