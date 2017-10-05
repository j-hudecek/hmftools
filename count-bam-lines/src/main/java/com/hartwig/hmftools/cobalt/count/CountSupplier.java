package com.hartwig.hmftools.cobalt.count;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hartwig.hmftools.cobalt.CountBamLinesApplication;
import com.hartwig.hmftools.common.chromosome.ChromosomeLength;
import com.hartwig.hmftools.common.chromosome.ChromosomeLengthFactory;
import com.hartwig.hmftools.common.chromosome.ChromosomeLengthFile;
import com.hartwig.hmftools.common.cobalt.ReadCount;
import com.hartwig.hmftools.common.cobalt.ReadCountFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

public class CountSupplier {

    private static final Logger LOGGER = LogManager.getLogger(CountBamLinesApplication.class);

    private final int threadCount;
    private final int windowSize;
    private final int minQuality;
    private final String chromosomeLengthFileName;
    private final String countFileName;

    public CountSupplier(final int threadCount, final int windowSize, final int minQuality, final String chromosomeLengthFileName,
            final String countFileName) {
        this.threadCount = threadCount;
        this.windowSize = windowSize;
        this.minQuality = minQuality;
        this.chromosomeLengthFileName = chromosomeLengthFileName;
        this.countFileName = countFileName;
    }

    public Multimap<String, ReadCount> fromFile() throws IOException {
        LOGGER.info("Reading read count from {}", countFileName);
        return ReadCountFile.readFile(countFileName);
    }

    public Multimap<String, ReadCount> fromBam(@NotNull final File inputFile) throws IOException, ExecutionException, InterruptedException {

        LOGGER.info("Calculating Read Count from {}", inputFile.toString());
        final SamReaderFactory readerFactory = SamReaderFactory.make();
        final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("bam-%d").build();
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount, namedThreadFactory);

        final List<ChromosomeLength> lengths;
        try (SamReader reader = readerFactory.open(inputFile)) {
            lengths = ChromosomeLengthFactory.create(reader.getFileHeader());
        }
        ChromosomeLengthFile.write(chromosomeLengthFileName, lengths);

        final List<Future<ChromosomeReadCount>> futures = Lists.newArrayList();
        for (ChromosomeLength chromosome : lengths) {
            final ChromosomeReadCount callable = new ChromosomeReadCount(inputFile,
                    readerFactory,
                    chromosome.chromosome(),
                    chromosome.position(),
                    windowSize,
                    minQuality);
            futures.add(executorService.submit(callable));
        }

        LOGGER.info("Persisting read counts to {}", countFileName);
        ReadCountFile.createFile(windowSize, countFileName);
        final ListMultimap<String, ReadCount> readCounts = ArrayListMultimap.create();
        for (Future<ChromosomeReadCount> future : futures) {
            final ChromosomeReadCount readCount = future.get();
            final String chromosome = readCount.chromosome();
            final List<ReadCount> result = readCount.readCount();

            persist(countFileName, chromosome, result);
            readCounts.putAll(chromosome, result);
        }

        LOGGER.info("Read Count Complete");
        executorService.shutdown();

        return readCounts;
    }

    private void persist(@NotNull final String filename, @NotNull final String chromosome, @NotNull List<ReadCount> readCounts)
            throws IOException {
        LOGGER.info("Persisting {} windows from chromosome {}", readCounts.size(), chromosome);
        ReadCountFile.append(filename, readCounts);
    }

}