ALTER TABLE land.ortszuordnung_mp
    ADD COLUMN oz_id character varying(7);
ALTER TABLE land.ortszuordnung_mp
    ADD CONSTRAINT ortszuordnung_mp_oz_id_fkey FOREIGN KEY (oz_id)
    REFERENCES stamm.ortszusatz (ozs_id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    NOT VALID;
