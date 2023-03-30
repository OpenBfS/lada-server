ALTER TABLE master.query_meas_facil_mp
    ADD UNIQUE (query_id, meas_facil_id);

ALTER TABLE master.rei_ag_gr_mp
    ADD UNIQUE(rei_ag_gr_id, rei_ag_id),
    ALTER COLUMN rei_ag_gr_id SET NOT NULL,
    ALTER COLUMN rei_ag_id SET NOT NULL;

ALTER TABLE master.rei_ag_gr_env_medium_mp
    ADD UNIQUE(rei_ag_gr_id, env_medium_id),
    ALTER COLUMN rei_ag_gr_id SET NOT NULL,
    ALTER COLUMN env_medium_id SET NOT NULL;

ALTER TABLE master.nucl_facil_gr_mp
    ADD UNIQUE(nucl_facil_gr_id, nucl_facil_id),
    ALTER COLUMN nucl_facil_gr_id SET NOT NULL,
    ALTER COLUMN nucl_facil_id SET NOT NULL;

--This needs further thematic clarification and cannot be implemented so far (constraint violations)
--ALTER TABLE lada.mpg_mmt_mp
--    ADD UNIQUE(mpg_id, mmt_id);
