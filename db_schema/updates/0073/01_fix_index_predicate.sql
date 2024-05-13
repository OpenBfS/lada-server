DROP INDEX master.global_tag_unique_idx;
CREATE UNIQUE INDEX global_tag_unique_idx ON master.tag (name)
    WHERE network_id IS NULL AND meas_facil_id IS NULL;
