ALTER TABLE master.nucl_facil_gr_mp ADD COLUMN nucl_facil_ext_id char(4);
UPDATE master.nucl_facil_gr_mp SET nucl_facil_ext_id =
    (SELECT ext_id FROM master.nucl_facil WHERE id = nucl_facil_id);
ALTER TABLE master.nucl_facil_gr_mp ALTER COLUMN nucl_facil_ext_id SET NOT NULL;

DROP VIEW stamm.kta_grp_zuord;
CREATE VIEW stamm.kta_grp_zuord AS SELECT
        id,
        nucl_facil_gr_id AS kta_grp_id,
        nucl_facil_ext_id AS kta_id,
        last_mod AS letzte_aenderung
    FROM master.nucl_facil_gr_mp;

-- Dropped in previous script
CREATE VIEW stamm.kta AS SELECT
        ext_id AS code,
        name AS bezeichnung,
        last_mod AS letzte_aenderung
    FROM master.nucl_facil;

ALTER TABLE master.nucl_facil_gr_mp DROP COLUMN nucl_facil_id;
ALTER TABLE master.nucl_facil
    DROP COLUMN id,
    ADD PRIMARY KEY (ext_id);
ALTER TABLE master.nucl_facil_gr_mp
    ADD FOREIGN KEY (nucl_facil_ext_id) REFERENCES master.nucl_facil;
