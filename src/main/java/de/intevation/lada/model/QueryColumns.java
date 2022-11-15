/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model;

import de.intevation.lada.model.stammdaten.GridColConf;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.FetchType;

/**
 * Persistent class containing user column definitions, used for
 * executing Queries.
 */
public class QueryColumns {

    @Basic(fetch = FetchType.EAGER)
    private List<GridColConf> columns;

    public QueryColumns() { }

    public void setColumns(List<GridColConf> columns) {
        this.columns = columns;
    }

    public List<GridColConf> getColumns() {
        return this.columns;
    }
}
