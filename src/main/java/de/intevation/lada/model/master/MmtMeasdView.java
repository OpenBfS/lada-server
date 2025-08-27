/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import org.hibernate.annotations.processing.CheckHQL;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = Names.SCHEMA_NAME)
@CheckHQL
@NamedQuery(name = Names.QUERY_GET_MEASD_FOR_MMT, query = """
    select m from Measd m join MmtMeasdView on m.id = measdId
    where mmtId = :mmt""")
public class MmtMeasdView implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private MmtMeasdViewPK id;

    @Column(insertable = false, updatable = false)
    private String measdId;

    @Column(insertable = false, updatable = false)
    private String mmtId;

    public MmtMeasdView() {
    }

    public MmtMeasdViewPK getMmtMessgroessePK() {
        return this.id;
    }

    public void setMmtMessgroessePK(MmtMeasdViewPK i) {
        this.id = i;
    }

    public String getMeasdId() {
        return this.measdId;
    }

    public void setMeasdId(String measdId) {
        this.measdId = measdId;
    }

    public String getMmtId() {
        return this.mmtId;
    }

    public void setMmtId(String mmtId) {
        this.mmtId = mmtId;
    }

}
