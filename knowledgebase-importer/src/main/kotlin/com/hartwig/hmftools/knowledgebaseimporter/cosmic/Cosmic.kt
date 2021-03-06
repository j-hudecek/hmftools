package com.hartwig.hmftools.knowledgebaseimporter.cosmic

import com.hartwig.hmftools.knowledgebaseimporter.Knowledgebase
import com.hartwig.hmftools.knowledgebaseimporter.diseaseOntology.Doid
import com.hartwig.hmftools.knowledgebaseimporter.output.*
import com.hartwig.hmftools.knowledgebaseimporter.readCSVRecords
import org.apache.commons.csv.CSVRecord

class Cosmic(fusionsLocation: String) : Knowledgebase {
    override val source = "cosmic"
    override val knownVariants: List<KnownVariantOutput> = listOf()
    override val knownFusionPairs by lazy { readCSVRecords(fusionsLocation) { readFusion(it) }.distinct() }
    override val promiscuousGenes: List<PromiscuousGene> = listOf()
    override val actionableVariants: List<ActionableVariantOutput> = listOf()
    override val actionableCNVs: List<ActionableCNVOutput> = listOf()
    override val actionableFusionPairs: List<ActionableFusionPairOutput> = listOf()
    override val actionablePromiscuousGenes: List<ActionablePromiscuousGeneOutput> = listOf()
    override val actionableRanges: List<ActionableGenomicRangeOutput> = listOf()
    override val cancerTypes: Map<String, Set<Doid>> = mapOf()

    private fun readFusion(csvRecord: CSVRecord): FusionPair {
        val fiveGene = csvRecord["5' Partner"].split("_").first()
        val threeGene = csvRecord["3' Partner"].split("_").first()
        return FusionPair(fiveGene, threeGene)
    }
}
