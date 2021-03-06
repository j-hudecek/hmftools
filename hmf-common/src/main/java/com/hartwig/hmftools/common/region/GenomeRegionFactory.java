package com.hartwig.hmftools.common.region;

import org.jetbrains.annotations.NotNull;

public final class GenomeRegionFactory {

    private GenomeRegionFactory() {
    }

    @NotNull
    public static GenomeRegion create(@NotNull final String chromosome, final long start, final long end) {
        return ImmutableGenomeRegionImpl.builder().chromosome(chromosome).start(start).end(end).build();
    }
}
