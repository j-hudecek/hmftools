package com.hartwig.hmftools.common.purple.copynumber.sv;

import static java.util.Collections.singleton;

import static com.hartwig.hmftools.common.purple.copynumber.sv.StructuralVariantLegsFactoryTest.createLeg;
import static com.hartwig.hmftools.common.purple.copynumber.sv.StructuralVariantPloidyTest.CHROMOSOME;
import static com.hartwig.hmftools.common.purple.copynumber.sv.StructuralVariantPloidyTest.PURE;

import static org.apache.commons.math3.util.Precision.EPSILON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.hartwig.hmftools.common.purple.PurityAdjuster;
import com.hartwig.hmftools.common.purple.PurpleDatamodelTest;
import com.hartwig.hmftools.common.purple.copynumber.PurpleCopyNumber;
import com.hartwig.hmftools.common.purple.gender.Gender;
import com.hartwig.hmftools.common.region.GenomeRegionSelectorFactory;
import com.hartwig.hmftools.common.variant.structural.StructuralVariantLeg;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class StructuralVariantPloidyFactoryTest {

    private static final StructuralVariantLegPloidyFactory<PurpleCopyNumber> PURE_PLOIDY_FACTORY =
            new StructuralVariantLegPloidyFactory<>(PURE, PurpleCopyNumber::averageTumorCopyNumber);

    @Test
    public void testSingleValidLeg() {
        final StructuralVariantLeg start = createLeg(1001, 1, 0.25);

        final PurpleCopyNumber left = copyNumber(1, 1000, 4);
        final PurpleCopyNumber middle = copyNumber(1001, 2000, 3);
        final PurpleCopyNumber right = copyNumber(2001, 3000, 4);

        final StructuralVariantLegs legs = ImmutableStructuralVariantLegs.builder().start(start).end(Optional.empty()).build();
        final ListMultimap<String, PurpleCopyNumber> copyNumbers = copyNumbers(left, middle, right);
        final List<StructuralVariantLegPloidy> ploidies = PURE_PLOIDY_FACTORY.create(legs, copyNumbers);
        assertEquals(1, ploidies.size());
        for (StructuralVariantLegPloidy ploidy : ploidies) {
            assertEquals(1d, ploidy.averageImpliedPloidy(), EPSILON);
            assertEquals(1d, ploidy.weight(), EPSILON);
        }
    }

    @Test
    public void testBothLegsValid() {
        final StructuralVariantLeg start = createLeg(1001, 1, 0.25);
        final StructuralVariantLeg end = createLeg(2001, -1, 0.25);

        final PurpleCopyNumber left = copyNumber(1, 1000, 4);
        final PurpleCopyNumber middle = copyNumber(1001, 2000, 3);
        final PurpleCopyNumber right = copyNumber(2001, 3000, 4);

        final StructuralVariantLegs legs = ImmutableStructuralVariantLegs.builder().start(start).end(end).build();
        final ListMultimap<String, PurpleCopyNumber> copyNumbers = copyNumbers(left, middle, right);
        final List<StructuralVariantLegPloidy> ploidies = PURE_PLOIDY_FACTORY.create(legs, copyNumbers);
        assertEquals(2, ploidies.size());
        for (StructuralVariantLegPloidy ploidy : ploidies) {
            assertEquals(1d, ploidy.averageImpliedPloidy(), EPSILON);
            assertEquals(2d, ploidy.weight(), EPSILON);
        }
    }

    @Test
    public void testExcludeNegativeAndZeroCopyNumbers() {
        final StructuralVariantLeg positiveLeg = createLeg(1001, 1, 0.25);
        final StructuralVariantLeg negativeLeg = createLeg(2001, -1, 0.25);
        final PurpleCopyNumber left = copyNumber(1, 1000, -0.01);
        final PurpleCopyNumber right = copyNumber(2001, 3000, 0);

        assertFalse(PURE_PLOIDY_FACTORY.create(positiveLeg, GenomeRegionSelectorFactory.create(singleton(left))).isPresent());
        assertFalse(PURE_PLOIDY_FACTORY.create(negativeLeg, GenomeRegionSelectorFactory.create(singleton(right))).isPresent());
    }

    @Test
    public void testExcludeInfiniteVAF() {
        final StructuralVariantLeg leg = createLeg(1001, -1, 1);
        final PurpleCopyNumber left = copyNumber(1, 1000, 3);

        assertFalse(PURE_PLOIDY_FACTORY.create(leg, GenomeRegionSelectorFactory.create(singleton(left))).isPresent());
    }

    @Test
    public void testSelectCorrectOrAlternativeCopyNumberForLeg() {
        final StructuralVariantLeg positiveLeg = createLeg(1001, 1, 0.25);
        final StructuralVariantLeg negativeLeg = createLeg(2001, -1, 0.25);
        final PurpleCopyNumber left = copyNumber(1, 1000, 4);
        final PurpleCopyNumber middle = copyNumber(1001, 2000, 3);
        final PurpleCopyNumber right = copyNumber(2001, 3000, 4);

        assertPloidy(1, false, PURE_PLOIDY_FACTORY.create(positiveLeg, GenomeRegionSelectorFactory.create(singleton(left))));
        assertPloidy(1, true, PURE_PLOIDY_FACTORY.create(positiveLeg, GenomeRegionSelectorFactory.create(singleton(middle))));

        assertPloidy(1, true, PURE_PLOIDY_FACTORY.create(negativeLeg, GenomeRegionSelectorFactory.create(singleton(middle))));
        assertPloidy(1, false, PURE_PLOIDY_FACTORY.create(negativeLeg, GenomeRegionSelectorFactory.create(singleton(right))));
    }

    @Test
    public void testPurityAdjustedPloidy() {
        final StructuralVariantLeg leg = createLeg(1001, 1, 0.5);
        final List<PurpleCopyNumber> copyNumbers = Lists.newArrayList(copyNumber(1, 1000, 2), copyNumber(1001, 200, 1));

        Optional<ModifiableStructuralVariantLegPloidy> purePloidy =
                PURE_PLOIDY_FACTORY.create(leg, GenomeRegionSelectorFactory.create(copyNumbers));
        assertPloidy(1d, purePloidy);

        final PurityAdjuster diluted = new PurityAdjuster(Gender.FEMALE, 0.8, 1);
        final StructuralVariantLegPloidyFactory<PurpleCopyNumber> dilutedFactory =
                new StructuralVariantLegPloidyFactory<>(diluted, PurpleCopyNumber::averageTumorCopyNumber);
        Optional<ModifiableStructuralVariantLegPloidy> dilutedPloidy =
                dilutedFactory.create(leg, GenomeRegionSelectorFactory.create(copyNumbers));
        assertPloidy(1.25d, dilutedPloidy);

        final PurityAdjuster male = new PurityAdjuster(Gender.MALE, 0.8, 1);
        final StructuralVariantLegPloidyFactory<PurpleCopyNumber> maleFactory =
                new StructuralVariantLegPloidyFactory<>(male, PurpleCopyNumber::averageTumorCopyNumber);
        Optional<ModifiableStructuralVariantLegPloidy> malePloidy =
                maleFactory.create(leg, GenomeRegionSelectorFactory.create(copyNumbers));
        assertPloidy(1.125d, malePloidy);
    }

    private void assertPloidy(double expected, @NotNull final Optional<ModifiableStructuralVariantLegPloidy> ploidy) {
        assertEquals(expected, ploidy.map(ModifiableStructuralVariantLegPloidy::unweightedImpliedPloidy).orElse(0D), EPSILON);
    }

    private void assertPloidy(double expectedPloidy, boolean alternate,
            @NotNull final Optional<ModifiableStructuralVariantLegPloidy> ploidy) {
        assertEquals(expectedPloidy, ploidy.map(ModifiableStructuralVariantLegPloidy::unweightedImpliedPloidy).orElse(0D), EPSILON);
        if (alternate) {
            assertNotEquals(1d, ploidy.map(ModifiableStructuralVariantLegPloidy::weight).orElse(0D), EPSILON);
        } else {
            assertEquals(1d, ploidy.map(ModifiableStructuralVariantLegPloidy::weight).orElse(0D), EPSILON);
        }
    }

    @NotNull
    private static PurpleCopyNumber copyNumber(long start, long end, double copyNumber) {
        return PurpleDatamodelTest.createCopyNumber(CHROMOSOME, start, end, copyNumber).build();
    }

    @NotNull
    private static ListMultimap<String, PurpleCopyNumber> copyNumbers(@NotNull PurpleCopyNumber... copyNumbers) {
        final ListMultimap<String, PurpleCopyNumber> result = ArrayListMultimap.create();
        result.putAll(CHROMOSOME, Lists.newArrayList(copyNumbers));
        return result;
    }
}
