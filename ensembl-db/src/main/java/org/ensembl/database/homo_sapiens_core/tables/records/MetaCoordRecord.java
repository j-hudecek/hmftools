/*
 * This file is generated by jOOQ.
*/
package org.ensembl.database.homo_sapiens_core.tables.records;


import javax.annotation.Generated;

import org.ensembl.database.homo_sapiens_core.tables.MetaCoord;
import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.TableRecordImpl;
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
public class MetaCoordRecord extends TableRecordImpl<MetaCoordRecord> implements Record3<String, UInteger, Integer> {

    private static final long serialVersionUID = 1037559700;

    /**
     * Setter for <code>homo_sapiens_core_89_37.meta_coord.table_name</code>.
     */
    public void setTableName(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.meta_coord.table_name</code>.
     */
    public String getTableName() {
        return (String) get(0);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.meta_coord.coord_system_id</code>.
     */
    public void setCoordSystemId(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.meta_coord.coord_system_id</code>.
     */
    public UInteger getCoordSystemId() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>homo_sapiens_core_89_37.meta_coord.max_length</code>.
     */
    public void setMaxLength(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>homo_sapiens_core_89_37.meta_coord.max_length</code>.
     */
    public Integer getMaxLength() {
        return (Integer) get(2);
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<String, UInteger, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<String, UInteger, Integer> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return MetaCoord.META_COORD.TABLE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field2() {
        return MetaCoord.META_COORD.COORD_SYSTEM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return MetaCoord.META_COORD.MAX_LENGTH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getTableName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value2() {
        return getCoordSystemId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getMaxLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaCoordRecord value1(String value) {
        setTableName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaCoordRecord value2(UInteger value) {
        setCoordSystemId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaCoordRecord value3(Integer value) {
        setMaxLength(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaCoordRecord values(String value1, UInteger value2, Integer value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MetaCoordRecord
     */
    public MetaCoordRecord() {
        super(MetaCoord.META_COORD);
    }

    /**
     * Create a detached, initialised MetaCoordRecord
     */
    public MetaCoordRecord(String tableName, UInteger coordSystemId, Integer maxLength) {
        super(MetaCoord.META_COORD);

        set(0, tableName);
        set(1, coordSystemId);
        set(2, maxLength);
    }
}