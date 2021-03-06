/*
 * This file is generated by jOOQ.
*/
package org.ensembl.database.homo_sapiens_core.tables.records;


import javax.annotation.Generated;

import org.ensembl.database.homo_sapiens_core.tables.AttribType;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
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
public class AttribTypeRecord extends UpdatableRecordImpl<AttribTypeRecord> implements Record4<UShort, String, String, String> {

    private static final long serialVersionUID = 1043022815;

    /**
     * Setter for <code>homo_sapiens_core_89_37.attrib_type.attrib_type_id</code>.
     */
    public void setAttribTypeId(UShort value) {
        set(0, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.attrib_type.attrib_type_id</code>.
     */
    public UShort getAttribTypeId() {
        return (UShort) get(0);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.attrib_type.code</code>.
     */
    public void setCode(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.attrib_type.code</code>.
     */
    public String getCode() {
        return (String) get(1);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.attrib_type.name</code>.
     */
    public void setName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.attrib_type.name</code>.
     */
    public String getName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.attrib_type.description</code>.
     */
    public void setDescription(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.attrib_type.description</code>.
     */
    public String getDescription() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<UShort> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<UShort, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<UShort, String, String, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UShort> field1() {
        return AttribType.ATTRIB_TYPE.ATTRIB_TYPE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return AttribType.ATTRIB_TYPE.CODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return AttribType.ATTRIB_TYPE.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return AttribType.ATTRIB_TYPE.DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UShort value1() {
        return getAttribTypeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttribTypeRecord value1(UShort value) {
        setAttribTypeId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttribTypeRecord value2(String value) {
        setCode(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttribTypeRecord value3(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttribTypeRecord value4(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttribTypeRecord values(UShort value1, String value2, String value3, String value4) {
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
     * Create a detached AttribTypeRecord
     */
    public AttribTypeRecord() {
        super(AttribType.ATTRIB_TYPE);
    }

    /**
     * Create a detached, initialised AttribTypeRecord
     */
    public AttribTypeRecord(UShort attribTypeId, String code, String name, String description) {
        super(AttribType.ATTRIB_TYPE);

        set(0, attribTypeId);
        set(1, code);
        set(2, name);
        set(3, description);
    }
}
