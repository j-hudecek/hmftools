package com.hartwig.hmftools.common.region;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.hartwig.hmftools.common.position.GenomePosition;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

public class GenomeRegionSelectorTest {

    private GenomeRegionSelector<GenomeRegion> standardSelector;
    private GenomeRegionSelector<GenomeRegion> chromosomeSelector;

    private final GenomeRegion region1 = GenomeRegionFactory.create("1", 1, 100);
    private final GenomeRegion region2 = GenomeRegionFactory.create("1", 101, 200);
    private final GenomeRegion region3 = GenomeRegionFactory.create("2", 301, 400);
    private final GenomeRegion region4 = GenomeRegionFactory.create("2", 501, 600);
    private final GenomeRegion region5 = GenomeRegionFactory.create("3", 601, 700);

    @Before
    public void setup() {
        List<GenomeRegion> regions = Lists.newArrayList(region1, region2, region3, region4, region5);
        standardSelector = GenomeRegionSelectorFactory.create(regions);
        chromosomeSelector = GenomeRegionSelectorFactory.create(Multimaps.index(regions, GenomeRegion::chromosome));
    }

    @Test
    public void testExpectedOperation() {
        assertSelection(region1, createPosition("1", 1));
        assertSelection(region1, createPosition("1", 10));
        assertSelection(region1, createPosition("1", 100));

        assertSelection(region2, createPosition("1", 101));
        assertSelection(region2, createPosition("1", 200));

        assertAbsence(createPosition("1", 201));
        assertAbsence(createPosition("1", 300));
        assertAbsence(createPosition("1", 350));
        assertAbsence(createPosition("2", 300));

        assertSelection(region3, createPosition("2", 301));
        assertSelection(region3, createPosition("2", 400));

        assertAbsence(createPosition("2", 401));
        assertAbsence(createPosition("2", 500));

        assertSelection(region4, createPosition("2", 501));
        assertSelection(region4, createPosition("2", 600));

        assertAbsence(createPosition("2", 601));
        assertAbsence(createPosition("3", 600));

        assertSelection(region5, createPosition("3", 601));
        assertSelection(region5, createPosition("3", 700));
    }

    @Test
    public void testChromosomeNotAvailable() {
        assertAbsence(createPosition("4", 601));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStandardBackwards() {
        assertEquals(Optional.empty(), standardSelector.select(createPosition("1", 1000)));
        assertEquals(Optional.empty(), standardSelector.select(createPosition("1", 999)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChromosomeBackwards() {
        assertEquals(Optional.empty(), chromosomeSelector.select(createPosition("1", 1000)));
        assertEquals(Optional.empty(), chromosomeSelector.select(createPosition("1", 999)));
    }

    private void assertSelection(@NotNull final GenomeRegion expected, @NotNull final GenomePosition position) {
        Optional<GenomeRegion> chromRegion = chromosomeSelector.select(position);
        assertTrue(chromRegion.isPresent());
        assertEquals(expected, chromRegion.get());

        Optional<GenomeRegion> standardRegion = standardSelector.select(position);
        assertTrue(standardRegion.isPresent());
        assertEquals(expected, standardRegion.get());
    }

    private void assertAbsence(final GenomePosition position) {
        assertEquals(Optional.empty(), chromosomeSelector.select(position));
        assertEquals(Optional.empty(), standardSelector.select(position));
    }

    private static GenomePosition createPosition(final String chromosome, final long position) {
        return new GenomePosition() {
            @NotNull
            @Override
            public String chromosome() {
                return chromosome;
            }

            @Override
            public long position() {
                return position;
            }
        };
    }
}
