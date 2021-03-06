package com.hartwig.hmftools.common.purple.purity;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public abstract class FittedPurity implements Comparable<FittedPurity> {

    public abstract double purity();

    public abstract double normFactor();

    public abstract double ploidy();

    public abstract double score();

    public abstract double diploidProportion();

    public abstract double somaticDeviation();

    @Override
    public int compareTo(@NotNull FittedPurity o) {
        return Double.compare(score(), o.score());
    }
}
