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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import de.intevation.lada.model.land.CommSample;
import de.intevation.lada.model.land.Mpg;
import de.intevation.lada.model.land.MpgMmtMp;
import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.land.Ortszuordnung;
import de.intevation.lada.model.land.OrtszuordnungMp;
import de.intevation.lada.model.land.Sample;
import de.intevation.lada.model.land.ZusatzWert;
import de.intevation.lada.model.master.EnvDescrip;
import de.intevation.lada.model.master.EnvDescripEnvMediumMp;
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;

/**
 * This factory creates probe objects and its children using a messprogramm
 * as template.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ProbeFactory {

    private static final int LM12 = 12;

    private static final int POS9 = 9;

    private static final int ZEBS3 = 3;

    private static final int ZEBS5 = 5;

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

    private static Hashtable<String, int[]> fieldsTable =
        new Hashtable<String, int[]>();

    public ProbeFactory() {
        final int[] t  = {Calendar.DAY_OF_YEAR, Calendar.DAY_OF_YEAR, 1 };
        final int[] w  = {Calendar.DAY_OF_YEAR, Calendar.DAY_OF_YEAR,
                     N_WEEK_DAYS };
        final int[] w2 = {Calendar.DAY_OF_YEAR, Calendar.DAY_OF_YEAR,
                     N_WEEK_DAYS * 2 };
        final int[] w4 = {Calendar.DAY_OF_YEAR, Calendar.DAY_OF_YEAR,
                     N_WEEK_DAYS * 4 };

        final int[] m = {Calendar.MONTH, Calendar.DAY_OF_MONTH, 1 };
        final int[] q = {Calendar.MONTH, Calendar.DAY_OF_MONTH, 3 };
        final int[] h = {Calendar.MONTH, Calendar.DAY_OF_MONTH, 6 };

        final int[] j = {Calendar.YEAR, Calendar.DAY_OF_YEAR, 1 };

        fieldsTable.put("T", t);
        fieldsTable.put("W", w);
        fieldsTable.put("W2", w2);
        fieldsTable.put("W4", w4);
        fieldsTable.put("M", m);
        fieldsTable.put("Q", q);
        fieldsTable.put("H", h);
        fieldsTable.put("J", j);
    }

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

            this.intervallField = fieldsTable
                .get(messprogramm.getSamplePd())[0];
            this.subIntField = fieldsTable
                .get(messprogramm.getSamplePd())[1];
            this.intervallFactor = fieldsTable
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

    private List<Map<String, Object>> protocol;

    private Map<String, Object> currentProtocol;

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
        protocol = new ArrayList<>();

        /* Adjust to end of the day as we want to generate Sample objects
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
     *
     * @return The new probe object.
     */
    private Sample createObjects(
        Mpg messprogramm,
        Date startDate,
        Date endDate,
        boolean dryrun
    ) {
        currentProtocol = new HashMap<>();
        QueryBuilder<Sample> builderProbe =
            repository.queryBuilder(Sample.class);
        builderProbe.and("mpgId", messprogramm.getId());
        builderProbe.and("schedStartDate", startDate);
        builderProbe.and("schedEndDate", endDate);

        QueryBuilder<MpgMmtMp> builder =
            repository.queryBuilder(MpgMmtMp.class);
        builder.and("mpgId", messprogramm.getId());
        Response response = repository.filter(builder.getQuery());
        @SuppressWarnings("unchecked")
        List<MpgMmtMp> mmts = (List<MpgMmtMp>) response.getData();
        List<String> messungProtocol = new ArrayList<>();
        List<Sample> proben =
            repository.filterPlain(builderProbe.getQuery());

        QueryBuilder<OrtszuordnungMp> builderOrt =
            repository.queryBuilder(OrtszuordnungMp.class);
        builderOrt.and("messprogrammId", messprogramm.getId());
        List<OrtszuordnungMp> orte =
            repository.filterPlain(builderOrt.getQuery());

        if (!proben.isEmpty()) {
            proben.get(0).setFound(true);
            toProtocol(proben.get(0), dryrun);
            protocol.add(currentProtocol);
            for (int i = 0; i < mmts.size(); i++) {
                MpgMmtMp mmt = mmts.get(i);
                messungProtocol.add(mmt.getMmtId());
            }
            currentProtocol.put("mmt", messungProtocol);
            for (OrtszuordnungMp ort : orte) {
                Site o = repository.getByIdPlain(
                    Site.class, ort.getOrtId());
                currentProtocol.put("gemId", o.getMunicId());
            }
            return proben.get(0);
        }
        Sample probe = new Sample();
        probe.setOprModeId(messprogramm.getOprModeId());
        probe.setRegulationId(messprogramm.getRegulationId());
        probe.setEnvDescripDisplay(messprogramm.getEnvDescripId());
        probe = findMedia(probe);
        probe.setMeasFacilId(messprogramm.getMeasFacilId());
        probe.setApprLabId(messprogramm.getApprLabId());
        probe.setSampleMethId(messprogramm.getSampleMethId());
        probe.setSamplerId(messprogramm.getSamplerId());
        probe.setSchedStartDate(new Timestamp(startDate.getTime()));
        probe.setSchedEndDate(new Timestamp(endDate.getTime()));
        probe.setIsTest(messprogramm.getIsTest());
        probe.setEnvMediumId(messprogramm.getEnvMediumId());
        probe.setMpgId(messprogramm.getId());
        probe.setStateMpgId(messprogramm.getStateMpgId());
        probe.setReiAgGrId(messprogramm.getReiAgGrId());
        probe.setNuclFacilGrId(messprogramm.getNuclFacilGrId());
        probe.setFound(false);

        createObject(probe, dryrun);
        toProtocol(probe, dryrun);

        //Create zusatzwert objects
        Set<SampleSpecif> pZusatzs = messprogramm.getSampleSpecifs();
        List<String> zusatzWerts = new ArrayList<String>();
        if (pZusatzs != null) {
            for (SampleSpecif pZusatz: pZusatzs) {
                ZusatzWert zusatz = new ZusatzWert();
                zusatz.setProbeId(probe.getId());
                zusatz.setPzsId(pZusatz.getId());
                createObject(zusatz, dryrun);
                zusatzWerts.add(zusatz.getPzsId());
            }
            currentProtocol.put("pZws", zusatzWerts);
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

        for (int i = 0; i < mmts.size(); i++) {
            MpgMmtMp mmt = mmts.get(i);
            Messung messung = new Messung();
            messung.setIsCompleted(false);
            messung.setIsScheduled(true);
            messung.setMmtId(mmt.getMmtId());
            messung.setSampleId(probe.getId());
            createObject(messung, dryrun);
            messungProtocol.add(mmt.getMmtId());
            for (int mw : mmt.getMeasds()) {
                Messwert wert = new Messwert();
                wert.setMessgroesseId(mw);
                wert.setMessungsId(messung.getId());
                if (messprogramm.getUnitId() != null) {
                    wert.setMehId(messprogramm.getUnitId());
                } else {
                    wert.setMehId(0);
                }
                createObject(wert, dryrun);
            }
        }
        currentProtocol.put("mmt", messungProtocol);
        for (OrtszuordnungMp ort : orte) {
            Ortszuordnung ortP = new Ortszuordnung();
            ortP.setOrtszuordnungTyp(ort.getOrtszuordnungTyp());
            ortP.setProbeId(probe.getId());
            ortP.setOrtId(ort.getOrtId());
            ortP.setOzId(ort.getOzId());
            ortP.setOrtszusatztext(ort.getOrtszusatztext());
            createObject(ortP, dryrun);
            Site o = repository.getByIdPlain(
                Site.class, ortP.getOrtId());
            currentProtocol.put("gemId", o.getMunicId());
        }
        // Reolad the probe to have the old id
        if (!dryrun) {
            probe = (Sample) repository.getById(
                Sample.class, probe.getId()).getData();
        }
        protocol.add(currentProtocol);
        return probe;
    }

    private void toProtocol(Sample probe, boolean dryrun) {
        currentProtocol.put("id", probe.getId());
        currentProtocol.put("externeProbeId", probe.getExtId());
        currentProtocol.put("mstId", probe.getMeasFacilId());
        currentProtocol.put("datenbasisId", probe.getRegulationId());
        currentProtocol.put("baId", probe.getOprModeId());
        currentProtocol.put("probenartId", probe.getSampleMethId());
        currentProtocol.put("solldatumBeginn", probe.getSchedStartDate());
        currentProtocol.put("solldatumEnde", probe.getSchedEndDate());
        currentProtocol.put("mprId", probe.getMpgId());
        currentProtocol.put("mediaDesk", probe.getEnvDescripDisplay());
        currentProtocol.put("umwId", probe.getEnvMediumId());
        currentProtocol.put("probeNehmerId", probe.getSamplerId());
        currentProtocol.put("found", probe.isFound());
        currentProtocol.put("dryrun", dryrun);
    }

    private void createObject(Object item, boolean dryrun) {
        if (!dryrun) {
            // TODO: Do not rely on this being successful
            repository.create(item);
        }
    }

    /**
     * Search for the umwelt id using the 'deskriptor'.
     *
     * @param   probe   The probe object.
     *
     * @return The updated probe object.
     */
    public Sample findUmweltId(Sample probe) {
        String mediaDesk = probe.getEnvDescripDisplay();
        if (mediaDesk != null) {
            String[] mediaDeskParts = mediaDesk.split(" ");
            if (mediaDeskParts.length <= 1) {
                return probe;
            }
            probe.setEnvMediumId(findUmwelt(mediaDeskParts));
        }
        return probe;
    }

    /**
     * Search for the media description using the 'deskriptor'.
     *
     * @param   probe   The probe object
     *
     * @return The updated probe object.
     */
    public Sample findMedia(Sample probe) {
        String mediaDesk = probe.getEnvDescripDisplay();
        if (mediaDesk != null) {
            Object result = repository.queryFromString(
                "SELECT "
                + de.intevation.lada.model.master.SchemaName.NAME
                + ".get_media_from_media_desk( :mediaDesk );")
                    .setParameter("mediaDesk", mediaDesk)
                    .getSingleResult();
            probe.setEnvDescripName(result != null ? result.toString() : "");
        }
        return probe;
    }

    /**
     * Search for the umwelt id using the 'deskriptor'.
     *
     * @param   messprogramm    The messprogramm
     *
     * @return The updated messprogramm.
     */
    public Mpg findUmweltId(Mpg messprogramm) {
        String mediaDesk = messprogramm.getEnvDescripId();
        if (mediaDesk != null) {
            String[] mediaDeskParts = mediaDesk.split(" ");
            if (mediaDeskParts.length <= 1) {
                return messprogramm;
            }
            messprogramm.setEnvMediumId(findUmwelt(mediaDeskParts));
        }
        return messprogramm;
    }


    /**
     * Find the umwelt id for a given deskriptor.
     *
     * @param   mediaDesk   The deskriptor string
     *
     * @return The umwelt id or an empty string.
     */
    public String findUmwelt(String[] mediaDesk) {
        List<Integer> mediaIds = new ArrayList<Integer>();
        boolean zebs = false;
        Integer parent = null;
        Integer hdParent = null;
        Integer ndParent = null;
        if ("01".equals(mediaDesk[1])) {
            zebs = true;
        }
        for (int i = 1; i < mediaDesk.length; i++) {
            if ("00".equals(mediaDesk[i])) {
                mediaIds.add(-1);
                continue;
            }
            if (zebs && i < ZEBS5) {
                parent = hdParent;
            } else if (!zebs && i < ZEBS3) {
                parent = hdParent;
            } else {
                parent = ndParent;
            }
            QueryBuilder<EnvDescrip> builder =
                repository.queryBuilder(EnvDescrip.class);
            if (parent != null) {
                builder.and("predId", parent);
            }
            builder.and("levVal", mediaDesk[i]);
            builder.and("lev", i - 1);
            Response response =
                repository.filter(builder.getQuery());
            @SuppressWarnings("unchecked")
            List<EnvDescrip> data = (List<EnvDescrip>) response.getData();
            if (data.isEmpty()) {
                return null;
            }
            hdParent = data.get(0).getId();
            mediaIds.add(data.get(0).getId());
            if (i == 2) {
                ndParent = data.get(0).getId();
            }
        }
        return getUmwelt(mediaIds, zebs);
    }

    /**
     * Find the umwelt id in the database using media deskriptor ids.
     *
     * @param   media   The list of media ids.
     * @param   isZebs  Flag for type of the deskriptor.
     *
     * @return The umwelt id or an empty string.
     */
    private String getUmwelt(List<Integer> media, boolean isZebs) {
        QueryBuilder<EnvDescripEnvMediumMp> builder =
            repository.queryBuilder(EnvDescripEnvMediumMp.class);

        if (media.size() == 0) {
            return null;
        }

        int size = 1;
        for (int i = 0; i < media.size(); i++) {
            String field = "s" + (i > POS9 ? i : "0" + i);
            QueryBuilder<EnvDescripEnvMediumMp> tmp = builder.getEmptyBuilder();
            if (media.get(i) != -1) {
                tmp.and(field, media.get(i));
                tmp.or(field, null);
                builder.and(tmp);
            } else {
                builder.and(field, null);
            }
        }
        Response response =
            repository.filter(builder.getQuery());
        @SuppressWarnings("unchecked")
        List<EnvDescripEnvMediumMp> data =
            (List<EnvDescripEnvMediumMp>) response.getData();
        if (data.isEmpty()) {
            return null;
        }

        boolean unique = isUnique(data);
        if (unique) {
            return data.get(0).getEnvMediumId();
        } else {
            int found = -1;
            int lastMatch = -LM12;
            for (int i = 0; i < data.size(); i++) {
                int matches = -LM12;
                for (int j = size; j < LM12; j++) {
                    switch (j) {
                        case 1: if (media.get(1).equals(data.get(i).getS01())
                            || media.get(1).equals(-1) && data.get(i).getS01()
                                == null
                                ) {
                                    matches += 1;
                                }
                                break;
                        case 2: if (media.get(2).equals(data.get(i).getS02())
                            || media.get(2).equals(-1) && data.get(i).getS02()
                                == null
                                ) {
                                    matches += 1;
                                }
                                break;
                        case 3: if (media.get(3).equals(data.get(i).getS03())
                            || media.get(3).equals(-1) && data.get(i).getS03()
                                == null
                                ) {
                                    matches += 1;
                                }
                                break;
                        case 4: if (media.get(4).equals(data.get(i).getS04())
                            || media.get(4).equals(-1) && data.get(i).getS04()
                                == null
                                ) {
                                    matches += 1;
                                }
                                break;
                        case 5: if (media.get(5).equals(data.get(i).getS05())
                            || media.get(5).equals(-1) && data.get(i).getS05()
                                == null
                                ) {
                                    matches += 1;
                                }
                                break;
                        case 6: if (media.get(6).equals(data.get(i).getS06())
                            || media.get(6).equals(-1) && data.get(i).getS06()
                                == null
                                ) {
                                    matches += 1;
                                }
                                break;
                        case 7: if (media.get(7).equals(data.get(i).getS07())
                            || media.get(7).equals(-1) && data.get(i).getS07()
                                == null
                                ) {
                                    matches += 1;
                                }
                                break;
                        case 8: if (media.get(8).equals(data.get(i).getS08())
                            || media.get(8).equals(-1) && data.get(i).getS08()
                                == null
                                ) {
                                    matches += 1;
                                }
                                break;
                        case 9: if (media.get(9).equals(data.get(i).getS09())
                            || media.get(9).equals(-1) && data.get(i).getS09()
                                == null
                                ) {
                                    matches += 1;
                                }
                                break;
                        case 10: if (media.get(10).equals(data.get(i).getS10())
                            || media.get(10).equals(-1) && data.get(i).getS10()
                                == null
                                ) {
                                    matches += 1;
                                }
                                break;
                        case 11: if (media.get(11).equals(data.get(i).getS11())
                            || media.get(11).equals(-1) && data.get(i).getS11()
                                == null
                                ) {
                                    matches += 1;
                                }
                                break;
                        default: break;
                    }
                    if (matches > lastMatch) {
                        lastMatch = matches;
                        found = i;
                    }
                }
            }
            if (found >= 0) {
                return data.get(found).getEnvMediumId();
            }
            return null;
        }
    }

    /**
     * Determine if the entries in the list have the same umwelt id.
     *
     * @param   list    A list of DescriptorUmwelt objects.
     *
     * @return true if the objects have the same umwelt id else false.
     */
    private boolean isUnique(List<EnvDescripEnvMediumMp> list) {
        if (list.isEmpty()) {
            return false;
        }
        String element = list.get(0).getEnvMediumId();
        for (int i = 1; i < list.size(); i++) {
            if (!element.equals(list.get(i).getEnvMediumId())) {
                return false;
            }
        }
        return true;
    }

    public List<Map<String, Object>> getProtocol() {
        return protocol;
    }

    /**
     * Set the minimal 'deskriptor' acording to the umwelt.
     *
     * @param   probe    The probe
     *
     * @return The updated messprogramm.
     */
    public Sample getInitialMediaDesk(Sample probe) {
        String umweltId = probe.getEnvMediumId();
        if (umweltId != null) {
            probe.setEnvDescripDisplay(getInitialMediaDesk(umweltId));
        }
        return probe;
    }

    /**
     * Set the minimal 'deskriptor' acording to the umwelt.
     *
     * @param   messprogramm    The messprogramm
     *
     * @return The updated messprogramm.
     */
    public Mpg getInitialMediaDesk(Mpg messprogramm) {
        String umweltId = messprogramm.getEnvMediumId();
        if (umweltId != null) {
            messprogramm.setEnvDescripId(getInitialMediaDesk(umweltId));
        }
        return messprogramm;
    }

    /**
     * Find the minimal deskriptor string for the specified umwelt id
     *
     * @param   umId  The umwelt id.
     *
     * @return The deskripto string.
     */
    private String getInitialMediaDesk(String umwId) {
        logger.debug("getInitialMediaDesk - umw_id: " + umwId);
        String mediaDesk = "D:";
        QueryBuilder<EnvDescripEnvMediumMp> builder =
            repository.queryBuilder(EnvDescripEnvMediumMp.class);
        builder.and("envMediumId",umwId);
        Response response =
            repository.filter(builder.getQuery());
        @SuppressWarnings("unchecked")
        List<EnvDescripEnvMediumMp> data =
            (List<EnvDescripEnvMediumMp>) response.getData();
        if (data.isEmpty()) {
            logger.debug("getInitialMediaDesk - media_desk : D: 00 00 00 00 00 00 00 00 00 00 00 00");
            return "D: 00 00 00 00 00 00 00 00 00 00 00 00";
        } else {
            Integer s00 = data.get(0).getS00();
            Integer s01 = data.get(0).getS01();
            Integer s02 = data.get(0).getS02();
            Integer s03 = data.get(0).getS03();
            Integer s04 = data.get(0).getS04();
            Integer s05 = data.get(0).getS05();
            Integer s06 = data.get(0).getS06();
            Integer s07 = data.get(0).getS07();
            Integer s08 = data.get(0).getS08();
            Integer s09 = data.get(0).getS09();
            Integer s10 = data.get(0).getS10();
            Integer s11 = data.get(0).getS11();
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getS00() == null || !data.get(i).getS00().equals(s00)) {
                    s00 = null;
                }
                if (data.get(i).getS01() == null || !data.get(i).getS01().equals(s01)) {
                    s01 = null;
                }
                if (data.get(i).getS02() == null || !data.get(i).getS02().equals(s02)) {
                    s02 = null;
                }
                if (data.get(i).getS03() == null || !data.get(i).getS03().equals(s03)) {
                    s03 = null;
                }
                if (data.get(i).getS04() == null || !data.get(i).getS04().equals(s04)) {
                    s04 = null;
                }
                if (data.get(i).getS05() == null || !data.get(i).getS05().equals(s05)) {
                    s05 = null;
                }
                if (data.get(i).getS06() == null || !data.get(i).getS06().equals(s06)) {
                    s06 = null;
                }
                if (data.get(i).getS07() == null || !data.get(i).getS07().equals(s07)) {
                    s07 = null;
                }
                if (data.get(i).getS08() == null || !data.get(i).getS08().equals(s08)) {
                    s08 = null;
                }
                if (data.get(i).getS09() == null || !data.get(i).getS09().equals(s09)) {
                    s09 = null;
                }
                if (data.get(i).getS10() == null || !data.get(i).getS10().equals(s10)) {
                    s10 = null;
                }
                if (data.get(i).getS11() == null || !data.get(i).getS11().equals(s11)) {
                    s11 = null;
                }
            }
            EnvDescrip d;
            if (s00 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s00);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            if (s01 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s01);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            if (s02 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s02);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            if (s03 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s03);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            if (s04 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s04);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            if (s05 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s05);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            if (s06 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s06);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            if (s07 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s07);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            if (s08 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s08);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            if (s09 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s09);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            if (s10 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s10);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            if (s11 == null) {
                mediaDesk = mediaDesk + " 00";
            } else {
                d = repository.getByIdPlain(EnvDescrip.class, s11);
                mediaDesk = mediaDesk + String.format(SN_FORMAT,d.getLevVal());
            }
            logger.debug("getInitialMediaDesk - umw_desk: " + mediaDesk);
            return mediaDesk;
        }
    }
}
