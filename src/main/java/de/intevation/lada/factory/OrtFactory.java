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
import de.intevation.lada.model.stammdaten.Ort;
import de.intevation.lada.model.stammdaten.Staat;
import de.intevation.lada.model.stammdaten.Verwaltungseinheit;
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
    public void transformCoordinates(Ort ort) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        if (ort.getKdaId() == null
            || ort.getKoordXExtern() == null
            || ort.getKoordXExtern().equals("")
            || ort.getKoordYExtern() == null
            || ort.getKoordYExtern().equals("")
        ) {
            /* TODO: The checked conditions are mostly also checked in KdaUtil.
             * Do we really need a different StatusCode here? */
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.IMP_INVALID_VALUE);
            err.setKey("coordinates");
            err.setValue(ort.getKdaId()
                + " " + ort.getKoordXExtern() + " " + ort.getKoordYExtern());
            errors.add(err);
            return;
        }
        Integer kda = ort.getKdaId();
        String xCoord = ort.getKoordXExtern();
        String yCoord = ort.getKoordYExtern();

        KdaUtil.Result coords = new KdaUtil().transform(
            kda, KdaUtil.KDA_GD, xCoord, yCoord);
        if (coords == null) {
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.GEO_NOT_MATCHING);
            err.setKey("kdaId");
            err.setValue(ort.getKdaId()
                + " " + ort.getKoordXExtern() + " " + ort.getKoordYExtern());
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
    public Ort completeOrt(Ort ort) {
        if (errors == null) {
            errors = new ArrayList<ReportItem>();
        } else {
            errors.clear();
        }
        QueryBuilder<Ort> builder = repository.queryBuilder(Ort.class);
        if (ort.getKdaId() != null
            && ort.getKoordXExtern() != null
            && ort.getKoordYExtern() != null
        ) {
            builder.and("kdaId", ort.getKdaId());
            builder.and("koordXExtern", ort.getKoordXExtern());
            builder.and("koordYExtern", ort.getKoordYExtern());
            builder.and("netzbetreiberId", ort.getNetzbetreiberId());
            List<Ort> orte =
                repository.filterPlain(builder.getQuery());
            if (orte != null && !orte.isEmpty()) {
                return orte.get(0);
            }
        } else if (ort.getGemId() != null) {
            builder.and("gemId", ort.getGemId());
            builder.and("netzbetreiberId", ort.getNetzbetreiberId());
            List<Ort> orte =
                repository.filterPlain(builder.getQuery());
            if (orte != null && !orte.isEmpty()) {
                if (orte.size() == 1) {
                    return orte.get(0);
                } else {
                    //get verwaltungseinheiten
                    Verwaltungseinheit v = repository.getByIdPlain(
                        Verwaltungseinheit.class, ort.getGemId());
                    if (v != null) {
                        for (Ort oElem : orte) {
                            //Todo: Check for different kda-types
                            if (oElem.getKoordXExtern().equals(
                                    String.valueOf(v.getMittelpunkt().getX()))
                                && oElem.getKoordYExtern().equals(
                                    String.valueOf(v.getMittelpunkt().getY()))
                            ) {
                                return oElem;
                            }
                        }
                    } else {
                        logger.debug("1. we need an else here ...");
                    }
                }
            }
        } else  if (ort.getStaatId() != null) {
            builder.and("staatId", ort.getStaatId());
            builder.and("ortTyp", ORTTYP5);
            builder.and("netzbetreiberId", ort.getNetzbetreiberId());
            List<Ort> orte =
                repository.filterPlain(builder.getQuery());
            if (orte != null && !orte.isEmpty()) {
                return orte.get(0);
            }
        }

        return createOrt(ort);
    }

    private Ort createOrt(Ort ort) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        boolean hasKoord = false;
        boolean hasGem = false;
        boolean hasStaat = false;
        //set default value for attribute "unscharf"
        ort.setUnscharf(false);
        if (ort.getKdaId() != null
            && ort.getKoordXExtern() != null
            && ort.getKoordYExtern() != null
        ) {
            transformCoordinates(ort);
            hasKoord = true;
        }
        if (ort.getGemId() == null && hasKoord) {
            findVerwaltungseinheit(ort);
        }
        if (ort.getGemId() != null) {
            if (ort.getStaatId() == null) {
                ort.setStaatId(0);
            }
            Verwaltungseinheit v = repository.getByIdPlain(
                Verwaltungseinheit.class, ort.getGemId());
            //Ort exists - check for OrtId
            QueryBuilder<Ort> builderExists =
                repository.queryBuilder(Ort.class);
            builderExists.and("netzbetreiberId", ort.getNetzbetreiberId());
            builderExists.andLike("ortId", "%" + ort.getGemId());
            List<Ort> ortExists = repository.filterPlain(
                builderExists.getQuery());
            if (v == null) {
                ReportItem err = new ReportItem();
                err.setCode(StatusCodes.IMP_INVALID_VALUE);
                err.setKey("gem_id");
                err.setValue(ort.getGemId());
                errors.add(err);
                return null;
            } else if (ortExists.isEmpty()) {
                if (!hasKoord) {
                    if (ort.getKdaId() == null) {
                        ort.setKdaId(KdaUtil.KDA_GD);
                        ort.setKoordYExtern(
                            String.valueOf(v.getMittelpunkt().getY()));
                        ort.setKoordXExtern(
                            String.valueOf(v.getMittelpunkt().getX()));
                    } else {
                        KdaUtil.Result coords = new KdaUtil().transform(
                            KdaUtil.KDA_GD,
                            ort.getKdaId(),
                            String.valueOf(v.getMittelpunkt().getX()),
                            String.valueOf(v.getMittelpunkt().getY()));
                        ort.setKoordYExtern(coords.getY());
                        ort.setKoordXExtern(coords.getX());
                    }
                    ort.setOrtTyp(ORTTYP4);
                    //set ortId
                    if (v.getIsGemeinde()) {
                        ort.setOrtId("GEM_" + ort.getGemId());
                    } else if (!v.getIsGemeinde() && v.getIsLandkreis()) {
                       ort.setOrtId("LK_" + ort.getGemId());
                    } else if (
                        !v.getIsGemeinde()
                        && !v.getIsLandkreis()
                        && v.getIsRegbezirk()
                    ) {
                        ort.setOrtId("RB_" + ort.getGemId());
                    } else if (
                        !v.getIsGemeinde()
                        && !v.getIsLandkreis()
                        && !v.getIsRegbezirk()
                        && v.getIsBundesland()
                    ) {
                        ort.setOrtId("BL_" + ort.getGemId());
                    }
                }
                if (ort.getKurztext() == null || ort.getKurztext().equals("")) {
                    ort.setKurztext(ort.getOrtId());
                }
                if (ort.getLangtext() == null || ort.getLangtext().equals("")) {
                    ort.setLangtext(v.getBezeichnung());
                }
                if (ort.getBerichtstext() == null
                    || ort.getBerichtstext().equals("")
                ) {
                    ort.setBerichtstext(v.getBezeichnung());
                }
                transformCoordinates(ort);

                hasGem = true;
            } else if (ortExists.size() > 0 && !hasKoord) {
                return ortExists.get(0);
            } else {
                return ort;
            }
        }
        if (ort.getStaatId() != null
            && !hasKoord
            && !hasGem
        ) {
            Staat staat =
                repository.getByIdPlain(
                    Staat.class, ort.getStaatId());
            ort.setKdaId(staat.getKdaId());
            ort.setKoordXExtern(staat.getKoordXExtern());
            ort.setKoordYExtern(staat.getKoordYExtern());
            ort.setLangtext(staat.getStaat());
            ort.setOrtTyp(ORTTYP5);
            ort.setOrtId("STAAT_" + staat.getId());
            if (staat.getStaatIso() != null) {
                ort.setKurztext("STAAT_" + staat.getStaatIso());
            } else {
                ort.setKurztext("STAAT_" + staat.getId());
            }
            ort.setBerichtstext(staat.getStaat());
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
    public void findVerwaltungseinheit(Ort ort) {
        if (ort.getGeom() == null) {
            return;
        }
        Query q = repository.entityManager()
            .createQuery("SELECT vg.gemId "
                + "FROM Verwaltungsgrenze vg "
                + "WHERE is_gemeinde = TRUE "
                + "AND contains(vg.shape, :geom) = TRUE");
        q.setParameter("geom", ort.getGeom());
        List<?> ret = q.getResultList();
        if (!ret.isEmpty()) {
            ort.setGemId(ret.get(0).toString());
            ort.setStaatId(0);
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
