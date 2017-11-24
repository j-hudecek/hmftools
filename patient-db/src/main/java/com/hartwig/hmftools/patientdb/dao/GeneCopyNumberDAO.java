package com.hartwig.hmftools.patientdb.dao;

import static com.hartwig.hmftools.patientdb.Config.BATCH_INSERT_SIZE;
import static com.hartwig.hmftools.patientdb.database.hmfpatients.Tables.GENECOPYNUMBER;
import static com.hartwig.hmftools.patientdb.database.hmfpatients.tables.Copynumber.COPYNUMBER;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Iterables;
import com.hartwig.hmftools.common.gene.GeneCopyNumber;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep15;

class GeneCopyNumberDAO {

    @NotNull
    private final DSLContext context;

    GeneCopyNumberDAO(@NotNull final DSLContext context) {
        this.context = context;
    }

    void writeCopyNumber(@NotNull final String sample, @NotNull List<GeneCopyNumber> copyNumbers) {
        Timestamp timestamp = new Timestamp(new Date().getTime());
        context.delete(GENECOPYNUMBER).where(GENECOPYNUMBER.SAMPLEID.eq(sample)).execute();

        for (List<GeneCopyNumber> splitCopyNumbers : Iterables.partition(copyNumbers, BATCH_INSERT_SIZE)) {
            InsertValuesStep15 inserter = context.insertInto(GENECOPYNUMBER,
                    GENECOPYNUMBER.SAMPLEID,
                    GENECOPYNUMBER.CHROMOSOME,
                    GENECOPYNUMBER.START,
                    GENECOPYNUMBER.END,
                    GENECOPYNUMBER.GENE,
                    GENECOPYNUMBER.MINCOPYNUMBER,
                    GENECOPYNUMBER.MAXCOPYNUMBER,
                    GENECOPYNUMBER.MEANCOPYNUMBER,
                    GENECOPYNUMBER.SOMATICREGIONS,
                    GENECOPYNUMBER.GERMLINEHOMREGIONS,
                    GENECOPYNUMBER.GERMLINEHETREGIONS,
                    GENECOPYNUMBER.TRANSCRIPTID,
                    GENECOPYNUMBER.TRANSCRIPTVERSION,
                    GENECOPYNUMBER.CHROMOSOMEBAND,
                    COPYNUMBER.MODIFIED);
            splitCopyNumbers.forEach(x -> addCopynumberRecord(timestamp, inserter, sample, x));
            inserter.execute();
        }

    }

    private void addCopynumberRecord(Timestamp timestamp, InsertValuesStep15 inserter, String sample, GeneCopyNumber gene) {
        inserter.values(sample,
                gene.chromosome(),
                gene.start(),
                gene.end(),
                gene.gene(),
                gene.minCopyNumber(),
                gene.maxCopyNumber(),
                gene.meanCopyNumber(),
                gene.somaticRegions(),
                gene.germlineHomRegions(),
                gene.germlineHet2HomRegions(),
                gene.transcriptID(),
                gene.transcriptVersion(),
                gene.chromosomeBand(),
                timestamp);
    }
}
