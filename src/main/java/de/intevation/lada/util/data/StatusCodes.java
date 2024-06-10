/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.data;

public class StatusCodes {
    private StatusCodes() { };
    public static final int OK = 200;
    public static final int PRESENT = 601;
    public static final int NOT_A_COMMENT = 607;
    public static final int ERROR_DB_CONNECTION = 603;
    public static final int ERROR_VALIDATION = 604;
    public static final int VALUE_AMBIGOUS = 611;
    public static final int VALUE_MISSING = 631;
    public static final int VALUE_NOT_MATCHING = 632;
    public static final int VAL_DESK = 633;
    public static final int VAL_DATE_IN_FUTURE = 641;
    public static final int VAL_MEAS_DATE_BEFORE = 642;
    public static final int VAL_END_BEFORE_BEGIN = 643;
    public static final int VAL_EXISTS = 646;
    public static final int STATUS_RO = 654;
    public static final int ORT_SINGLE_UORT = 655;
    public static final int IMP_PARSER_ERROR = 670;
    public static final int IMP_PRESENT = 671;
    public static final int IMP_DUPLICATE = 672;
    public static final int IMP_MISSING_VALUE = 673;
    public static final int IMP_DATE_ERROR = 674;
    public static final int IMP_INVALID_VALUE = 675;
    public static final int IMP_UNCHANGABLE = 676;
    public static final int NO_ACCESS = 698;
    public static final int NOT_ALLOWED = 699;
}
