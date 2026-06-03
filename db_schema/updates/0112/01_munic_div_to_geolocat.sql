ALTER TABLE lada.geolocat
    ADD COLUMN munic_div_id integer REFERENCES master.munic_div;
ALTER TABLE lada.geolocat_mpg
    ADD COLUMN munic_div_id integer REFERENCES master.munic_div;

-- Drop legacy relations
UPDATE master.site SET munic_div_id = NULL;
