SET role lada;

-- Updates fk column names to match the naming convention: {referenced_table}_{referenced_column}
ALTER TABLE master.filter RENAME COLUMN type TO filter_type_id;

ALTER TABLE master.site RENAME COLUMN munic_id TO admin_unit_id;
ALTER TABLE master.site RENAME COLUMN rei_nucl_facil_gr_id TO nucl_facil_gr_id;

ALTER TABLE master.query_meas_facil_mp RENAME COLUMN query_id TO query_user_id;

ALTER TABLE master.targ_act_targ RENAME COLUMN targ_env_medium_gr_id TO targ_env_gr_id;

ALTER TABLE master.env_medium RENAME COLUMN coord_ofc TO meas_facil_id;

ALTER TABLE master.ref_val RENAME COLUMN ref_val_meas_id TO ref_val_measure_id;

ALTER TABLE master.munic_div RENAME COLUMN munic_id TO admin_unit_id;

ALTER TABLE master.sample_specif RENAME COLUMN unit_id TO meas_unit_id;

ALTER TABLE lada.meas_val RENAME COLUMN unit_id TO meas_unit_id;

ALTER TABLE lada.mpg RENAME COLUMN munic_id TO admin_unit_id;
ALTER TABLE lada.mpg RENAME COLUMN state_mpg_id TO mpg_categ_id;
ALTER TABLE lada.mpg RENAME COLUMN unit_id TO meas_unit_id;

ALTER TABLE lada.status_prot RENAME column status_comb TO status_mp_id;

ALTER TABLE lada.mpg_mmt_measd_mp RENAME COLUMN mpg_mmt_id TO mpg_mmt_mp_id;

ALTER TABLE lada.sample RENAME COLUMN state_mpg_id TO mpg_categ_id;

-- Update view columns
ALTER TABLE public.lada_meas_val RENAME COLUMN unit_id TO meas_unit_id;
ALTER TABLE public.lada_meas_val RENAME COLUMN status_comb TO status_mp_id;

ALTER TABLE lada.meas_val_view RENAME COLUMN unit_id TO meas_unit_id;
ALTER TABLE lada.meas_val_view RENAME COLUMN status_comb TO status_mp_id;

-- Update functions

CREATE OR REPLACE FUNCTION lada.set_measm_status() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;

CREATE OR REPLACE FUNCTION lada.update_status_measm() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
                UPDATE lada.measm SET status = NEW.id WHERE id = NEW.measm_id;
        END CASE;
        RETURN NEW;
    END
$$;

CREATE OR REPLACE FUNCTION lada.get_measms_per_site (site_id int)
RETURNS int
LANGUAGE plpgsql
AS
$$
DECLARE measms_per_site int;
BEGIN

    EXECUTE
    '
    SELECT COUNT(DISTINCT sa.id)
    FROM master.site s
    INNER JOIN lada.geolocat g ON s.id=g.site_id
    INNER JOIN lada.sample sa ON g.sample_id=sa.id
    INNER JOIN lada.measm m ON m.sample_id=sa.id
    INNER JOIN lada.status_prot sp ON m.status=sp.id
    WHERE s.id=' || site_id || ' and sp.status_mp_id IN (2,6,10)
    ' INTO measms_per_site;

    RETURN measms_per_site;

END;
$$;
