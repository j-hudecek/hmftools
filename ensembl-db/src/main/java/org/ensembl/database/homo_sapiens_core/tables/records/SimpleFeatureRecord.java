/*
 * This file is generated by jOOQ.
*/
package org.ensembl.database.homo_sapiens_core.tables.records;


import javax.annotation.Generated;

import org.ensembl.database.homo_sapiens_core.tables.SimpleFeature;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SimpleFeatureRecord extends UpdatableRecordImpl<SimpleFeatureRecord> implements Record8<UInteger, UInteger, UInteger, UInteger, Byte, String, UShort, Double> {

    private static final long serialVersionUID = -1850059458;

    /**
     * Setter for <code>homo_sapiens_core_89_37.simple_feature.simple_feature_id</code>.
     */
    public void setSimpleFeatureId(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.simple_feature.simple_feature_id</code>.
     */
    public UInteger getSimpleFeatureId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.simple_feature.seq_region_id</code>.
     */
    public void setSeqRegionId(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.simple_feature.seq_region_id</code>.
     */
    public UInteger getSeqRegionId() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.simple_feature.seq_region_start</code>.
     */
    public void setSeqRegionStart(UInteger value) {
        set(2, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.simple_feature.seq_region_start</code>.
     */
    public UInteger getSeqRegionStart() {
        return (UInteger) get(2);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.simple_feature.seq_region_end</code>.
     */
    public void setSeqRegionEnd(UInteger value) {
        set(3, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.simple_feature.seq_region_end</code>.
     */
    public UInteger getSeqRegionEnd() {
        return (UInteger) get(3);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.simple_feature.seq_region_strand</code>.
     */
    public void setSeqRegionStrand(Byte value) {
        set(4, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.simple_feature.seq_region_strand</code>.
     */
    public Byte getSeqRegionStrand() {
        return (Byte) get(4);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.simple_feature.display_label</code>.
     */
    public void setDisplayLabel(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.simple_feature.display_label</code>.
     */
    public String getDisplayLabel() {
        return (String) get(5);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.simple_feature.analysis_id</code>.
     */
    public void setAnalysisId(UShort value) {
        set(6, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.simple_feature.analysis_id</code>.
     */
    public UShort getAnalysisId() {
        return (UShort) get(6);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.simple_feature.score</code>.
     */
    public void setScore(Double value) {
        set(7, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.simple_feature.score</code>.
     */
    public Double getScore() {
        return (Double) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<UInteger, UInteger, UInteger, UInteger, Byte, String, UShort, Double> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<UInteger, UInteger, UInteger, UInteger, Byte, String, UShort, Double> valuesRow() {
        return (Row8) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field1() {
        return SimpleFeature.SIMPLE_FEATURE.SIMPLE_FEATURE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field2() {
        return SimpleFeature.SIMPLE_FEATURE.SEQ_REGION_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field3() {
        return SimpleFeature.SIMPLE_FEATURE.SEQ_REGION_START;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field4() {
        return SimpleFeature.SIMPLE_FEATURE.SEQ_REGION_END;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Byte> field5() {
        return SimpleFeature.SIMPLE_FEATURE.SEQ_REGION_STRAND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return SimpleFeature.SIMPLE_FEATURE.DISPLAY_LABEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UShort> field7() {
        return SimpleFeature.SIMPLE_FEATURE.ANALYSIS_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Double> field8() {
        return SimpleFeature.SIMPLE_FEATURE.SCORE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value1() {
        return getSimpleFeatureId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value2() {
        return getSeqRegionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value3() {
        return getSeqRegionStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value4() {
        return getSeqRegionEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte value5() {
        return getSeqRegionStrand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getDisplayLabel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UShort value7() {
        return getAnalysisId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double value8() {
        return getScore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleFeatureRecord value1(UInteger value) {
        setSimpleFeatureId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleFeatureRecord value2(UInteger value) {
        setSeqRegionId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleFeatureRecord value3(UInteger value) {
        setSeqRegionStart(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleFeatureRecord value4(UInteger value) {
        setSeqRegionEnd(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleFeatureRecord value5(Byte value) {
        setSeqRegionStrand(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleFeatureRecord value6(String value) {
        setDisplayLabel(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleFeatureRecord value7(UShort value) {
        setAnalysisId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleFeatureRecord value8(Double value) {
        setScore(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleFeatureRecord values(UInteger value1, UInteger value2, UInteger value3, UInteger value4, Byte value5, String value6, UShort value7, Double value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached SimpleFeatureRecord
     */
    public SimpleFeatureRecord() {
        super(SimpleFeature.SIMPLE_FEATURE);
    }

    /**
     * Create a detached, initialised SimpleFeatureRecord
     */
    public SimpleFeatureRecord(UInteger simpleFeatureId, UInteger seqRegionId, UInteger seqRegionStart, UInteger seqRegionEnd, Byte seqRegionStrand, String displayLabel, UShort analysisId, Double score) {
        super(SimpleFeature.SIMPLE_FEATURE);

        set(0, simpleFeatureId);
        set(1, seqRegionId);
        set(2, seqRegionStart);
        set(3, seqRegionEnd);
        set(4, seqRegionStrand);
        set(5, displayLabel);
        set(6, analysisId);
        set(7, score);
    }
}
