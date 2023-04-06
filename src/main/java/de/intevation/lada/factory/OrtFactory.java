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

import javax.inject.Inject;
import javax.persistence.Query;
import org.jboss.logging.Logger;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

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
    private Logger logger;

    @Inject
    private Repository repository;

    private List<ReportItem> errors;

    /**
     * Transform the external coordinates to the geom representation.
     * @param ort the ort
     */
    public void transformCoordinates(Site ort) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        if (ort.getSpatRefSysId() == null
            || ort.getCoordXExt() == null
            || ort.getCoordXExt().equals("")
            || ort.getCoordYExt() == null
            || ort.getCoordYExt().equals("")
        ) {
            /* TODO: The checked conditions are mostly also checked in KdaUtil.
             * Do we really need a different StatusCode here? */
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.IMP_INVALID_VALUE);
            err.setKey("coordinates");
            err.setValue(ort.getSpatRefSysId()
                + " " + ort.getCoordXExt() + " " + ort.getCoordYExt());
            errors.add(err);
            return;
        }
        Integer kda = ort.getSpatRefSysId();
        String xCoord = ort.getCoordXExt();
        String yCoord = ort.getCoordYExt();

        KdaUtil.Result coords = new KdaUtil().transform(
            kda, KdaUtil.KDA_GD, xCoord, yCoord);
        if (coords == null) {
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.GEO_NOT_MATCHING);
            err.setKey("kdaId");
            err.setValue(ort.getSpatRefSysId()
                + " " + ort.getCoordXExt() + " " + ort.getCoordYExt());
            errors.add(err);
            return;
        }
        ort.setGeom(generateGeom(
                Double.parseDouble(coords.getX()),
                Double.parseDouble(coords.getY())));
        return;
    }


    /**
     * Use given attribute to try to add other attributes.
     * To set futher attributes at least one of the following attribute set
     * need to be present:
     * - kda, x, y
     * - gemId
     * - staat
     *
     * @param ort The incomplete ort
     * @return The resulting ort.
     */
    public Site completeOrt(Site ort) {
        if (errors == null) {
            errors = new ArrayList<ReportItem>();
        } else {
            errors.clear();
        }

        QueryBuilder<Site> builder = repository.queryBuilder(Site.class)
            .and("networkId", ort.getNetworkId());

        if (ort.getSpatRefSysId() != null
            && ort.getCoordXExt() != null
            && ort.getCoordYExt() != null
        ) {
            builder.and("spatRefSysId", ort.getSpatRefSysId());
            builder.and("coordXExt", ort.getCoordXExt());
            builder.and("coordYExt", ort.getCoordYExt());
            List<Site> orte =
                repository.filterPlain(builder.getQuery());
            if (orte != null && !orte.isEmpty()) {
                return orte.get(0);
            }
        } else if (ort.getAdminUnitId() != null) {
            builder.and("adminUnitId", ort.getAdminUnitId());
            List<Site> orte =
                repository.filterPlain(builder.getQuery());
            if (orte != null && !orte.isEmpty()) {
                if (orte.size() == 1) {
                    return orte.get(0);
                } else {
                    //get verwaltungseinheiten
                    AdminUnit v = repository.getByIdPlain(
                        AdminUnit.class, ort.getAdminUnitId());
                    if (v != null) {
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
                    } else {
                        logger.debug("1. we need an else here ...");
                    }
                }
            }
        } else  if (ort.getStateId() != null) {
            builder.and("stateId", ort.getStateId());
            builder.and("siteClassId", ORTTYP5);
            List<Site> orte =
                repository.filterPlain(builder.getQuery());
            if (orte != null && !orte.isEmpty()) {
                return orte.get(0);
            }
        }

        return createOrt(ort);
    }

    private Site createOrt(Site ort) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        boolean hasKoord = false;
        boolean hasGem = false;
        boolean hasStaat = false;
        //set default value for attribute "unscharf"
        ort.setIsFuzzy(false);
        if (ort.getSpatRefSysId() != null
            && ort.getCoordXExt() != null
            && ort.getCoordYExt() != null
        ) {
            transformCoordinates(ort);
            hasKoord = true;
        }
        if (ort.getAdminUnitId() == null && hasKoord) {
            findVerwaltungseinheit(ort);
        }
        if (ort.getAdminUnitId() != null) {
            if (ort.getStateId() == null) {
                ort.setStateId(0);
            }
            AdminUnit v = repository.getByIdPlain(
                AdminUnit.class, ort.getAdminUnitId());
            //Ort exists - check for OrtId
            QueryBuilder<Site> builderExists =
                repository.queryBuilder(Site.class)
                .and("networkId", ort.getNetworkId())
                .andLike("adminUnitId", "%" + ort.getAdminUnitId());
            List<Site> ortExists = repository.filterPlain(
                builderExists.getQuery());
            if (v == null) {
                ReportItem err = new ReportItem();
                err.setCode(StatusCodes.IMP_INVALID_VALUE);
                err.setKey("gem_id");
                err.setValue(ort.getAdminUnitId());
                errors.add(err);
                return null;
            } else if (ortExists.isEmpty()) {
                if (!hasKoord) {
                    if (ort.getSpatRefSysId() == null) {
                        ort.setSpatRefSysId(KdaUtil.KDA_GD);
                        ort.setCoordYExt(
                            String.valueOf(v.getGeomCenter().getY()));
                        ort.setCoordXExt(
                            String.valueOf(v.getGeomCenter().getX()));
                    } else {
                        KdaUtil.Result coords = new KdaUtil().transform(
                            KdaUtil.KDA_GD,
                            ort.getSpatRefSysId(),
                            String.valueOf(v.getGeomCenter().getX()),
                            String.valueOf(v.getGeomCenter().getY()));
                        if (coords != null) {
                            ort.setCoordYExt(coords.getY());
                            ort.setCoordXExt(coords.getX());
                        }
                    }
                    ort.setSiteClassId(ORTTYP4);
                    //set ortId
                    if (v.getIsMunic()) {
                        ort.setExtId("GEM_" + ort.getAdminUnitId());
                    } else if (!v.getIsMunic() && v.getIsRuralDist()) {
                       ort.setExtId("LK_" + ort.getAdminUnitId());
                    } else if (!v.getIsMunic()
                            && !v.getIsRuralDist()
                            && v.getIsGovDist()) {
                        ort.setExtId("RB_" + ort.getAdminUnitId());
                    } else if (!v.getIsMunic()
                            && !v.getIsRuralDist()
                            && !v.getIsGovDist()
                            && v.getIsState()) {
                        ort.setExtId("BL_" + ort.getAdminUnitId());
                    }
                }
                if (ort.getShortText() == null
                    || ort.getShortText().equals("")) {
                    ort.setShortText(ort.getExtId());
                }
                if (ort.getLongText() == null
                    || ort.getLongText().equals("")) {
                    ort.setLongText(v.getName());
                }
                if (ort.getReiReportText() == null
                    || ort.getReiReportText().equals("")
                ) {
                    ort.setReiReportText(v.getName());
                }
                transformCoordinates(ort);

                hasGem = true;
            } else if (ortExists.size() > 0 && !hasKoord) {
                return ortExists.get(0);
            } else {
                return ort;
            }
        }
        if (ort.getStateId() != null
            && !hasKoord
            && !hasGem
        ) {
            State staat =
                repository.getByIdPlain(
                    State.class, ort.getStateId());
            ort.setSpatRefSysId(staat.getSpatRefSysId());
            ort.setCoordXExt(staat.getCoordXExt());
            ort.setCoordYExt(staat.getCoordYExt());
            ort.setLongText(staat.getCtry());
            ort.setSiteClassId(ORTTYP5);
            ort.setExtId("STAAT_" + staat.getId());
            if (staat.getIso3166() != null) {
                ort.setShortText("STAAT_" + staat.getIso3166());
            } else {
                ort.setShortText("STAAT_" + staat.getId());
            }
            ort.setReiReportText(staat.getCtry());
            transformCoordinates(ort);
            hasStaat = true;
        }
        if (!hasKoord && !hasGem && !hasStaat) {
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.VALUE_AMBIGOUS);
            err.setKey("ort");
            err.setValue("");
            errors.add(err);
        }
        return ort;
    }

    /**
     * Use the geom of an ort object to determine the verwaltungseinheit.
     * If verwaltungseinheit was found the gemId is used as reference in the ort
     * object.
     *
     * @param ort   The ort object
     */
    public void findVerwaltungseinheit(Site ort) {
        if (ort.getGeom() == null) {
            return;
        }
        Query q = repository.entityManager()
            .createNativeQuery("SELECT vg.munic_id "
                + "FROM master.admin_border_view vg "
                + "WHERE is_munic = TRUE "
                + "AND public.st_contains(vg.shape, :geom) = TRUE");
        q.setParameter("geom", ort.getGeom());
        List<?> ret = q.getResultList();
        if (!ret.isEmpty()) {
            ort.setAdminUnitId(ret.get(0).toString());
            ort.setStateId(0);
        }
        return;
    }

    private Point generateGeom(Double x, Double y) {
        GeometryFactory geomFactory = new GeometryFactory();
        Coordinate coord = new Coordinate(x, y);
        Point geom = geomFactory.createPoint(coord);
        geom.setSRID(EPSG4326);
        return geom;
    }

    public List<ReportItem> getErrors() {
        return errors;
    }

    /**
     * Check if the factory has any errors.
     * @return True if there are errors
     */
    public boolean hasErrors() {
        return !(errors == null) && !errors.isEmpty();
    }
}
