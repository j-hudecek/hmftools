/*
 * This file is generated by jOOQ.
*/
package org.ensembl.database.homo_sapiens_core.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.ensembl.database.homo_sapiens_core.HomoSapiensCore_89_37;
import org.ensembl.database.homo_sapiens_core.Keys;
import org.ensembl.database.homo_sapiens_core.tables.records.AnalysisDescriptionRecord;
import org.jooq.Field;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;
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
public class AnalysisDescription extends TableImpl<AnalysisDescriptionRecord> {

    private static final long serialVersionUID = 2003394097;

    /**
     * The reference instance of <code>homo_sapiens_core_89_37.analysis_description</code>
     */
    public static final AnalysisDescription ANALYSIS_DESCRIPTION = new AnalysisDescription();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AnalysisDescriptionRecord> getRecordType() {
        return AnalysisDescriptionRecord.class;
    }

    /**
     * The column <code>homo_sapiens_core_89_37.analysis_description.analysis_id</code>.
     */
    public final TableField<AnalysisDescriptionRecord, UShort> ANALYSIS_ID = createField("analysis_id", org.jooq.impl.SQLDataType.SMALLINTUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>homo_sapiens_core_89_37.analysis_description.description</code>.
     */
    public final TableField<AnalysisDescriptionRecord, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>homo_sapiens_core_89_37.analysis_description.display_label</code>.
     */
    public final TableField<AnalysisDescriptionRecord, String> DISPLAY_LABEL = createField("display_label", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

    /**
     * The column <code>homo_sapiens_core_89_37.analysis_description.displayable</code>.
     */
    public final TableField<AnalysisDescriptionRecord, Byte> DISPLAYABLE = createField("displayable", org.jooq.impl.SQLDataType.TINYINT.nullable(false).defaultValue(org.jooq.impl.DSL.inline("1", org.jooq.impl.SQLDataType.TINYINT)), this, "");

    /**
     * The column <code>homo_sapiens_core_89_37.analysis_description.web_data</code>.
     */
    public final TableField<AnalysisDescriptionRecord, String> WEB_DATA = createField("web_data", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>homo_sapiens_core_89_37.analysis_description</code> table reference
     */
    public AnalysisDescription() {
        this("analysis_description", null);
    }

    /**
     * Create an aliased <code>homo_sapiens_core_89_37.analysis_description</code> table reference
     */
    public AnalysisDescription(String alias) {
        this(alias, ANALYSIS_DESCRIPTION);
    }

    private AnalysisDescription(String alias, Table<AnalysisDescriptionRecord> aliased) {
        this(alias, aliased, null);
    }

    private AnalysisDescription(String alias, Table<AnalysisDescriptionRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return HomoSapiensCore_89_37.HOMO_SAPIENS_CORE_89_37;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<AnalysisDescriptionRecord>> getKeys() {
        return Arrays.<UniqueKey<AnalysisDescriptionRecord>>asList(Keys.KEY_ANALYSIS_DESCRIPTION_ANALYSIS_IDX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnalysisDescription as(String alias) {
        return new AnalysisDescription(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public AnalysisDescription rename(String name) {
        return new AnalysisDescription(name, null);
    }
}
