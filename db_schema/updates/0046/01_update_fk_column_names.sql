-- Updates fk column names to match the naming convention: {referenced_table}_{referenced_column}
ALTER TABLE master.filter RENAME COLUMN type TO filter_type_id;

ALTER TABLE master.site RENAME COLUMN munic_id TO admin_unit_id;
ALTER TABLE master.site RENAME COLUMN rei_nucl_facil_gr_id TO nucl_facil_gr_id;

ALTER TABLE master.query_meas_facil_mp RENAME COLUMN query_id TO query_user_id;

ALTER TABLE master.targ_act_targ RENAME COLUMN targ_env_medium_gr_id TO targ_env_gr_id;

ALTER TABLE master.env_medium RENAME COLUMN coord_ofc TO meas_facil_id;

ALTER TABLE master.ref_val RENAME COLUMN ref_val_meas_id TO ref_val_measure_id;

ALTER TABLE master.munic_div RENAME COLUMN munic_id TO admin_unit_id;

ALTER TABLE master.sample_specif RENAME COLUMN unit_id TO meas_unit_id;

ALTER TABLE lada.meas_val RENAME COLUMN unit_id TO meas_unit_id;

ALTER TABLE lada.mpg RENAME COLUMN munic_id TO admin_unit_id;
ALTER TABLE lada.mpg RENAME COLUMN state_mpg_id TO mpg_categ_id;
ALTER TABLE lada.mpg RENAME COLUMN unit_id TO meas_unit_id;

ALTER TABLE lada.status_prot RENAME column status_comb TO status_mp_id;

ALTER TABLE lada.mpg_mmt_measd_mp RENAME COLUMN mpg_mmt_id TO mpg_mmt_mp_id;

ALTER TABLE lada.sample RENAME COLUMN state_mpg_id TO mpg_categ_id;
