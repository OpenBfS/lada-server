DO $$
DECLARE drop_constr varchar;
BEGIN
    drop_constr = DISTINCT constraint_name
        FROM information_schema.constraint_column_usage
            JOIN information_schema.table_constraints
                USING (constraint_name, table_schema, table_name)
        WHERE table_schema = 'lada'
            AND table_name = 'mpg_mmt_mp'
            AND constraint_type = 'UNIQUE'
            AND column_name in('mpg_id', 'mmt_id');
    EXECUTE format(
        'ALTER TABLE lada.mpg_mmt_mp DROP CONSTRAINT %I', drop_constr);
END$$;
