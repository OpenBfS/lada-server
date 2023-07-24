SET role lada;

UPDATE master.audit_trail
SET row_data=public.jsonb_rename_keys(row_data, ARRAY['x_coord_ext', 'coord_x_ext', 'y_coord_ext', 'coord_y_ext']),
changed_fields=public.jsonb_rename_keys(changed_fields, ARRAY['x_coord_ext', 'coord_x_ext', 'y_coord_ext', 'coord_y_ext'])
WHERE table_name='site';

ALTER TABLE lada.mpg RENAME CONSTRAINT mpg_state_mpg_id_fkey TO mpg_mpg_categ_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT sample_state_mpg_id_fkey TO sample_mpg_categ_id_fkey;

ALTER TABLE IF EXISTS master.convers_dm_fm RENAME COLUMN unit_id TO meas_unit_id;
ALTER TABLE IF EXISTS master.convers_dm_fm RENAME COLUMN to_unit_id TO to_meas_unit_id;

UPDATE lada.audit_trail
SET row_data=public.jsonb_rename_keys(row_data, ARRAY['unit_id', 'meas_unit_id']),
changed_fields=public.jsonb_rename_keys(changed_fields, ARRAY['unit_id', 'meas_unit_id'])
WHERE table_name='meas_val';

UPDATE lada.audit_trail
SET row_data=public.jsonb_rename_keys(row_data, ARRAY['state_mpg_id', 'mpg_categ_id']),
changed_fields=public.jsonb_rename_keys(changed_fields, ARRAY['state_mpg_id', 'mpg_categ_id'])
WHERE table_name='sample';