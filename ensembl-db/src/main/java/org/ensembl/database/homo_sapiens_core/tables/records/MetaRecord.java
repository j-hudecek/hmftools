/*
 * This file is generated by jOOQ.
*/
package org.ensembl.database.homo_sapiens_core.tables.records;


import javax.annotation.Generated;

import org.ensembl.database.homo_sapiens_core.tables.Meta;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;


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
public class MetaRecord extends UpdatableRecordImpl<MetaRecord> implements Record4<Integer, UInteger, String, String> {

    private static final long serialVersionUID = -1814078129;

    /**
     * Setter for <code>homo_sapiens_core_89_37.meta.meta_id</code>.
     */
    public void setMetaId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.meta.meta_id</code>.
     */
    public Integer getMetaId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.meta.species_id</code>.
     */
    public void setSpeciesId(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.meta.species_id</code>.
     */
    public UInteger getSpeciesId() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.meta.meta_key</code>.
     */
    public void setMetaKey(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.meta.meta_key</code>.
     */
    public String getMetaKey() {
        return (String) get(2);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.meta.meta_value</code>.
     */
    public void setMetaValue(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.meta.meta_value</code>.
     */
    public String getMetaValue() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Integer, UInteger, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Integer, UInteger, String, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return Meta.META.META_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field2() {
        return Meta.META.SPECIES_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Meta.META.META_KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Meta.META.META_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getMetaId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value2() {
        return getSpeciesId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getMetaKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getMetaValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaRecord value1(Integer value) {
        setMetaId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaRecord value2(UInteger value) {
        setSpeciesId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaRecord value3(String value) {
        setMetaKey(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaRecord value4(String value) {
        setMetaValue(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaRecord values(Integer value1, UInteger value2, String value3, String value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MetaRecord
     */
    public MetaRecord() {
        super(Meta.META);
    }

    /**
     * Create a detached, initialised MetaRecord
     */
    public MetaRecord(Integer metaId, UInteger speciesId, String metaKey, String metaValue) {
        super(Meta.META);

        set(0, metaId);
        set(1, speciesId);
        set(2, metaKey);
        set(3, metaValue);
    }
}
