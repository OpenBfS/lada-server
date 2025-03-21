/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.inject.Inject;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jboss.logging.Logger;

import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.master.ImportConf;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.StatusCodes;

/**
 * Importer for the LAF file format.
 */
public class LafImporter {

    @Inject
    private Logger logger;

    @Inject
    private LafObjectMapper mapper;

    private Map<String, List<ReportItem>> errors =
        new HashMap<String, List<ReportItem>>();
    private Map<String, List<ReportItem>> warnings =
        new HashMap<String, List<ReportItem>>();
    private Map<String, List<ReportItem>> notifications =
        new HashMap<String, List<ReportItem>>();
    private List<Integer> importProbeIds;

    /**
     * Start the import of the LAF data.
     * @param lafString The laf formated data as string
     * @param userInfo The current user info
     * @param measFacilId Default measFacilId
     * @param config The import config to use
     */
    public void doImport(
        String lafString,
        UserInfo userInfo,
        String measFacilId,
        List<ImportConf> config
    ) {
        // Append newline to avoid parser errors.
        // Every line can be the last line, so it is easier to append a
        // newline here than to extend the grammar with EOF for every line.
        lafString += "\r\n";
        errors = new HashMap<String, List<ReportItem>>();
        warnings = new HashMap<String, List<ReportItem>>();
        notifications = new HashMap<String, List<ReportItem>>();

        importProbeIds = new ArrayList<Integer>();

        try {
            CharStream ais = CharStreams.fromStream(new ByteArrayInputStream(
                lafString.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
            LafLexer lexer = new LafLexer(ais);
            CommonTokenStream cts = new CommonTokenStream(lexer);
            LafParser parser = new LafParser(cts);
            LafErrorListener errorListener = LafErrorListener.INSTANCE;
            errorListener.reset();
            parser.addErrorListener(errorListener);
            ParseTree tree = parser.probendatei();
            LafObjectListener listener = new LafObjectListener();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, tree);
            List<ReportItem> parserWarnings = listener.getParserWarnings();
            if (!listener.hasUebertragungsformat()) {
                ReportItem warn = new ReportItem();
                warn.setKey("UEBERTRAGUNGSFORMAT");
                warn.setValue("");
                warn.setCode(StatusCodes.IMP_MISSING_VALUE);
                parserWarnings.add(warn);
            }
            if (!listener.hasVersion()) {
                ReportItem warn = new ReportItem();
                warn.setKey("VERSION");
                warn.setValue("");
                warn.setCode(StatusCodes.IMP_MISSING_VALUE);
                parserWarnings.add(warn);
            }
            if (!errorListener.getErrors().isEmpty()) {
                errors.put("Parser", errorListener.getErrors());
                return;
            }
            errors.putAll(listener.getErrors());
            warnings.putAll(listener.getWarnings());
            if (!parserWarnings.isEmpty()) {
                warnings.put("Parser", parserWarnings);
            }
            mapper.setUserInfo(userInfo);
            mapper.setConfig(config);
            mapper.setMeasFacilId(measFacilId);
            mapper.mapObjects(listener.getData());
            importProbeIds = mapper.getImportedProbeIds();
            for (Entry<String, List<ReportItem>> entry
                : mapper.getErrors().entrySet()
            ) {
                if (errors.containsKey(entry.getKey())) {
                    errors.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    errors.put(entry.getKey(), entry.getValue());
                }
            }

            for (Entry<String, List<ReportItem>> entry
                : mapper.getWarnings().entrySet()
            ) {
                if (warnings.containsKey(entry.getKey())) {
                    warnings.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    warnings.put(entry.getKey(), entry.getValue());
                }
            }

            for (Entry<String, List<ReportItem>> entry
                : mapper.getNotifications().entrySet()
            ) {
                if (notifications.containsKey(entry.getKey())) {
                    notifications.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    notifications.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException e) {
            logger.debug("Exception while reading LAF input", e);
        }
    }

    public Map<String, List<ReportItem>> getErrors() {
        return this.errors;
    }

    public Map<String, List<ReportItem>> getWarnings() {
        return this.warnings;
    }

    public Map<String, List<ReportItem>> getNotifications() {
        return this.notifications;
    }

    public List<Integer> getImportedIds() {
        return this.importProbeIds;
    }
}
