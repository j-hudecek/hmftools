package com.hartwig.hmftools.strelka.mnv;

import static com.hartwig.hmftools.strelka.mnv.MNVMerger.GERMLINE_PON_FIELD;
import static com.hartwig.hmftools.strelka.mnv.MNVMerger.SOMATIC_PON_FIELD;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.apache.commons.lang3.tuple.Pair;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFFileReader;

@Value.Immutable
@Value.Style(allParameters = true,
             passAnnotations = { NotNull.class, Nullable.class })
public abstract class VcfMNVValidator implements MNVValidator {
    @NotNull
    abstract String tumorVCF();

    @NotNull
    @Value.Derived
    protected Map<VariantKey, VariantContext> variantMap() {
        final VCFFileReader vcfReader = new VCFFileReader(new File(tumorVCF()), false);
        final Map<VariantKey, VariantContext> variantMap = new HashMap<>();
        for (final VariantContext variant : vcfReader) {
            final VariantKey key = ImmutableVariantKey.of(variant.getContig(), variant.getStart(), variant.getEnd());
            variantMap.put(key, variant);
        }
        return variantMap;
    }

    @Override
    @NotNull
    public List<VariantContext> mergeVariants(@NotNull final PotentialMNVRegion potentialMnvRegion, @NotNull final MNVMerger merger) {
        if (potentialMnvRegion.potentialMnvs().size() == 0) {
            return potentialMnvRegion.variants();
        } else {
            return outputVariants(potentialMnvRegion, merger);
        }
    }

    // MIVO: outputs only patched variants (mnvs with gaps)
    @NotNull
    public List<VariantContext> correctedVariants(@NotNull final PotentialMNVRegion potentialMnvRegion, @NotNull final MNVMerger merger) {
        if (potentialMnvRegion.potentialMnvs().size() == 0) {
            return Collections.emptyList();
        } else if (potentialMnvRegion.potentialMnvs().stream().noneMatch(potentialMNV -> potentialMNV.gapPositions().size() > 0)) {
            return Collections.emptyList();
        } else {
            final List<VariantContext> result = Lists.newArrayList();
            final Multimap<VariantContext, PotentialMNV> mnvCandidates = findMnvCandidates(potentialMnvRegion);
            mnvCandidates.keySet().forEach(mnv -> {
                final PotentialMNV bestCandidate = mnvCandidates.get(mnv).stream().min(potentialMNVComparator()).get();
                final Map<String, Object> newAttributes = merger.createMnvAttributes(bestCandidate.variants());
                final Map<String, Object> patchedAttributes = patchAttributes(mnv.getAttributes(), newAttributes);
                if (bestCandidate.gapPositions().size() > 0) {
                    result.add(new VariantContextBuilder(mnv).attributes(patchedAttributes).make());
                }
            });
            result.sort(Comparator.comparing(VariantContext::getStart)
                    .thenComparing(variantContext -> variantContext.getReference().length()));
            return result;
        }
    }

    @NotNull
    private List<VariantContext> outputVariants(@NotNull final PotentialMNVRegion potentialMnvRegion, @NotNull final MNVMerger merger) {
        final List<VariantContext> result = Lists.newArrayList();
        final Set<PotentialMNV> correctedCandidates = Sets.newHashSet();
        final Multimap<VariantContext, PotentialMNV> mnvCandidates = findMnvCandidates(potentialMnvRegion);
        mnvCandidates.keySet().stream().map(mnv -> patchVariant(mnv, mnvCandidates.get(mnv), merger)).forEach(mnvCandidatePair -> {
            result.add(mnvCandidatePair.getKey());
            correctedCandidates.add(mnvCandidatePair.getValue());
        });
        result.addAll(MNVRegionValidator.nonMnvVariants(potentialMnvRegion, correctedCandidates));
        result.sort(Comparator.comparing(VariantContext::getStart).thenComparing(variantContext -> variantContext.getReference().length()));
        return result;
    }

    @NotNull
    private Multimap<VariantContext, PotentialMNV> findMnvCandidates(@NotNull final PotentialMNVRegion potentialMnvRegion) {
        final Multimap<VariantContext, PotentialMNV> mnvCandidates = ArrayListMultimap.create();
        for (final PotentialMNV potentialMnv : potentialMnvRegion.potentialMnvs()) {
            final VariantKey potentialMnvKey =
                    ImmutableVariantKey.of(potentialMnv.chromosome(), potentialMnv.start(), potentialMnv.end() - 1);
            if (variantMap().containsKey(potentialMnvKey)) {
                final VariantContext mnvVariant = variantMap().get(potentialMnvKey);
                if (potentialMnvMatchesCorrectedMnv(potentialMnv, mnvVariant)) {
                    mnvCandidates.put(mnvVariant, potentialMnv);
                }
            }
        }
        return mnvCandidates;
    }

    // MIVO: re-create mnv attributes from individual variants and re-build mnv variantContext using corrected alleles
    @NotNull
    private static Pair<VariantContext, PotentialMNV> patchVariant(@NotNull final VariantContext mnv,
            @NotNull final Collection<PotentialMNV> candidates, @NotNull final MNVMerger merger) {
        final List<PotentialMNV> sortedCandidates = candidates.stream().sorted(potentialMNVComparator()).collect(Collectors.toList());
        final PotentialMNV bestCandidate = sortedCandidates.get(0);
        final Map<String, Object> attributes = merger.createMnvAttributes(bestCandidate.variants());
        final VariantContext patchedVariant = MNVMerger.buildMnv(bestCandidate.variants(), mnv.getAlleles(), attributes);
        return Pair.of(patchedVariant, bestCandidate);
    }

    // MIVO: patch existing mnv attributes with pon counts
    @NotNull
    private static Map<String, Object> patchAttributes(@NotNull final Map<String, Object> oldAttributes,
            @NotNull final Map<String, Object> newAttributes) {
        final Map<String, Object> result = Maps.newHashMap(oldAttributes);
        if (newAttributes.containsKey(SOMATIC_PON_FIELD)) {
            result.put(SOMATIC_PON_FIELD, newAttributes.get(SOMATIC_PON_FIELD));
        }
        if (newAttributes.containsKey(GERMLINE_PON_FIELD)) {
            result.put(GERMLINE_PON_FIELD, newAttributes.get(GERMLINE_PON_FIELD));
        }
        return result;
    }

    @VisibleForTesting
    static boolean potentialMnvMatchesCorrectedMnv(@NotNull final PotentialMNV potentialMNV, @NotNull final VariantContext mnv) {
        int refPosition = 0;
        int altPosition = 0;
        for (final VariantContext potentialMnvVariant : potentialMNV.variants()) {
            if (potentialMNV.gapPositions().contains(potentialMnvVariant.getStart() - 1)) {
                if (!isGapPosition(mnv, refPosition, altPosition)) {
                    return false;
                }
                refPosition++;
                altPosition++;
            }
            if (!variantContainedInMnv(potentialMnvVariant, mnv, refPosition, altPosition)) {
                return false;
            }
            refPosition += potentialMnvVariant.getReference().getBaseString().length();
            altPosition += potentialMnvVariant.getAlternateAllele(0).getBaseString().length();
        }
        return true;
    }

    private static boolean isGapPosition(@NotNull final VariantContext mnv, final int refPos, final int altPos) {
        final String mnvRef = mnv.getReference().getBaseString();
        final String mnvAlt = mnv.getAlternateAllele(0).getBaseString();
        return mnvRef.charAt(refPos) == mnvAlt.charAt(altPos);
    }

    @VisibleForTesting
    static boolean variantContainedInMnv(@NotNull final VariantContext variant, @NotNull final VariantContext mnv, final int refStart,
            final int altStart) {
        final String variantRef = variant.getReference().getBaseString();
        final String variantAlt = variant.getAlternateAllele(0).getBaseString();
        final String mnvRef = mnv.getReference().getBaseString();
        final String mnvAlt = mnv.getAlternateAllele(0).getBaseString();
        try {
            return mnvRef.substring(refStart, refStart + variantRef.length()).equals(variantRef) && mnvAlt.substring(altStart,
                    altStart + variantAlt.length()).equals(variantAlt);
        } catch (final IndexOutOfBoundsException e) {
            return false;
        }
    }

    @NotNull
    private static Comparator<PotentialMNV> potentialMNVComparator() {
        final Comparator<PotentialMNV> sizeComparator = Comparator.comparing(mnv -> mnv.variants().size());
        final Comparator<PotentialMNV> startPositionComparator = Comparator.comparing(PotentialMNV::start);
        return sizeComparator.reversed().thenComparing(startPositionComparator);
    }

    @Value.Immutable
    @Value.Style(allParameters = true,
                 passAnnotations = { NotNull.class, Nullable.class })
    static abstract class VariantKey {
        @NotNull
        abstract String chromosome();

        abstract int start();

        abstract int end();
    }
}