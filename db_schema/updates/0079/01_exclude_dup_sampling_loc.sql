ALTER TABLE lada.geolocat
    DROP CONSTRAINT geolocat_sample_id_excl,
    ADD EXCLUDE (sample_id WITH =) WHERE (type_regulation IN('E', 'R'));

ALTER TABLE lada.geolocat_mpg
    DROP CONSTRAINT geolocat_mpg_mpg_id_excl,
    ADD EXCLUDE (mpg_id WITH =) WHERE (type_regulation IN('E', 'R'));
