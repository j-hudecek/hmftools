package com.hartwig.hmftools.svannotation;

import java.util.List;

import com.hartwig.hmftools.common.variant.structural.EnrichedStructuralVariant;
import com.hartwig.hmftools.svannotation.annotations.StructuralVariantAnnotation;

import org.jetbrains.annotations.NotNull;

public interface VariantAnnotator {

    @NotNull
    List<StructuralVariantAnnotation> annotateVariants(@NotNull List<EnrichedStructuralVariant> variants);
}
