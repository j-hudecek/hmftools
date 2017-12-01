package com.hartwig.hmftools.svannotation.analysis;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.hartwig.hmftools.common.cosmicfusions.COSMICGeneFusionData;
import com.hartwig.hmftools.common.cosmicfusions.COSMICGeneFusionModel;
import com.hartwig.hmftools.common.region.hmfslicer.HmfGenomeRegion;
import com.hartwig.hmftools.common.variant.structural.StructuralVariant;
import com.hartwig.hmftools.svannotation.VariantAnnotator;
import com.hartwig.hmftools.svannotation.annotations.GeneAnnotation;
import com.hartwig.hmftools.svannotation.annotations.GeneDisruption;
import com.hartwig.hmftools.svannotation.annotations.GeneFusion;
import com.hartwig.hmftools.svannotation.annotations.ImmutableGeneDisruption;
import com.hartwig.hmftools.svannotation.annotations.ImmutableGeneFusion;
import com.hartwig.hmftools.svannotation.annotations.StructuralVariantAnnotation;
import com.hartwig.hmftools.svannotation.annotations.Transcript;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StructuralVariantAnalyzer {

    private final VariantAnnotator annotator;
    private final Collection<HmfGenomeRegion> regions;
    private final COSMICGeneFusionModel fusionModel;

    public StructuralVariantAnalyzer(final VariantAnnotator annotator, final Collection<HmfGenomeRegion> regions,
            final COSMICGeneFusionModel fusionModel) {
        this.annotator = annotator;
        this.regions = regions;
        this.fusionModel = fusionModel;
    }

    private boolean inHmfPanel(final GeneAnnotation g) {
        return regions.stream().anyMatch(r -> g.getSynonyms().contains(r.geneID()));
    }

    private boolean transcriptsMatchKnownFusion(final COSMICGeneFusionData fusion, final Transcript five, final Transcript three) {
        final boolean fiveValid = fusion.fiveTranscript() == null
                ? five.getGeneAnnotation().getSynonyms().stream().anyMatch(s -> s.equals(fusion.fiveGene()))
                : fusion.fiveTranscript().equals(five.getTranscriptId());
        final boolean threeValid = fusion.threeTranscript() == null ? three.getGeneAnnotation()
                .getSynonyms()
                .stream()
                .anyMatch(s -> s.equals(fusion.threeGene())) : fusion.threeTranscript().equals(three.getTranscriptId());
        return fiveValid && threeValid;
    }

    private boolean transcriptsMatchKnownFusion(final Transcript five, final Transcript three) {
        return fusionModel.fusions().stream().anyMatch(f -> transcriptsMatchKnownFusion(f, five, three));
    }

    private boolean isPromiscuous(final GeneAnnotation gene) {
        return Stream.of(fusionModel.promiscuousFivePrime(), fusionModel.promiscuousThreePrime())
                .anyMatch(l -> l.stream().anyMatch(g -> gene.getSynonyms().contains(g.geneName())));
    }

    private boolean oneEndPromiscuous(final Transcript five, final Transcript three) {
        final boolean promiscuousFive = fusionModel.promiscuousFivePrime()
                .stream()
                .anyMatch(p -> p.transcript() != null
                        ? p.transcript().equals(five.getTranscriptId())
                        : p.geneName().equals(five.getGeneName()));
        final boolean promiscuousThree = fusionModel.promiscuousThreePrime()
                .stream()
                .anyMatch(p -> p.transcript() != null
                        ? p.transcript().equals(three.getTranscriptId())
                        : p.geneName().equals(three.getGeneName()));
        return promiscuousFive || promiscuousThree;
    }

    private boolean intronicDisruption(final Transcript a, final Transcript b) {
        final boolean sameTranscript = a.getTranscriptId().equals(b.getTranscriptId());
        final boolean bothIntronic = a.isIntronic() && b.isIntronic();
        final boolean sameExonUpstream = a.getExonUpstream() == b.getExonUpstream();
        return sameTranscript && bothIntronic && sameExonUpstream;
    }

    private String exonDescription(final Transcript t, final boolean upstream) {
        if (t.isPromoter()) {
            return "Promoter Region";
        } else if (t.isExonic()) {
            return String.format("Exon %d", upstream ? t.getExonUpstream() : t.getExonDownstream());
        } else if (t.isIntronic()) {
            return String.format("Intron %d", t.getExonUpstream());
        } else {
            return String.format("Error up(%d) down(%d)", t.getExonUpstream(), t.getExonDownstream());
        }
    }

    private List<GeneFusion> processFusions(final List<StructuralVariantAnnotation> annotations) {

        // left is upstream, right is downstream
        final List<Pair<Transcript, Transcript>> fusions = Lists.newArrayList();

        for (final StructuralVariantAnnotation sv : annotations) {

            final List<Pair<Transcript, Transcript>> svFusions = Lists.newArrayList();

            for (final GeneAnnotation g : sv.getStart().getGeneAnnotations()) {

                final boolean g_upstream = g.getStrand() * g.getBreakend().getOrientation() > 0;

                for (final GeneAnnotation o : sv.getEnd().getGeneAnnotations()) {

                    final boolean o_upstream = o.getStrand() * o.getBreakend().getOrientation() > 0;
                    if (g_upstream == o_upstream) {
                        continue;
                    }

                    for (final Transcript t1 : g.getTranscripts()) {
                        if (!t1.isIntronic()) {
                            continue;
                        }

                        for (final Transcript t2 : o.getTranscripts()) {
                            if (!t2.isIntronic()) {
                                continue;
                            }

                            if (g_upstream && t1.getExonUpstreamPhase() == t2.getExonDownstreamPhase()) {
                                svFusions.add(Pair.of(t1, t2));
                            } else if (!g_upstream && t2.getExonUpstreamPhase() == t1.getExonDownstreamPhase()) {
                                svFusions.add(Pair.of(t2, t1));
                            }

                        }
                    }
                }
            }

            // from here, select either the canonical -> canonical transcript fusion
            // then the longest where one end is canonical
            // then the longest combined transcript

            Optional<Pair<Transcript, Transcript>> fusion =
                    svFusions.stream().filter(p -> p.getLeft().isCanonical() && p.getRight().isCanonical()).findFirst();

            if (fusion.isPresent()) {
                fusions.add(fusion.get());
                continue;
            }

            fusion = svFusions.stream()
                    .filter(p -> p.getLeft().isCanonical() || p.getRight().isCanonical())
                    .sorted(Comparator.comparingInt(a -> a.getLeft().getExonMax() + a.getRight().getExonMax()))
                    .reduce((a, b) -> b); // get longest

            if (fusion.isPresent()) {
                fusions.add(fusion.get());
                continue;
            }

            svFusions.stream()
                    .sorted(Comparator.comparingInt(a -> a.getLeft().getExonMax() + a.getRight().getExonMax()))
                    .reduce((a, b) -> b) // get longest
                    .ifPresent(fusions::add);
        }

        // transform results to reported details

        final List<GeneFusion> result = Lists.newArrayList();
        for (final Pair<Transcript, Transcript> fusion : fusions) {
            final Transcript upstream = fusion.getLeft(), downstream = fusion.getRight();
            final boolean sameGene = upstream.getGeneName().equals(downstream.getGeneName());

            if (sameGene) {
                if (!intronicDisruption(upstream, downstream) && isPromiscuous(upstream.getGeneAnnotation())) {
                    // okay
                } else {
                    continue;
                }
            } else if (transcriptsMatchKnownFusion(upstream, downstream)) {
                // in cosmic fusion list
            } else if (oneEndPromiscuous(upstream, downstream)) {
                // one end is promiscuous
            } else {
                continue;
            }

            final Double fiveAF = upstream.getBreakend().getAlleleFrequency();
            final Double threeAF = downstream.getBreakend().getAlleleFrequency();

            final GeneFusion details = ImmutableGeneFusion.builder()
                    .type(upstream.getBreakend().getStructuralVariant().getVariant().type().toString())
                    .start(upstream.getBreakend().getPositionString())
                    .geneStart(upstream.getGeneName())
                    .geneContextStart(exonDescription(upstream, true))
                    .transcriptStart(upstream.getTranscriptId())
                    .end(downstream.getBreakend().getPositionString())
                    .geneEnd(downstream.getGeneName())
                    .geneContextEnd(exonDescription(downstream, false))
                    .transcriptEnd(downstream.getTranscriptId())
                    .vaf(formatNullablePercent(fiveAF) + " " + formatNullablePercent(threeAF))
                    .build();

            result.add(details);
            annotations.remove(upstream.getBreakend().getStructuralVariant());
        }

        return result;
    }

    private List<GeneDisruption> processDisruptions(final List<StructuralVariantAnnotation> annotations) {

        final List<GeneAnnotation> geneAnnotations = Lists.newArrayList();
        for (final StructuralVariantAnnotation sv : annotations) {

            final boolean intronicExists = sv.getStart()
                    .getGeneAnnotations()
                    .stream()
                    .filter(g -> g.getCanonical() != null)
                    .anyMatch(g -> sv.getEnd()
                            .getGeneAnnotations()
                            .stream()
                            .filter(o -> o.getCanonical() != null)
                            .anyMatch(o -> intronicDisruption(g.getCanonical(), o.getCanonical())));
            if (intronicExists) {
                continue;
            }

            geneAnnotations.addAll(sv.getStart().getGeneAnnotations());
            geneAnnotations.addAll(sv.getEnd().getGeneAnnotations());
        }

        final ArrayListMultimap<String, GeneAnnotation> geneMap = ArrayListMultimap.create();
        for (final GeneAnnotation g : geneAnnotations) {
            if (!inHmfPanel(g)) {
                continue;
            }
            geneMap.put(g.getGeneName(), g);
        }

        final List<GeneDisruption> disruptions = Lists.newArrayList();
        for (final String geneName : geneMap.keySet()) {
            for (final GeneAnnotation g : geneMap.get(geneName)) {

                // don't care if we aren't in the canonical transcript
                if (g.getCanonical() == null) {
                    continue;
                }

                final GeneDisruption disruption = ImmutableGeneDisruption.builder()
                        .geneName(geneName)
                        .location(g.getBreakend().getPositionString())
                        .geneContext(exonDescription(g.getCanonical(), true))
                        .transcript(g.getCanonical().getTranscriptId())
                        .partner(g.getOtherBreakend().getPositionString())
                        .type(g.getBreakend().getStructuralVariant().getVariant().type().toString())
                        .orientation(g.getBreakend().getOrientation() > 0 ? "5'" : "3'")
                        .vaf(formatNullablePercent(g.getBreakend().getAlleleFrequency()))
                        .build();

                disruptions.add(disruption);
                annotations.remove(g.getBreakend().getStructuralVariant());
            }
        }

        return disruptions;
    }

    @NotNull
    private static String formatNullablePercent(final @Nullable Double percentage) {
        return percentage == null ? "Na" : formatPercent(percentage);
    }

    @NotNull
    private static String formatPercent(final double percentage) {
        return Long.toString(Math.round(percentage * 100D)) + "%";
    }

    @NotNull
    public StructuralVariantAnalysis run(@NotNull final List<StructuralVariant> variants) {

        final List<StructuralVariantAnnotation> annotations = annotator.annotateVariants(variants);
        final List<GeneFusion> fusions = processFusions(annotations);
        final List<GeneDisruption> disruptions = processDisruptions(annotations);

        return ImmutableStructuralVariantAnalysis.of(annotations, fusions, disruptions);
    }
}