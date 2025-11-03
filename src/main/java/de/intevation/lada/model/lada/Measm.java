/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Transient;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.processing.CheckHQL;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.master.Mmt;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.validation.constraints.HasMeasmStartDateRegulation1;
import de.intevation.lada.validation.constraints.HasMeasmStartDateRegulationNot1;
import de.intevation.lada.validation.constraints.HasMeasPdNotSampleMeth9OrRegulation1;
import de.intevation.lada.validation.constraints.HasMeasPdSampleMeth9OrRegulation1;
import de.intevation.lada.validation.constraints.HasObligMeasds;
import de.intevation.lada.validation.constraints.Immutable;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.MeasuringAfterSampling;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.constraints.Unique;
import de.intevation.lada.validation.groups.CreateErrors;
import de.intevation.lada.validation.groups.DatabaseConstraints;
import de.intevation.lada.validation.groups.Notifications;
import de.intevation.lada.validation.groups.Warnings;


// The DynamicInsert Annotation has the effect, that the persisted object still
// has all the "null"-values. There is no reloading after the persistence
// process!
@Entity
@DynamicInsert(true)
@Table(schema = Names.SCHEMA_NAME)
@GroupSequence({ Measm.class, DatabaseConstraints.class })
@Unique(groups = DatabaseConstraints.class,
    clazz = Measm.class, fields = { "minSampleId", "sample" })
@Immutable(groups = DatabaseConstraints.class,
    clazz = Measm.class, fields = Measm_.EXT_ID)
@MeasuringAfterSampling(groups = Warnings.class)
@HasMeasPdNotSampleMeth9OrRegulation1(groups = Warnings.class)
@HasMeasPdSampleMeth9OrRegulation1(groups = Notifications.class)
@HasMeasmStartDateRegulation1(groups = Warnings.class)
@HasMeasmStartDateRegulationNot1(groups = Notifications.class)
@HasObligMeasds(groups = Notifications.class)
@CheckHQL
@NamedQuery(name = Names.QUERY_DELETE_MEAS_VALS,
    query = "delete from MeasVal where measm = :m")
public class Measm extends BelongsToSample
    implements Taggable<TagLinkMeasm>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Boolean isCompleted;

    private Boolean isScheduled;

    @Null(groups = CreateErrors.class,
        message = "{de.intevation.lada.validation.ReadOnlyField.message}")
    @Column(insertable = false, updatable = false)
    private Integer extId;

    private Integer measPd;

    @Temporal(TIMESTAMP)
    @PastOrPresent(groups = Warnings.class)
    private Date measmStartDate;

    @NotBlank
    @Size(max = 2)
    @IsValidPrimaryKey(groups = DatabaseConstraints.class, clazz = Mmt.class)
    private String mmtId;

    @Size(max = 4)
    @NotEmptyNorWhitespace
    @NotBlank(groups = Notifications.class)
    private String minSampleId;

    /**
     * Latest StatusProt entry
     */
    @Transient
    private StatusProt statusProt;

    @OneToMany(mappedBy = StatusProt_.MEASM,
        cascade = CascadeType.REMOVE,
        fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<StatusProt> statusProts;

    @OneToMany(mappedBy = CommMeasm_.MEASM,
        cascade = CascadeType.REMOVE,
        fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<CommMeasm> commMeasms;

    @OneToMany(mappedBy = MeasVal_.MEASM,
        cascade = CascadeType.REMOVE,
        fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<MeasVal> measVals;

    /* Work around the fact that hibernate does not provide means to have
    a ManyToMany association without cascading to the link table */
    @OneToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE,
            CascadeType.REFRESH},
        fetch = FetchType.EAGER)
    @JoinColumn(name = "measm_id", insertable = false, updatable = false)
    @JsonbTransient
    private Set<TagLinkMeasm> tagLinks = new HashSet<>();
    @Transient
    private List<Tag> tags;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date treeMod;

    @Transient
    private Boolean statusEdit;

    @Transient
    private Boolean statusEditMst;

    @Transient
    private Boolean statusEditLand;

    @Transient
    private Boolean statusEditLst;


    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsCompleted() {
        return this.isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Boolean getIsScheduled() {
        return this.isScheduled;
    }

    public void setIsScheduled(Boolean isScheduled) {
        this.isScheduled = isScheduled;
    }

    public Integer getExtId() {
        return this.extId;
    }

    public void setExtId(Integer extId) {
        this.extId = extId;
    }

    public Integer getMeasPd() {
        return this.measPd;
    }

    public void setMeasPd(Integer measPd) {
        this.measPd = measPd;
    }

    public Date getMeasmStartDate() {
        return this.measmStartDate;
    }

    public void setMeasmStartDate(Date measmStartDate) {
        this.measmStartDate = measmStartDate;
    }

    public String getMmtId() {
        return this.mmtId;
    }

    public void setMmtId(String mmtId) {
        this.mmtId = mmtId;
    }

    public String getMinSampleId() {
        return this.minSampleId;
    }

    public void setMinSampleId(String minSampleId) {
        this.minSampleId = minSampleId;
    }

    public Date getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Date treeMod) {
        this.treeMod = treeMod;
    }

    /**
     * @return the statusEdit
     */
    public Boolean getStatusEdit() {
        return statusEdit;
    }
    public Boolean getStatusEditMst() {
        return statusEditMst;
    }
    public Boolean getStatusEditLand() {
        return statusEditLand;
    }
    public Boolean getStatusEditLst() {
        return statusEditLst;
    }

    /**
     * @param statusEdit the statusEdit to set
     */
    public void setStatusEdit(Boolean statusEdit) {
        this.statusEdit = statusEdit;
    }
    public void setStatusEditMst(Boolean statusEditMst) {
        this.statusEditMst = statusEditMst;
    }
    public void setStatusEditLand(Boolean statusEditLand) {
        this.statusEditLand = statusEditLand;
    }
    public void setStatusEditLst(Boolean statusEditLst) {
        this.statusEditLst = statusEditLst;
    }

    public StatusProt getStatusProt() {
        if (this.statusProt == null && this.statusProts != null) {
            this.statusProt = this.statusProts.stream().sorted(
                new Comparator<StatusProt>() {
                    @Override
                    public int compare(StatusProt arg0, StatusProt arg1) {
                        Date date0 = arg0.getDate();
                        Date date1 = arg1.getDate();
                        if (date0 == null || date1 == null) {
                            return 0;
                        }

                        int isAfter = - date0.compareTo(date1);
                        if (isAfter != 0) {
                            return isAfter;
                        }

                        // Try breaking ties using statusVal
                        Integer statusValId0 = arg0.getStatusValId();
                        Integer statusValId1 = arg1.getStatusValId();
                        if (statusValId0 == null || statusValId1 == null) {
                            return 0;
                        }
                        return statusValId0.compareTo(statusValId1);
                    }
                }).findFirst().orElse(null);
        }
        return this.statusProt;
    }

    public List<StatusProt> getStatusProts() {
        return this.statusProts;
    }

    public void setStatusProts(List<StatusProt> statusProts) {
        this.statusProts = statusProts;
    }

    public List<CommMeasm> getCommMeasms() {
        return this.commMeasms;
    }

    public void setCommMeasms(List<CommMeasm> commMeasms) {
        this.commMeasms = commMeasms;
    }

    public List<MeasVal> getMeasVals() {
        return this.measVals;
    }

    public void setMeasVals(List<MeasVal> measVals) {
        this.measVals = measVals;
    }

    @Override
    public Set<TagLinkMeasm> getTagLinks() {
        return this.tagLinks;
    }

    @Override
    public List<Tag> getTags() {
        if (this.tags == null && this.tagLinks != null) {
            this.tags = new ArrayList<>(
                this.tagLinks.stream().map(link -> link.tag).toList());
        }
        return this.tags;
    }

    @Override
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public TagLinkMeasm createTagLink(Tag tag) {
        return new TagLinkMeasm(tag, this.id);
    }

    @Override
    public boolean hasErrorsWithChilds() {
        return this.hasErrors()
            || hasMessagesWithChilds(BaseModel::hasErrorsWithChilds);
    }

    @Override
    public boolean hasWarningsWithChilds() {
        return this.hasWarnings()
            || hasMessagesWithChilds(BaseModel::hasWarningsWithChilds);
    }

    @Override
    public boolean hasNotificationsWithChilds() {
        return this.hasNotifications()
            || hasMessagesWithChilds(BaseModel::hasNotificationsWithChilds);
    }

    private boolean hasMessagesWithChilds(Predicate<BaseModel> p) {
        return Stream.of(
            this.statusProts,
            this.commMeasms,
            this.measVals,
            this.getTags()
        ).filter(c -> c != null).flatMap(Collection::stream).anyMatch(p);
    }
}
