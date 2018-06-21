package com.hartwig.hmftools.strelka;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.google.common.collect.Sets;
import com.hartwig.hmftools.common.slicing.Slicer;
import com.hartwig.hmftools.common.slicing.SlicerFactory;
import com.hartwig.hmftools.strelka.mnv.ImmutableMNVMerger;
import com.hartwig.hmftools.strelka.mnv.ImmutableVcfMNVValidator;
import com.hartwig.hmftools.strelka.mnv.MNVDetector;
import com.hartwig.hmftools.strelka.mnv.MNVMerger;
import com.hartwig.hmftools.strelka.mnv.PotentialMNVRegion;
import com.hartwig.hmftools.strelka.mnv.VcfMNVValidator;

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
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

public class PatchedStrelkaPostProcessApplication {
    private static final Logger LOGGER = LogManager.getLogger(PatchedStrelkaPostProcessApplication.class);

    private static final String HIGH_CONFIDENCE_BED = "hc_bed";
    private static final String INPUT_VCF = "v";
    private static final String OUTPUT_VCF = "o";
    private static final String SAMPLE_NAME = "t";
    private static final String CORRECTED_VCF = "c";
    private static final String REANNOTATED_MNVS_VCF = "r";

    public static void main(final String... args) throws ParseException, IOException {
        final Options options = createOptions();
        final CommandLine cmd = createCommandLine(options, args);

        final String highConfidenceBed = cmd.getOptionValue(HIGH_CONFIDENCE_BED);
        final String inputVcf = cmd.getOptionValue(INPUT_VCF);
        final String outputVcf = cmd.getOptionValue(OUTPUT_VCF);
        final String sampleName = cmd.getOptionValue(SAMPLE_NAME);
        final String correctedVcf = cmd.getOptionValue(CORRECTED_VCF);
        final String reAnnotatedMnvsVcf = cmd.getOptionValue(REANNOTATED_MNVS_VCF);

        if (highConfidenceBed == null || inputVcf == null || outputVcf == null || sampleName == null || correctedVcf == null
                || reAnnotatedMnvsVcf == null) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Strelka Post Process", options);
            System.exit(1);
        }
        final Slicer highConfidenceSlicer = SlicerFactory.fromBedFile(highConfidenceBed);
        LOGGER.info("Starting strelka post process patch on " + inputVcf);
        processVariants(inputVcf, highConfidenceSlicer, outputVcf, sampleName, correctedVcf, reAnnotatedMnvsVcf);
    }

    @NotNull
    private static Options createOptions() {
        final Options options = new Options();
        options.addOption(HIGH_CONFIDENCE_BED, true, "Path towards the high confidence bed");
        options.addOption(INPUT_VCF, true, "Path towards the input VCF");
        options.addOption(OUTPUT_VCF, true, "Path towards the output VCF");
        options.addOption(SAMPLE_NAME, true, "Name of the sample");
        options.addOption(CORRECTED_VCF, true, "Path towards the previously mnv-corrected VCF");
        options.addOption(REANNOTATED_MNVS_VCF, true, "Path towards the re-annotated mnv output VCF");
        return options;
    }

    @NotNull
    private static CommandLine createCommandLine(@NotNull final Options options, @NotNull final String... args) throws ParseException {
        final CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    private static void processVariants(@NotNull final String filePath, @NotNull final Slicer highConfidenceSlicer,
            @NotNull final String outputVcf, @NotNull final String sampleName, @NotNull final String correctedVcf,
            @NotNull final String reAnnotatedMnvsVcf) {
        final VCFFileReader vcfReader = new VCFFileReader(new File(filePath), false);
        final VCFFileReader correctedVcfReader = new VCFFileReader(new File(correctedVcf), false);

        final VCFHeader outputHeader = generateOutputHeader(vcfReader.getFileHeader(), sampleName);

        final VariantContextWriter writer = new VariantContextWriterBuilder().setOutputFile(outputVcf)
                .setReferenceDictionary(outputHeader.getSequenceDictionary())
                .build();
        writer.writeHeader(outputHeader);
        final VariantContextWriter reAnnotatedMnvWriter = new VariantContextWriterBuilder().setOutputFile(reAnnotatedMnvsVcf)
                .setReferenceDictionary(outputHeader.getSequenceDictionary())
                .build();
        reAnnotatedMnvWriter.writeHeader(correctedVcfReader.getFileHeader());
        final VcfMNVValidator validator = ImmutableVcfMNVValidator.of(correctedVcf);
        final MNVMerger merger = ImmutableMNVMerger.of(outputHeader);

        Pair<PotentialMNVRegion, Optional<PotentialMNVRegion>> outputPair = ImmutablePair.of(PotentialMNVRegion.empty(), Optional.empty());

        final VariantContextFilter filter = new StrelkaPostProcess(highConfidenceSlicer);
        for (final VariantContext variantContext : vcfReader) {
            if (filter.test(variantContext)) {
                final VariantContext simplifiedVariant = StrelkaPostProcess.simplifyVariant(variantContext, sampleName);
                final PotentialMNVRegion potentialMNV = outputPair.getLeft();
                outputPair = MNVDetector.fitsMNVRegion(potentialMNV, simplifiedVariant);
                outputPair.getRight().ifPresent(mnvRegion -> {
                    validator.mergeVariants(mnvRegion, merger).forEach(writer::add);
                    validator.correctedVariants(mnvRegion, merger).forEach(reAnnotatedMnvWriter::add);
                });
            }
        }
        validator.mergeVariants(outputPair.getLeft(), merger).forEach(writer::add);
        validator.correctedVariants(outputPair.getLeft(), merger).forEach(reAnnotatedMnvWriter::add);
        reAnnotatedMnvWriter.close();
        writer.close();
        vcfReader.close();
        correctedVcfReader.close();
        LOGGER.info("Written output variants to " + outputVcf);
    }

    @NotNull
    public static VCFHeader generateOutputHeader(@NotNull final VCFHeader header, @NotNull final String sampleName) {
        final VCFHeader outputVCFHeader = new VCFHeader(header.getMetaDataInInputOrder(), Sets.newHashSet(sampleName));
        outputVCFHeader.addMetaDataLine(new VCFFormatHeaderLine(VCFConstants.GENOTYPE_ALLELE_DEPTHS,
                VCFHeaderLineCount.R,
                VCFHeaderLineType.Integer,
                "Allelic depths for the ref and alt alleles in the order listed"));
        outputVCFHeader.addMetaDataLine(new VCFFormatHeaderLine(VCFConstants.GENOTYPE_KEY, 1, VCFHeaderLineType.String, "Genotype"));
        outputVCFHeader.addMetaDataLine(new VCFHeaderLine("StrelkaGATKCompatibility",
                "Added GT fields to strelka calls for gatk compatibility."));
        outputVCFHeader.addMetaDataLine(new VCFInfoHeaderLine("MAPPABILITY", 1, VCFHeaderLineType.Float, "Mappability (percentage)"));
        outputVCFHeader.addMetaDataLine(new VCFInfoHeaderLine("SOMATIC_PON_COUNT",
                1,
                VCFHeaderLineType.Integer,
                "Number of times the variant appears in the somatic PON"));
        outputVCFHeader.addMetaDataLine(new VCFInfoHeaderLine("GERMLINE_PON_COUNT",
                1,
                VCFHeaderLineType.Integer,
                "Number of times the variant appears in the germline PON"));
        return outputVCFHeader;
    }
}
