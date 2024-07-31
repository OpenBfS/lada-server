/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.factory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.model.lada.GeolocatMpg_;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.MpgMmtMp;
import de.intevation.lada.model.lada.MpgMmtMp_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.model.master.EnvDescrip;
import de.intevation.lada.model.master.EnvDescripEnvMediumMp;
import de.intevation.lada.model.master.EnvDescripEnvMediumMp_;
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.data.EnvMedia;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * This factory creates probe objects and its children using a messprogramm
 * as template.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ProbeFactory {

    private static final int SEC59 = 59;

    private static final int MIN59 = 59;

    private static final int HOD23 = 23;

    @Inject Logger logger;

    // Number of days in one week
    private static final int N_WEEK_DAYS = 7;

    // Day of year representing February 28
    private static final int FEBRUARY_28 = 58;

    // pattern to format deskriptor sn
    private static final String SN_FORMAT = " %02d";

    private static final Map<String, int[]> FIELDS_TABLE = Map.of(
        Mpg.DAILY, new int[]{
            Calendar.DAY_OF_YEAR, Calendar.DAY_OF_YEAR, 1},
        Mpg.WEEKLY, new int[]{
            Calendar.DAY_OF_YEAR, Calendar.DAY_OF_YEAR, N_WEEK_DAYS },
        Mpg.TWO_WEEKLY, new int[]{
            Calendar.DAY_OF_YEAR, Calendar.DAY_OF_YEAR, N_WEEK_DAYS * 2 },
        Mpg.FOUR_WEEKLY, new int[]{
            Calendar.DAY_OF_YEAR, Calendar.DAY_OF_YEAR, N_WEEK_DAYS * 4 },
        Mpg.MONTHLY, new int[]{
            Calendar.MONTH, Calendar.DAY_OF_MONTH, 1 },
        Mpg.QUARTERLY, new int[]{
            Calendar.MONTH, Calendar.DAY_OF_MONTH, 3 },
        Mpg.HALF_YEARLY, new int[]{
            Calendar.MONTH, Calendar.DAY_OF_MONTH, 6 },
        Mpg.YEARLY, new int[]{
            Calendar.YEAR, Calendar.DAY_OF_YEAR, 1 });

    /**
     * Time interval in sense of lada.
     */
    private class Intervall {
        private static final int DSOY = 365;

        /**
         * Start of sub-intervall relative to intervall start in days (1-based).
         */
        private final int teilVon;

        /**
         * End of sub-intervall relative to intervall start in days (1-based).
         */
        private final int teilBis;

        /**
         * Field number in Calendar object representing this intervall's unit.
         */
        private final int intervallField;

        /**
         * Field number in Calendar object representing this intervall's.
         * sub-intervall unit
         */
        private final int subIntField;

        /**
         * Number of units of intervallField representing this intervall's size.
         */
        private final int intervallFactor;

        /**
         * Calendar object representing the this intervall's start.
         */
        private Calendar from;

        /**
         * Constructs an Intervall from a given Messprogramm and initial
         * start date, which will be adjusted to the next possible start
         * of an intervall of the type given by the Messprogramm.
         *
         * @param Mpg the Messprogramm to use
         * @param Calendar initial start date
         */
        Intervall(
            Mpg messprogramm,
            Calendar start
        ) {
            this.teilVon = messprogramm.getSamplePdStartDate();
            this.teilBis = messprogramm.getSamplePdEndDate();

            this.intervallField = FIELDS_TABLE
                .get(messprogramm.getSamplePd())[0];
            this.subIntField = FIELDS_TABLE
                .get(messprogramm.getSamplePd())[1];
            this.intervallFactor = FIELDS_TABLE
                .get(messprogramm.getSamplePd())[2];

            this.from = (Calendar) start.clone();

            // Align with beginning of next interval
            if (intervallField == Calendar.DAY_OF_YEAR
                && intervallFactor % N_WEEK_DAYS == 0
            ) {
                /* Intervalls representing multiples of weeks should start
                 * at Monday at or following the given start */
                if (from.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    from.add(Calendar.WEEK_OF_YEAR, 1);
                    from.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                }
            } else {
                /* Other intervalls should start at the beginning of the
                 * natural intervall at or following the given start (e.g.
                 * first day of next quarter) */
                int startIntField = start.get(intervallField);
                if (startIntField % intervallFactor != 0) {
                    from.add(
                        intervallField,
                        intervallFactor - startIntField % intervallFactor
                    );
                }

                /* Ensure we do not generate Sample objects with
                 * begin before start, which might otherwise happen if
                 * start is after what teilVon represents for this intervall */
                if (this.getFrom().before(start)) {
                    this.roll();
                }
            }
        }

       /**
        * Return given calendar adjusted to given day within intervall.
        * If the given amount of days is bigger than the number of days
        * in the intervall, the result is adjusted to the end of the intervall.
        *
        * This is used to compute the actual start and end date
        * of the actual sub-intervall.
        *
        * @param cal Calendar to be adjusted
        * @param teil day in intervall to adjust to
        *
        * @return the adjusted Calendar object.
        */
        private Calendar adjustSubIntField(Calendar cal, int teil) {
            int adjust = 0;
            if (intervallField != subIntField) {
                if (subIntField == Calendar.DAY_OF_YEAR) {
                    // Adjust in leap year
                    teil += startInLeapYear() && teil > FEBRUARY_28
                        ? 1
                        : 0;
                }
            } else {
                /* If intervallField == subIntField, we need to actually
                 * add to the value of intervallField */
                adjust += cal.get(intervallField) - 1;
            }

            /* If the given amount of days is bigger than the number of days
             * in the intervall, the number of days in the intervall is added
             * instead (i.e. the result is adjusted to the end of the
             * intervall).*/
            int subIntValue = adjust + Math.min(teil, getDuration());
            cal.set(subIntField, subIntValue);

            return cal;
        }

        /**
         * @return int Duration in days of the actual intervall
         *
         * Sum of actual maxima for subIntField from beginning of
         * actual intervall for the next intervallFactor values intervallField
         * or just intervallFactor, if subIntField == intervallField.
         */
        private int getDuration() {
            if (subIntField == intervallField) {
                return intervallFactor;
            }
            int duration = 0;
            Calendar tmp = (Calendar) from.clone();

            /* reset to beginning of intervall, e.g. first day of quarter
             * to compensate possible overflow if
             * teilVon > maximum of intervallField: */
            int intValue = from.get(intervallField);
            tmp.set(
                intervallField,
                intValue - intValue % intervallFactor
            );
            tmp.set(subIntField, tmp.getActualMinimum(subIntField));

            for (int i = 0; i < intervallFactor; i++) {
                duration += tmp.getActualMaximum(subIntField);
                tmp.add(intervallField, 1);
            }
            return duration;
        }

        /**
         * @return Calendar Get this intervall's sub-intervall start
         */
        public Calendar getFrom() {
            return adjustSubIntField((Calendar) from.clone(), teilVon);
        }

        /**
         * @return Calendar Get this intervall's sub-intervall end
         */
        public Calendar getTo() {
            return adjustSubIntField((Calendar) from.clone(), teilBis);
        }

        /**
         * @return boolean Does the actual intervall start in a leap year?
         */
        public boolean startInLeapYear() {
            return from.getActualMaximum(Calendar.DAY_OF_YEAR) > DSOY;
        }

        /**
         * @return int Returns the day number within the year of
         * this intervall's start
         */
        public int getStartDOY() {
            return getFrom().get(Calendar.DAY_OF_YEAR);
        }

        /**
         * Move intervall start to start of following intervall.
         */
        public void roll() {
            from.add(intervallField, intervallFactor);
        }

    }
    // end Intervall class


    /**
     * The data repository.
     */
    @Inject
    private Repository repository;

    @Inject
    private EnvMedia envMediaUtil;

    /**
     * Create a list of probe objects.
     *
     * @param messprogramm    Messprogramm
     * @param start  The start date
     * @param end    The end date
     * @param dryrun Persist objects only if set to false
     *
     * @return List of probe objects.
     */
    public List<Sample> create(
        Mpg messprogramm, Calendar start, Calendar end, boolean dryrun
    ) {
        /* Adjust to end of the day as we want to generate Probe objects
         * before or at this day. */
        end.set(Calendar.HOUR_OF_DAY, HOD23);
        end.set(Calendar.MINUTE, MIN59);
        end.set(Calendar.SECOND, SEC59);

        int gueltigVon = messprogramm.getValidStartDate();
        int gueltigBis = messprogramm.getValidEndDate();
        int offset = messprogramm.getSamplePdOffset();

        List<Sample> proben = new ArrayList<Sample>();

        for (Intervall intervall = new Intervall(messprogramm, start);
             intervall.getFrom().before(end);
             intervall.roll()
        ) {
            /* Leap year adaption of validity period.
             * It is assumed here (and should be enforced by the data model)
             * that gueltigVon and gueltigBis are always given relative to
             * a non-leap year. E.g. a value of 59 is assumed to denote
             * March 1 and thus has to be adapted in a leap year. */
            int leapDay = intervall.startInLeapYear() ? 1 : 0;
            int actualGueltigVon =
                gueltigVon  - 1 > FEBRUARY_28
                ? gueltigVon + leapDay
                : gueltigVon;
            int actualGueltigBis =
                gueltigBis - 1 > FEBRUARY_28
                ? gueltigBis + leapDay
                : gueltigBis;

            int solldatumBeginnDOY = intervall.getStartDOY() + offset;
            Calendar sollFrom = intervall.getFrom();
            sollFrom.add(Calendar.DATE, offset);
            Calendar sollTo = intervall.getTo();
            sollTo.add(Calendar.DATE, offset);

            if ((
                    // Validity within one year
                    actualGueltigVon < actualGueltigBis
                    && solldatumBeginnDOY >= actualGueltigVon
                    && solldatumBeginnDOY <= actualGueltigBis
                ) || (
                    // Validity over turn of the year
                    actualGueltigVon > actualGueltigBis
                    && (solldatumBeginnDOY >= actualGueltigVon
                        || solldatumBeginnDOY <= actualGueltigBis)
                )
            ) {
                Sample probe = createObjects(
                    messprogramm,
                    sollFrom.getTime(),
                    sollTo.getTime(),
                    dryrun
                );
                if (probe != null) {
                    proben.add(probe);
                }
            }
        }

        return proben;
    }

    /**
     * Create a single probe object.
     *
     * @param   messprogramm    The messprogramm containing probe details
     * @param   startDate       The date for 'solldatumbeginn'
     * @param   endDate         The date for 'solldatumende'
     * @param   dryrun          Do not persist created objects
     * @return The new probe object.
     */
    private Sample createObjects(
        Mpg messprogramm,
        Date startDate,
        Date endDate,
        boolean dryrun
    ) {
        QueryBuilder<MpgMmtMp> builder = repository
            .queryBuilder(MpgMmtMp.class)
            .and(MpgMmtMp_.mpgId, messprogramm.getId());
        List<MpgMmtMp> mmts = repository.filter(builder.getQuery());

        QueryBuilder<GeolocatMpg> builderOrt = repository
            .queryBuilder(GeolocatMpg.class)
            .and(GeolocatMpg_.mpgId, messprogramm.getId());
        List<GeolocatMpg> orte =
            repository.filter(builderOrt.getQuery());

        // Prepare data for informative transient attributes
        List<String> messungProtocol = new ArrayList<>();
        for (MpgMmtMp mmt: mmts) {
            messungProtocol.add(mmt.getMmtId());
        }

        String gemId = "";
        for (GeolocatMpg ort : orte) {
            if ("E".equals(ort.getTypeRegulation())) {
                gemId = repository.getById(
                    Site.class, ort.getSiteId()).getAdminUnitId();
            }
        }

        // Check for existing matching entity
        QueryBuilder<Sample> builderProbe = repository
            .queryBuilder(Sample.class)
            .and(Sample_.mpgId, messprogramm.getId())
            .and(Sample_.schedStartDate, startDate)
            .and(Sample_.schedEndDate, endDate);
        List<Sample> proben = repository.filter(builderProbe.getQuery());

        // Add informative transient attributes to existing entity
        if (!proben.isEmpty()) {
            proben.get(0).setFound(true);
            proben.get(0).setMmt(messungProtocol);
            proben.get(0).setGemId(gemId);
            return proben.get(0);
        }

        // Create new entity
        Sample probe = new Sample();
        probe.setOprModeId(messprogramm.getOprModeId());
        probe.setRegulationId(messprogramm.getRegulationId());
        probe.setEnvDescripDisplay(messprogramm.getEnvDescripDisplay());
        findMedia(probe);
        probe.setMeasFacilId(messprogramm.getMeasFacilId());
        probe.setApprLabId(messprogramm.getApprLabId());
        probe.setSampleMethId(messprogramm.getSampleMethId());
        probe.setSamplerId(messprogramm.getSamplerId());
        probe.setSchedStartDate(new Timestamp(startDate.getTime()));
        probe.setSchedEndDate(new Timestamp(endDate.getTime()));
        probe.setIsTest(messprogramm.getIsTest());
        probe.setEnvMediumId(messprogramm.getEnvMediumId());
        probe.setMpgId(messprogramm.getId());
        probe.setMpgCategId(messprogramm.getMpgCategId());
        probe.setReiAgGrId(messprogramm.getReiAgGrId());
        probe.setNuclFacilGrId(messprogramm.getNuclFacilGrId());
        probe.setFound(false);
        probe.setMmt(messungProtocol);
        probe.setGemId(gemId);

        createObject(probe, dryrun);

        //Create zusatzwert objects
        Set<SampleSpecif> pZusatzs = messprogramm.getSampleSpecifs();
        List<String> zusatzWerts = new ArrayList<String>();
        if (pZusatzs != null) {
            for (SampleSpecif pZusatz: pZusatzs) {
                SampleSpecifMeasVal zusatz = new SampleSpecifMeasVal();
                zusatz.setSampleId(probe.getId());
                zusatz.setSampleSpecifId(pZusatz.getId());
                createObject(zusatz, dryrun);
                zusatzWerts.add(zusatz.getSampleSpecifId());
            }
        }

        if (messprogramm.getCommSample() != null
            && !messprogramm.getCommSample().equals("")
        ) {
            CommSample kommentar = new CommSample();
            kommentar.setDate(new Timestamp(new Date().getTime()));
            kommentar.setSampleId(probe.getId());
            kommentar.setText(messprogramm.getCommSample());
            kommentar.setMeasFacilId(messprogramm.getMeasFacilId());

            createObject(kommentar, dryrun);
        }

        for (MpgMmtMp mmt: mmts) {
            Measm messung = new Measm();
            messung.setIsCompleted(false);
            messung.setIsScheduled(true);
            messung.setMmtId(mmt.getMmtId());
            messung.setSampleId(probe.getId());
            createObject(messung, dryrun);
            for (int mw : mmt.getMeasds()) {
                MeasVal wert = new MeasVal();
                wert.setMeasdId(mw);
                wert.setMeasmId(messung.getId());
                if (messprogramm.getMeasUnitId() != null) {
                    wert.setMeasUnitId(messprogramm.getMeasUnitId());
                } else {
                    wert.setMeasUnitId(0);
                }
                createObject(wert, dryrun);
            }
        }
        for (GeolocatMpg ort : orte) {
            Geolocat ortP = new Geolocat();
            ortP.setTypeRegulation(ort.getTypeRegulation());
            ortP.setSampleId(probe.getId());
            ortP.setSiteId(ort.getSiteId());
            ortP.setPoiId(ort.getPoiId());
            ortP.setAddSiteText(ort.getAddSiteText());
            createObject(ortP, dryrun);
        }
        // Reolad the probe to have the old id
        if (!dryrun) {
            probe = repository.getById(Sample.class, probe.getId());
        }
        return probe;
    }

    private void createObject(Object item, boolean dryrun) {
        if (!dryrun) {
            // TODO: Do not rely on this being successful
            repository.create(item);
        }
    }

    /**
     * Search for the media description using the 'deskriptor'.
     *
     * @param   probe   The probe object
     */
    public void findMedia(Sample probe) {
        String mediaDesk = probe.getEnvDescripDisplay();
        if (mediaDesk != null) {
            Object result = repository.queryFromString(
                "SELECT "
                + de.intevation.lada.model.master.SchemaName.NAME
                + ".get_media_from_media_desk( :mediaDesk );")
                    .setParameter("mediaDesk", mediaDesk)
                    .getSingleResult();
            probe.setEnvDescripName(result != null ? result.toString() : null);
        }
    }

    /**
     * Find the envMediumId matching envDescripDisplay.
     *
     * @param envDescripDisplay
     *
     * @return The envMediumId or null
     */
    public String findEnvMediumId(String envDescripDisplay) {
        Map<String, Integer> media;
        try {
            media = envMediaUtil.findEnvDescripIds(envDescripDisplay);
        } catch (EnvMedia.InvalidEnvDescripDisplayException e) {
            return null;
        }

        return EnvMedia.findEnvMediumId(
            media,
            envMediaUtil.findEnvDescripEnvMediumMps(media, true));
    }

    /**
     * Find the minimal deskriptor string for the specified umwelt id.
     *
     * @param   umwId  The umwelt id.
     *
     * @return The deskriptor string.
     */
    public String getInitialMediaDesk(String umwId) {
        if (umwId == null) {
            return null;
        }
        logger.debug("getInitialMediaDesk - umw_id: " + umwId);
        QueryBuilder<EnvDescripEnvMediumMp> builder = repository
            .queryBuilder(EnvDescripEnvMediumMp.class)
            .and(EnvDescripEnvMediumMp_.envMediumId, umwId);
        List<EnvDescripEnvMediumMp> data =
            repository.filter(builder.getQuery());
        if (data.isEmpty()) {
            final String empty = "D: 00 00 00 00 00 00 00 00 00 00 00 00";
            logger.debug("getInitialMediaDesk - media_desk : " + empty);
            return empty;
        } else {
            List<Integer> levels = new ArrayList<>(EnvMedia.ENV_DESCRIP_LEVELS);
            for (int lev = 0; lev < EnvMedia.ENV_DESCRIP_LEVELS; lev++) {
                levels.add(EnvMedia.getEnvDescripId(lev, data.get(0)));
            }
            for (EnvDescripEnvMediumMp mp : data) {
                for (int lev = 0; lev < EnvMedia.ENV_DESCRIP_LEVELS; lev++) {
                    Integer envDescripId = EnvMedia.getEnvDescripId(lev, mp);
                    if (envDescripId == null
                        || !envDescripId.equals(levels.get(lev))
                    ) {
                        levels.set(lev, null);
                    }
                }
            }

            String mediaDesk = "D:";
            for (Integer levId: levels) {
                mediaDesk += String.format(SN_FORMAT, levId == null
                    ? 0
                    : repository.getById(EnvDescrip.class, levId).getLevVal());
            }
            logger.debug("getInitialMediaDesk - umw_desk: " + mediaDesk);
            return mediaDesk;
        }
    }
}
