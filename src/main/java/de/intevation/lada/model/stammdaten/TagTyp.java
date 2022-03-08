/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the tag_typ database table.
 */
@Entity
@Table(name = "tag_typ", schema = SchemaName.NAME)
public class TagTyp {

    //Constants
    //Default time after which mst tags expire in days
    public static final int MST_TAG_EXPIRATION_TIME = 365;
    //Default time after which auto tags expire in days
    public static final int AUTO_TAG_EXPIRATION_TIME = 584;
    //Tag type ids
    public static final String TAG_TYPE_GLOBAL = "global";
    public static final String TAG_TYPE_NETZBETREIBER = "netzbetreiber";
    public static final String TAG_TYPE_MST = "mst";
    public static final String TAG_TYPE_AUTO = "auto";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, updatable = false)
    private String id;

    @Column(name = "tagtyp", insertable = false, updatable = false)
    private String tagTyp;

    public TagTyp() { }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTagTyp() {
        return this.tagTyp;
    }

    public void setTagTyp(String tag) {
        this.tagTyp = tag;
    }
}
