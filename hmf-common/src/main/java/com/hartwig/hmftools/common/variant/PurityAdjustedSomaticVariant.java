package com.hartwig.hmftools.common.variant;

import com.hartwig.hmftools.common.purple.region.GermlineStatus;

import org.jetbrains.annotations.NotNull;

public interface PurityAdjustedSomaticVariant extends SomaticVariant {

    double adjustedCopyNumber();

    double adjustedVAF();

    double ploidy();

    double minorAllelePloidy();

    boolean biallelic();

    @NotNull
    GermlineStatus germlineStatus();
}
