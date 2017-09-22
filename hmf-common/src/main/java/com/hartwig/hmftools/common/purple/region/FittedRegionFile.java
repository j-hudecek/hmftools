package com.hartwig.hmftools.common.purple.region;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.hartwig.hmftools.common.copynumber.freec.FreecStatus;
import com.hartwig.hmftools.common.purple.segment.StructuralVariantSupport;

import org.jetbrains.annotations.NotNull;

public enum FittedRegionFile {
    ;

    private static final String EXTENSION = ".purple.fitted";
    private static final String DELIMITER = "\t";
    static final String HEADER_PREFIX = "#";

    public static void writeCopyNumber(@NotNull final String basePath, @NotNull final String sample,
            @NotNull Collection<FittedRegion> copyNumbers) throws IOException {
        final String filePath = basePath + File.separator + sample + EXTENSION;
        final Collection<String> lines = Lists.newArrayList();
        lines.add(header());
        copyNumbers.stream().map(FittedRegionFile::toString).forEach(lines::add);

        Files.write(new File(filePath).toPath(), lines);
    }

    @NotNull
    private static String header() {
        return new StringJoiner(DELIMITER, HEADER_PREFIX, "").add("chromosome")
                .add("start")
                .add("end")
                .add("status")
                .add("fittedPloidy")
                .add("bafCount")
                .add("observedBAF")
                .add("modelBAF")
                .add("bafDeviation")
                .add("observedTumorRatio")
                .add("observedNormalRatio")
                .add("modelTumorRatio")
                .add("cnvDeviation")
                .add("deviation")
                .add("tumorCopyNumber")
                .add("broadTumorCopyNumber")
                .add("broadBAF")
                .add("segmentTumorCopyNumber")
                .add("segmentBAF")
                .add("refNormalisedCopyNumber")
                .add("ratioSupport")
                .add("structuralVariantSupport")
                .add("observedTumorRatioCount")
                .toString();
    }

    @NotNull
    static String toString(@NotNull final FittedRegion copyNumber) {
        return new StringJoiner(DELIMITER).add(String.valueOf(copyNumber.chromosome()))
                .add(String.valueOf(copyNumber.start()))
                .add(String.valueOf(copyNumber.end()))
                .add(String.valueOf(copyNumber.status()))
                .add(String.valueOf(copyNumber.modelPloidy()))
                .add(String.valueOf(copyNumber.bafCount()))
                .add(String.valueOf(copyNumber.observedBAF()))
                .add(String.valueOf(copyNumber.modelBAF()))
                .add(String.valueOf(copyNumber.bafDeviation()))
                .add(String.valueOf(copyNumber.observedTumorRatio()))
                .add(String.valueOf(copyNumber.observedNormalRatio()))
                .add(String.valueOf(copyNumber.modelTumorRatio()))
                .add(String.valueOf(copyNumber.cnvDeviation()))
                .add(String.valueOf(copyNumber.deviation()))
                .add(String.valueOf(copyNumber.tumorCopyNumber()))
                .add(String.valueOf(copyNumber.broadTumorCopyNumber()))
                .add(String.valueOf(copyNumber.broadBAF()))
                .add(String.valueOf(copyNumber.segmentTumorCopyNumber()))
                .add(String.valueOf(copyNumber.segmentBAF()))
                .add(String.valueOf(copyNumber.refNormalisedCopyNumber()))
                .add(String.valueOf(copyNumber.ratioSupport()))
                .add(String.valueOf(copyNumber.structuralVariantSupport()))
                .add(String.valueOf(copyNumber.observedTumorRatioCount()))
                .toString();
    }

    @NotNull
    static FittedRegion fromString(@NotNull final String purity) {
        String[] values = purity.split(DELIMITER);
        return ImmutableFittedRegion.builder()
                .chromosome(values[0])
                .start(Long.valueOf(values[1]))
                .end(Long.valueOf(values[2]))
                .status(FreecStatus.valueOf(values[3]))
                .modelPloidy(Integer.valueOf(values[4]))
                .bafCount(Integer.valueOf(values[5]))
                .observedBAF(Double.valueOf(values[6]))
                .modelBAF(Double.valueOf(values[7]))
                .bafDeviation(Double.valueOf(values[8]))
                .observedTumorRatio(Double.valueOf(values[9]))
                .observedNormalRatio(Double.valueOf(values[10]))
                .modelTumorRatio(Double.valueOf(values[11]))
                .cnvDeviation(Double.valueOf(values[12]))
                .deviation(Double.valueOf(values[13]))
                .tumorCopyNumber(Double.valueOf(values[14]))
                .broadTumorCopyNumber(Double.valueOf(values[15]))
                .broadBAF(Double.valueOf(values[16]))
                .segmentTumorCopyNumber(Double.valueOf(values[17]))
                .segmentBAF(Double.valueOf(values[18]))
                .refNormalisedCopyNumber(Double.valueOf(values[19]))
                .ratioSupport(Boolean.valueOf(values[20]))
                .structuralVariantSupport(StructuralVariantSupport.valueOf(values[21]))
                .observedTumorRatioCount(Integer.valueOf(values[22]))
                .build();
    }

}