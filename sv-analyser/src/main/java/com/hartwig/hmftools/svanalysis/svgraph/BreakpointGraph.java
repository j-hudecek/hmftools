package com.hartwig.hmftools.svanalysis.svgraph;

import com.google.common.collect.*;
import com.hartwig.hmftools.common.position.GenomePosition;
import com.hartwig.hmftools.common.purple.copynumber.PurpleCopyNumber;
import com.hartwig.hmftools.common.purple.segment.SegmentSupport;
import com.hartwig.hmftools.common.variant.structural.EnrichedStructuralVariant;
import com.hartwig.hmftools.common.variant.structural.EnrichedStructuralVariantLeg;
import com.hartwig.hmftools.common.variant.structural.ImmutableEnrichedStructuralVariant;
import com.hartwig.hmftools.common.variant.structural.StructuralVariantType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BreakpointGraph {
    private static final Logger LOGGER = LogManager.getLogger(BreakpointGraph.class);

    private final BgSegment unplacedSegment = BgSegment.createUnplacedSegment();
    private final Map<BgSegment, List<BgAdjacency>> startEdges = new HashMap<>();
    private final Map<BgSegment, List<BgAdjacency>> endEdges = new HashMap<>();

    private BreakpointGraph() {
        this.startEdges.put(unplacedSegment, new ArrayList<>());
        this.endEdges.put(unplacedSegment, new ArrayList<>());
    }
    public BreakpointGraph(List<PurpleCopyNumber> cnRecords, List<EnrichedStructuralVariant> svRecords) {
        this();
        Map<String, List<BgSegment>> segments = cnRecords.stream()
                .map(cn -> new BgSegment(cn))
                .sorted(ByGenomicPosition)
                .collect(Collectors.groupingBy(GenomePosition::chromosome, Collectors.toList()));
        for (List<BgSegment> chrSegs: segments.values()) {
            for (int i = 0; i < chrSegs.size(); i++) {
                BgSegment s = chrSegs.get(i);
                startEdges.put(s, new ArrayList<>());
                endEdges.put(s, new ArrayList<>());
            }
            for (int i = 0; i < chrSegs.size() - 1; i++) {
                BgSegment left = chrSegs.get(i);
                BgSegment right = chrSegs.get(i + 1);
                BgReferenceEdge edge = new BgReferenceEdge(left, right);
                addEdge(left, 1, right, -1, edge);
            }
        }
        for (EnrichedStructuralVariant sv : svRecords) {
            addSV(sv, segments);
        }
        sanityCheck();
    }
    public BgSegment getUnplacedSegment() {
        return unplacedSegment;
    }
    public Collection<BgSegment> getAllSegments() {
        return startEdges.keySet();
    }
    public List<EnrichedStructuralVariant> getAllStructuralVariants() {
        return getAllEdges().stream()
                .filter(e -> e instanceof BgSv)
                .map(e -> ((BgSv)e).sv())
                .collect(Collectors.toList());
    }
    public Collection<BgEdge> getAllEdges() {
        Set<BgEdge> edges = Streams.concat(startEdges.values().stream(), endEdges.values().stream())
                .flatMap(adjList -> adjList.stream())
                .map(adj -> adj.edge())
                .collect(Collectors.toSet());
        return edges;
    }
    public BgSegment getSegment(PurpleCopyNumber purpleCopyNumber) {
        return startEdges.keySet().stream()
                .filter(s -> s.cn() == purpleCopyNumber)
                .findFirst()
                .orElse(null);
    }
    private void addSV(EnrichedStructuralVariant sv, Map<String, List<BgSegment>> segmentLookup) {
        BgSegment startSegment = containingSegment(sv.start(), segmentLookup);
        BgSegment endSegment  = containingSegment(sv.end(), segmentLookup);
        // sanity checks
        if ((sv.start() != null && startSegment == null) ||
                (sv.end() != null && endSegment == null) ||
                (sv.start().orientation() == 1 && startSegment.endPosition() != sv.start().position()) ||
                (sv.start().orientation() == -1 && startSegment.startPosition() != sv.start().position()) ||
                (sv.end() != null && sv.end().orientation() == 1 && endSegment.endPosition() != sv.end().position()) ||
                (sv.end() != null && sv.end().orientation() == -1 && endSegment.startPosition() != sv.end().position())) {
            LOGGER.info("Discarding SV {}:{}{} to {}:{}{} as bounds do not match CN segments {} and {}.",
                    sv.start().chromosome(), sv.start().position(), sv.start().orientation() == 1 ? "+" : "-",
                    sv.end() == null ? "" : sv.end().chromosome(), sv.end() == null ? "" : sv.end().position(), sv.end() == null ? "" : (sv.end().orientation() == 1 ? "+" : "-"),
                    startSegment, endSegment);
            return;
        }
        addEdge(startSegment, startSegment == unplacedSegment ? -1 :  sv.start().orientation(),
                endSegment, endSegment == unplacedSegment ? -1 : sv.end().orientation(),
                new BgSv(sv));
    }
    private BgSegment containingSegment(EnrichedStructuralVariantLeg leg, Map<String, List<BgSegment>> segmentLookup) {
        if (leg == null) {
            return unplacedSegment;
        }
        List<BgSegment> segments = segmentLookup.get(leg.chromosome());
        if (segments == null) {
            return unplacedSegment;
        }
        int position = Collections.binarySearch(segments, leg);
        if (position < 0) {
            position = -2 - position;
        }
        if (position >= 0 && position < segments.size()) {
            return segments.get(position);
        }
        return unplacedSegment;
    }
    private void addEdge(BgSegment start, int startOrientation, BgSegment end, int endOrientation, BgEdge edge) {
        BgAdjacency leftAdj = ImmutableBgAdjacencyImpl.builder()
                .fromSegment(start)
                .fromOrientation(startOrientation)
                .toSegment(end)
                .toOrientation(endOrientation)
                .edge(edge)
                .linkedBy(edge.sv() == null ? ImmutableList.of() : edge.sv().startLinks())
                .build();
        BgAdjacency rightAdj = ImmutableBgAdjacencyImpl.builder()
                .fromSegment(end)
                .fromOrientation(endOrientation)
                .toSegment(start)
                .toOrientation(startOrientation)
                .edge(edge)
                .linkedBy(edge.sv() == null ? ImmutableList.of() : edge.sv().endLinks())
                .build();
        addAdjacency(leftAdj);
        addAdjacency(rightAdj);
    }
    private void addAdjacency(BgAdjacency adj) {
        switch (adj.fromOrientation()) {
            case 1:
                endEdges.get(adj.fromSegment()).add(adj);
                break;
            case -1:
                startEdges.get(adj.fromSegment()).add(adj);
                break;
            default:
                throw new IllegalArgumentException("Invalid orientation");
        }
    }

    public int mergeReferenceSegments(boolean mergeAcrossCentromere) {
        for (BgSegment segment : endEdges.keySet()) {
            BgSegment nextSegment = nextReferenceSegment(segment);
            if (nextSegment != null && svCount(getOutgoing(segment, 1)) == 0 && svCount(getOutgoing(nextSegment, -1)) == 0) {
                if (mergeAcrossCentromere || (nextSegment.cn().segmentStartSupport() != SegmentSupport.CENTROMERE && segment.cn().segmentEndSupport() != SegmentSupport.CENTROMERE)) {
                    merge(segment, nextSegment);
                    return 1 + mergeReferenceSegments(mergeAcrossCentromere);
                }
            }
        }
        return 0;
    }
    private boolean isStartLeg(BgAdjacency adj) {
        EnrichedStructuralVariant sv = adj.edge().sv();
        if (adj.fromSegment() == getUnplacedSegment()) return false;
        String fromChromosome = adj.fromSegment().chromosome();
        long fromPosition = adj.fromOrientation() == -1 ? adj.fromSegment().startPosition() : adj.fromSegment().endPosition();
        if (fromChromosome.equals(sv.start().chromosome()) &&
                fromPosition == sv.start().position() &&
                adj.fromOrientation() == sv.start().orientation()) {
            return true;
        }
        if (fromChromosome.equals(sv.end().chromosome()) &&
                fromPosition == sv.end().position() &&
                adj.fromOrientation() == sv.end().orientation()) {
            return false;
        }
        throw new RuntimeException(String.format("Unable to determine sv leg for adjacency %s", adj));
    }
    private EnrichedStructuralVariant createSpanningSv(BgAdjacency left, BgAdjacency right) {
        EnrichedStructuralVariant leftSv = left.edge().sv();
        EnrichedStructuralVariant rightSv = right.edge().sv();
        EnrichedStructuralVariantLeg leftLeg = isStartLeg(left) ? leftSv.end() : leftSv.start();
        EnrichedStructuralVariantLeg rightLeg = isStartLeg(right) ? rightSv.end() : rightSv.start();
        return ImmutableEnrichedStructuralVariant.builder()
                .from(leftSv)
                .ploidy((leftSv.ploidy() + rightSv.ploidy()) / 2)
                .start(leftLeg)
                .end(rightLeg)
                .startLinkedBy(isStartLeg(left) ? leftSv.endLinkedBy() : leftSv.startLinkedBy())
                .endLinkedBy(isStartLeg(right) ? rightSv.endLinkedBy() : rightSv.startLinkedBy())
                .type(StructuralVariantType.BND)
                .id(leftSv.id() + "-" + rightSv.id())
                .insertSequence(leftSv.insertSequence() + "-" + String.format("%s:%d-%d", left.fromSegment().chromosome(), left.fromSegment().position(), right.fromSegment().endPosition()) + "-" + rightSv.insertSequence())
                .qualityScore(leftSv.qualityScore() + rightSv.qualityScore())
                .imprecise(leftSv.imprecise() || rightSv.imprecise())
                .filter(leftSv.filter() + rightSv.filter())
                .event(leftSv.event() + ";" + rightSv.event())
                .build();
    }
    private void mergeAdjacentSvs(double ploidy, BgAdjacency left, BgAdjacency right) {
        assert(left.fromOrientation() == -1);
        assert(right.fromOrientation() == 1);
        assert(left.fromSegment().chromosome().equals(right.fromSegment().chromosome()));
        assert(left.fromSegment().position() <= right.fromSegment().position());
        assert(left.edge() != right.edge());
        removeAdjacency(left);
        removeAdjacency(right);
        addEdge(left.toSegment(), left.toOrientation(), right.toSegment(), right.toOrientation(), new BgSv(createSpanningSv(left, right)));
        BgSegment segment = left.fromSegment();
        while (segment != right.fromSegment()) {
            segment.adjustPloidy(-ploidy);
            segment = nextReferenceSegment(segment);
        }
        segment.adjustPloidy(-ploidy);
    }

    private Collection<BreakendConsistency> getConsistencySet(final double ploidy, final BgAdjacency ... svs) {
        return Stream.concat(Arrays.stream(svs), Arrays.stream(svs).map(adj -> getPartner(adj)))
                .filter(adj -> adj.fromSegment() != getUnplacedSegment())
                .distinct()
                .map(adj -> getConsistency(ploidy, adj))
                .collect(Collectors.toList());
    }
    private BreakendConsistency getConsistency(final double ploidy, final BgAdjacency sv) {
        BgSegment referenceSegment = sv.fromOrientation() == 1 ? nextReferenceSegment(sv.fromSegment()) : prevReferenceSegment(sv.fromSegment());
        List<EnrichedStructuralVariant> alternatePaths = getOutgoing(sv.fromSegment(), sv.fromOrientation()).stream()
                .filter(adj -> !adj.isReference() && adj != sv)
                .map(adj -> adj.edge().sv())
                .collect(Collectors.toList());
        List<EnrichedStructuralVariant> oppositeOrientationSvs = getOutgoing(referenceSegment, sv.fromOrientation() * -1).stream()
                .filter(adj -> !adj.isReference())
                .map(adj -> adj.edge().sv())
                .collect(Collectors.toList());
        return new BreakendConsistency(ploidy, sv.fromSegment(), referenceSegment, sv.edge().sv(), alternatePaths, oppositeOrientationSvs);
    }

    private void removeAdjacency(@NotNull BgAdjacency adj) {
        removeAdjacency(adj, getPartner(adj));
    }
    private void removeAdjacency(@NotNull BgAdjacency leftAdj, @NotNull BgAdjacency rightAdj) {
        if (leftAdj == null || rightAdj == null) {
            throw new NullPointerException();
        }
        if (leftAdj.edge() != rightAdj.edge()) {
            throw new IllegalArgumentException("Adjacencies are not paired.");
        }
        if (leftAdj == rightAdj) {
            throw new IllegalArgumentException("Adjacencies are the same object");
        }
        if (!getOutgoing(leftAdj.fromSegment(), leftAdj.fromOrientation()).remove(leftAdj)) {
            throw new IllegalStateException("Sanity check failure: removed non-existent adjacency");
        }
        if (!getOutgoing(rightAdj.fromSegment(), rightAdj.fromOrientation()).remove(rightAdj)) {
            throw new IllegalStateException("Sanity check failure: removed non-existent adjacency");
        }
        sanityCheck();
    }

    public BgSegment prevReferenceSegment(BgSegment segment) {
        for (BgAdjacency adj : getOutgoing(segment, -1)) {
            if (adj.edge() instanceof BgReferenceEdge) {
                return adj.toSegment();
            }
        }
        return null;
    }
    public BgSegment nextReferenceSegment(BgSegment segment) {
        for (BgAdjacency adj : getOutgoing(segment, 1)) {
            if (adj.edge() instanceof BgReferenceEdge) {
                return adj.toSegment();
            }
        }
        return null;
    }
    public static BgAdjacency firstSv(List<BgAdjacency> adjacencies) {
        for (BgAdjacency adj : adjacencies) {
            if (!adj.isReference()) {
                return adj;
            }
        }
        return null;
    }
    public static int svCount(List<BgAdjacency> adjacencies) {
        int count = 0;
        for (BgAdjacency adj : adjacencies) {
            if (!adj.isReference()) {
                count++;
            }
        }
        return count;
    }
    public List<BgAdjacency> getOutgoing(BgSegment segment, int orientation) {
        switch (orientation) {
            case 1:
                return endEdges.get(segment);
            case -1:
                return startEdges.get(segment);
            default:
                throw new IllegalArgumentException("Invalid orientation");
        }
    }
    public BgSegment merge(BgSegment left, BgSegment right) {
        if (left == unplacedSegment || right == unplacedSegment) {
            throw new IllegalArgumentException("Cannot merge placeholder unplaced DNA segment.");
        }
        List<BgAdjacency> leftStart = startEdges.get(left);
        List<BgAdjacency> rightStart = startEdges.get(right);
        List<BgAdjacency> leftEnd = endEdges.get(left);
        List<BgAdjacency> rightEnd = endEdges.get(right);

        if (svCount(leftEnd) > 0 || svCount(rightStart) > 0) {
            throw new IllegalArgumentException("Cannot merge DNA segments separated by a SV.");
        }
        if (leftEnd.size() != 1 || rightStart.size() != 1 ||
                leftEnd.get(0).toSegment() != right ||
                rightStart.get(0).toSegment() != left) {
            throw new IllegalArgumentException("Cannot merge DNA segments that are not adjacent in the reference.");
        }
        // Create new segment
        BgSegment merged = BgSegment.merge(left, right);
        // update adjacency from segments
        List<BgAdjacency> newStart = leftStart.stream()
                .map(adj -> replaceSegments(adj, left, right, merged))
                .collect(Collectors.toList());
        List<BgAdjacency> newEnd = rightEnd.stream()
                .map(adj -> replaceSegments(adj, left, right, merged))
                .collect(Collectors.toList());
        startEdges.put(merged, newStart);
        endEdges.put(merged, newEnd);
        // Update the other side of the adjacencies
        for (BgAdjacency adj: Iterables.concat(leftStart, rightEnd)) {
            BgAdjacency partner = getPartner(adj);
            BgAdjacency newPartner = replaceSegments(partner, left, right, merged);
            getOutgoing(adj.toSegment(), adj.toOrientation()).set(getOutgoing(adj.toSegment(), adj.toOrientation()).indexOf(partner), newPartner);
        }
        // remove old segments from the graph
        startEdges.remove(left);
        startEdges.remove(right);
        endEdges.remove(left);
        endEdges.remove(right);
        LOGGER.debug("Merged CN: {}:{}-{} CN={} with {}:{}-{} CN={} to create {}:{}-{} CN={}",
                left.chromosome(), left.startPosition(), left.endPosition(), left.ploidy(),
                right.chromosome(), right.startPosition(), right.endPosition(), right.ploidy(),
                merged.chromosome(), merged.startPosition(), merged.endPosition(), merged.ploidy());
        sanityCheck();
        return merged;
    }
    private static BgAdjacency replaceSegments(BgAdjacency adj, BgSegment left, BgSegment right, BgSegment merged) {
        ImmutableBgAdjacencyImpl.Builder builder = ImmutableBgAdjacencyImpl.builder()
                .from(adj);
        if (adj.fromSegment() == left) {
            builder.fromSegment(merged);
        }
        if (adj.toSegment() == left) {
            builder.toSegment(merged);
        }
        if (adj.fromSegment() == right) {
            builder.fromSegment(merged);
        }
        if (adj.toSegment() == right) {
            builder.toSegment(merged);
        }
        return builder.build();
    }
    private BgAdjacency getPartner(BgAdjacency adj) {
        for (BgAdjacency remoteAdj : getOutgoing(adj.toSegment(), adj.toOrientation())) {
            if (remoteAdj != adj && remoteAdj.edge() == adj.edge()) {
                return remoteAdj;
            }
        }
        throw new IllegalStateException("Partner adjacency missing from graph");
    }
    private Collection<Pair<BgAdjacency, BgAdjacency>> getAssemblyLinks() {
        Set<Pair<BgAdjacency, BgAdjacency>> asmLinks = new HashSet<>();
        Map<String, BgAdjacency> endLinks = endEdges.values().stream()
                .flatMap(list -> list.stream())
                .flatMap(adj -> adj.linkedBy().stream().map(link -> ImmutablePair.of(link, adj)))
                .collect(Collectors.toMap(p -> p.left, p -> p.right));
        // we only need to iterate over the start edges since assembly links
        // involve a segment of DNA so we need one to come into the segment
        // and the other to go out of the segment
        for (List<BgAdjacency> adjList : startEdges.values()) {
            for (BgAdjacency left : adjList) {
                if (!left.isReference()) {
                    for (String link : left.linkedBy()) {
                        if (link.startsWith("asm")) {
                            BgAdjacency right = endLinks.get(link);
                            // self linked assemblies are in theory possible but we'll ignore them for now
                            if (right != null && left.edge().sv() != right.edge().sv()) {
                                asmLinks.add(ImmutablePair.of(left, right));
                            }
                        }
                    }
                }
            }
        }
        return asmLinks;
    }
    //region Simplifications
    public List<Simplification> simplify(SimplificationStrategy strategy) {
        List<Simplification> simplified = new ArrayList<>();
        List<Simplification> var = simplifyEvent(strategy);
        while (var != null) {
            simplified.addAll(var);
            mergeReferenceSegments(false);
            var = simplifyEvent(strategy);
        }
        return simplified;
    }
    public List<Simplification> simplifyEvent(SimplificationStrategy strategy) {
        List<Simplification> list = new ArrayList<>();
        Simplification var = simplifySimpleDuplications(strategy);
        if (var != null) {
            list.add(var);
        }
        var = simplifySimpleDeletion(strategy);
        if (var != null) {
            list.add(var);
        }
        list.addAll(simplifyAssemblyLinks(strategy));
        return list;
    }
    public Simplification simplifySimpleDuplications(SimplificationStrategy strategy) {
        for (BgSegment segment : startEdges.keySet()) {
            List<BgAdjacency> prev = startEdges.get(segment);
            List<BgAdjacency> next = endEdges.get(segment);
            // simple duplication check
            if (svCount(prev) == 1 && svCount(next) == 1) {
                BgAdjacency prevSv = firstSv(prev);
                BgAdjacency nextSv = firstSv(next);
                if (prevSv.edge() == nextSv.edge()) {
                    BgSegment prevSegment = prevReferenceSegment(segment);
                    BgSegment nextSegment = nextReferenceSegment(segment);
                    if (svCount(getOutgoing(prevSegment, 1)) == 0 && svCount(getOutgoing(nextSegment, -1)) == 0) {
                        EnrichedStructuralVariant sv = prevSv.edge().sv();
                        double ploidy = segment.ploidy() - (prevSegment.ploidy() + nextSegment.ploidy()) / 2;
                        Simplification duplication = ImmutableSimplificationImpl.builder()
                                .type(SimplificationType.SimpleDuplication)
                                .ploidy(ploidy)
                                .addVariants(sv)
                                .consistency(getConsistencySet(ploidy, nextSv))
                                .build();
                        if (strategy.shouldSimplify(duplication)) {
                            LOGGER.debug("Simplifying simple tandem duplication of {}", segment);
                            segment.adjustPloidy(duplication.ploidy());
                            removeAdjacency(prevSv, nextSv);
                            return duplication;
                        }
                    }
                }
            }
        }
        return null;
    }
    public Simplification simplifySimpleDeletion(SimplificationStrategy strategy) {
        for (BgSegment segment : startEdges.keySet()) {
            BgSegment prevSegment = prevReferenceSegment(segment);
            BgSegment nextSegment = nextReferenceSegment(segment);
            if (prevSegment != null && nextSegment != null &&
                    svCount(getOutgoing(segment, -1)) == 0 &&
                    svCount(getOutgoing(segment, 1)) == 0 &&
                    svCount(getOutgoing(prevSegment, 1)) == 1 &&
                    svCount(getOutgoing(nextSegment, -1)) == 1) {
                BgAdjacency leftSv = firstSv(getOutgoing(prevSegment, 1));
                BgAdjacency rightSv = firstSv(getOutgoing(nextSegment, -1));
                if (leftSv.edge() == rightSv.edge()) {
                    EnrichedStructuralVariant sv = leftSv.edge().sv();
                    // we have a deletion
                    double ploidy = (prevSegment.ploidy() + nextSegment.ploidy()) / 2 - segment.ploidy();
                    Simplification deletion = ImmutableSimplificationImpl.builder()
                            .type(SimplificationType.SimpleDeletion)
                            .ploidy(ploidy)
                            .addVariants(leftSv.edge().sv())
                            .consistency(getConsistencySet(ploidy, leftSv))
                            .build();
                    if (strategy.shouldSimplify(deletion)) {
                        LOGGER.debug("Simplifying simple deletion of {}", segment);
                        segment.adjustPloidy(deletion.ploidy());

                        removeAdjacency(leftSv, rightSv);
                        return deletion;
                    }
                }
            }
        }
        return null;
    }
    public List<Simplification> simplifyAssemblyLinks(SimplificationStrategy strategy) {
        List<Simplification> simplifications = new ArrayList<>();
        Collection<Pair<BgAdjacency, BgAdjacency>> potentialLinks = getAssemblyLinks();
        for (Pair<BgAdjacency, BgAdjacency> links : potentialLinks) {
            BgAdjacency left = links.getLeft();
            BgAdjacency right = links.getRight();
            // We need to check if this simplification is still valid since
            // simplifying a pair of linked SVs in a chain will replace that SV
            // with a new one hence invalidating our link
            if (getOutgoing(left.fromSegment(), -1).contains(left) &&
                    getOutgoing(right.fromSegment(), 1).contains(right)) {
                double ploidy = (left.edge().ploidy() + right.edge().ploidy()) / 2;
                Simplification chain = ImmutableSimplificationImpl.builder()
                        .type(SimplificationType.Chain)
                        .ploidy(ploidy)
                        .addVariants(
                                left.edge().sv(),
                                right.edge().sv())
                        .consistency(getConsistencySet(ploidy, left, right))
                        .build();
                if (strategy.shouldSimplify(chain)) {
                    LOGGER.debug("Simplifying assembly linked events {} and {}", left, right);
                    mergeAdjacentSvs(ploidy, left, right);
                    simplifications.add(chain);
                }
            }
        }
        return simplifications;
    }

    //endregion
    private void sanityCheck() {
        assert(startEdges.keySet().containsAll(endEdges.keySet()));
        assert(endEdges.keySet().containsAll(startEdges.keySet()));
        for (BgSegment segment : startEdges.keySet()) {
            for (BgAdjacency adj : startEdges.get(segment)) {
                assert(adj.fromOrientation() == -1);
                assert(adj.fromSegment() == segment);
                assert(startEdges.containsKey(adj.toSegment()));
                List<BgAdjacency> remoteAdjList;
                switch (adj.toOrientation()) {
                    case 1:
                        remoteAdjList = endEdges.get(adj.toSegment());
                        break;
                    case -1:
                        remoteAdjList = startEdges.get(adj.toSegment());
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid orientation");
                }
                assert (Iterables.any(remoteAdjList, remoteAdj -> remoteAdj.edge() == adj.edge()));
            }
        }
    }
    private static final Ordering<GenomePosition> ByGenomicPosition = new Ordering<GenomePosition>() {
        public int compare(GenomePosition o1, GenomePosition o2) {
            return ComparisonChain.start()
                    .compare(o1.chromosome(), o2.chromosome())
                    .compare(o1.position(), o2.position())
                    .result();
        }
    };
}
