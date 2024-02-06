-- Schema "lada"
DROP FUNCTION lada.update_letzte_aenderung CASCADE;
CREATE FUNCTION lada.update_last_mod() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        NEW.last_mod = now() AT TIME ZONE 'utc';
        RETURN NEW;
    END;
$$;
CREATE TRIGGER last_mod_mpg BEFORE UPDATE ON mpg
    FOR EACH ROW EXECUTE PROCEDURE lada.update_last_mod();
CREATE TRIGGER last_mod_mpg_mmt_mp BEFORE UPDATE ON mpg_mmt_mp
    FOR EACH ROW EXECUTE PROCEDURE lada.update_last_mod();
CREATE TRIGGER last_mod_sample BEFORE UPDATE ON sample
    FOR EACH ROW EXECUTE PROCEDURE lada.update_last_mod();
CREATE TRIGGER last_mod_geolocat BEFORE UPDATE ON geolocat
    FOR EACH ROW EXECUTE PROCEDURE lada.update_last_mod();
CREATE TRIGGER last_mod_geolocat_mpg BEFORE UPDATE ON geolocat_mpg
    FOR EACH ROW EXECUTE PROCEDURE lada.update_last_mod();
CREATE TRIGGER last_mod_zusatzwert BEFORE UPDATE ON sample_specif_meas_val
    FOR EACH ROW EXECUTE PROCEDURE lada.update_last_mod();
CREATE TRIGGER last_mod_measm BEFORE UPDATE ON measm
    FOR EACH ROW EXECUTE PROCEDURE lada.update_last_mod();
CREATE TRIGGER last_mod_meas_val BEFORE UPDATE ON meas_val
    FOR EACH ROW EXECUTE PROCEDURE lada.update_last_mod();

DROP FUNCTION lada.update_tree_modified_probe CASCADE;
CREATE FUNCTION lada.update_tree_mod_sample() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        NEW.tree_mod = now() AT TIME ZONE 'utc';
        UPDATE lada.measm SET tree_mod = now() AT TIME ZONE 'utc' WHERE sample_id = NEW.id;
        UPDATE lada.geolocat SET tree_mod = now() AT TIME ZONE 'utc' WHERE sample_id = NEW.id;
        UPDATE lada.sample_specif_meas_val SET tree_mod = now() AT TIME ZONE 'utc' WHERE sample_id = NEW.id;
        RETURN NEW;
    END;
$$;
CREATE TRIGGER tree_mod_sample BEFORE UPDATE ON sample
    FOR EACH ROW EXECUTE PROCEDURE lada.update_tree_mod_sample();

DROP FUNCTION lada.update_tree_modified CASCADE;
CREATE FUNCTION lada.update_tree_mod() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        NEW.tree_mod = now() AT TIME ZONE 'utc';
        RETURN NEW;
    END;
$$;
CREATE TRIGGER tree_mod_geolocat BEFORE UPDATE ON geolocat
    FOR EACH ROW EXECUTE PROCEDURE lada.update_tree_mod();
CREATE TRIGGER tree_mod_zusatzwert BEFORE UPDATE ON sample_specif_meas_val
    FOR EACH ROW EXECUTE PROCEDURE lada.update_tree_mod();
CREATE TRIGGER tree_mod_meas_val BEFORE UPDATE ON meas_val
    FOR EACH ROW EXECUTE PROCEDURE lada.update_tree_mod();
CREATE TRIGGER tree_mod_status_prot BEFORE UPDATE ON status_prot
    FOR EACH ROW EXECUTE PROCEDURE lada.update_tree_mod();

DROP FUNCTION lada.update_tree_modified_messung CASCADE;
CREATE FUNCTION lada.update_tree_mod_measm() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        NEW.tree_mod = now() AT TIME ZONE 'utc';
        UPDATE lada.meas_val SET tree_mod = now() AT TIME ZONE 'utc' WHERE measm_id = NEW.id;
        UPDATE lada.status_prot SET tree_mod = now() AT TIME ZONE 'utc' WHERE measm_id = NEW.id;
        RETURN NEW;
    END;
$$;
CREATE TRIGGER tree_mod_measm BEFORE UPDATE ON measm
    FOR EACH ROW EXECUTE PROCEDURE lada.update_tree_mod_measm();

DROP FUNCTION lada.set_messung_status CASCADE;
CREATE FUNCTION lada.set_measm_status() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE status_id integer;
    BEGIN
        INSERT INTO lada.status_prot
            (meas_facil_id, date, text, measm_id, status_comb)
        VALUES ((SELECT meas_facil_id
                     FROM lada.sample
                     WHERE id = NEW.sample_id),
                now() AT TIME ZONE 'utc', '', NEW.id, 1)
        RETURNING id into status_id;
        UPDATE lada.measm SET status = status_id where id = NEW.id;
        RETURN NEW;
    END;
$$;
CREATE TRIGGER status_measm AFTER INSERT ON lada.measm
    FOR EACH ROW EXECUTE PROCEDURE lada.set_measm_status();

DROP FUNCTION lada.update_status_messung CASCADE;
CREATE FUNCTION lada.update_status_measm() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        CASE
            WHEN new.status_comb in (2, 3, 4, 5, 6, 7, 8, 10, 11, 12)
            THEN
                UPDATE lada.measm SET is_completed = true, status = NEW.id
                    WHERE id = NEW.measm_id;
            WHEN new.status_comb in (1, 9, 13)
            THEN
                UPDATE lada.measm SET is_completed = false, status = NEW.id
                    WHERE id = NEW.measm_id;
            ELSE
                UPDATE lada.measm SET status = NEW.id WHERE id = NEW.measm_id;
        END CASE;
        RETURN NEW;
    END
$$;
CREATE TRIGGER update_measm_after_status_prot_created
    AFTER INSERT ON lada.status_prot
    FOR EACH ROW EXECUTE PROCEDURE lada.update_status_measm();

DROP FUNCTION lada.set_messung_ext_id CASCADE;
CREATE FUNCTION lada.set_measm_ext_id() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        IF NEW.ext_id IS NULL THEN
            NEW.ext_id = (
                SELECT coalesce(max(ext_id),0)
                   FROM lada.measm
                   WHERE sample_id = NEW.sample_id) + 1;
        END IF;
        RETURN NEW;
    END;
$$;
CREATE TRIGGER ext_id BEFORE INSERT ON lada.measm
    FOR EACH ROW EXECUTE PROCEDURE lada.set_measm_ext_id();

-- Schema "master"
DROP FUNCTION master.update_letzte_aenderung CASCADE;
CREATE FUNCTION master.update_last_mod() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        NEW.last_mod = now() AT TIME ZONE 'utc';
        RETURN NEW;
    END;
$$;
CREATE TRIGGER last_mod_spat_ref_sys BEFORE UPDATE ON master.spat_ref_sys
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_meas_unit BEFORE UPDATE ON master.meas_unit
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_unit_convers BEFORE UPDATE ON master.unit_convers
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_opr_mode BEFORE UPDATE ON master.opr_mode
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_state BEFORE UPDATE ON master.state
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_network BEFORE UPDATE ON master.network
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_meas_facil BEFORE UPDATE ON master.meas_facil
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_env_medium BEFORE UPDATE ON master.env_medium
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_auth_funct BEFORE UPDATE ON master.auth_funct
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_auth BEFORE UPDATE ON master.auth
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_auth_coord_ofc_env_medium_mp
    BEFORE UPDATE ON master.auth_coord_ofc_env_medium_mp
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_regulation BEFORE UPDATE ON master.regulation
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_dataset_creator BEFORE UPDATE ON master.dataset_creator
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_env_descrip_env_medium_mp
    BEFORE UPDATE ON master.env_descrip_env_medium_mp
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_env_descrip BEFORE UPDATE ON master.env_descrip
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_mmt BEFORE UPDATE ON master.mmt
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_measd BEFORE UPDATE ON master.measd
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_measd_gr BEFORE UPDATE ON master.measd_gr
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_mpg_categ BEFORE UPDATE ON master.mpg_categ
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_measd_gr_mp BEFORE UPDATE ON master.measd_gr_mp
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_mmt_measd_gr_mp BEFORE UPDATE ON master.mmt_measd_gr_mp
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_rei_ag BEFORE UPDATE ON master.rei_ag
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_rei_ag_gr BEFORE UPDATE ON master.rei_ag_gr
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_rei_ag_gr_mp BEFORE UPDATE ON master.rei_ag_gr_mp
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_rei_ag_gr_env_medium_mp
    BEFORE UPDATE ON master.rei_ag_gr_env_medium_mp
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_nucl_facil BEFORE UPDATE ON master.nucl_facil
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_nucl_facil_gr BEFORE UPDATE ON master.nucl_facil_gr
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_nucl_facil_gr_mp BEFORE UPDATE ON master.nucl_facil_gr_mp
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_site_class BEFORE UPDATE ON master.site_class
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_poi BEFORE UPDATE ON master.poi
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_munic_div BEFORE UPDATE ON master.munic_div
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_site BEFORE UPDATE ON master.site
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_type_regulation BEFORE UPDATE ON master.type_regulation
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_oblig_measd_mp BEFORE UPDATE ON master.oblig_measd_mp
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_sample_specif BEFORE UPDATE ON master.sample_specif
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_env_specif_mp BEFORE UPDATE ON master.env_specif_mp
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_sample_meth BEFORE UPDATE ON master.sample_meth
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_sampler BEFORE UPDATE ON master.sampler
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_tz BEFORE UPDATE ON master.tz
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_import_conf BEFORE UPDATE ON master.import_conf
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_convers_dm_fm BEFORE UPDATE ON master.convers_dm_fm
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_ref_val_measure BEFORE UPDATE ON master.ref_val_measure
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_ref_val BEFORE UPDATE ON master.ref_val
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_targ_act_mmt_gr BEFORE UPDATE ON master.targ_act_mmt_gr
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_targ_act_mmt_gr_mp BEFORE UPDATE ON master.targ_act_mmt_gr_mp
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_targ_env_gr BEFORE UPDATE ON master.targ_env_gr
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_targ_env_gr_mp BEFORE UPDATE ON master.targ_env_gr_mp
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();
CREATE TRIGGER last_mod_targ_act_targ BEFORE UPDATE ON master.targ_act_targ
   FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();

DROP FUNCTION master.set_ort_id CASCADE;
CREATE FUNCTION master.set_site_id() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE value text;
    BEGIN
        value = '#'::text || lpad(NEW.id::text, 9, '0'::text);
        IF NEW.ext_id IS NULL THEN
            NEW.ext_id = value;
        END IF;
        IF NEW.long_text IS NULL OR NEW.long_text = '' THEN
            NEW.long_text = value;
        END IF;
        IF NEW.short_text IS NULL OR NEW.short_text = '' THEN
            NEW.short_text = value;
        END IF;
        RETURN NEW;
    END;
$$;
CREATE TRIGGER set_site_id_site BEFORE INSERT ON master.site
   FOR EACH ROW EXECUTE PROCEDURE master.set_site_id();
