-- Remove redundant empty string check and allow WITH-queries
DO $$
DECLARE
    check_constr varchar;
BEGIN
    FOR check_constr IN (SELECT constraint_name
        FROM information_schema.constraint_column_usage
        JOIN information_schema.check_constraints USING (constraint_name)
        WHERE table_name = 'base_query' and table_schema='master' and check_constraints.constraint_schema='master')
    LOOP
        EXECUTE format(
            'ALTER TABLE master.base_query DROP CONSTRAINT %I', check_constr);
    END LOOP;
END $$;
ALTER TABLE master.base_query ADD CHECK(
    master.check_sql(sql)
    AND sql ~ '^(SELECT|WITH)'
    AND sql !~* '.*DELETE.*|.*DROP.*|.*TRUNCATE.*|.*INSERT.*|.*UPDATE.*|.*GRANT.*|.*REVOKE.*')
