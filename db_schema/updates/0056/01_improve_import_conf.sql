--alleviate UNIQUE constraint errors on SNAPSHOT 230814 lada
UPDATE master.import_conf SET from_val='Bq/m3' WHERE id IN (140, 37, 73);

ALTER TABLE master.import_conf
    ALTER to_val SET NOT NULL,
    ADD CHECK(action = 'DEFAULT' OR from_val IS NOT NULL),
    ADD CHECK(action <> 'TRANSFORM'
            OR from_val SIMILAR TO '[0-9a-f]+' AND to_val SIMILAR TO '[0-9a-f]+'),
    ADD UNIQUE(meas_facil_id, name, attribute, action, from_val);


