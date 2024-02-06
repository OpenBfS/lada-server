SET role lada;

DROP VIEW stamm.sollist_mmtgrp_zuord;
ALTER TABLE master.targ_act_mmt_gr_mp
    DROP COLUMN id,
    ADD PRIMARY KEY(mmt_id, targ_act_mmt_gr_id);
CREATE VIEW stamm.sollist_mmtgrp_zuord AS SELECT
	mmt_id,
	targ_act_mmt_gr_id AS sollist_mmtgrp_id
FROM master.targ_act_mmt_gr_mp;

DROP VIEW stamm.sollist_umwgrp_zuord;
ALTER TABLE master.targ_env_gr_mp
    DROP COLUMN id,
    ADD PRIMARY KEY(targ_env_gr_id, env_medium_id);
CREATE VIEW stamm.sollist_umwgrp_zuord AS SELECT
	targ_env_gr_id AS sollist_umwgrp_id,
	env_medium_id AS umw_id
FROM master.targ_env_gr_mp;
