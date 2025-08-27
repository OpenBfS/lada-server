ALTER TABLE master.spat_ref_sys
    ADD UNIQUE(name),
    ALTER name SET NOT NULL;

ALTER TABLE master.opr_mode
    ADD UNIQUE(name);

ALTER TABLE master.network
    ALTER name SET NOT NULL;

ALTER TABLE master.regulation
    ADD UNIQUE(name),
    ALTER name SET NOT NULL;

ALTER TABLE master.env_descrip_env_medium_mp
    ADD UNIQUE NULLS NOT DISTINCT
        (s00, s01, s02, s03, s04, s05, s06, s07, s08, s09, s10, s11);

CREATE UNIQUE INDEX ON master.base_query (md5(sql));

ALTER TABLE master.query_user
    ADD UNIQUE(name, lada_user_id);

ALTER TABLE master.mmt
    ALTER name SET NOT NULL;

ALTER TABLE master.measd_gr
    ADD UNIQUE(name),
    ALTER name SET NOT NULL;

ALTER TABLE master.rei_ag
    ADD UNIQUE(name),
    ALTER descr SET NOT NULL;

ALTER TABLE master.rei_ag_gr
    ADD UNIQUE(name),
    ALTER name SET NOT NULL,
    ALTER descr SET NOT NULL;

ALTER TABLE master.nucl_facil_gr
    ADD UNIQUE(ext_id),
    ALTER name SET NOT NULL;

ALTER TABLE master.type_regulation
    ALTER name SET NOT NULL;

ALTER TABLE master.sample_meth
    ADD UNIQUE(ext_id),
    ALTER name SET NOT NULL;

ALTER TABLE master.disp
    ADD UNIQUE(name, format);

ALTER TABLE master.targ_act_mmt_gr
    ADD UNIQUE(name),
    ALTER name SET NOT NULL,
    ALTER descr SET NOT NULL;

ALTER TABLE master.targ_env_gr
    ALTER targ_env_gr_displ SET NOT NULL;
