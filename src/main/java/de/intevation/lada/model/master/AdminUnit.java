/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.locationtech.jts.geom.Point;

@Entity
@Table(schema = SchemaName.NAME)
public class AdminUnit implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String name;

    private String stateId;

    private Boolean isState;

    private Boolean isMunic;

    private Boolean isRuralDist;

    private Boolean isGovDist;

    private String ruralDistId;

    private String zip;

    private String govDistId;

    @Column(columnDefinition = "geometry(Point, 4326)")
    @JsonbTransient
    private Point geomCenter;

    public AdminUnit() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStateId() {
        return this.stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public Boolean getIsState() {
        return this.isState;
    }

    public void setIsState(Boolean isState) {
        this.isState = isState;
    }

    public Boolean getIsMunic() {
        return this.isMunic;
    }

    public void setIsMunic(Boolean isMunic) {
        this.isMunic = isMunic;
    }

    public Boolean getIsRuralDist() {
        return this.isRuralDist;
    }

    public void setIsRuralDist(Boolean isRuralDist) {
        this.isRuralDist = isRuralDist;
    }

    public Boolean getIsGovDist() {
        return this.isGovDist;
    }

    public void setIsGovDist(Boolean isGovDist) {
        this.isGovDist = isGovDist;
    }

    public String getRuralDistId() {
        return this.ruralDistId;
    }

    public void setRuralDistId(String ruralDistId) {
        this.ruralDistId = ruralDistId;
    }

    public Double getLatitude() {
        return this.geomCenter != null
            ? this.geomCenter.getY()
            : null;
    }

    public Double getLongitude() {
        return this.geomCenter != null
            ? this.geomCenter.getX()
            : null;
    }

    public String getZip() {
        return this.zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getGovDistId() {
        return this.govDistId;
    }

    public void setGovDistId(String govDistId) {
        this.govDistId = govDistId;
    }

    public Point getGeomCenter() {
        return geomCenter;
    }

    public void setGeomCenter(Point geomCenter) {
        this.geomCenter = geomCenter;
    }

}
