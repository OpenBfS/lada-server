ALTER TABLE master.query_meas_facil_mp
    ADD CONSTRAINT query_meas_facil_mp_query_id_meas_facil_id_unique
    UNIQUE (query_id, meas_facil_id);

ALTER TABLE master.rei_ag_gr_mp
    ADD CONSTRAINT rei_ag_gr_mp_rei_ag_gr_id_rei_ag_id_unique
        UNIQUE(rei_ag_gr_id, rei_ag_id),
    ALTER COLUMN rei_ag_gr_id SET NOT NULL,
    ALTER COLUMN rei_ag_id SET NOT NULL;

ALTER TABLE master.rei_ag_gr_env_medium_mp
    ADD CONSTRAINT rei_ag_gr_env_medium_mp_rei_ag_gr_id_env_medium_id_unique
        UNIQUE(rei_ag_gr_id, env_medium_id),
    ALTER COLUMN rei_ag_gr_id SET NOT NULL,
    ALTER COLUMN env_medium_id SET NOT NULL;

ALTER TABLE master.nucl_facil_gr_mp
    ADD CONSTRAINT nucl_facil_gr_mp_nucl_facil_gr_id_nucl_facil_id_unique
        UNIQUE(nucl_facil_gr_id, nucl_facil_id),
    ALTER COLUMN nucl_facil_gr_id SET NOT NULL,
    ALTER COLUMN nucl_facil_id SET NOT NULL;