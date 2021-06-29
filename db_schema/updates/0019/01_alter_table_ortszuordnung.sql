ALTER TABLE land.ortszuordnung
    ADD COLUMN oz_id character varying(7);
ALTER TABLE land.ortszuordnung
    ADD CONSTRAINT ortszuordnung_oz_id_fkey FOREIGN KEY (oz_id)
    REFERENCES stamm.ortszusatz (ozs_id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    NOT VALID;
