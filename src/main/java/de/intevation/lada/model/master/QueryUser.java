/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(schema = SchemaName.NAME)
public class QueryUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String descr;

    @NotBlank
    private String name;

    private Integer ladaUserId;

    //uni-directional many-to-one association to Query
    private Integer baseQueryId;

    //bi-directional many-to-one association to QueryMessstelle
    @OneToMany(
        mappedBy = "queryUser",
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    private List<QueryMeasFacilMp> messStelles;

    @Transient
    private String[] messStellesIds;

    public QueryUser() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescr() {
        return this.descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLadaUserId() {
        return this.ladaUserId;
    }

    public void setLadaUserId(Integer userId) {
        this.ladaUserId = userId;
    }

    public Integer getBaseQueryId() {
        return this.baseQueryId;
    }

    public void setBaseQueryId(Integer query) {
        this.baseQueryId = query;
    }

    /**
     * Get all messstelle objects.
     * @return the messstelle objects
     */
    @JsonbTransient
    public List<QueryMeasFacilMp> getMessStelles() {
        if (this.messStelles == null) {
            this.messStelles = new ArrayList<QueryMeasFacilMp>();
        }
        return this.messStelles;
    }

    public void setMessStelles(List<QueryMeasFacilMp> messStelles) {
        this.messStelles = messStelles;
    }

    /**
     * Add a query messstelle object.
     * @param messStelle the query messstelle
     * @return the query messstelle obejct
     */
    public QueryMeasFacilMp addMessStelle(QueryMeasFacilMp messStelle) {
        getMessStelles().add(messStelle);
        messStelle.setQueryUser(this);

        return messStelle;
    }

    /**
     * Remove a query messstelle object.
     * @param messStelle the query messstelle
     * @return the query messstelle obejct
     */
    public QueryMeasFacilMp removeMessStelle(QueryMeasFacilMp messStelle) {
        getMessStelles().remove(messStelle);
        messStelle.setQueryUser(null);

        return messStelle;
    }

    /**
     * @return String[] IDs of referenced Messstellen
     */
    public String[] getMessStellesIds() {
        if (this.messStellesIds == null && this.messStelles != null) {
            List<String> ids = new ArrayList<>();
            for (QueryMeasFacilMp ms: this.messStelles) {
                ids.add(ms.getMeasFacilId());
            }
            this.messStellesIds = ids.toArray(new String[ids.size()]);
        }
        return this.messStellesIds;
    }

    public void setMessStellesIds(String[] messStellesIds) {
        this.messStellesIds = messStellesIds;
    }
}
