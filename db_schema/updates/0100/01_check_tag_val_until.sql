ALTER TABLE master.tag
    ADD CHECK(val_until IS NULL OR meas_facil_id IS NOT NULL OR is_auto_tag);
