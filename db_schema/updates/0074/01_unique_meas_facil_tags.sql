UPDATE master.tag SET network_id = NULL WHERE tag_type = 'mst';
ALTER TABLE master.tag
    ADD CHECK(network_id IS NULL OR meas_facil_id IS NULL),
    DROP CONSTRAINT tag_name_network_id_meas_facil_id_key;
CREATE UNIQUE INDEX meas_facil_tag_unique_idx ON master.tag (name, meas_facil_id)
    WHERE network_id IS NULL;
