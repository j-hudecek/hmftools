package com.hartwig.hmftools.common.genepanel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.Resources;
import com.hartwig.hmftools.common.region.HmfTranscriptRegion;

import org.jetbrains.annotations.NotNull;

public final class HmfGenePanelSupplier {

    private HmfGenePanelSupplier() {
    }

    @NotNull
    public static List<HmfTranscriptRegion> hmfPanelGeneList() throws IOException {
        final Set<String> panel = hmfPanelGeneSet();
        return allGeneList().stream().filter(x -> panel.contains(x.gene())).collect(Collectors.toList());
    }

    @NotNull
    @VisibleForTesting
    static Set<String> hmfPanelGeneSet() throws IOException {
        return Sets.newHashSet(Resources.readLines(Resources.getResource("genepanel/gene_panel"), Charset.defaultCharset()));
    }

    @NotNull
    public static SortedSetMultimap<String, HmfTranscriptRegion> allGenesPerChromosomeMap() {
        return toSortedMap(allGeneList());
    }

    @NotNull
    public static List<HmfTranscriptRegion> allGeneList() {
        final InputStream inputStream = HmfGenePanelSupplier.class.getResourceAsStream("/genepanel/all_genes.tsv");
        return HmfGenomeFileLoader.fromInputStream(inputStream);
    }

    @NotNull
    public static Map<String, HmfTranscriptRegion> allGenesMap() {
        List<HmfTranscriptRegion> regions = allGeneList();
        Map<String, HmfTranscriptRegion> geneMap = Maps.newHashMap();
        for (HmfTranscriptRegion region : regions) {
            assert !geneMap.containsKey(region.gene());
            geneMap.put(region.gene(), region);
        }

        return geneMap;
    }

    @NotNull
    private static SortedSetMultimap<String, HmfTranscriptRegion> toSortedMap(@NotNull final List<HmfTranscriptRegion> regions) {
        final SortedSetMultimap<String, HmfTranscriptRegion> regionMap = TreeMultimap.create();
        for (HmfTranscriptRegion region : regions) {
            regionMap.put(region.chromosome(), region);
        }

        return regionMap;
    }
}