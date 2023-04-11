SET role lada;

CREATE OR REPLACE FUNCTION lada.set_measm_status()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
    DECLARE status_id integer;
    BEGIN
        INSERT INTO lada.status_prot
            (meas_facil_id, date, text, measm_id, status_mp_id)
        VALUES ((SELECT meas_facil_id
                     FROM lada.sample
                     WHERE id = NEW.sample_id),
                now() AT TIME ZONE 'utc', '', NEW.id, 1)
        RETURNING id into status_id;
        UPDATE lada.measm SET status = status_id where id = NEW.id;
        RETURN NEW;
    END;
$BODY$;

CREATE OR REPLACE FUNCTION lada.update_status_measm()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
    BEGIN
        CASE
            WHEN new.status_mp_id in (2, 3, 4, 5, 6, 7, 8, 10, 11, 12)
            THEN
                UPDATE lada.measm SET is_completed = true, status = NEW.id
                    WHERE id = NEW.measm_id;
            WHEN new.status_mp_id in (1, 9, 13)
            THEN
                UPDATE lada.measm SET is_completed = false, status = NEW.id
                    WHERE id = NEW.measm_id;
            ELSE
                UPDATE lada.measm SET status = NEW.id 
                    WHERE id = NEW.measm_id;
        END CASE;
        RETURN NEW;
    END
$BODY$;
