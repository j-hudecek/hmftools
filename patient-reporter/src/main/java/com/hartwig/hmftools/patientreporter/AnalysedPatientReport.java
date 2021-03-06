package com.hartwig.hmftools.patientreporter;

import java.util.List;
import java.util.Optional;

import com.hartwig.hmftools.common.purple.gene.GeneCopyNumber;
import com.hartwig.hmftools.common.purple.purity.FittedPurityStatus;
import com.hartwig.hmftools.common.variant.EnrichedSomaticVariant;
import com.hartwig.hmftools.patientreporter.algo.DriverProbabilityModel;
import com.hartwig.hmftools.svannotation.annotations.GeneDisruption;
import com.hartwig.hmftools.svannotation.annotations.GeneFusion;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(allParameters = true,
             passAnnotations = { NotNull.class, Nullable.class })
public abstract class AnalysedPatientReport implements PatientReport {
    @NotNull
    @Override
    public abstract SampleReport sampleReport();

    @NotNull
    public abstract FittedPurityStatus fitStatus();

    public abstract double impliedPurity();

    @NotNull
    public abstract DriverProbabilityModel driverProbabilityModel();

    @NotNull
    public abstract List<EnrichedSomaticVariant> somaticVariants();

    public abstract double microsatelliteIndelsPerMb();

    public abstract int tumorMutationalLoad();

    public abstract double tumorMutationalBurden();

    @NotNull
    public abstract List<GeneCopyNumber> geneCopyNumbers();

    @NotNull
    public abstract List<GeneFusion> geneFusions();

    @NotNull
    public abstract List<GeneDisruption> geneDisruptions();

    @NotNull
    public abstract String circosPath();

    @Override
    @NotNull
    public abstract Optional<String> comments();

    @NotNull
    @Override
    public abstract String signaturePath();
}
