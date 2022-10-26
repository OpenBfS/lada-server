/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.land;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the tagzuordnung database table.
 */
@Entity
@Table(name = "tagzuordnung", schema = SchemaName.LEGACY_NAME)
public class TagZuordnung {
    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "probe_id")
    private Integer probeId;

    @Column(name = "messung_id")
    private Integer messungId;

    @Column(name = "tag_id")
    private Integer tagId;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMessungId() {
        return this.messungId;
    }

    public void setMessungId(Integer messungId) {
        this.messungId = messungId;
    }

    public Integer getProbeId() {
        return this.probeId;
    }

    public void setProbeId(Integer probe) {
        this.probeId = probe;
    }

    /**
     * @return ID of the referenced tag
     */
    public Integer getTagId() {
        return this.tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }
}
