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

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jboss.logging.Logger;

import de.intevation.lada.importer.Report;
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

    private Report report;

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
        report = new Report();

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
                report.addErrors(Map.of("Parser", errorListener.getErrors()));
                return;
            }
            report.addErrors(listener.getErrors());
            report.addWarnings(listener.getWarnings());
            if (!parserWarnings.isEmpty()) {
                report.addWarnings(Map.of("Parser", parserWarnings));
            }
            mapper.setUserInfo(userInfo);
            mapper.setConfig(config);
            mapper.setMeasFacilId(measFacilId);
            mapper.mapObjects(listener.getData());
            report.setSampleIds(mapper.getImportedProbeIds());
            report.addErrors(mapper.getErrors());
            report.addWarnings(mapper.getWarnings());
            report.addNotifications(mapper.getNotifications());
        } catch (IOException e) {
            logger.debug("Exception while reading LAF input", e);
        }
    }

    public Report getReport() {
        return this.report;
    }
}
