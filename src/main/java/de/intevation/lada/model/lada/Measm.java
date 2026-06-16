/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.REMOVE;
import jakarta.persistence.Transient;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Formula;
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
@DynamicInsert()
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
@NamedQuery(name = "hasCompleteMeasVals", query = """
    select exists (select 1 from MeasVal
        where measm = :m and (measVal is not null or lessThanLOD is not null))
    """)
@NamedQuery(name = "deleteMeasVals",
    query = "delete from MeasVal where measm = :m")
@NamedQuery(name = "measmStatus", query = """
    select statusMp from StatusProt where measm = :m
    order by seqNo desc fetch first 1 rows only""")
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

    @PastOrPresent(groups = Warnings.class)
    private Instant measmStartDate;

    @NotBlank
    @Size(max = 2)
    @IsValidPrimaryKey(groups = DatabaseConstraints.class, clazz = Mmt.class)
    private String mmtId;

    @Size(max = 4)
    @NotEmptyNorWhitespace
    @NotBlank(groups = Notifications.class)
    private String minSampleId;

    @OneToMany(mappedBy = StatusProt_.MEASM,
        cascade = { REMOVE, DETACH },
        fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    /* TODO: Use StatusProt_.SEQ_NO.
       See https://hibernate.atlassian.net/browse/HHH-20556 */
    @OrderBy("seqNo")
    @SuppressWarnings("serial")
    private List<StatusProt> statusProts;

    @OneToMany(mappedBy = CommMeasm_.MEASM,
        cascade = { REMOVE, DETACH },
        fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @SuppressWarnings("serial")
    private List<CommMeasm> commMeasms;

    @OneToMany(mappedBy = MeasVal_.MEASM,
        cascade = { REMOVE, DETACH },
        fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotEmpty(groups = Warnings.class)
    @SuppressWarnings("serial")
    private List<MeasVal> measVals;

    /* Work around the fact that hibernate does not provide means to have
    a ManyToMany association without cascading to the link table */
    @OneToMany(cascade = { PERSIST, MERGE, REFRESH, DETACH },
        fetch = FetchType.EAGER)
    @JoinColumn(name = "measm_id", insertable = false, updatable = false)
    @JsonbTransient
    @SuppressWarnings("serial")
    private Set<TagLinkMeasm> tagLinks = new HashSet<>();
    @Transient
    @SuppressWarnings("serial")
    private List<Tag> tags;

    @Column(insertable = false, updatable = false)
    private Instant treeMod;

    @Schema(readOnly = true)
    @Formula("""
        (SELECT count(*) FROM lada.meas_val v WHERE {alias}.id = v.measm_id)
        """)
    private int measValsCount;

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

    public Instant getMeasmStartDate() {
        return this.measmStartDate;
    }

    public void setMeasmStartDate(Instant measmStartDate) {
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

    public Instant getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Instant treeMod) {
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

    public int getMeasValsCount() {
        return measValsCount;
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
