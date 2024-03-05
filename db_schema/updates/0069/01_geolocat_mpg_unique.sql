SET ROLE LADA;
ALTER TABLE lada.geolocat_mpg ADD UNIQUE (mpg_id, site_id, type_regulation);
