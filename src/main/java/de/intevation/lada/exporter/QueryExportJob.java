/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

import de.intevation.lada.data.requests.ExportParameters;
import de.intevation.lada.data.requests.QueryExportParameters;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.StatusProt_;
import de.intevation.lada.model.master.Filter;
import de.intevation.lada.model.master.FilterType;
import de.intevation.lada.model.master.FilterType_;
import de.intevation.lada.model.master.GridColConf;
import de.intevation.lada.model.master.GridColMp;
import de.intevation.lada.model.master.MeasUnit_;
import de.intevation.lada.model.master.StatusLev_;
import de.intevation.lada.model.master.StatusMp_;
import de.intevation.lada.model.master.StatusVal_;
import de.intevation.lada.query.QueryTools;
import de.intevation.lada.util.data.QueryBuilder;
import jakarta.persistence.TypedQuery;


/**
 * Abstract class for an export of query results.
 */
public abstract class QueryExportJob<T extends ExportParameters> extends ExportJob<T> {

    public static final String ID_TYPE_SAMPLE = "probeId";
    public static final String ID_TYPE_MEASM = "messungId";

    /**
     * Map of data types and the according sub data key.
     */
    public static final Map<String, String> ID_TYPE_TO_SUBDATA_KEY = Map.of(
        ID_TYPE_SAMPLE, "Messungen",
        ID_TYPE_MEASM, "messwerte");

    public static final String SUBDATA_MEASM_STATUS_MP = "statusMp";
    public static final String SUBDATA_MEASVAL_UNIT = "measUnitId";

    protected static final String PRIMARY_DATA_ID_PARAM = "primaryId";
    private static final String MEASMS_QUERY_TPL =
        "select %s from Measm m where m.sample.id = :" + PRIMARY_DATA_ID_PARAM;
    private static final String MEASVALS_QUERY_TPL =
        "select %s from MeasVal v where v.measm.id = :" + PRIMARY_DATA_ID_PARAM;

    private static final String STATUSMP_SUBQUERY = String.format("""
        (select s.%s.%s.%s || ' - ' || s.%1$s.%s.%s from m.%s s
         order by s.%s desc fetch first 1 rows only
        )""",
        StatusProt_.STATUS_MP,
        StatusMp_.STATUS_LEV, StatusLev_.LEV,
        StatusMp_.STATUS_VAL, StatusVal_.VAL,
        Measm_.STATUS_PROTS, StatusProt_.SEQ_NO);

    /**
     * True if subdata shall be fetched from the database and exported.
     */
    protected boolean exportSubdata;

    /**
     * Sub data column names to export.
     */
    protected List<String> subDataColumns;

    /**
     * Column containing the id.
     */
    protected String idColumn;

    /**
     * Identifier type.
     */
    protected String idType;

    /**
     * Query result.
     */
    protected List<GridColConf> columns;

    /**
     * Columns to use for export.
     */
    protected List<String> columnsToExport;

    /**
     * Date format to convert timestamps to (time zone defaults to UTC).
     */
    protected DateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Query id.
     */
    protected Integer qId;

    /**
     * Constructor.
     */
    public QueryExportJob() {
        columns = new ArrayList <GridColConf>();
        columnsToExport = new ArrayList<String>();

        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Creates an ID list filter for the given dataIndex.
     * @param dataIndex ID column name
     * @return Filter object
     */
    private Filter createIdListFilter(String dataIndex) {
        //Get Filter type from db
        QueryBuilder<FilterType> builder =
            repository.queryBuilder(FilterType.class);
        builder.and(FilterType_.type, "genericid");
        FilterType filterType =
            repository.filter(builder.getQuery()).get(0);

        //Create filter object
        Filter filter = new Filter();
        filter.setFilterType(filterType);
        filter.setParam(dataIndex);
        filter.setSql(String.format(
                "CAST(%1$s AS text) IN ( :%1$s )", dataIndex));
        return filter;
    }

    /**
     * Execute query to fetch export data and merge sub-data, if requested.
     * @return Query result, including sub-data, if requested.
     */
    protected Stream<Map<String, Object>> getExportData() {
        QueryTools queryTools = new QueryTools(repository, columns);

        Stream<Map<String, Object>> primaryDataStream =
            queryTools.getResultForQuery();
        if (!exportSubdata) {
            return primaryDataStream;
        }

        final String subDataQuery;
        switch (this.idType) {
        case ID_TYPE_SAMPLE:
            subDataQuery = String.format(MEASMS_QUERY_TPL,
                String.join(", ", this.subDataColumns)
                .replace(SUBDATA_MEASM_STATUS_MP, STATUSMP_SUBQUERY)
            );
            break;
        case ID_TYPE_MEASM:
            subDataQuery = String.format(MEASVALS_QUERY_TPL,
                String.join(", ", this.subDataColumns)
                .replace(SUBDATA_MEASVAL_UNIT,
                    MeasVal_.MEAS_UNIT + "." + MeasUnit_.UNIT_SYMBOL));
            break;
        default:
            throw new IllegalArgumentException(
                String.format("Unknown idType: %s", this.idType));
        }
        return mergeSubData(primaryDataStream, repository.entityManager()
            .createQuery(subDataQuery, Object[].class));
    }

    /**
     * Merge primary result and sub-data.
     *
     * @param primaryData The primary query result
     * @param subDataQuery Query to load sub-data for a set of primary data
     * @return Merged data
     */
    protected abstract Stream<Map<String, Object>> mergeSubData(
        Stream<Map<String, Object>> primaryData,
        TypedQuery<Object[]> subDataQuery
    );

    /**
     * Parse export parameters.
     */
    protected void parseExportParameters() {
        if (exportParameters == null
                || !(exportParameters instanceof QueryExportParameters)) {
            return;
        }
        QueryExportParameters queryExportParameters =
            (QueryExportParameters) this.exportParameters;

        //Get identifier type
        this.idColumn = queryExportParameters.getIdField();
        //Get target timezone
        if (queryExportParameters.getTimezone() != null) {
            this.dateFormat.setTimeZone(queryExportParameters.getTimezone());
        }

        //Get sub data columns if subdata shall be exported
        String[] subDataCols = queryExportParameters.getSubDataColumns();
        this.exportSubdata = subDataCols != null && subDataCols.length > 0;
        if (exportSubdata) {
            subDataColumns = Arrays.asList(subDataCols);
        }

        queryExportParameters.getColumns().forEach(column -> {
            GridColMp gridColumn = repository.getById(
                GridColMp.class, column.getGridColMpId());

            column.setGridColMp(gridColumn);

            //Check if the column contains the id
            if (column.getGridColMp().getDataIndex().equals(idColumn)) {
                // Get the column type
                this.idType = gridColumn.getDisp().getName();

                // Get IDs to filter result
                List<String> idsToExport = queryExportParameters.getIdFilter();

                if (idsToExport != null && idsToExport.size() > 0) {
                    // Prepare filtering by IDs
                    Filter filter = createIdListFilter(
                        gridColumn.getDataIndex());
                    gridColumn.setFilter(filter);
                    // TODO: This is a hack to avoid in transactional context:
                    //java.lang.IllegalStateException:
                    //org.hibernate.TransientPropertyValueException:
                    //object references an unsaved transient instance -
                    //save the transient instance before flushing :
                    //de.intevation.lada.model.stammdaten.GridColumn.filter
                    //-> de.intevation.lada.model.stammdaten.Filter
                    repository.entityManager().detach(gridColumn);

                    String filterValue = String.join(",", idsToExport);
                    column.setFilterVal(filterValue);
                    column.setIsFilterActive(true);
                    column.setIsFilterNull(false);
                    column.setIsFilterNegate(false);
                    column.setIsFilterRegex(false);
                }

            }
            columns.add(column);
            if (column.isExport()) {
                columnsToExport.add(column.getGridColMp().getDataIndex());
            }
        });

        if (columns.size() == 0 || columnsToExport.size() == 0) {
            throw new IllegalArgumentException("No columns to export given");
        }

        //Get query id
        GridColMp gridColumn = repository.getById(
            GridColMp.class,
            Integer.valueOf(columns.get(0).getGridColMpId())
        );
        qId = gridColumn.getBaseQueryId();
    }

    protected abstract Exporter<T> getExporter();

    @Override
    public void runWithTx() {
        parseExportParameters();

        try (Writer writer = createTmpFileWriter()) {
            getExporter().export(
                getExportData(),
                writer,
                this.exportParameters,
                this.columnsToExport,
                this.idType == null
                ? null
                : ID_TYPE_TO_SUBDATA_KEY.get(this.idType),
                this.qId,
                this.dateFormat,
                this.bundle);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
