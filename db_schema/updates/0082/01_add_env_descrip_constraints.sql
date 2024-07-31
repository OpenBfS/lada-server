ALTER TABLE master.env_descrip
    ALTER lev SET NOT NULL,
    ALTER lev_val SET NOT NULL,
    ALTER name SET NOT NULL,
    ADD CHECK(lev BETWEEN 0 AND 11),
    ADD CHECK(lev_val BETWEEN 0 AND 99),
    ADD CHECK(lev = 0 AND pred_id IS NULL OR lev > 0 AND pred_id IS NOT NULL),
    ADD UNIQUE(pred_id, lev, lev_val),
    ADD EXCLUDE(lev_val WITH =) WHERE (lev = 0);
