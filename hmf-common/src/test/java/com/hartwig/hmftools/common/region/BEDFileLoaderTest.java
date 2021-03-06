package com.hartwig.hmftools.common.region;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.io.Resources;

import org.junit.Test;

public class BEDFileLoaderTest {

    private static final String BED_FILE_BASE_PATH = Resources.getResource("bed").getPath();

    private static final String VALID_BED = "valid.bed";

    @Test
    public void verifyStartIsOneBased() throws IOException {
        final String bedFile = BED_FILE_BASE_PATH + File.separator + VALID_BED;
        final SortedSetMultimap<String, GenomeRegion> regions = BEDFileLoader.fromBedFile(bedFile);
        assertEquals(1, regions.get("1").first().start());
        assertEquals(1, regions.get("1").first().end());
    }
}
