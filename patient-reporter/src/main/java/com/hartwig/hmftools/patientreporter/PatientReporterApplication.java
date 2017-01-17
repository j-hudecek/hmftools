package com.hartwig.hmftools.patientreporter;

import static com.hartwig.hmftools.common.variant.predicate.VariantFilter.filter;
import static com.hartwig.hmftools.common.variant.predicate.VariantFilter.passOnly;
import static com.hartwig.hmftools.common.variant.predicate.VariantPredicates.isMissense;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.hartwig.hmftools.common.copynumber.CopyNumber;
import com.hartwig.hmftools.common.copynumber.cnv.CNVFileLoader;
import com.hartwig.hmftools.common.exception.EmptyFileException;
import com.hartwig.hmftools.common.exception.HartwigException;
import com.hartwig.hmftools.common.variant.SomaticVariant;
import com.hartwig.hmftools.common.variant.VariantConsequence;
import com.hartwig.hmftools.common.variant.vcf.VCFFileLoader;
import com.hartwig.hmftools.common.variant.vcf.VCFFileWriter;
import com.hartwig.hmftools.common.variant.vcf.VCFSomaticFile;
import com.hartwig.hmftools.patientreporter.copynumber.CopyNumberAnalyser;
import com.hartwig.hmftools.patientreporter.copynumber.CopyNumberStats;
import com.hartwig.hmftools.patientreporter.slicing.GenomeRegion;
import com.hartwig.hmftools.patientreporter.slicing.Slicer;
import com.hartwig.hmftools.patientreporter.slicing.SlicerFactory;
import com.hartwig.hmftools.patientreporter.util.ConsequenceCount;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatientReporterApplication {

    private static final Logger LOGGER = LogManager.getLogger(PatientReporterApplication.class);
    private static final String SOMATIC_EXTENSION = "_melted.vcf";
    private static final String COPYNUMBER_DIRECTORY = "copyNumber";
    private static final String COPYNUMBER_EXTENSION = ".bam_CNVs";
    private static final String FREEC_DIRECTORY = "freec";

    private static final String RUN_DIRECTORY_ARGS_DESC = "A path towards a single rundir.";
    private static final String RUN_DIRECTORY = "rundir";

    private static final String CPCT_SLICING_BED_ARGS_DESC = "A path towards the CPCT slicing bed.";
    private static final String CPCT_SLICING_BED = "cpct_slicing_bed";

    private static final String HIGH_CONFIDENCE_BED_ARGS_DESC = "A path towards the high confidence bed.";
    private static final String HIGH_CONFIDENCE_BED = "high_confidence_bed";

    private static final String HMF_SLICING_BED_ARGS_DESC = "A path towards the HMF slicing bed.";
    private static final String HMF_SLICING_BED = "hmf_slicing_bed";

    private static final String OUTPUT_DIR_ARGS_DESC = "A path where, if provided, output files will be written to.";
    private static final String OUTPUT_DIR = "output_dir";

    private static final String BATCH_MODE_ARGS_DESC = "If set, runs in batch mode (Caution!!! Korneel Only)";
    private static final String BATCH_MODE = "batch_mode";

    @NotNull
    private final String runDirectory;
    @NotNull
    private final ConsensusRule consensusRule;
    @NotNull
    private final Slicer hmfSlicer;
    @Nullable
    private final String outputDirectory;
    private final boolean batchMode;

    public static void main(final String... args) throws ParseException, IOException, HartwigException {
        final Options options = createOptions();
        final CommandLine cmd = createCommandLine(options, args);

        final String runDir = cmd.getOptionValue(RUN_DIRECTORY);
        final String cpctSlicingBed = cmd.getOptionValue(CPCT_SLICING_BED);
        final String highConfidenceBed = cmd.getOptionValue(HIGH_CONFIDENCE_BED);
        final String hmfSlicingBed = cmd.getOptionValue(HMF_SLICING_BED);
        final String outputDirectory = cmd.getOptionValue(OUTPUT_DIR);
        final boolean batchMode = cmd.hasOption(BATCH_MODE);

        if (runDir == null || cpctSlicingBed == null || highConfidenceBed == null || hmfSlicingBed == null) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Patient-Reporter", options);
            System.exit(1);
        }

        if (outputDirectory != null) {
            final Path outputPath = new File(outputDirectory).toPath();
            if (!Files.exists(outputPath) || !Files.isDirectory(outputPath)) {
                LOGGER.warn(OUTPUT_DIR + " has to be an existing directory!");
                System.exit(1);
            }
        }

        final ConsensusRule consensusRule = new ConsensusRule(SlicerFactory.fromBedFile(highConfidenceBed),
                SlicerFactory.fromBedFile(cpctSlicingBed));
        new PatientReporterApplication(runDir, consensusRule, SlicerFactory.fromBedFile(hmfSlicingBed),
                outputDirectory, batchMode).run();
    }

    @NotNull
    private static Options createOptions() {
        final Options options = new Options();

        options.addOption(RUN_DIRECTORY, true, RUN_DIRECTORY_ARGS_DESC);
        options.addOption(CPCT_SLICING_BED, true, CPCT_SLICING_BED_ARGS_DESC);
        options.addOption(HIGH_CONFIDENCE_BED, true, HIGH_CONFIDENCE_BED_ARGS_DESC);
        options.addOption(HMF_SLICING_BED, true, HMF_SLICING_BED_ARGS_DESC);
        options.addOption(OUTPUT_DIR, true, OUTPUT_DIR_ARGS_DESC);
        options.addOption(BATCH_MODE, false, BATCH_MODE_ARGS_DESC);

        return options;
    }

    @NotNull
    private static CommandLine createCommandLine(@NotNull final Options options, @NotNull final String... args)
            throws ParseException {
        final CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    PatientReporterApplication(@NotNull final String runDirectory, @NotNull final ConsensusRule consensusRule,
            @NotNull final Slicer hmfSlicer, @Nullable final String outputDirectory, final boolean batchMode) {
        this.runDirectory = runDirectory;
        this.consensusRule = consensusRule;
        this.hmfSlicer = hmfSlicer;
        this.outputDirectory = outputDirectory;
        this.batchMode = batchMode;
    }

    void run() throws IOException, HartwigException {
        final ConsequenceRule consequenceRule = new ConsequenceRule(hmfSlicer);
        if (batchMode) {
            batchRun(consequenceRule);
        } else {
            patientRun(consequenceRule);
        }
    }

    private void batchRun(@NotNull final ConsequenceRule consequenceRule) throws IOException, HartwigException {
        // KODU: We assume "run directory" is a path with a lot of directories on which we can all run in patient mode.
        VariantConsequence[] consequences = VariantConsequence.values();

        String header = "SAMPLE,VARIANT_COUNT,PASS_ONLY_COUNT,CONSENSUS_COUNT,MISSENSE_COUNT,CONSEQUENCE_COUNT";
        for (final VariantConsequence consequence : consequences) {
            header += ("," + consequence.name() + "_COUNT");
        }
        System.out.println(header);

        for (final Path run : Files.list(new File(runDirectory).toPath()).collect(Collectors.toList())) {
            final VCFSomaticFile variantFile = VCFFileLoader.loadSomaticVCF(run.toFile().getPath(), SOMATIC_EXTENSION);

            final SomaticVariantReport report = SomaticVariantReport.fromVCFFile(variantFile, consensusRule,
                    consequenceRule);
            final Map<VariantConsequence, Integer> counts = ConsequenceCount.count(report.consensusPassedVariants);
            String consequenceList = Strings.EMPTY;
            for (final VariantConsequence consequence : consequences) {
                consequenceList += ("," + counts.get(consequence).toString());
            }

            final String out =
                    variantFile.sample() + "," + report.allVariants.size() + "," + report.allPassedVariants.size()
                            + "," + report.consensusPassedVariants.size() + "," + report.missenseVariants.size() + ","
                            + report.consequencePassedVariants.size() + consequenceList;
            System.out.println(out);
        }
    }

    private void patientRun(@NotNull final ConsequenceRule consequenceRule) throws IOException, HartwigException {
        LOGGER.info("Running patient reporter on " + runDirectory);
        final String sample = analyzeSomaticVariants(consequenceRule);
        analyzeCopyNumbers(sample);
    }

    @NotNull
    private String analyzeSomaticVariants(@NotNull final ConsequenceRule consequenceRule)
            throws IOException, HartwigException {
        final VCFSomaticFile variantFile = VCFFileLoader.loadSomaticVCF(runDirectory, SOMATIC_EXTENSION);
        LOGGER.info("  Extracted variants for sample " + variantFile.sample());

        final SomaticVariantReport report = SomaticVariantReport.fromVCFFile(variantFile, consensusRule,
                consequenceRule);
        LOGGER.info("  Total number of variants : " + report.allVariants.size());
        LOGGER.info("  Number of variants after applying pass-only filter : " + report.allPassedVariants.size());
        LOGGER.info("  Number of variants after applying consensus rule : " + report.consensusPassedVariants.size());
        LOGGER.info("  Number of missense variants in consensus rule (mutational load) : "
                + report.missenseVariants.size());
        LOGGER.info("  Number of consequential variants to report : " + report.consequencePassedVariants.size());

        if (outputDirectory != null) {
            final String consensusVCF =
                    outputDirectory + File.separator + variantFile.sample() + "_consensus_variants.vcf";
            VCFFileWriter.writeSomaticVCF(consensusVCF, report.consensusPassedVariants);
            LOGGER.info("    Written consensus-passed variants to " + consensusVCF);

            final String consequenceVCF =
                    outputDirectory + File.separator + variantFile.sample() + "_consequential_variants.vcf";
            VCFFileWriter.writeSomaticVCF(consequenceVCF, report.consequencePassedVariants);
            LOGGER.info("    Written consequential variants to " + consequenceVCF);
        }

        return variantFile.sample();
    }

    private void analyzeCopyNumbers(@NotNull final String sample) throws IOException, HartwigException {
        final String cnvBasePath = guessCNVBasePath(sample) + File.separator + FREEC_DIRECTORY;
        List<CopyNumber> copyNumbers;

        try {
            copyNumbers = CNVFileLoader.loadCNV(cnvBasePath, sample, COPYNUMBER_EXTENSION);
        } catch (EmptyFileException e) {
            // KODU: It could be that the sample simply does not have any amplifications...
            copyNumbers = Lists.newArrayList();
        }

        final Map<GenomeRegion, CopyNumberStats> stats = CopyNumberAnalyser.run(hmfSlicer.regions(), copyNumbers);
        LOGGER.info("  Determined copy number stats for " + stats.size() + " genomic regions");

        if (outputDirectory != null) {
            final List<String> lines = Lists.newArrayList();
            lines.add("GENE,CNV_MIN,CNV_MEAN,CNV_MAX");
            for (final Map.Entry<GenomeRegion, CopyNumberStats> entry : stats.entrySet()) {
                final CopyNumberStats stat = entry.getValue();
                if (stat.min() != 2 || stat.max() != 2) {
                    lines.add(entry.getKey().annotation() + "," + stat.min() + "," + stat.mean() + "," + stat.max());
                }
            }
            final String filePath = outputDirectory + File.separator + sample + "_CNV.csv";
            Files.write(new File(filePath).toPath(), lines);
            LOGGER.info("    Written all non-default CNV stats to " + filePath);
        }
    }

    @NotNull
    private String guessCNVBasePath(@NotNull final String sample) throws IOException {
        final String basePath = runDirectory + File.separator + COPYNUMBER_DIRECTORY;

        for (Path path : Files.list(new File(basePath).toPath()).collect(Collectors.toList())) {
            if (path.toFile().isDirectory() && path.getFileName().toFile().getName().contains(sample)) {
                return path.toString();
            }
        }

        throw new FileNotFoundException(
                "Could not determine CNV location in " + runDirectory + " using sample " + sample);
    }

    private static class SomaticVariantReport {
        @NotNull
        private final List<SomaticVariant> allVariants;
        @NotNull
        private final List<SomaticVariant> allPassedVariants;
        @NotNull
        private final List<SomaticVariant> consensusPassedVariants;
        @NotNull
        private final List<SomaticVariant> missenseVariants;
        @NotNull
        private final List<SomaticVariant> consequencePassedVariants;

        @NotNull
        static SomaticVariantReport fromVCFFile(@NotNull final VCFSomaticFile variantFile,
                @NotNull final ConsensusRule consensusRule, @NotNull final ConsequenceRule consequenceRule) {
            final List<SomaticVariant> allVariants = variantFile.variants();
            final List<SomaticVariant> allPassedVariants = passOnly(allVariants);
            final List<SomaticVariant> consensusPassedVariants = consensusRule.apply(allPassedVariants);
            final List<SomaticVariant> missenseVariants = filter(consensusPassedVariants, isMissense());
            final List<SomaticVariant> consequencePassedVariants = consequenceRule.apply(consensusPassedVariants);
            return new SomaticVariantReport(allVariants, allPassedVariants, consensusPassedVariants, missenseVariants,
                    consequencePassedVariants);
        }

        private SomaticVariantReport(@NotNull final List<SomaticVariant> allVariants,
                @NotNull final List<SomaticVariant> allPassedVariants,
                @NotNull final List<SomaticVariant> consensusPassedVariants,
                @NotNull final List<SomaticVariant> missenseVariants,
                @NotNull final List<SomaticVariant> consequencePassedVariants) {
            this.allVariants = allVariants;
            this.allPassedVariants = allPassedVariants;
            this.consensusPassedVariants = consensusPassedVariants;
            this.missenseVariants = missenseVariants;
            this.consequencePassedVariants = consequencePassedVariants;
        }
    }
}
