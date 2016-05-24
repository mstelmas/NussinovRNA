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
}
