package com.hartwig.hmftools.patientdb.data;

import static com.hartwig.hmftools.patientdb.dao.PotentiallyActionableItemsDAO.FIVE_BREAKEND;
import static com.hartwig.hmftools.patientdb.dao.PotentiallyActionableItemsDAO.THREE_BREAKEND;
import static com.hartwig.hmftools.patientdb.database.hmfpatients.Tables.STRUCTURALVARIANT;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;

@Value.Immutable
@Value.Style(allParameters = true,
             passAnnotations = { NotNull.class, Nullable.class })
public abstract class PotentialActionableFusion {
    @NotNull
    public abstract String sampleId();

    @NotNull
    public abstract String fiveGene();

    @NotNull
    public abstract String threeGene();

    @NotNull
    public static PotentialActionableFusion of(@NotNull final Record mysqlRecord) {
        return ImmutablePotentialActionableFusion.of(mysqlRecord.get(STRUCTURALVARIANT.SAMPLEID),
                mysqlRecord.get(FIVE_BREAKEND.GENE),
                mysqlRecord.get(THREE_BREAKEND.GENE));
    }
}
