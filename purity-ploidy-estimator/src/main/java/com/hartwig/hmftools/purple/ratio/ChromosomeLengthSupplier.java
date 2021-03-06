package com.hartwig.hmftools.purple.ratio;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.hartwig.hmftools.common.chromosome.ChromosomeLength;
import com.hartwig.hmftools.common.chromosome.ChromosomeLengthFactory;
import com.hartwig.hmftools.common.chromosome.ChromosomeLengthFile;
import com.hartwig.hmftools.common.cobalt.CobaltRatio;
import com.hartwig.hmftools.purple.config.CommonConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ChromosomeLengthSupplier implements Supplier<Map<String, ChromosomeLength>> {

    private static final Logger LOGGER = LogManager.getLogger(ChromosomeLengthSupplier.class);

    @NotNull
    private final Map<String, ChromosomeLength> chromosomeLengths;

    public ChromosomeLengthSupplier(@NotNull final CommonConfig config, @NotNull Multimap<String, CobaltRatio> cobaltRatios)
            throws IOException {
        final String chrLengthFile = ChromosomeLengthFile.generateFilename(config.cobaltDirectory(), config.tumorSample());
        if (new File(chrLengthFile).exists()) {
            LOGGER.info("Loading chromosome lengths from {}", chrLengthFile);
            chromosomeLengths =
                    ChromosomeLengthFile.read(chrLengthFile).stream().collect(Collectors.toMap(ChromosomeLength::chromosome, item -> item));
        } else {
            LOGGER.info("Generating chromosome lengths from tumor read ratios");
            chromosomeLengths = ChromosomeLengthFactory.create(config.windowSize(), cobaltRatios);
        }
    }

    @Override
    @NotNull
    public Map<String, ChromosomeLength> get() {
        return chromosomeLengths;
    }
}
