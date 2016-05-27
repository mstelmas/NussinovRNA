package org.mbi.nussinovrna;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mbi.nussinovrna.exceptions.ViennaFormatException;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;
import org.mbi.nussinovrna.rna.RnaSequence;

import static org.assertj.core.api.Assertions.assertThat;

public class ViennaConverterTest {

    private final RnaSecondaryStruct rnaSecondaryStruct = RnaSecondaryStruct.builder()
            .rnaSequence(RnaSequence.of("GCACGACG"))
            .secondaryStructMap(
                    new ImmutableMap.Builder<Integer, Integer>()
                        .put(0, 1)
                        .put(3, 7)
                        .put(4, 6)
                        .build()
            )
            .nussinovMap(null)
            .build();


    private final RnaSecondaryStruct overlappingSecondaryStruct = RnaSecondaryStruct.builder()
            .rnaSequence(RnaSequence.of("GCACGACG"))
            .secondaryStructMap(
                    new ImmutableMap.Builder<Integer, Integer>()
                            .put(0, 1)
                            .put(3, 4)
                            .put(4, 6)
                            .build()
            )
            .nussinovMap(null)
            .build();


    @Test
    public void shouldConvertToViennaProperly() {
        final String viennaFormat = ViennaConverter.toViennaFormat(rnaSecondaryStruct);
        assertThat(viennaFormat).isEqualTo("().((.))");
    }

    @Test(expected = ViennaFormatException.class)
    public void shouldDetectOverlappingSecondaryStruct() {
        ViennaConverter.toViennaFormatWithValidation(overlappingSecondaryStruct);
    }
}
