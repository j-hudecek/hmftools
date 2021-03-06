package com.hartwig.hmftools.patientreporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.hartwig.hmftools.common.fusions.KnownFusionsModel;
import com.hartwig.hmftools.common.region.BEDFileLoader;
import com.hartwig.hmftools.common.variant.enrich.HotspotEnrichment;
import com.hartwig.hmftools.patientreporter.algo.DrupActionabilityModel;
import com.hartwig.hmftools.patientreporter.algo.GeneModel;
import com.hartwig.hmftools.patientreporter.algo.GeneModelFactory;

import org.jetbrains.annotations.NotNull;

import htsjdk.samtools.reference.IndexedFastaSequenceFile;

final class SequencedReportDataLoader {

    private SequencedReportDataLoader() {
    }

    @NotNull
    static SequencedReportData buildFromFiles(@NotNull String fusionPairsLocation, @NotNull String promiscuousFiveLocation,
            @NotNull String promiscuousThreeLocation, @NotNull String drupGeneCsv, @NotNull String hotspotTsv,
            @NotNull String fastaFileLocation, @NotNull String highConfidenceBed) throws IOException {
        final DrupActionabilityModel drupActionabilityModel = new DrupActionabilityModel(drupGeneCsv);
        final GeneModel panelGeneModel = GeneModelFactory.create(drupActionabilityModel);

        final KnownFusionsModel knownFusionsModel = KnownFusionsModel.fromInputStreams(new FileInputStream(fusionPairsLocation),
                new FileInputStream(promiscuousFiveLocation),
                new FileInputStream(promiscuousThreeLocation));

        return ImmutableSequencedReportData.of(panelGeneModel,
                HotspotEnrichment.fromHotspotsFile(hotspotTsv),
                knownFusionsModel,
                new IndexedFastaSequenceFile(new File(fastaFileLocation)),
                BEDFileLoader.fromBedFile(highConfidenceBed));
    }
}
