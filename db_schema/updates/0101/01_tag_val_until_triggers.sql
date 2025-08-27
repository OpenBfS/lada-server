-- Tags
CREATE FUNCTION master.tag_val_until_meas_facil_default()
    RETURNS timestamp without time zone STABLE PARALLEL SAFE
    RETURN current_timestamp AT TIME ZONE 'utc' + '365 days';

CREATE FUNCTION master.set_tag_val_until() RETURNS trigger LANGUAGE plpgsql AS $$
    BEGIN
        CASE
            WHEN NEW.meas_facil_id IS NOT NULL THEN
                NEW.val_until = master.tag_val_until_meas_facil_default();
            WHEN NEW.is_auto_tag THEN
                NEW.val_until = current_timestamp AT TIME ZONE 'utc' + '584 days';
        END CASE;
        RETURN NEW;
    END;
$$;

CREATE TRIGGER default_val_until BEFORE INSERT OR UPDATE OF val_until
    ON master.tag FOR EACH ROW
    WHEN (NEW.val_until IS NULL
        AND (NEW.meas_facil_id IS NOT NULL OR NEW.is_auto_tag))
    EXECUTE PROCEDURE master.set_tag_val_until();


-- Tag links
CREATE FUNCTION lada.extend_tag_val_until() RETURNS trigger LANGUAGE plpgsql AS $$
    DECLARE new_validity CONSTANT timestamp without time zone
        = master.tag_val_until_meas_facil_default();
    BEGIN
        UPDATE master.tag SET val_until = new_validity
            WHERE meas_facil_id IS NOT NULL
                AND val_until < new_validity
                AND id IN(SELECT tag_id FROM added_tag_links);
        RETURN NULL;
    END;
$$;

CREATE TRIGGER extend_tag_val_until AFTER INSERT ON lada.tag_link_measm
    REFERENCING NEW TABLE AS added_tag_links
    FOR EACH STATEMENT EXECUTE PROCEDURE lada.extend_tag_val_until();

CREATE TRIGGER extend_tag_val_until AFTER INSERT ON lada.tag_link_sample
    REFERENCING NEW TABLE AS added_tag_links
    FOR EACH STATEMENT EXECUTE PROCEDURE lada.extend_tag_val_until();
