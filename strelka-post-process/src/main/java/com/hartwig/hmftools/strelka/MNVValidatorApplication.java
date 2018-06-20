package com.hartwig.hmftools.strelka;

import static com.hartwig.hmftools.strelka.StrelkaPostProcessApplication.generateOutputHeader;

import java.io.File;
import java.util.Optional;

import com.hartwig.hmftools.strelka.mnv.BamMNVValidator;
import com.hartwig.hmftools.strelka.mnv.ImmutableBamMNVValidator;
import com.hartwig.hmftools.strelka.mnv.ImmutableMNVMerger;
import com.hartwig.hmftools.strelka.mnv.MNVDetector;
import com.hartwig.hmftools.strelka.mnv.MNVMerger;
import com.hartwig.hmftools.strelka.mnv.PotentialMNVRegion;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

public class MNVValidatorApplication {
    private static final Logger LOGGER = LogManager.getLogger(MNVValidatorApplication.class);

    private static final String INPUT_VCF = "v";
    private static final String TUMOR_BAM = "b";
    private static final String OUTPUT_VCF = "o";

    public static void main(final String... args) throws ParseException {
        final Options options = createOptions();
        final CommandLine cmd = createCommandLine(options, args);

        final String inputVcf = cmd.getOptionValue(INPUT_VCF);
        final String tumorBam = cmd.getOptionValue(TUMOR_BAM);
        final String outputVcf = cmd.getOptionValue(OUTPUT_VCF);

        if (inputVcf == null || tumorBam == null || outputVcf == null) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("MNV Validator", options);
            System.exit(1);
        }
        LOGGER.info("Validating mnvs in {} using bam {}.", inputVcf, tumorBam);
        processVariants(inputVcf, outputVcf, tumorBam);
    }

    @NotNull
    private static Options createOptions() {
        final Options options = new Options();
        options.addOption(INPUT_VCF, true, "Path towards the input VCF");
        options.addOption(TUMOR_BAM, true, "Path towards the tumor BAM");
        options.addOption(OUTPUT_VCF, true, "Path towards the output VCF");
        return options;
    }

    @NotNull
    private static CommandLine createCommandLine(@NotNull final Options options, @NotNull final String... args) throws ParseException {
        final CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    private static void processVariants(@NotNull final String filePath, @NotNull final String outputVcf, @NotNull final String tumorBam) {
        final VCFFileReader vcfReader = new VCFFileReader(new File(filePath), false);
        final VCFHeader outputHeader = generateOutputHeader(vcfReader.getFileHeader(), "TUMOR");
        final VariantContextWriter vcfWriter = new VariantContextWriterBuilder().setOutputFile(outputVcf)
                .setReferenceDictionary(vcfReader.getFileHeader().getSequenceDictionary())
                .build();
        vcfWriter.writeHeader(vcfReader.getFileHeader());
        final BamMNVValidator validator = ImmutableBamMNVValidator.of(tumorBam);
        final MNVMerger merger = ImmutableMNVMerger.of(outputHeader);
        Pair<PotentialMNVRegion, Optional<PotentialMNVRegion>> outputPair = ImmutablePair.of(PotentialMNVRegion.empty(), Optional.empty());
        for (final VariantContext variant : vcfReader) {
            final PotentialMNVRegion potentialMNV = outputPair.getLeft();
            outputPair = MNVDetector.fitsMNVRegion(potentialMNV, variant);
            outputPair.getRight().ifPresent(mnvRegion -> validator.mergeVariants(mnvRegion, merger).forEach(vcfWriter::add));
        }
        validator.mergeVariants(outputPair.getLeft(), merger).forEach(vcfWriter::add);
        vcfWriter.close();
        vcfReader.close();
        LOGGER.info("Written output variants to " + outputVcf);
    }
}
