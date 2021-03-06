package com.hartwig.hmftools.svanalysis.types;

import static java.lang.Math.abs;
import static java.lang.Math.round;

import static com.hartwig.hmftools.common.variant.structural.StructuralVariantType.DEL;
import static com.hartwig.hmftools.common.variant.structural.StructuralVariantType.DUP;
import static com.hartwig.hmftools.common.variant.structural.StructuralVariantType.INS;
import static com.hartwig.hmftools.svanalysis.annotators.FragileSiteAnnotator.NO_FS;
import static com.hartwig.hmftools.svanalysis.annotators.LineElementAnnotator.NO_LINE_ELEMENT;
import static com.hartwig.hmftools.svanalysis.types.SvLinkedPair.ASSEMBLY_MATCH_MATCHED;
import static com.hartwig.hmftools.svanalysis.types.SvLinkedPair.ASSEMBLY_MATCH_NONE;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.hmftools.common.variant.structural.EnrichedStructuralVariant;
import com.hartwig.hmftools.common.variant.structural.ImmutableStructuralVariantData;
import com.hartwig.hmftools.common.variant.structural.StructuralVariantData;
import com.hartwig.hmftools.common.variant.structural.StructuralVariantLeg;
import com.hartwig.hmftools.common.variant.structural.StructuralVariantType;

public class SvClusterData
{
    private final String mId; // sourced from either VCF or DB

    // full set of DB fields
    private final StructuralVariantData mSVData;
    private String mStartArm;
    private String mEndArm;
    private int mPonCount;
    private int mPonRegionCount; // allowing for a small buffer either side of a PON
    private String mStartFragileSite;
    private String mEndFragileSite;
    private String mStartLineElement;
    private String mEndLineElement;

    private String mAssemblyStartData;
    private String mAssemblyEndData;

    private boolean mDupBEStart;
    private boolean mDupBEEnd;

    private String mTransType;
    private int mTransLength;
    private String mTransSvLinks;

    private long mNearestSvDistance;
    private String mNearestSvRelation;

    private SvGeneData mStartGeneData;
    private SvGeneData mEndGeneData;

    private List<String> mStartTempInsertionAssemblies;
    private List<String> mStartDsbAssemblies;
    private List<String> mStartOtherAssemblies;
    private List<String> mEndTempInsertionAssemblies;
    private List<String> mEndDsbAssemblies;
    private List<String> mEndOtherAssemblies;
    private String mStartAssemblyMatchType;
    private String mEndAssemblyMatchType;
    private boolean mStartIsReplicatedLink;
    private boolean mEndIsReplicatedLink;
    private boolean mIsReplicatedSv;
    private final SvClusterData mReplicatedSv;

    public static String ASSEMBLY_TYPE_DSB = "dsb";
    public static String ASSEMBLY_TYPE_TI = "asm";
    public static String ASSEMBLY_TYPE_OTHER = "bpb";

    public static String RELATION_TYPE_NEIGHBOUR = "NHBR";
    public static String RELATION_TYPE_OVERLAP = "OVRL";

    // iterators for start and end data
    public static int SVI_START = 0;
    public static int SVI_END = 1;

    public SvClusterData(final StructuralVariantData svData)
    {
        mId = svData.id();
        mSVData = svData;
        mStartArm = "";
        mEndArm = "";
        mPonCount = 0;
        mPonRegionCount = 0;
        mStartFragileSite = NO_FS;
        mEndFragileSite = NO_FS;
        mStartLineElement = NO_LINE_ELEMENT;
        mEndLineElement = NO_LINE_ELEMENT;

        mNearestSvDistance = -1;
        mNearestSvRelation = "";

        setAssemblyData(mSVData.startLinkedBy(), mSVData.endLinkedBy());
        mIsReplicatedSv = false;
        mReplicatedSv = null;

        mDupBEStart = false;
        mDupBEEnd = false;

        mTransType = "";
        mTransLength = 0;
        mTransSvLinks = "";

        mStartGeneData = null;
        mEndGeneData = null;
    }

    public static SvClusterData from(final EnrichedStructuralVariant enrichedSV)
    {
        StructuralVariantData svData =
            ImmutableStructuralVariantData.builder()
                .id(enrichedSV.id())
                .startChromosome(enrichedSV.chromosome(true))
                .endChromosome(enrichedSV.chromosome(false))
                .startPosition(enrichedSV.position(true))
                .endPosition(enrichedSV.position(false))
                .startOrientation(enrichedSV.orientation(true))
                .endOrientation(enrichedSV.orientation(false))
                .startAF(enrichedSV.start().alleleFrequency())
                .adjustedStartAF(enrichedSV.start().adjustedAlleleFrequency())
                .adjustedStartCopyNumber(enrichedSV.start().adjustedCopyNumber())
                .adjustedStartCopyNumberChange(enrichedSV.start().adjustedCopyNumberChange())
                .endAF(enrichedSV.end().alleleFrequency())
                .adjustedEndAF(enrichedSV.end().adjustedAlleleFrequency())
                .adjustedEndCopyNumber(enrichedSV.end().adjustedCopyNumber())
                .adjustedEndCopyNumberChange(enrichedSV.end().adjustedCopyNumberChange())
                .ploidy(enrichedSV.ploidy())
                .type(enrichedSV.type())
                .build();

        return new SvClusterData(svData);
    }

    public SvClusterData(final SvClusterData other)
    {
        mId = other.getSvData().id() + "r";
        mSVData = other.getSvData();
        mStartArm = other.getStartArm();
        mEndArm = other.getEndArm();
        mPonCount = other.getPonCount();
        mPonRegionCount = other.getPonRegionCount();
        mStartFragileSite = other.isStartFragileSite();
        mEndFragileSite = other.isEndFragileSite();
        mStartLineElement = other.isStartLineElement();
        mEndLineElement = other.isEndLineElement();
        mNearestSvDistance = other.getNearestSvDistance();
        mNearestSvRelation = other.getNearestSvRelation();
        setAssemblyData(mSVData.startLinkedBy(), mSVData.endLinkedBy());
        mStartAssemblyMatchType = other.getAssemblyMatchType(true);
        mEndAssemblyMatchType = other.getAssemblyMatchType(false);
        mDupBEStart = other.isDupBEStart();
        mDupBEEnd = other.isDupBEEnd();
        mIsReplicatedSv = true;
        mReplicatedSv = other;
    }

    public final String id() { return mId; }
    public final StructuralVariantData getSvData() { return mSVData; }

    // for convenience
    public boolean equals(final SvClusterData other) { return id().equals(other.id()); }

    public final String chromosome(boolean isStart) { return isStart ? mSVData.startChromosome() : mSVData.endChromosome(); }
    public final long position(boolean isStart) { return isStart ? mSVData.startPosition() : mSVData.endPosition(); }
    public final byte orientation(boolean isStart){ return isStart ? mSVData.startOrientation() : mSVData.endOrientation(); }
    public final double copyNumber(boolean isStart){ return isStart ? mSVData.adjustedStartCopyNumber() : mSVData.adjustedEndCopyNumber(); }
    public final double copyNumberChange(boolean isStart){ return isStart ? mSVData.adjustedStartCopyNumberChange() : mSVData.adjustedEndCopyNumberChange(); }
    public final StructuralVariantType type() { return mSVData.type(); }

    public boolean isNullBreakend() { return mSVData.endChromosome().equals("0") && mSVData.endPosition() < 0; }

    public final String posId()
    {
        return String.format("id(%s) position(%s:%d:%d -> %s:%d:%d)",
                id(), chromosome(true), orientation(true), position(true),
                chromosome(false), orientation(false), position(false));
    }

    public final String posId(boolean useStart)
    {
        return String.format("%s: %s %s:%d:%d)",
                id(), useStart ? "start" :"end", chromosome(useStart), orientation(useStart), position(useStart));
    }

    public final String arm(boolean isStart) { return isStart ? mStartArm : mEndArm; }
    public final String getStartArm() { return mStartArm; }
    public final String getEndArm() { return mEndArm; }
    public void setChromosomalArms(final String start, final String end)
    {
        mStartArm = start;
        mEndArm = end;
    }

    public final long length()
    {
        if(type() == StructuralVariantType.BND || position(false) < 0)
            return 0;

        return abs(position(false) - position(true));
    }

    public boolean isReplicatedSv() { return mIsReplicatedSv; }
    public final SvClusterData getReplicatedSv() { return mReplicatedSv; }
    public boolean equals(final SvClusterData other, boolean allowReplicated)
    {
        if(this == other)
            return true;

        if(allowReplicated)
            return (this == other.getReplicatedSv() || mReplicatedSv == other);

        return false;
    }

    public int impliedCopyNumber(boolean useStart)
    {
        return (int)round(copyNumberChange(useStart));
    }

    public long getNearestSvDistance() { return mNearestSvDistance; }
    public void setNearestSvDistance(long distance) { mNearestSvDistance = distance; }
    public String getNearestSvRelation() { return mNearestSvRelation; }
    public void setNearestSvRelation(final String rel) { mNearestSvRelation = rel; }

    public void setPonCount(int count) { mPonCount = count; }
    public int getPonCount() { return mPonCount; }

    public void setPonRegionCount(int count) { mPonRegionCount = count; }
    public int getPonRegionCount() { return mPonRegionCount; }

    public void setFragileSites(String typeStart, String typeEnd) { mStartFragileSite = typeStart; mEndFragileSite = typeEnd; }
    public String isStartFragileSite() { return mStartFragileSite; }
    public String isEndFragileSite() { return mEndFragileSite; }

    public void setLineElements(String typeStart, String typeEnd) { mStartLineElement = typeStart; mEndLineElement = typeEnd; }
    public String isStartLineElement() { return mStartLineElement; }
    public String isEndLineElement() { return mEndLineElement; }

    public boolean isDupBEStart() { return mDupBEStart; }
    public boolean isDupBEEnd() { return mDupBEEnd; }
    public void setIsDupBEStart(boolean toggle) { mDupBEStart = toggle; }
    public void setIsDupBEEnd(boolean toggle) { mDupBEEnd = toggle; }

    public final SvGeneData getStartGeneData() { return mStartGeneData; }
    public final SvGeneData getEndGeneData() { return mEndGeneData; }

    public void setGeneData(final SvGeneData gd, boolean isStart)
    {
        if(isStart)
            mStartGeneData = gd;
        else
            mEndGeneData = gd;
    }

    public final String typeStr()
    {
        if(type() != StructuralVariantType.BND && mStartArm != mEndArm)
        {
            return "CRS";
        }
        else
        {
            return type().toString();
        }
    }

    public final boolean isLocal()
    {
        // means that both ends are within the same chromosomal arm
        return chromosome(true).equals(chromosome(false)) && mStartArm.equals(mEndArm);
    }

    public final boolean isSimpleType()
    {
        return (type() == DEL || type() == DUP || type() == INS);
    }

    public static boolean isStart(int svIter) { return svIter == SVI_START; }

    public String getTransType() { return mTransType; }
    public int getTransLength() { return mTransLength; }
    public String getTransSvLinks() { return mTransSvLinks; }

    public void setTransData(String transType, int transLength, final String transSvLinks)
    {
        mTransType = transType;
        mTransLength = transLength;
        mTransSvLinks = transSvLinks;
    }

    public String getAssemblyStart() { return mAssemblyStartData; }
    public String getAssemblyEnd() { return mAssemblyEndData; }

    public List<String> getTempInsertionAssemblies(boolean useStart) { return useStart ? mStartTempInsertionAssemblies : mEndTempInsertionAssemblies; }
    public List<String> getDsbAssemblies(boolean useStart) { return useStart ? mStartDsbAssemblies : mEndDsbAssemblies; }
    public List<String> getOtherAssemblies(boolean useStart) { return useStart ? mStartOtherAssemblies : mEndOtherAssemblies; }

    public final String getAssemblyMatchType(boolean useStart) { return useStart ? mStartAssemblyMatchType : mEndAssemblyMatchType; }
    public boolean isAssemblyMatched(boolean useStart) { return getAssemblyMatchType(useStart).equals(ASSEMBLY_MATCH_MATCHED); }

    public void setAssemblyMatchType(String type, boolean useStart)
    {
        if(useStart)
            mStartAssemblyMatchType = type;
        else
            mEndAssemblyMatchType = type;
    }

    private void setAssemblyData(final String assemblyStart, final String assemblyEnd)
    {
        mStartTempInsertionAssemblies = Lists.newArrayList();
        mStartDsbAssemblies = Lists.newArrayList();
        mStartOtherAssemblies = Lists.newArrayList();
        mEndTempInsertionAssemblies = Lists.newArrayList();
        mEndDsbAssemblies = Lists.newArrayList();
        mEndOtherAssemblies = Lists.newArrayList();
        mAssemblyStartData = "";
        mAssemblyEndData = "";
        mStartAssemblyMatchType = ASSEMBLY_MATCH_NONE;
        mEndAssemblyMatchType = ASSEMBLY_MATCH_NONE;
        mStartIsReplicatedLink = false;
        mEndIsReplicatedLink = false;

        if(!mSVData.startLinkedBy().isEmpty() && !mSVData.startLinkedBy().equals("."))
        {
            mAssemblyStartData = mSVData.startLinkedBy().replaceAll(",", ";");

            String[] assemblyList = mAssemblyStartData.split(";");

            for(int i = 0; i < assemblyList.length; ++i)
            {
                if(assemblyList[i].contains(ASSEMBLY_TYPE_TI))
                    mStartTempInsertionAssemblies.add(assemblyList[i]);
                else if(assemblyList[i].contains(ASSEMBLY_TYPE_DSB))
                    mStartDsbAssemblies.add(assemblyList[i]);
                else
                    mStartOtherAssemblies.add(assemblyList[i]);
            }
        }

        if(!mSVData.endLinkedBy().isEmpty() && !mSVData.endLinkedBy().equals("."))
        {
            mAssemblyEndData = mSVData.endLinkedBy().replaceAll(",", ";");

            String[] assemblyList = mAssemblyEndData.split(";");
            for(int i = 0; i < assemblyList.length; ++i)
            {
                if(assemblyList[i].contains(ASSEMBLY_TYPE_TI))
                    mEndTempInsertionAssemblies.add(assemblyList[i]);
                else if(assemblyList[i].contains(ASSEMBLY_TYPE_DSB))
                    mEndDsbAssemblies.add(assemblyList[i]);
                else
                    mEndOtherAssemblies.add(assemblyList[i]);
            }
        }
    }

    public boolean hasReplicatedLink() { return mStartIsReplicatedLink || mEndIsReplicatedLink; }
    public boolean isReplicatedLink(boolean useStart) { return useStart ? mStartIsReplicatedLink : mEndIsReplicatedLink; }
    public void setIsReplicatedLink(boolean useStart, boolean toggle)
    {
        if(useStart)
            mStartIsReplicatedLink = toggle;
        else
            mEndIsReplicatedLink = toggle;
    }

    public static boolean haveLinkedAssemblies(final SvClusterData var1, final SvClusterData var2, boolean v1Start, boolean v2Start)
    {
        for(String assemb1 : var1.getTempInsertionAssemblies(v1Start))
        {
            if(var2.getTempInsertionAssemblies(v2Start).contains(assemb1))
                return true;
        }

        return false;
    }

}
