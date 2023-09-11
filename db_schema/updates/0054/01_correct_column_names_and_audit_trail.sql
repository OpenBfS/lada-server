SET role lada;

UPDATE master.audit_trail
SET row_data=public.jsonb_rename_keys(row_data, ARRAY['x_coord_ext', 'coord_x_ext', 'y_coord_ext', 'coord_y_ext']),
changed_fields=public.jsonb_rename_keys(changed_fields, ARRAY['x_coord_ext', 'coord_x_ext', 'y_coord_ext', 'coord_y_ext'])
WHERE table_name='site';

-- Rename constraints according to renaming columns in update #46
DO $$
DECLARE
    old_name CONSTANT varchar = 'state_mpg';
    new_name CONSTANT varchar = 'mpg_categ';
    s varchar; t varchar; c varchar;
BEGIN
    FOR s, t, c IN SELECT tc.table_schema, tc.table_name, constraint_name
        FROM information_schema.constraint_column_usage AS ccu
            JOIN information_schema.table_constraints AS tc
                USING (constraint_schema, constraint_name)
        WHERE ccu.table_schema = 'master'
            AND ccu.table_name = 'mpg_categ' AND ccu.column_name = 'id'
    LOOP
        IF c LIKE '%' || old_name || '%' THEN
            EXECUTE format('ALTER TABLE %I.%I RENAME CONSTRAINT %I TO %I',
                s, t, c, replace(c, old_name, new_name));
        END IF;
    END LOOP;
END$$;

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
