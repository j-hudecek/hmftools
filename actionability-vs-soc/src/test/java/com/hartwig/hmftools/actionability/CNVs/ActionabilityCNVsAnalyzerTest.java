package com.hartwig.hmftools.actionability.CNVs;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.hartwig.hmftools.actionability.cancerTypeMapping.CancerTypeAnalyzer;
import com.hartwig.hmftools.actionability.cancerTypeMapping.CancerTypeReading;
import com.hartwig.hmftools.actionability.cancerTypeMapping.ImmutableCancerTypeReading;
import com.hartwig.hmftools.common.purple.copynumber.CopyNumberMethod;
import com.hartwig.hmftools.common.purple.gene.GeneCopyNumber;
import com.hartwig.hmftools.common.purple.gene.ImmutableGeneCopyNumber;
import com.hartwig.hmftools.common.purple.segment.SegmentSupport;

import org.junit.Ignore;
import org.junit.Test;

public class ActionabilityCNVsAnalyzerTest {


    @Ignore
    public void ActionabilityWorksCNVs() {
        ActionabilityCNVs actionanilityCNV = ImmutableActionabilityCNVs.builder()
                .gene("ERBB2")
                .cnvType("Amplification")
                .source("oncoKb")
                .reference("ERBB2:amp")
                .drugsName("Trastuzumab")
                .drugsType("EGFR inhibitor")
                .cancerType("Breast Cancer")
                .source("1")
                .hmfLevel("A")
                .hmfResponse("Responsive")
                .build();

        CancerTypeReading reading = ImmutableCancerTypeReading.builder()
                .doidSet("1612")
                .cancerType("Breast")
                .build();

        ActionabilityCNVsAnalyzer cnvAnalyzer = new ActionabilityCNVsAnalyzer(Lists.newArrayList(actionanilityCNV));
        CancerTypeAnalyzer cancerType = new CancerTypeAnalyzer(Lists.newArrayList(reading));

        GeneCopyNumber geneCopyNumber = ImmutableGeneCopyNumber.builder()
                .gene("ERBB2")
                .maxCopyNumber(45)
                .minCopyNumber(30.001)
                .somaticRegions(1)
                .germlineHet2HomRegions(0)
                .germlineHomRegions(0)
                .minRegions(1)
                .minRegionStart(1)
                .minRegionEnd(10)
                .minRegionStartSupport(SegmentSupport.NONE)
                .minRegionEndSupport(SegmentSupport.NONE)
                .minRegionMethod(CopyNumberMethod.UNKNOWN)
                .nonsenseBiallelicCount(0)
                .nonsenseNonBiallelicCount(0)
                .nonsenseNonBiallelicPloidy(0)
                .spliceBiallelicCount(0)
                .spliceNonBiallelicCount(0)
                .missenseNonBiallelicPloidy(0)
                .minMinorAllelePloidy(0)
                .transcriptID("trans")
                .transcriptVersion(1)
                .chromosomeBand("12.1")
                .chromosome("1")
                .start(1)
                .end(2)
                .spliceNonBiallelicPloidy(0)
                .missenseBiallelicCount(0)
                .missenseNonBiallelicCount(0)
                .build();

     //   assertEquals(true, cnvAnalyzer.actionableCNVs(geneCopyNumber, cancerType, "1612", "Breast"));
     //   assertEquals(false, cnvAnalyzer.actionableCNVs(geneCopyNumber, cancerType, "1612", "Skin"));

    }
}