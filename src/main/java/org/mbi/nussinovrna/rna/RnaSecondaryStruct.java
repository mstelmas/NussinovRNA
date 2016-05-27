package org.mbi.nussinovrna.rna;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Getter
@Builder
@RequiredArgsConstructor
public class RnaSecondaryStruct {
    @NonNull private final RnaSequence rnaSequence;
    @NonNull private final Map<Integer, Integer> secondaryStructMap;
    private final int[][] nussinovMap;
}
