/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import org.locationtech.jts.geom.MultiPolygon;

@Entity
@Table(schema = SchemaName.NAME)
public class AdminBorderView implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String municId;

    private Boolean isMunic;

    @Type(type = "jts_geometry")
    @Column(columnDefinition = "geometry(MultiPolygon, 4326)")
    private MultiPolygon shape;

    public AdminBorderView() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMunicId() {
        return this.municId;
    }

    public void setMunicId(String gemId) {
        this.municId = gemId;
    }

    public Boolean getIsMunic() {
        return this.isMunic;
    }

    public void setIsMunic(Boolean isMunic) {
        this.isMunic = isMunic;
    }

    public MultiPolygon getShape() {
        return this.shape;
    }

    public void setShape(MultiPolygon shape) {
        this.shape = shape;
    }

}
