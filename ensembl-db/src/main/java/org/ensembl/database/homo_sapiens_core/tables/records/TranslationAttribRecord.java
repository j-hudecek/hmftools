/*
 * This file is generated by jOOQ.
*/
package org.ensembl.database.homo_sapiens_core.tables.records;


import javax.annotation.Generated;

import org.ensembl.database.homo_sapiens_core.tables.TranslationAttrib;
import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.TableRecordImpl;
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
public class TranslationAttribRecord extends TableRecordImpl<TranslationAttribRecord> implements Record3<UInteger, UShort, String> {

    private static final long serialVersionUID = 837127470;

    /**
     * Setter for <code>homo_sapiens_core_89_37.translation_attrib.translation_id</code>.
     */
    public void setTranslationId(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.translation_attrib.translation_id</code>.
     */
    public UInteger getTranslationId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.translation_attrib.attrib_type_id</code>.
     */
    public void setAttribTypeId(UShort value) {
        set(1, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.translation_attrib.attrib_type_id</code>.
     */
    public UShort getAttribTypeId() {
        return (UShort) get(1);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.translation_attrib.value</code>.
     */
    public void setValue(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.translation_attrib.value</code>.
     */
    public String getValue() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<UInteger, UShort, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<UInteger, UShort, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field1() {
        return TranslationAttrib.TRANSLATION_ATTRIB.TRANSLATION_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UShort> field2() {
        return TranslationAttrib.TRANSLATION_ATTRIB.ATTRIB_TYPE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return TranslationAttrib.TRANSLATION_ATTRIB.VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value1() {
        return getTranslationId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UShort value2() {
        return getAttribTypeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TranslationAttribRecord value1(UInteger value) {
        setTranslationId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TranslationAttribRecord value2(UShort value) {
        setAttribTypeId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TranslationAttribRecord value3(String value) {
        setValue(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TranslationAttribRecord values(UInteger value1, UShort value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TranslationAttribRecord
     */
    public TranslationAttribRecord() {
        super(TranslationAttrib.TRANSLATION_ATTRIB);
    }

    /**
     * Create a detached, initialised TranslationAttribRecord
     */
    public TranslationAttribRecord(UInteger translationId, UShort attribTypeId, String value) {
        super(TranslationAttrib.TRANSLATION_ATTRIB);

        set(0, translationId);
        set(1, attribTypeId);
        set(2, value);
    }
}
