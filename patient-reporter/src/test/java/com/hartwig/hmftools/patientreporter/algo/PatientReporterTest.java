package com.hartwig.hmftools.patientreporter.algo;

import static com.hartwig.hmftools.patientreporter.PatientReporterTestUtil.testBaseReportData;
import static com.hartwig.hmftools.patientreporter.PatientReporterTestUtil.testKnownFusionModel;
import static com.hartwig.hmftools.patientreporter.PatientReporterTestUtil.testSequencedReportData;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hartwig.hmftools.common.variant.structural.EnrichedStructuralVariant;
import com.hartwig.hmftools.patientreporter.BaseReportData;
import com.hartwig.hmftools.patientreporter.SequencedReportData;
import com.hartwig.hmftools.svannotation.VariantAnnotator;
import com.hartwig.hmftools.svannotation.analysis.StructuralVariantAnalyzer;
import com.hartwig.hmftools.svannotation.annotations.GeneAnnotation;
import com.hartwig.hmftools.svannotation.annotations.StructuralVariantAnnotation;
import com.hartwig.hmftools.svannotation.annotations.Transcript;

import org.jetbrains.annotations.NotNull;
import org.jooq.types.UInteger;
import org.junit.Test;

public class PatientReporterTest {

    private static final String RUN_DIRECTORY = Resources.getResource("example").getPath();

    @Test
    public void canRunOnRunDirectory() throws IOException {
        final BaseReportData baseReportData = testBaseReportData();
        final SequencedReportData reporterData = testSequencedReportData();
        final StructuralVariantAnalyzer svAnalyzer = new StructuralVariantAnalyzer(new TestAnnotator(),
                reporterData.panelGeneModel().disruptionGeneIDPanel(),
                testKnownFusionModel());
        final PatientReporter algo = ImmutablePatientReporter.of(baseReportData, reporterData, svAnalyzer);
        assertNotNull(algo.run(RUN_DIRECTORY, null));
    }

    private static class TestAnnotator implements VariantAnnotator {
        @Override
        @NotNull
        public List<StructuralVariantAnnotation> annotateVariants(@NotNull List<EnrichedStructuralVariant> variants) {
            final List<StructuralVariantAnnotation> result = Lists.newArrayList();
            for (final EnrichedStructuralVariant sv : variants) {
                final StructuralVariantAnnotation ann = new StructuralVariantAnnotation(sv);
                final GeneAnnotation g1 = new GeneAnnotation(sv,
                        true,
                        "PNPLA7",
                        "ENSG00000130653",
                        -1,
                        Collections.singletonList("PNPLA7"),
                        Lists.newArrayList(375775),
                        "q13");
                g1.addTranscript(new Transcript(g1,
                        "ENST00000406427",
                        12,
                        0,
                        13,
                        0,
                        37,
                        true,
                        UInteger.valueOf(100),
                        UInteger.valueOf(200)));
                ann.annotations().add(g1);

                final GeneAnnotation g2 = new GeneAnnotation(sv,
                        false,
                        "TMPRSS2",
                        "ENSG00000184012",
                        -1,
                        Collections.singletonList("TMPRSS2"),
                        Lists.newArrayList(7113),
                        "q14");
                g2.addTranscript(new Transcript(g2, "ENST00000398585", 1, 0, 2, 0, 14, true, UInteger.valueOf(300), UInteger.valueOf(400)));
                ann.annotations().add(g2);

                result.add(ann);
            }
            return result;
        }
    }
}
