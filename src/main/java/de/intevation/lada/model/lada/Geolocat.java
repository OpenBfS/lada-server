/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.util.Date;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import de.intevation.lada.model.master.Poi;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.TypeRegulation;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.constraints.Unique;
import de.intevation.lada.validation.groups.DatabaseConstraints;


@Entity
@Table(schema = SchemaName.NAME)
@GroupSequence({ Geolocat.class, DatabaseConstraints.class })
@Unique(fields = {"typeRegulation", "sample", "site"},
    groups = DatabaseConstraints.class, clazz = Geolocat.class)
@Unique(fields = {"sample"},
    predicateFields = { "typeRegulation" },
    predicateValues = { "ANY (ARRAY['E', 'R'])" },
    propertyNodeName = "typeRegulation",
    message = "{de.intevation.lada.validation.GeolocatUniqueSamplingLocation}",
    groups = DatabaseConstraints.class, clazz = Geolocat.class)
public class Geolocat extends BelongsToSample implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class SiteToId implements JsonbAdapter<Site, Integer> {
        @PersistenceContext
        EntityManager em;

        @Override
        public Site adaptFromJson(Integer id) {
            if (em == null) {
                // Mock site when deserializing in client-side tests
                return new Site();
            }
            return em.find(Site.class, id);
        }

        @Override
        public Integer adaptToJson(Site site) {
            return site.getId();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    @JsonbProperty("siteId")
    @JsonbTypeAdapter(SiteToId.class)
    @Schema(implementation = Integer.class)
    @NotNull
    @ManyToOne
    private Site site;

    @NotBlank
    @Size(max = 1)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = TypeRegulation.class)
    private String typeRegulation;

    @Size(max = 100)
    @NotEmptyNorWhitespace
    private String addSiteText;

    @Size(max = 7)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Poi.class)
    private String poiId;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date treeMod;


    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

    public Site getSite() {
        return this.site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getTypeRegulation() {
        return this.typeRegulation;
    }

    public void setTypeRegulation(String typeRegulation) {
        this.typeRegulation = typeRegulation;
    }

    public String getAddSiteText() {
        return this.addSiteText;
    }

    public void setAddSiteText(String addSiteText) {
        this.addSiteText = addSiteText;
    }

    public String getPoiId() {
        return this.poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public Date getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Date treeMod) {
        this.treeMod = treeMod;
    }
}
