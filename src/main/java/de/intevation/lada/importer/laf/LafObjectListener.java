/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.util.data.StatusCodes;

/**
 * Listener to process the LAF data items.
 */
public class LafObjectListener extends LafBaseListener {

    LafRawData data;
    LafRawData.Sample currentProbe;
    LafRawData.Messung currentMessung;
    Map<String, String> currentUOrt;
    Map<String, String> currentEOrt;
    Map<String, List<ReportItem>> errors;
    Map<String, List<ReportItem>> warnings;
    ArrayList<ReportItem> currentErrors;
    ArrayList<ReportItem> currentWarnings;
    ArrayList<ReportItem> parserWarnings;

    private boolean hasDatenbasis = false;
    private boolean hasMessprogramm = false;
    private boolean hasUmwelt = false;
    private boolean hasZeitbasis = false;
    private boolean hasUebertragungsformat = false;
    private boolean hasVersion = false;
    private boolean hasEHerkunfstland = false;
    private boolean hasEGemeinde = false;
    private boolean hasEKoordinaten = false;
    private boolean probenNrContext = false;

    public LafObjectListener() {
        data = new LafRawData();
        errors = new HashMap<>();
        warnings = new HashMap<>();
        currentErrors = new ArrayList<>();
        currentWarnings = new ArrayList<>();
        parserWarnings = new ArrayList<>();
        currentUOrt = new HashMap<>();
        currentEOrt = new HashMap<>();
    }

    public LafRawData getData() {
        return data;
    }

    /**
     * @return the errors
     */
    public Map<String, List<ReportItem>> getErrors() {
        return errors;
    }

    /**
     * @return the warnings
     */
    public Map<String, List<ReportItem>> getWarnings() {
        return warnings;
    }

    public List<ReportItem> getParserWarnings() {
        return parserWarnings;
    }

    /**
     * @return the hasUebertragungsformat
     */
    public boolean hasUebertragungsformat() {
        return hasUebertragungsformat;
    }

    /**
     * @return the hasVersion
     */
    public boolean hasVersion() {
        return hasVersion;
    }

    @Override public void enterEnd(LafParser.EndContext ctx) {
        if (!parserWarnings.isEmpty()) {
            warnings.put("Parser", parserWarnings);
        }
        if (currentProbe != null) {
            data.addProbe(currentProbe);
            if (!currentErrors.isEmpty()) {
                errors.put(currentProbe.getIdentifier(), currentErrors);
            }
            if (!currentWarnings.isEmpty()) {
                warnings.put(currentProbe.getIdentifier(), currentWarnings);
            }

            currentErrors.clear();
            currentWarnings.clear();
            currentProbe = null;
            hasDatenbasis = false;
            hasMessprogramm = false;
            hasUmwelt = false;
            hasZeitbasis = false;
        }
    }

    @Override public void enterProbe(LafParser.ProbeContext ctx) {
        probenNrContext = false;
        if (currentMessung != null) {
            currentProbe.addMessung(currentMessung);
            currentMessung = null;
        }
        if (currentUOrt != null && !currentUOrt.isEmpty()) {
            currentProbe.addUrsprungsOrt(currentUOrt);
            currentUOrt.clear();
        }
        if (currentEOrt != null && !currentEOrt.isEmpty()) {
            currentProbe.addEntnahmeOrt(currentEOrt);
            currentEOrt.clear();
        }
        currentProbe = data.new Sample();
        hasEKoordinaten = false;
        hasEGemeinde = false;
        hasEHerkunfstland = false;
    }

    @Override public void exitProbe(LafParser.ProbeContext ctx) {
        data.addProbe(currentProbe);
        if (currentMessung != null) {
            currentProbe.addMessung(currentMessung);
            currentMessung = null;
        }
        if (currentUOrt != null && !currentUOrt.isEmpty()) {
            currentProbe.addUrsprungsOrt(currentUOrt);
            currentUOrt.clear();
        }
        if (currentEOrt != null && !currentEOrt.isEmpty()) {
            currentProbe.addEntnahmeOrt(currentEOrt);
            currentEOrt.clear();
        }
        if (!currentErrors.isEmpty()) {
            if (errors.containsKey(currentProbe.getIdentifier())) {
                errors.get(
                    currentProbe.getIdentifier()).addAll(
                        new ArrayList<>(currentErrors));
            } else {
                errors.put(
                    currentProbe.getIdentifier(),
                    new ArrayList<>(currentErrors));
            }
        }
        if (!currentWarnings.isEmpty()) {
            if (warnings.containsKey(currentProbe.getIdentifier())) {
                warnings.get(
                    currentProbe.getIdentifier()).addAll(
                        new ArrayList<>(currentWarnings));
            } else {
                warnings.put(
                    currentProbe.getIdentifier(),
                    new ArrayList<>(currentWarnings));
            }
        }
        currentProbe = data.new Sample();
        currentErrors.clear();
        currentWarnings.clear();
        currentProbe = null;
        hasEKoordinaten = false;
        hasEGemeinde = false;
        hasEHerkunfstland = false;
        hasDatenbasis = false;
        hasMessprogramm = false;
        hasUmwelt = false;
        hasZeitbasis = false;
    }

    @Override public void enterMessung(LafParser.MessungContext ctx) {
        if (probenNrContext) {
            return;
        }
        if (currentMessung != null) {
            currentProbe.addMessung(currentMessung);
        }
        if (currentUOrt != null && !currentUOrt.isEmpty()) {
            currentProbe.addUrsprungsOrt(currentUOrt);
            currentUOrt.clear();
        }
        if (currentEOrt != null && !currentEOrt.isEmpty()) {
            currentProbe.addEntnahmeOrt(currentEOrt);
            currentEOrt.clear();
        }
        currentMessung = data.new Messung();
        currentMessung.setHasErrors(false);
    }

    @Override public void exitMessung(LafParser.MessungContext ctx) {
        if (probenNrContext) {
            return;
        }
        currentProbe.addMessung(currentMessung);
        currentMessung = null;
    }

    @Override public void enterUrsprungsort(LafParser.UrsprungsortContext ctx) {
        if (currentMessung != null && !probenNrContext) {
            currentProbe.addMessung(currentMessung);
            currentMessung = data.new Messung();
            currentMessung.setHasErrors(false);
        }
        if (currentUOrt != null && !currentUOrt.isEmpty()) {
            currentProbe.addUrsprungsOrt(currentUOrt);
            currentUOrt.clear();
        }
        if (currentEOrt != null && !currentEOrt.isEmpty()) {
            currentProbe.addEntnahmeOrt(currentEOrt);
            currentEOrt.clear();
        }
    }

    @Override public void enterUebertragungsformat(
        LafParser.UebertragungsformatContext ctx
    ) {
        if (this.hasUebertragungsformat()) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            ReportItem warn = new ReportItem();
            warn.setKey(ctx.getChild(0).toString());
            warn.setValue("");
            warn.setCode(StatusCodes.IMP_MISSING_VALUE);
            parserWarnings.add(warn);
        }
        String value = ctx.getChild(1).toString();
        // Trim double qoutes.
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C1)
            || !value.equals("7")
        ) {
            ReportItem warn = new ReportItem();
            warn.setKey(ctx.getChild(0).toString());
            warn.setValue(value);
            warn.setCode(StatusCodes.VALUE_NOT_MATCHING);
            parserWarnings.add(warn);
        }
        hasUebertragungsformat = true;
    }

    @Override public void enterVersion(LafParser.VersionContext ctx) {
        if (this.hasVersion()) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            ReportItem warn = new ReportItem();
            warn.setKey(ctx.getChild(0).toString());
            warn.setValue("");
            warn.setCode(StatusCodes.IMP_MISSING_VALUE);
            parserWarnings.add(warn);
        }
        String value = ctx.getChild(1).toString();
        // Trim double qoutes.
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C4)
            || !value.equals("0084")
        ) {
            ReportItem warn = new ReportItem();
            warn.setKey(ctx.getChild(0).toString());
            warn.setValue(value);
            warn.setCode(StatusCodes.VALUE_NOT_MATCHING);
            parserWarnings.add(warn);
        }
        hasVersion = true;
    }

    @Override public void enterDatenbasis(LafParser.DatenbasisContext ctx) {
        if (this.hasDatenbasis) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        // Trim double qoutes.
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C6)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
        this.hasDatenbasis = true;
    }

    @Override public void enterDatenbasis_s(LafParser.Datenbasis_sContext ctx) {
        if (this.hasDatenbasis) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I2)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
        this.hasDatenbasis = true;
    }

    @Override public void enterNetzkennung(LafParser.NetzkennungContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C2)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterErzeuger(LafParser.ErzeugerContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C2)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterStaat_der_messstelle_lang(
        LafParser.Staat_der_messstelle_langContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterStaat_der_messstelle_kurz(
        LafParser.Staat_der_messstelle_kurzContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C5)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterStaat_der_messstelle_s(
        LafParser.Staat_der_messstelle_sContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterMessstelle(LafParser.MessstelleContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C5)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterMesslabor(LafParser.MesslaborContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C5)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterProbe_id(LafParser.Probe_idContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C16)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterMessungs_id(LafParser.Messungs_idContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I2)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (currentMessung == null) {
            currentMessung = data.new Messung();
        }
        currentMessung.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterProben_nr(LafParser.Proben_nrContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C13)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String hnr = "";
        String nnr = "";
        if (value.length() <= 9) {
            hnr = value;
        } else if (value.length() > 9 && value.length() <= 13) {
            hnr = value.substring(0, 9);
            nnr = value.substring(9, value.length());
            if (currentMessung == null) {
                currentMessung = data.new Messung();
            }
            currentMessung.addAttribute("NEBENPROBENNUMMER", nnr);
        } else {
            // empty value
            return;
        }
        currentProbe.addAttribute("HAUPTPROBENNUMMER", hnr);
        probenNrContext = true;
    }

    @Override public void enterHauptprobennummer(
        LafParser.HauptprobennummerContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C20)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterNebenprobennummer(
        LafParser.NebenprobennummerContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C4)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (currentMessung == null) {
            currentMessung = data.new Messung();
        }
        currentMessung.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterMessprogramm_c(
        LafParser.Messprogramm_cContext ctx
    ) {
        if (this.hasMessprogramm) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
        this.hasMessprogramm = true;
    }

    @Override public void enterMessprogramm_s(
        LafParser.Messprogramm_sContext ctx
    ) {
        if (this.hasMessprogramm) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C1)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
        this.hasMessprogramm = true;
    }

    @Override public void enterMessprogramm_land(
        LafParser.Messprogramm_landContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C3)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterProbenahmeinstitution(
        LafParser.ProbenahmeinstitutionContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C9)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterProbenart(LafParser.ProbenartContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C1)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterZeitbasis(LafParser.ZeitbasisContext ctx) {
        if (this.hasZeitbasis) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
        this.hasZeitbasis = true;
    }

    @Override public void enterZeitbasis_s(LafParser.Zeitbasis_sContext ctx) {
        if (this.hasZeitbasis) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I1)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
        this.hasZeitbasis = true;
    }

    @Override public void enterSoll_datum_uhrzeit_a(
        LafParser.Soll_datum_uhrzeit_aContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        if (ctx.getChildCount() < 4) {
            return;
        }
        String date = ctx.getChild(1).toString();
        date = date.replaceAll("\"", "");
        if (!date.matches(LafDataTypes.D8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(date);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String time = ctx.getChild(2).toString();
        time = time.replaceAll("\"", "");
        if (!time.matches(LafDataTypes.T4)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(time);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), date + ' ' + time);
    }

    @Override public void enterSoll_datum_uhrzeit_e(
        LafParser.Soll_datum_uhrzeit_eContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        if (ctx.getChildCount() < 4) {
            return;
        }
        String date = ctx.getChild(1).toString();
        date = date.replaceAll("\"", "");
        if (!date.matches(LafDataTypes.D8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(date);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String time = ctx.getChild(2).toString();
        time = time.replaceAll("\"", "").trim();
        if (!time.matches(LafDataTypes.T4)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(time);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), date + ' ' + time);
    }

    @Override public void enterUrsprungs_datum_uhrzeit(
        LafParser.Ursprungs_datum_uhrzeitContext ctx
    ) {

        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        if (ctx.getChildCount() < 4) {
            return;
        }
        String date = ctx.getChild(1).toString();
        date = date.replaceAll("\"", "");
        if (!date.matches(LafDataTypes.D8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(date);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String time = ctx.getChild(2).toString();
        time = time.replaceAll("\"", "");
        if (!time.matches(LafDataTypes.T4)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(time);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), date + ' ' + time);
    }

    @Override public void enterProbenahme_datum_uhrzeit_a(
        LafParser.Probenahme_datum_uhrzeit_aContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        if (ctx.getChildCount() < 4) {
            return;
        }
        String date = ctx.getChild(1).toString();
        date = date.replaceAll("\"", "");
        if (!date.matches(LafDataTypes.D8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(date);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String time = ctx.getChild(2).toString();
        time = time.replaceAll("\"", "");
        if (!time.matches(LafDataTypes.T4)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(time);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), date + ' ' + time);
    }

    @Override public void enterProbenahme_datum_uhrzeit_e(
        LafParser.Probenahme_datum_uhrzeit_eContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        if (ctx.getChildCount() < 4) {
            return;
        }
        String date = ctx.getChild(1).toString();
        date = date.replaceAll("\"", "");
        if (!date.matches(LafDataTypes.D8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(date);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String time = ctx.getChild(2).toString();
        time = time.replaceAll("\"", "");
        if (!time.matches(LafDataTypes.T4)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(time);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), date + ' ' + time);
    }

    @Override public void enterUmweltbereich_c(
        LafParser.Umweltbereich_cContext ctx
    ) {
        if (this.hasUmwelt) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
        this.hasUmwelt = true;
    }

    @Override public void enterUmweltbereich_s(
        LafParser.Umweltbereich_sContext ctx
    ) {
        if (this.hasUmwelt) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C3)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
        this.hasUmwelt = true;
    }

    @Override public void enterDeskriptoren(LafParser.DeskriptorenContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C26)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterRei_programmpunkt(
        LafParser.Rei_programmpunktContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C21)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterRei_programmpunktgruppe(
        LafParser.Rei_programmpunktgruppeContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C21)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterReferenz_datum_uhrzeit(
        LafParser.Referenz_datum_uhrzeitContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        if (ctx.getChildCount() < 4) {
            return;
        }
        String date = ctx.getChild(1).toString();
        date = date.replaceAll("\"", "");
        if (!date.matches(LafDataTypes.D8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(date);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String time = ctx.getChild(2).toString();
        time = time.replaceAll("\"", "");
        if (!time.matches(LafDataTypes.T4)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(time);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), date + ' ' + time);
    }

    @Override public void enterTestdaten(LafParser.TestdatenContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.BOOL)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterSzenario(LafParser.SzenarioContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C20)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterSek_datenbasis(
        LafParser.Sek_datenbasisContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterSek_datenbasis_s(
        LafParser.Sek_datenbasis_sContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I2)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterU_herkunftsland_lang(
        LafParser.U_herkunftsland_langContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentUOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
    }

    @Override public void enterU_herkunftsland_kurz(
        LafParser.U_herkunftsland_kurzContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C5)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentUOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
    }

    @Override public void enterU_herkunftsland_s(
        LafParser.U_herkunftsland_sContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentUOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
    }

    @Override public void enterU_gemeindeschluessel(
        LafParser.U_gemeindeschluesselContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentUOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
    }

    @Override public void enterU_gemeindename(
        LafParser.U_gemeindenameContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentUOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
   }

    @Override public void enterU_orts_zusatzkennzahl(
        LafParser.U_orts_zusatzkennzahlContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I3)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentUOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
    }

    @Override public void enterU_koordinaten(
        LafParser.U_koordinatenContext ctx
    ) {
        if (ctx.getChildCount() < 4) {
            return;
        }
        String art = ctx.getChild(1).toString();
        art = art.replaceAll("\"", "").trim();
        if (!art.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(art);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String koord1 = ctx.getChild(2).toString();
        koord1 = koord1.replaceAll("\"", "").trim();
        if (!koord1.matches(LafDataTypes.C22)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(koord1);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String koord2 = ctx.getChild(3).toString();
        koord2 = koord2.replaceAll("\"", "").trim();
        if (!koord2.matches(LafDataTypes.C22)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(koord2);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentUOrt.put("U_KOORDINATEN_X", koord1);
        currentUOrt.put("U_KOORDINATEN_Y", koord2);
        currentUOrt.put("U_KOORDINATEN_ART", art);
    }

    @Override public void enterU_koordinaten_s(
        LafParser.U_koordinaten_sContext ctx
    ) {
        if (ctx.getChildCount() < 4) {
            return;
        }
        String art = ctx.getChild(1).toString();
        art = art.replaceAll("\"", "");
        if (!art.matches(LafDataTypes.I2)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(art);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String koord1 = ctx.getChild(2).toString();
        koord1 = koord1.replaceAll("\"", "").trim();
        if (!koord1.matches(LafDataTypes.C22)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(koord1);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String koord2 = ctx.getChild(3).toString();
        koord2 = koord2.replaceAll("\"", "").trim();
        if (!koord2.matches(LafDataTypes.C22)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(koord2);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentUOrt.put("U_KOORDINATEN_X", koord1);
        currentUOrt.put("U_KOORDINATEN_Y", koord2);
        currentUOrt.put("U_KOORDINATEN_ART_S", art);
    }

    @Override public void enterU_orts_zusatzcode(
        LafParser.U_orts_zusatzcodeContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentUOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
    }

    @Override public void enterU_orts_zusatztext(
        LafParser.U_orts_zusatztextContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.MC50)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentUOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
    }

    @Override public void enterU_nuts_code(LafParser.U_nuts_codeContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C10)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentUOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
    }

    @Override public void enterP_herkunftsland_lang(
        LafParser.P_herkunftsland_langContext ctx
    ) {
        if (hasEHerkunfstland) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
        hasEHerkunfstland = true;
   }

    @Override public void enterP_herkunftsland_kurz(
        LafParser.P_herkunftsland_kurzContext ctx
    ) {
        if (hasEHerkunfstland) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C5)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
        hasEHerkunfstland = true;
    }

    @Override public void enterP_herkunftsland_s(
        LafParser.P_herkunftsland_sContext ctx
    ) {
        if (hasEHerkunfstland) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
        hasEHerkunfstland = true;
    }

    @Override public void enterP_gemeindeschluessel(
        LafParser.P_gemeindeschluesselContext ctx
    ) {
        if (hasEGemeinde) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
        hasEGemeinde = true;
    }

    @Override public void enterP_gemeindename(
        LafParser.P_gemeindenameContext ctx
    ) {
        if (hasEGemeinde) {
            return;
        }
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
        hasEGemeinde = true;
    }

    @Override public void enterP_orts_zusatzkennzahl(
        LafParser.P_orts_zusatzkennzahlContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I3)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
   }

    @Override public void enterP_koordinaten(
        LafParser.P_koordinatenContext ctx
    ) {
        if (hasEKoordinaten) {
            return;
        }
        if (ctx.getChildCount() < 4) {
            return;
        }
        String art = ctx.getChild(1).toString();
        art = art.replaceAll("\"", "").trim();
        if (!art.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(art);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String koord1 = ctx.getChild(2).toString();
        koord1 = koord1.replaceAll("\"", "").trim();
        if (!koord1.matches(LafDataTypes.C22)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(koord1);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String koord2 = ctx.getChild(3).toString();
        koord2 = koord2.replaceAll("\"", "").trim();
        if (!koord2.matches(LafDataTypes.C22)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(koord2);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentEOrt.put("P_KOORDINATEN_X", koord1);
        currentEOrt.put("P_KOORDINATEN_Y", koord2);
        currentEOrt.put("P_KOORDINATEN_ART", art);
        hasEKoordinaten = true;
    }

    @Override public void enterP_koordinaten_s(
        LafParser.P_koordinaten_sContext ctx
    ) {
        if (hasEKoordinaten) {
            return;
        }
        if (ctx.getChildCount() < 4) {
            return;
        }
        String art = ctx.getChild(1).toString();
        art = art.replaceAll("\"", "");
        if (!art.matches(LafDataTypes.I2)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(art);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String koord1 = ctx.getChild(2).toString();
        koord1 = koord1.replaceAll("\"", "").trim();
        if (!koord1.matches(LafDataTypes.C22)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(koord1);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String koord2 = ctx.getChild(3).toString();
        koord2 = koord2.replaceAll("\"", "").trim();
        if (!koord2.matches(LafDataTypes.C22)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(koord2);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentEOrt.put("P_KOORDINATEN_X", koord1);
        currentEOrt.put("P_KOORDINATEN_Y", koord2);
        currentEOrt.put("P_KOORDINATEN_ART_S", art);
        hasEKoordinaten = true;
    }

    @Override public void enterP_orts_zusatzcode(
        LafParser.P_orts_zusatzcodeContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
    }

    @Override public void enterP_orts_zusatztext(
        LafParser.P_orts_zusatztextContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.MC50)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
    }

    @Override public void enterP_nuts_code(LafParser.P_nuts_codeContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C10)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
   }

    @Override public void enterP_site_id(LafParser.P_site_idContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
   }

    @Override public void enterP_site_name(LafParser.P_site_nameContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
    }

    @Override public void enterP_hoehe_nn(LafParser.P_hoehe_nnContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
   }

    @Override public void enterP_hoehe_land(LafParser.P_hoehe_landContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (!"".equals(value)) {
            currentEOrt.put(ctx.getChild(0).toString().toUpperCase(), value);
        }
   }

    @Override public void enterMehrzweckfeld(
        LafParser.MehrzweckfeldContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.MC300)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        currentProbe.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterMess_datum_uhrzeit(
        LafParser.Mess_datum_uhrzeitContext ctx
    ) {
        if (ctx.getChildCount() < 4) {
            return;
        }
        String date = ctx.getChild(1).toString();
        date = date.replaceAll("\"", "");
        if (!date.matches(LafDataTypes.D8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(date);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String time = ctx.getChild(2).toString();
        time = time.replaceAll("\"", "");
        if (!time.matches(LafDataTypes.T4)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(time);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (currentMessung == null) {
            currentMessung = data.new Messung();
        }
        currentMessung.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), date + ' ' + time);
    }

    @Override public void enterMesszeit_sekunden(
        LafParser.Messzeit_sekundenContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.I8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (currentMessung == null) {
            currentMessung = data.new Messung();
        }
        currentMessung.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterMessmethode_c(
        LafParser.Messmethode_cContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (currentMessung == null) {
            currentMessung = data.new Messung();
        }
        currentMessung.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterMessmethode_s(
        LafParser.Messmethode_sContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C2)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (currentMessung == null) {
            currentMessung = data.new Messung();
        }
        currentMessung.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterBearbeitungsstatus(
        LafParser.BearbeitungsstatusContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.C4)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (currentMessung == null) {
            currentMessung = data.new Messung();
        }
        currentMessung.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterPep_flag(LafParser.Pep_flagContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.BOOL)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (currentMessung == null) {
            currentMessung = data.new Messung();
        }
        currentMessung.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterErfassung_abgeschlossen(
        LafParser.Erfassung_abgeschlossenContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String value = ctx.getChild(1).toString();
        value = value.replaceAll("\"", "").trim();
        if (!value.matches(LafDataTypes.BOOL)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(value);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        if (currentMessung == null) {
            currentMessung = data.new Messung();
        }
        currentMessung.addAttribute(
            ctx.getChild(0).toString().toUpperCase(), value);
    }

    @Override public void enterProbenzusatzbeschreibung(
        LafParser.ProbenzusatzbeschreibungContext ctx
    ) {
        // c7* f12 c12 f9
        if (ctx.getChildCount() < 5) {
            return;
        }
        String groesse = ctx.getChild(1).toString();
        groesse = groesse.replaceAll("\"", "").trim();
        if (!groesse.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(groesse);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String wert = ctx.getChild(2).toString();
        wert = wert.replaceAll("\"", "");
        if (!wert.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(wert);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String einheit = ctx.getChild(3).toString();
        einheit = einheit.replaceAll("\"", "").trim();
        if (!einheit.matches(LafDataTypes.C12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(einheit);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        Map<String, String> zusatzwert = new HashMap<String, String>();
        if (ctx.getChildCount() >= 6) {
            String fehler = ctx.getChild(4).toString();
            fehler = fehler.replaceAll("\"", "");
            if (!fehler.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(fehler);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                return;
            }
            zusatzwert.put("MESSFEHLER", fehler);
        }
        zusatzwert.put("PZS", groesse);
        zusatzwert.put("MESSWERT_PZS", wert);
        zusatzwert.put("EINHEIT", einheit);
        currentProbe.addZusatzwert(zusatzwert);
    }

    @Override public void enterPzb_s(LafParser.Pzb_sContext ctx) {
        // sc8* f12 si3 f9
        if (ctx.getChildCount() < 5) {
            return;
        }
        String groesse = ctx.getChild(1).toString();
        groesse = groesse.replaceAll("\"", "").trim();
        if (!groesse.matches(LafDataTypes.C8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(groesse);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String wert = ctx.getChild(2).toString();
        wert = wert.replaceAll("\"", "");
        if (!wert.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(wert);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String einheit = ctx.getChild(3).toString();
        einheit = einheit.replaceAll("\"", "");
        if (!einheit.matches(LafDataTypes.I3)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(einheit);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        Map<String, String> zusatzwert = new HashMap<String, String>();
        if (ctx.getChildCount() >= 6) {
            String fehler = ctx.getChild(4).toString();
            fehler = fehler.replaceAll("\"", "");
            if (!fehler.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(fehler);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                return;
            }
            zusatzwert.put("MESSFEHLER", fehler);
        }
        zusatzwert.put("PZS_ID", groesse);
        zusatzwert.put("MESSWERT_PZS", wert);
        zusatzwert.put("EINHEIT_ID", einheit);
        currentProbe.addZusatzwert(zusatzwert);
    }

    @Override public void enterMesswert(LafParser.MesswertContext ctx) {
        // c50* f12 c12 f9**
        List<String> children = new ArrayList<String>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (!ctx.getChild(i).toString().startsWith(" ")) {
                children.add(ctx.getChild(i).toString());
            }
        }
        if (children.size() < 5) {
            currentMessung.setHasErrors(true);
            return;
        }
        String groesse = children.get(1);
        groesse = groesse.replaceAll("\"", "").trim();
        if (!groesse.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(groesse);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        String wert = children.get(2);
        wert = wert.replaceAll("\"", "");
        if (!wert.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(wert);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        String einheit = children.get(3);
        einheit = einheit.replaceAll("\"", "").trim();
        if (!einheit.matches(LafDataTypes.C12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(einheit);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        Map<String, String> messwert = new HashMap<String, String>();
        String fehler = null;
        if (ctx.getChildCount() >= 6) {
            fehler = children.get(4);
            fehler = fehler.replaceAll("\"", "");
            if (!fehler.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(fehler);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                currentMessung.setHasErrors(true);
                return;
            }
            messwert.put("MESSFEHLER", fehler);
        }
        messwert.put("MESSGROESSE", groesse);
        messwert.put("MESSWERT", wert);
        messwert.put("MESSEINHEIT", einheit);
        if (currentMessung == null) {
            currentMessung = data.new Messung();
            currentMessung.setHasErrors(false);
        }
        currentMessung.addMesswert(messwert);
    }

    @Override public void enterMesswert_s(LafParser.Messwert_sContext ctx) {
        // si8 f12 si3 f9**
        List<String> children = new ArrayList<String>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (!ctx.getChild(i).toString().startsWith(" ")) {
                children.add(ctx.getChild(i).toString());
            }
        }
        if (children.size() < 5) {
            currentMessung.setHasErrors(true);
            return;
        }
        String groesse = children.get(1);
        groesse = groesse.replaceAll("\"", "");
        if (!groesse.matches(LafDataTypes.I8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(groesse);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        String wert = children.get(2);
        wert = wert.replaceAll("\"", "");
        if (!wert.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(wert);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        String einheit = children.get(3);
        einheit = einheit.replaceAll("\"", "");
        if (!einheit.matches(LafDataTypes.I3)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(einheit);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        Map<String, String> messwert = new HashMap<String, String>();
        if (ctx.getChildCount() >= 6) {
            String fehler = children.get(4);
            fehler = fehler.replaceAll("\"", "");
            if (!fehler.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(fehler);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                currentMessung.setHasErrors(true);
                return;
            }
            messwert.put("MESSFEHLER", fehler);
        }
        messwert.put("MESSGROESSE_ID", groesse);
        messwert.put("MESSWERT", wert);
        messwert.put("MESSEINHEIT_ID", einheit);
        if (currentMessung == null) {
            currentMessung = data.new Messung();
            currentMessung.setHasErrors(false);
        }
        currentMessung.addMesswert(messwert);
    }

    @Override public void enterMesswert_i(LafParser.Messwert_iContext ctx) {
        // C50* f12 c12 f9** f9** f9** c50*
        List<String> children = new ArrayList<String>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (!ctx.getChild(i).toString().startsWith(" ")) {
                children.add(ctx.getChild(i).toString());
            }
        }
        if (children.size() < 8) {
            currentMessung.setHasErrors(true);
            return;
        }
        String groesse = children.get(1);
        groesse = groesse.replaceAll("\"", "").trim();
        if (!groesse.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(groesse);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        String wert = children.get(2);
        wert = wert.replaceAll("\"", "");
        if (!wert.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(wert);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        String einheit = children.get(3);
        einheit = einheit.replaceAll("\"", "").trim();
        if (!einheit.matches(LafDataTypes.C12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(einheit);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        Map<String, String> messwert = new HashMap<String, String>();
        if (ctx.getChildCount() >= 6) {
            String fehler = children.get(4);
            fehler = fehler.replaceAll("\"", "");
            if (!fehler.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(fehler);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                currentMessung.setHasErrors(true);
                return;
            }
            messwert.put("MESSFEHLER", fehler);
        }
        // TODO handle all values
        messwert.put("MESSGROESSE", groesse);
        messwert.put("MESSWERT", wert);
        messwert.put("MESSEINHEIT", einheit);
        if (currentMessung == null) {
            currentMessung = data.new Messung();
            currentMessung.setHasErrors(false);
        }
        currentMessung.addMesswert(messwert);
    }

    @Override public void enterMesswert_g(LafParser.Messwert_gContext ctx) {
        // C50* f12 c12 f9** f9** f9** c1
        if (currentMessung == null) {
            currentMessung = data.new Messung();
            currentMessung.setHasErrors(false);
        }
        List<String> children = new ArrayList<String>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (!ctx.getChild(i).toString().startsWith(" ")) {
                children.add(ctx.getChild(i).toString());
            }
        }
        if (children.size() < 8) {
            currentMessung.setHasErrors(true);
            return;
        }
        String groesse = children.get(1);
        groesse = groesse.replaceAll("\"", "").trim();
        if (!groesse.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(groesse);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        String wert = children.get(2);
        wert = wert.replaceAll("\"", "");
        if (!wert.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(wert);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        String einheit = children.get(3);
        einheit = einheit.replaceAll("\"", "").trim();
        if (!einheit.matches(LafDataTypes.C12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(einheit);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        Map<String, String> messwert = new HashMap<String, String>();
        String fehler = null;
        if (ctx.getChildCount() >= 8) {
            fehler = children.get(4);
            fehler = fehler.replaceAll("\"", "");
            if (!fehler.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(fehler);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                currentMessung.setHasErrors(true);
                return;
            }
            messwert.put("MESSFEHLER", fehler);
        }
        // TODO handle all values
        messwert.put("MESSGROESSE", groesse);
        messwert.put("MESSWERT", wert);
        messwert.put("MESSEINHEIT", einheit);
        currentMessung.addMesswert(messwert);
    }

    @Override public void enterMesswert_nwg(LafParser.Messwert_nwgContext ctx) {
        // C50* f12 c12 f9** f12
        if (currentMessung == null) {
            currentMessung = data.new Messung();
            currentMessung.setHasErrors(false);
        }
        List<String> children = new ArrayList<String>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (!ctx.getChild(i).toString().startsWith(" ")) {
                children.add(ctx.getChild(i).toString());
            }
        }
        if (children.size() < 5) {
            currentMessung.setHasErrors(true);
            return;
        }
        String groesse = children.get(1);
        groesse = groesse.replaceAll("\"", "").trim();
        if (!groesse.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(groesse);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        String wert = children.get(2);
        wert = wert.replaceAll("\"", "");
        if (!wert.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(wert);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        String einheit = children.get(3);
        einheit = einheit.replaceAll("\"", "").trim();
        if (!einheit.matches(LafDataTypes.C12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(einheit);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            currentMessung.setHasErrors(true);
            return;
        }
        Map<String, String> messwert = new HashMap<String, String>();
        if (ctx.getChildCount() >= 6) {
            String fehler = children.get(4);
            fehler = fehler.replaceAll("\"", "");
            if (!fehler.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(fehler);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                currentMessung.setHasErrors(true);
                return;
            }
            messwert.put("MESSFEHLER", fehler);
        }
        if (ctx.getChildCount() >= 7) {
            String nwg = children.get(5);
            nwg = nwg.replaceAll("\"", "");
            if (!nwg.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(nwg);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                currentMessung.setHasErrors(true);
                return;
            }
            messwert.put("NWG", nwg);
        }
        messwert.put("MESSGROESSE", groesse);
        messwert.put("MESSWERT", wert);
        messwert.put("MESSEINHEIT", einheit);
        currentMessung.addMesswert(messwert);
    }

    @Override public void enterMesswert_nwg_s(
        LafParser.Messwert_nwg_sContext ctx
    ) {
        if (currentMessung == null) {
            currentMessung = data.new Messung();
            currentMessung.setHasErrors(false);
        }
        List<String> children = new ArrayList<String>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (!ctx.getChild(i).toString().startsWith(" ")) {
                children.add(ctx.getChild(i).toString());
            }
        }
        if (children.size() < 6) {
            return;
        }
        String groesse = children.get(1);
        groesse = groesse.replaceAll("\"", "");
        if (!groesse.matches(LafDataTypes.I8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(groesse);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String wert = children.get(2);
        wert = wert.replaceAll("\"", "");
        if (!wert.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(wert);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String einheit = children.get(3);
        einheit = einheit.replaceAll("\"", "");
        if (!einheit.matches(LafDataTypes.I3)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(einheit);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        Map<String, String> messwert = new HashMap<String, String>();
        if (ctx.getChildCount() >= 6) {
            String fehler = children.get(4);
            fehler = fehler.replaceAll("\"", "");
            if (!fehler.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(fehler);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                return;
            }
            messwert.put("MESSFEHLER", fehler);
        }
        if (ctx.getChildCount() >= 7) {
            String nwg = children.get(5);
            nwg = nwg.replaceAll("\"", "");
            if (!nwg.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(nwg);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                return;
            }
            messwert.put("NWG", nwg);
        }
        // TODO handle all values
        messwert.put("MESSGROESSE_ID", groesse);
        messwert.put("MESSWERT", wert);
        messwert.put("MESSEINHEIT_ID", einheit);
        currentMessung.addMesswert(messwert);
    }

    @Override public void enterMesswert_nwg_i(
        LafParser.Messwert_nwg_iContext ctx
    ) {
        List<String> children = new ArrayList<String>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (!ctx.getChild(i).toString().startsWith(" ")) {
                children.add(ctx.getChild(i).toString());
            }
        }
        if (children.size() < 8) {
            return;
        }
        String groesse = children.get(1);
        groesse = groesse.replaceAll("\"", "").trim();
        if (!groesse.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(groesse);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String wert = children.get(2);
        wert = wert.replaceAll("\"", "");
        if (!wert.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(wert);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String einheit = children.get(3);
        einheit = einheit.replaceAll("\"", "").trim();
        if (!einheit.matches(LafDataTypes.C12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(einheit);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        Map<String, String> messwert = new HashMap<String, String>();
        if (ctx.getChildCount() >= 6) {
            String fehler = children.get(4);
            fehler = fehler.replaceAll("\"", "");
            if (!fehler.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(fehler);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                return;
            }
            messwert.put("MESSFEHLER", fehler);
        }
        if (ctx.getChildCount() >= 7) {
            String nwg = children.get(5);
            nwg = nwg.replaceAll("\"", "");
            if (!nwg.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(nwg);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                return;
            }
            messwert.put("NWG", nwg);
        }
        // TODO handle all values
        messwert.put("MESSGROESSE", groesse);
        messwert.put("MESSWERT", wert);
        messwert.put("MESSEINHEIT", einheit);
        if (currentMessung == null) {
            currentMessung = data.new Messung();
        }
        currentMessung.addMesswert(messwert);
    }

    @Override public void enterMesswert_nwg_g(
        LafParser.Messwert_nwg_gContext ctx
    ) {
        if (currentMessung == null) {
            currentMessung = data.new Messung();
            currentMessung.setHasErrors(false);
        }
        List<String> children = new ArrayList<String>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (!ctx.getChild(i).toString().startsWith(" ")) {
                children.add(ctx.getChild(i).toString());
            }
        }
        if (children.size() < 8) {
            return;
        }
        String groesse = children.get(1);
        groesse = groesse.replaceAll("\"", "").trim();
        if (!groesse.matches(LafDataTypes.C_STAR)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(groesse);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String wert = children.get(2);
        wert = wert.replaceAll("\"", "");
        if (!wert.matches(LafDataTypes.F9_10_12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(wert);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String einheit = children.get(3);
        einheit = einheit.replaceAll("\"", "").trim();
        if (!einheit.matches(LafDataTypes.C12)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(einheit);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        Map<String, String> messwert = new HashMap<String, String>();
        if (ctx.getChildCount() >= 6) {
            String fehler = children.get(4);
            fehler = fehler.replaceAll("\"", "");
            if (!fehler.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(fehler);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                return;
            }
            messwert.put("MESSFEHLER", fehler);
        }
        if (ctx.getChildCount() >= 6) {
            String nwg = children.get(5);
            nwg = nwg.replaceAll("\"", "");
            if (!nwg.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(nwg);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                return;
            }
            messwert.put("NWG", nwg);
        }
        if (ctx.getChildCount() >= 6) {
            String gw = children.get(8);
            gw = gw.replaceAll("\"", "");
            if (!gw.matches(LafDataTypes.F9_10_12)) {
                ReportItem err = new ReportItem();
                err.setKey(ctx.getChild(0).toString());
                err.setValue(gw);
                err.setCode(StatusCodes.IMP_PARSER_ERROR);
                currentErrors.add(err);
                return;
            }
            messwert.put("GRENZWERT", gw);
        }
        // TODO handle all values
        messwert.put("MESSGROESSE", groesse);
        messwert.put("MESSWERT", wert);
        messwert.put("MESSEINHEIT", einheit);
        currentMessung.addMesswert(messwert);
    }

    @Override public void enterKommentar(LafParser.KommentarContext ctx) {
        // c5 d8 t4 mc300
        if (currentMessung == null) {
            currentMessung = data.new Messung();
            currentMessung.setHasErrors(false);
        }
        if (ctx.getChildCount() < 6) {
            return;
        }
        String mst = ctx.getChild(1).toString();
        mst = mst.replaceAll("\"", "").trim();
        if (!mst.matches(LafDataTypes.C5)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(mst);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String date = ctx.getChild(2).toString();
        date = date.replaceAll("\"", "");
        if (!date.matches(LafDataTypes.D8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(date);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String time = ctx.getChild(3).toString();
        time = time.replaceAll("\"", "");
        if (!time.matches(LafDataTypes.T4)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(time);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String text = ctx.getChild(4).toString();
        text = text.replaceAll("\"", "");
        if (!text.matches(LafDataTypes.MC300)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(text);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        Map<String, String> kommentar = new HashMap<String, String>();
        kommentar.put("MST_ID", mst);
        kommentar.put("DATE", date);
        kommentar.put("TIME", time);
        kommentar.put("TEXT", text);
        currentMessung.addKommentar(kommentar);
    }

    @Override public void enterKommentar_t(LafParser.Kommentar_tContext ctx) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String text = ctx.getChild(1).toString();
        text = text.replaceAll("\"", "");
        if (!text.matches(LafDataTypes.MC300)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(text);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        Map<String, String> kommentar = new HashMap<String, String>();
        kommentar.put("TEXT", text);
        if (currentMessung == null) {
            currentMessung = data.new Messung();
        }
        currentMessung.addKommentar(kommentar);
    }

    @Override public void enterProbenkommentar(
        LafParser.ProbenkommentarContext ctx
    ) {
        // c5 d8 t4 mc300
        if (ctx.getChildCount() < 6) {
            return;
        }

        String mst = ctx.getChild(1).toString();
        mst = mst.replaceAll("\"", "").trim();
        if (!mst.matches(LafDataTypes.C5)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(mst);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String date = ctx.getChild(2).toString();
        date = date.replaceAll("\"", "");
        if (!date.matches(LafDataTypes.D8)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(date);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String time = ctx.getChild(3).toString();
        time = time.replaceAll("\"", "");
        if (!time.matches(LafDataTypes.T4)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(time);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        String text = ctx.getChild(4).toString();
        text = text.replaceAll("\"", "");
        if (!text.matches(LafDataTypes.MC300)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(text);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        Map<String, String> kommentar = new HashMap<String, String>();
        kommentar.put("MST_ID", mst);
        kommentar.put("DATE", date);
        kommentar.put("TIME", time);
        kommentar.put("TEXT", text);
        currentProbe.addKommentar(kommentar);
    }

    @Override public void enterProbenkommentar_t(
        LafParser.Probenkommentar_tContext ctx
    ) {
        if (ctx.getChildCount() == 2) {
            // empty value
            return;
        }
        String text = ctx.getChild(1).toString();
        text = text.replaceAll("\"", "");
        if (!text.matches(LafDataTypes.MC300)) {
            ReportItem err = new ReportItem();
            err.setKey(ctx.getChild(0).toString());
            err.setValue(text);
            err.setCode(StatusCodes.IMP_PARSER_ERROR);
            currentErrors.add(err);
            return;
        }
        Map<String, String> kommentar = new HashMap<String, String>();
        kommentar.put("TEXT", text);
        currentProbe.addKommentar(kommentar);
    }
}
