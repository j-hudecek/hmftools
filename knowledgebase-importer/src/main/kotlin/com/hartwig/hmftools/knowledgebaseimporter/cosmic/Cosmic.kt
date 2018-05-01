package com.hartwig.hmftools.knowledgebaseimporter.cosmic

import com.hartwig.hmftools.knowledgebaseimporter.FusionPair
import com.hartwig.hmftools.knowledgebaseimporter.Knowledgebase
import com.hartwig.hmftools.knowledgebaseimporter.PromiscuousGene
import com.hartwig.hmftools.knowledgebaseimporter.output.ActionableCNVOutput
import com.hartwig.hmftools.knowledgebaseimporter.output.ActionableFusionOutput
import com.hartwig.hmftools.knowledgebaseimporter.output.ActionableVariantOutput
import com.hartwig.hmftools.knowledgebaseimporter.output.KnownVariantOutput
import com.hartwig.hmftools.knowledgebaseimporter.readCSVRecords
import org.apache.commons.csv.CSVRecord

class Cosmic(fusionsLocation: String) : Knowledgebase {

    override val knownVariants: List<KnownVariantOutput> = listOf()
    override val knownFusionPairs: List<FusionPair> by lazy { readCSVRecords(fusionsLocation) { readFusion(it) }.distinct() }
    override val promiscuousGenes: List<PromiscuousGene> = listOf()
    override val actionableVariants: List<ActionableVariantOutput> = listOf()
    override val actionableCNVs: List<ActionableCNVOutput> = listOf()
    override val actionableFusions: List<ActionableFusionOutput> = listOf()

    private fun readFusion(csvRecord: CSVRecord): FusionPair {
        val fiveGene = csvRecord["5' Partner"].split("_").first()
        val threeGene = csvRecord["3' Partner"].split("_").first()
        return FusionPair(fiveGene, threeGene)
    }
}