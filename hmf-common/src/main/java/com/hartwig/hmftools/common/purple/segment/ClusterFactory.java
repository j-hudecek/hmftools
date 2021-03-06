package com.hartwig.hmftools.common.purple.segment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.hartwig.hmftools.common.cobalt.CobaltRatio;
import com.hartwig.hmftools.common.numeric.Doubles;
import com.hartwig.hmftools.common.pcf.PCFPosition;
import com.hartwig.hmftools.common.position.GenomePosition;
import com.hartwig.hmftools.common.variant.structural.StructuralVariant;
import com.hartwig.hmftools.common.window.Window;

import org.jetbrains.annotations.NotNull;

public class ClusterFactory {

    public final long windowSize;
    private final Window window;

    public ClusterFactory(final int windowSize) {
        this.windowSize = windowSize;
        this.window = new Window(windowSize);
    }

    @NotNull
    public ListMultimap<String, Cluster> cluster(@NotNull final List<StructuralVariant> variants,
            @NotNull final Multimap<String, PCFPosition> pcfPositions, @NotNull ListMultimap<String, CobaltRatio> ratios) {
        final Multimap<String, ClusterVariantLeg> positions = asMap(ClusterVariantLegFactory.create(variants));
        return cluster(positions, pcfPositions, ratios);
    }

    @NotNull
    private ListMultimap<String, Cluster> cluster(@NotNull final Multimap<String, ClusterVariantLeg> variantPositions,
            @NotNull final Multimap<String, PCFPosition> pcfPositions, @NotNull final ListMultimap<String, CobaltRatio> ratios) {
        ListMultimap<String, Cluster> clusters = ArrayListMultimap.create();
        for (String chromosome : pcfPositions.keySet()) {
            final Collection<PCFPosition> chromosomePcfPositions = pcfPositions.get(chromosome);
            final Collection<ClusterVariantLeg> chromosomeVariants =
                    variantPositions.containsKey(chromosome) ? variantPositions.get(chromosome) : Collections.EMPTY_LIST;
            final List<CobaltRatio> chromosomeRatios = ratios.containsKey(chromosome) ? ratios.get(chromosome) : Collections.EMPTY_LIST;
            clusters.putAll(chromosome, cluster(chromosomeVariants, chromosomePcfPositions, chromosomeRatios));
        }

        return clusters;
    }

    @NotNull
    @VisibleForTesting
    List<Cluster> cluster(@NotNull final Collection<ClusterVariantLeg> variantPositions,
            @NotNull final Collection<PCFPosition> pcfPositions, @NotNull final List<CobaltRatio> cobaltRatios) {
        final List<GenomePosition> allPositions = Lists.newArrayList();
        allPositions.addAll(variantPositions);
        allPositions.addAll(pcfPositions);
        Collections.sort(allPositions);

        final List<Cluster> result = Lists.newArrayList();

        int cobaltIndex = 0;
        ModifiableCluster segment = null;
        for (GenomePosition position : allPositions) {
            if (position.position() == 1) {
                continue;
            }

            while (cobaltIndex < cobaltRatios.size() - 1 && cobaltRatios.get(cobaltIndex).position() < position.position()) {
                cobaltIndex++;
            }

            final long earliestDetectableCopyNumberChangePosition =
                    earliestDetectableCopyNumberChangePosition(position.position(), cobaltIndex, cobaltRatios);
            if (segment == null || earliestDetectableCopyNumberChangePosition > segment.end()) {
                if (segment != null) {
                    result.add(segment);
                }
                segment = ModifiableCluster.create()
                        .setChromosome(position.chromosome())
                        .setStart(earliestDetectableCopyNumberChangePosition);
            }

            segment.setEnd(position.position());

            if (position instanceof ClusterVariantLeg) {
                segment.addVariants((ClusterVariantLeg) position);
            } else {
                segment.addPcfPositions((PCFPosition) position);
            }
        }
        if (segment != null) {
            result.add(segment);
        }

        return result;
    }

    @VisibleForTesting
    long earliestDetectableCopyNumberChangePosition(long position, int index, @NotNull final List<CobaltRatio> ratios) {
        assert (index <= ratios.size());
        final long min = window.start(position) - windowSize + 1;
        if (!ratios.isEmpty()) {
            for (int i = index; i >= 0; i--) {
                final CobaltRatio ratio = ratios.get(i);
                if (ratio.position() <= min && Doubles.greaterThan(ratio.tumorGCRatio(), -1)) {
                    return ratio.position() + 1;
                }
            }
        }

        return min;
    }

    @NotNull
    private static Multimap<String, ClusterVariantLeg> asMap(@NotNull final List<ClusterVariantLeg> variants) {
        final Multimap<String, ClusterVariantLeg> result = ArrayListMultimap.create();

        for (ClusterVariantLeg variant : variants) {
            result.put(variant.chromosome(), variant);
        }

        return result;
    }
}
