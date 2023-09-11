DO $$
BEGIN
    EXECUTE format(
        'ALTER TABLE master.import_conf DROP CONSTRAINT %I',
        (SELECT constraint_name FROM information_schema.constraint_column_usage
            JOIN information_schema.table_constraints
                USING (table_schema, table_name, constraint_name)
            WHERE table_schema = 'master' AND table_name = 'import_conf'
                AND column_name = 'action' AND constraint_type = 'CHECK'));
END$$;
UPDATE master.import_conf SET action = upper(action);
ALTER TABLE master.import_conf
    ADD CHECK (action IN('DEFAULT', 'CONVERT', 'TRANSFORM'));
