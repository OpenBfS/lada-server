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
import java.util.Set;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.constraints.Unique;
import de.intevation.lada.validation.groups.DatabaseConstraints;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Transient;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;



@Entity
@Table(schema = SchemaName.NAME)
@GroupSequence({ Sampler.class, DatabaseConstraints.class })
@Unique(groups = DatabaseConstraints.class,
    clazz = Sampler.class, fields = { "extId", "networkId" })
public class Sampler extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 25)
    @NotEmptyNorWhitespace
    private String editor;

    @Size(max = 60)
    @NotEmptyNorWhitespace
    private String comm;

    @Size(max = 80)
    @NotEmptyNorWhitespace
    private String inst;

    @Size(max = 80)
    @NotBlank
    private String descr;

    @Size(max = 10)
    @NotBlank
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
    @NotEmptyNorWhitespace
    private String city;

    @Size(max = 5)
    @NotEmptyNorWhitespace
    private String zip;

    @Size(max = 9)
    @NotBlank
    private String extId;

    @Size(max = 30)
    @NotEmptyNorWhitespace
    private String street;

    @Size(max = 20)
    @NotEmptyNorWhitespace
    private String phone;

    @Size(max = 3)
    @NotEmptyNorWhitespace
    private String phoneMobile;

    @Email
    @NotEmptyNorWhitespace
    private String email;

    @NotEmptyNorWhitespace
    private String routePlanning;

    @Size(max = 1)
    @NotEmptyNorWhitespace
    private String type;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "sampler_id", insertable = false, updatable = false)
    private Set<Sample> samples;

    @Transient
    private int referenceCount;

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

    /**
     * @return The number of Sample objects referencing this sampler.
     */
    public int getReferenceCount() {
        if (this.samples != null) {
            return this.samples.size();
        }
        return 0;
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
}
