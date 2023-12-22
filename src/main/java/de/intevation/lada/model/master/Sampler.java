/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;
import java.util.Date;

import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.groups.DatabaseConstraints;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Transient;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;



@Entity
@Table(schema = SchemaName.NAME)
@GroupSequence({ Sampler.class, DatabaseConstraints.class })
public class Sampler implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 25)
    private String editor;

    @Size(max = 60)
    private String comm;

    @Size(max = 80)
    private String inst;

    @Size(max = 80)
    @NotNull
    private String descr;

    @Size(max = 10)
    @NotNull
    private String shortText;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    @NotBlank
    @Size(max = 2)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Network.class)
    private String networkId;

    @Size(max = 20)
    private String city;

    @Size(max = 5)
    private String zip;

    @Size(max = 9)
    @NotNull
    private String extId;

    @Size(max = 30)
    private String street;

    @Size(max = 20)
    private String phone;

    @Size(max = 3)
    private String phoneMobile;

    @Email
    private String email;

    private String routePlanning;

    @Size(max = 1)
    private String type;

    @Transient
    private Integer referenceCount;

    @Transient
    private boolean readonly;

    public Sampler() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEditor() {
        return this.editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getComm() {
        return this.comm;
    }

    public void setComm(String comm) {
        this.comm = comm;
    }

    public String getInst() {
        return this.inst;
    }

    public void setInst(String inst) {
        this.inst = inst;
    }

    public String getDescr() {
        return this.descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getShortText() {
        return this.shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

    public String getNetworkId() {
        return this.networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return this.zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getExtId() {
        return this.extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getStreet() {
        return this.street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRoutePlanning() {
        return this.routePlanning;
    }

    public void setRoutePlanning(String routePlanning) {
        this.routePlanning = routePlanning;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getReferenceCount() {
        return this.referenceCount;
    }

    public void setReferenceCount(Integer referenceCount) {
        this.referenceCount = referenceCount;
    }

    public String getPhoneMobile() {
        return phoneMobile;
    }

    public void setPhoneMobile(String phoneMobile) {
        this.phoneMobile = phoneMobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the readonly
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * @param readonly the readonly to set
     */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

}
