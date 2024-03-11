/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.factory;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.master.AdminUnit;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.State;
import de.intevation.lada.util.data.KdaUtil;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;

/**
 * Class to create, transform and complete ort objects.
 */
public class OrtFactory {

    private static final int EPSG4326 = 4326;

    private static final int ORTTYP4 = 4; // Verwaltungseinheit
    private static final int ORTTYP5 = 5; // Staat

    @Inject
    private Repository repository;

    private List<ReportItem> errors = new ArrayList<>();

    /**
     * Transform the external coordinates to the geom representation.
     * @param ort the ort
     */
    private void transformCoordinates(Site ort) {
        Integer kda = ort.getSpatRefSysId();
        String xCoord = ort.getCoordXExt();
        String yCoord = ort.getCoordYExt();

        KdaUtil.Result coords = new KdaUtil().transform(
            KdaUtil.KDAS.get(kda), KdaUtil.KDA.GD, xCoord, yCoord);
        if (coords == null) {
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.GEO_NOT_MATCHING);
            err.setKey("spatRefSysId");
            err.setValue(kda + " " + xCoord + " " + yCoord);
            errors.add(err);
            return;
        }
        ort.setGeom(new GeometryFactory(new PrecisionModel(), EPSG4326)
            .createPoint(new Coordinate(
                Double.parseDouble(coords.getX()),
                Double.parseDouble(coords.getY()))));
        return;
    }

    /**
     * Search for exisiting Site entity matching either by coordinates,
     * adminUnit or state.
     *
     * @param ort The possibly incomplete Site object
     * @return The matching existing Site object or null if no match was found.
     */
    public Site findExistingSite(Site ort) {
        boolean hasKoord = false;

        // Search for matching existing site and return it
        QueryBuilder<Site> builder = repository.queryBuilder(Site.class)
            .and("networkId", ort.getNetworkId());
        if (ort.getSpatRefSysId() != null
            && ort.getCoordXExt() != null
            && ort.getCoordYExt() != null
        ) {
            hasKoord = true;
            builder.and("spatRefSysId", ort.getSpatRefSysId())
                .and("coordXExt", ort.getCoordXExt())
                .and("coordYExt", ort.getCoordYExt());
            List<Site> orte = repository.filterPlain(builder.getQuery());
            if (!orte.isEmpty()) {
                return orte.get(0);
            }
        } else if (ort.getAdminUnitId() != null) {
            builder.and("adminUnitId", ort.getAdminUnitId());
            List<Site> orte = repository.filterPlain(builder.getQuery());
            if (!orte.isEmpty()) {
                if (orte.size() == 1) {
                    return orte.get(0);
                } else {
                    AdminUnit v = repository.getByIdPlain(
                        AdminUnit.class, ort.getAdminUnitId());
                    for (Site oElem : orte) {
                        //Todo: Check for different kda-types
                        if (oElem.getCoordXExt().equals(
                                String.valueOf(v.getGeomCenter().getX()))
                            && oElem.getCoordYExt().equals(
                                String.valueOf(v.getGeomCenter().getY()))
                        ) {
                            return oElem;
                        }
                    }
                }
            }
        } else  if (ort.getStateId() != null) {
            builder.and("stateId", ort.getStateId())
                .and("siteClassId", ORTTYP5);
            List<Site> orte = repository.filterPlain(builder.getQuery());
            if (!orte.isEmpty()) {
                return orte.get(0);
            }
        }
        //Ort exists - check for OrtId
        if (ort.getAdminUnitId() != null && !hasKoord) {
            QueryBuilder<Site> builderExists =
                repository.queryBuilder(Site.class)
                .and("networkId", ort.getNetworkId())
                .andLike("extId", "%" + ort.getAdminUnitId());
            List<Site> ortExists = repository.filterPlain(
                builderExists.getQuery());
            if (!ortExists.isEmpty()) {
                return ortExists.get(0);
            }
        }
        return null;
    }

    /**
     * Use given attributes to try to add other attributes.
     * To set futher attributes, at least one of the following attribute sets
     * needs to be present:
     * - spatRefSysId, coordXExt, coordYExt
     * - adminUnitId
     * - stateId
     *
     * @param ort The possibly incomplete Site object
     */
    public void completeSite(Site ort) {
        errors.clear();

        boolean hasKoord = false;

        // Try setting geometry from coordinates
        if (ort.getSpatRefSysId() != null
            && ort.getCoordXExt() != null
            && ort.getCoordYExt() != null
        ) {
            hasKoord = true;
            transformCoordinates(ort);
        }

        // Try setting adminUnit from coordinates
        if (ort.getAdminUnitId() == null && ort.getGeom() != null) {
            try {
                ort.setAdminUnitId((String) repository.entityManager()
                    .createNativeQuery("SELECT vg.munic_id "
                        + "FROM master.admin_border_view vg "
                        + "WHERE is_munic "
                        + "AND public.st_contains(vg.shape, :geom) "
                        + "FETCH FIRST ROW ONLY",
                        String.class)
                    .setParameter("geom", ort.getGeom())
                    .getSingleResult());
            } catch (NoResultException nre) {
                // Nothing to do
            }
        }

        if (ort.getAdminUnitId() != null) {
            // An adminUnitId can only ever reference an administrative unit
            // of the state with database ID 0
            ort.setStateId(0);

            AdminUnit v = repository.getByIdPlain(
                AdminUnit.class, ort.getAdminUnitId());
            if (!hasKoord) {
                if (ort.getSpatRefSysId() == null) {
                    ort.setSpatRefSysId(KdaUtil.KDA_GD);
                    ort.setCoordYExt(
                        String.valueOf(v.getGeomCenter().getY()));
                    ort.setCoordXExt(
                        String.valueOf(v.getGeomCenter().getX()));
                } else {
                    KdaUtil.Result coords = new KdaUtil().transform(
                        KdaUtil.KDA.GD,
                        KdaUtil.KDAS.get(ort.getSpatRefSysId()),
                        String.valueOf(v.getGeomCenter().getX()),
                        String.valueOf(v.getGeomCenter().getY()));
                    if (coords != null) {
                        ort.setCoordYExt(coords.getY());
                        ort.setCoordXExt(coords.getX());
                    }
                }
                ort.setSiteClassId(ORTTYP4);

                String prefix = null;
                if (v.getIsMunic()) {
                    prefix = "GEM";
                } else if (v.getIsRuralDist()) {
                    prefix = "LK";
                } else if (v.getIsGovDist()) {
                    prefix = "RB";
                } else if (v.getIsState()) {
                    prefix = "BL";
                }
                if (prefix != null) {
                    ort.setExtId(prefix + "_" + ort.getAdminUnitId());
                }
            }
            if (ort.getShortText() == null) {
                ort.setShortText(ort.getExtId());
            }
            if (ort.getLongText() == null) {
                ort.setLongText(v.getName());
            }
            if (ort.getReiReportText() == null) {
                ort.setReiReportText(v.getName());
            }
            transformCoordinates(ort);
            return;
        }
        if (!hasKoord && ort.getStateId() != null) {
            State staat = repository.getByIdPlain(
                State.class, ort.getStateId());
            ort.setSpatRefSysId(staat.getSpatRefSysId());
            ort.setCoordXExt(staat.getCoordXExt());
            ort.setCoordYExt(staat.getCoordYExt());
            ort.setLongText(staat.getCtry());
            ort.setSiteClassId(ORTTYP5);
            final String prefix = "STAAT_";
            if (staat.getIso3166() != null) {
                ort.setExtId(prefix + staat.getIso3166());
                ort.setShortText(prefix + staat.getIso3166());
            } else {
                ort.setExtId(prefix + staat.getId());
                ort.setShortText(prefix + staat.getId());
            }
            ort.setReiReportText(staat.getCtry());
            transformCoordinates(ort);
        }
        return;
    }

    public List<ReportItem> getErrors() {
        return errors;
    }

    /**
     * Check if the factory has any errors.
     * @return True if there are errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
