package com.hartwig.hmftools.common.gene;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.InputStream;

import com.google.common.io.Resources;
import com.hartwig.hmftools.common.region.hmfslicer.HmfGenomeFileLoader;
import com.hartwig.hmftools.common.region.hmfslicer.HmfGenomeRegion;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CanonicalTranscriptFactoryTest {

    private static final String BASE_PATH = Resources.getResource("gene").getPath() + File.separator;

    @Test
    public void testKRAS() {
        final HmfGenomeRegion kras = select("KRAS.tsv");
        final CanonicalTranscript transcript  = CanonicalTranscriptFactory.create(kras);
        assertTranscript(transcript, 6,4, 1119, 189);
    }

    @Test
    public void testPTEN() {
        final HmfGenomeRegion region = select("PTEN.tsv");
        final CanonicalTranscript transcript  = CanonicalTranscriptFactory.create(region);
        assertTranscript(transcript, 9,9, 9027, 403);
    }

    @Test
    public void testWASH7P() {
        final HmfGenomeRegion region = select("WASH7P.tsv");
        final CanonicalTranscript transcript  = CanonicalTranscriptFactory.create(region);
        assertTranscript(transcript, 12,0, 1783, 0);
    }

    private void assertTranscript(@NotNull final CanonicalTranscript victim, int exons, int codingExons, long transcriptLength, int codons) {
        assertEquals(exons, victim.exons());
        assertEquals(codingExons, victim.codingExons());
        assertEquals(transcriptLength, victim.transcriptLength());
        assertEquals(codons, victim.codons());
    }


    private HmfGenomeRegion select(@NotNull final String file) {
        final InputStream inputStream = CanonicalTranscriptFactoryTest.class.getResourceAsStream("/gene/" + file);
        return HmfGenomeFileLoader.fromInputStream(inputStream).values().iterator().next();
    }

}