package com.hartwig.hmftools.knowledgebaseimporter.cgi

import com.hartwig.hmftools.extensions.csv.CsvReader
import com.hartwig.hmftools.knowledgebaseimporter.Knowledgebase
import com.hartwig.hmftools.knowledgebaseimporter.cgi.input.CgiActionableInput
import com.hartwig.hmftools.knowledgebaseimporter.cgi.input.CgiKnownInput
import com.hartwig.hmftools.knowledgebaseimporter.diseaseOntology.DiseaseOntology
import com.hartwig.hmftools.knowledgebaseimporter.knowledgebases.KnowledgebaseSource
import com.hartwig.hmftools.knowledgebaseimporter.knowledgebases.RecordAnalyzer
import com.hartwig.hmftools.knowledgebaseimporter.output.*

class Cgi(variantsLocation: String, biomarkersLocation: String, diseaseOntology: DiseaseOntology,
          private val recordAnalyzer: RecordAnalyzer, treatmentTypeMap: Map<String, String>) :
        Knowledgebase, KnowledgebaseSource<CgiKnownKbRecord, CgiActionableRecord> {

    override val source = "cgi"
    override val knownVariants by lazy { recordAnalyzer.knownVariants(listOf(this)).distinct() }
    override val knownFusionPairs by lazy { actionableKbRecords.flatMap { it.events }.filterIsInstance<FusionPair>().distinct() }
    override val promiscuousGenes by lazy { actionableKbRecords.flatMap { it.events }.filterIsInstance<PromiscuousGene>().distinct() }
    override val actionableVariants by lazy { actionableKbItems.filterIsInstance<ActionableVariantOutput>() }
    override val actionableCNVs by lazy { actionableKbItems.filterIsInstance<ActionableCNVOutput>() }
    override val actionableFusionPairs by lazy { actionableKbItems.filterIsInstance<ActionableFusionPairOutput>() }
    override val actionablePromiscuousGenes by lazy { actionableKbItems.filterIsInstance<ActionablePromiscuousGeneOutput>() }
    override val actionableRanges by lazy { actionableKbItems.filterIsInstance<ActionableGenomicRangeOutput>() }
    override val cancerTypes by lazy {
        actionableKbRecords.flatMap { it.actionability }.map { it.cancerType }
                .associateBy({ it }, { diseaseOntology.findDoids(it) })
    }
    override val knownKbRecords by lazy {
        CsvReader.readTSVByName<CgiKnownInput>(variantsLocation, nullString = "").mapNotNull { it.corrected() }.map { CgiKnownKbRecord(it) }
    }
    override val actionableKbRecords by lazy {
        CsvReader.readTSVByName<CgiActionableInput>(biomarkersLocation, nullString = "").mapNotNull { it.corrected() }
                .map { CgiActionableRecord(it, treatmentTypeMap) }
    }
    private val actionableKbItems by lazy { recordAnalyzer.actionableItems(listOf(this)).distinct() }
}
