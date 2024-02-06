ALTER TABLE lada.sample DROP CONSTRAINT sample_env_descrip_display_check;
ALTER TABLE lada.sample
    ADD CONSTRAINT sample_env_descrip_display_check CHECK 
        (env_descrip_display ~ '^D:( [0-9][0-9]){12}$');

ALTER TABLE lada.mpg DROP CONSTRAINT mpg_env_descrip_display_check;
ALTER TABLE lada.mpg
    ADD CONSTRAINT mpg_env_descrip_display_check CHECK 
        (env_descrip_display ~ '^D:( [0-9][0-9]){12}$');
--this needs imis3_db/lada/query_config/private_stammdaten_data_query.sql & update_query_config.sql to be run (removal of nuts_codes) 
/*ALTER TABLE master.base_query DROP CONSTRAINT base_query_sql_check;
ALTER TABLE master.base_query
    ADD CONSTRAINT base_query_sql_check CHECK 
        (master.check_sql(sql) AND sql ~ '^SELECT' AND sql !~* '.*DELETE.*|.*DROP.*|.*TRUNCATE.*|.*INSERT.*|.*UPDATE.*|.*GRANT.*|.*REVOKE.*');
*/