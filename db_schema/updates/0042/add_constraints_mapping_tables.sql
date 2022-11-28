--#4559

ALTER TABLE master.env_specif_mp ALTER COLUMN env_medium_id SET NOT NULL;
ALTER TABLE master.env_specif_mp ALTER COLUMN sample_specif_id SET NOT NULL;

ALTER TABLE master.auth_coord_ofc_env_medium_mp ADD CONSTRAINT meas_facil_id_env_medium_id_unq UNIQUE(meas_facil_id, env_medium_id);
ALTER TABLE master.auth_coord_ofc_env_medium_mp ALTER COLUMN meas_facil_id SET NOT NULL;
ALTER TABLE master.auth_coord_ofc_env_medium_mp ALTER COLUMN env_medium_id SET NOT NULL;