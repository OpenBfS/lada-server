/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.intevation.lada.model.master.Auth;

/**
 * Container for user specific information.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class UserInfo {
    private String name;
    private Integer userId;
    private List<Auth> auth;

    // Prevent instances without useful data.
    private UserInfo() { };

    public UserInfo(String name, Integer userId, List<Auth> auth) {
        this.name = name;
        this.userId = userId;
        this.auth = auth;
    }

    public class MessLaborId {
        private String messstelle;
        private String labor;

        /**
         * @return the messstelle
         */
        public String getMessstelle() {
            return messstelle;
        }

        /**
         * @param messstelle the messstelle to set
         */
        public void setMessstelle(String messstelle) {
            this.messstelle = messstelle;
        }

        /**
         * @return the labor
         */
        public String getLabor() {
            return labor;
        }

        /**
         * @param labor the labor to set
         */
        public void setLabor(String labor) {
            this.labor = labor;
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @return the user's roles
     */
    public List<Auth> getAuth() {
        return auth;
    }

    public List<MessLaborId> getMessLaborId() {
        List<MessLaborId> ret = new ArrayList<MessLaborId>();
        for (Auth a : auth) {
            if (a.getMeasFacilId() != null) {
                MessLaborId id = new MessLaborId();
                id.setMessstelle(a.getMeasFacilId());
                id.setLabor(a.getApprLabId());
                ret.add(id);
            }
        }
        return ret;
    }

    public boolean belongsTo(String messstelle, String labor) {
        for (Auth a : auth) {
            if (a.getMeasFacilId() == null) {
                continue;
            }
            if (a.getMeasFacilId().equals(messstelle)
                || (a.getApprLabId() != null
                    && a.getApprLabId().equals(labor))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the messstellen
     */
    public List<String> getMessstellen() {
        List<String> ret = new ArrayList<String>();
        for (Auth a : auth) {
            if (a.getMeasFacilId() != null) {
                ret.add(a.getMeasFacilId());
            }
        }
        return ret;
    }

    /**
     * @return the labor messstellen
     */
    public List<String> getLaborMessstellen() {
        List<String> ret = new ArrayList<String>();
        for (Auth a : auth) {
            if (a.getApprLabId() != null) {
                ret.add(a.getApprLabId());
            }
        }
        return ret;
    }

    /**
     * @return the netzbetreiber
     */
    public Set<String> getNetzbetreiber() {
        Set<String> ret = new HashSet<>();
        for (Auth a : auth) {
            if (a.getNetworkId() != null) {
                ret.add(a.getNetworkId());
            }
        }
        return ret;
    }

    public List<Integer> getFunktionen() {
        List<Integer> ret = new ArrayList<Integer>();
        for (Auth a : auth) {
            if (a.getAuthFunctId() != null) {
                ret.add(a.getAuthFunctId());
            }
        }
        return ret;
    }

    /**
     * @return the funktionen
     */
    public List<Integer> getFunktionenForMst(String mstId) {
        List<Integer> ret = new ArrayList<Integer>();
        for (Auth a : auth) {
            if (a.getMeasFacilId() != null && a.getMeasFacilId().equals(mstId)) {
                ret.add(a.getAuthFunctId());
            }
        }
        return ret;
    }

    /**
     * @return the funktionen
     */
    public List<Integer> getFunktionenForNetzbetreiber(String nId) {
        List<Integer> ret = new ArrayList<Integer>();
        for (Auth a : auth) {
            if (a.getNetworkId() != null
                && a.getNetworkId().equals(nId)) {
                ret.add(a.getAuthFunctId());
            }
        }
        return ret;
    }
}
