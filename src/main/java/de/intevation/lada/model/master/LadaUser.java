/* Copyright (C) 2016 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import static de.intevation.lada.model.master.Names.QUERY_PARAM_USER_NAME;

import java.io.Serializable;

import org.hibernate.annotations.processing.CheckHQL;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = Names.SCHEMA_NAME)
@CheckHQL
@NamedQuery(name = "insertUserName", query =
    "insert into LadaUser (name) VALUES (:"
    + QUERY_PARAM_USER_NAME + ") on conflict do nothing")
@NamedQuery(name = "getLadaUserId", query =
    "select id from LadaUser where name = :" + QUERY_PARAM_USER_NAME)
public class LadaUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    public LadaUser() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
