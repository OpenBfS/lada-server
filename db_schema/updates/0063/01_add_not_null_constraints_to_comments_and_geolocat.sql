-- Adds not null constraints to
-- * comm_sample.text and comm_measm.text
-- * geolocat.type_regulation and geolocat_mpg.type_regulation

set search_path to lada;

ALTER TABLE comm_sample ALTER COLUMN text SET NOT NULL;
ALTER TABLE comm_measm ALTER COLUMN text SET NOT NULL;

ALTER TABLE geolocat ALTER COLUMN type_regulation SET NOT NULL;
ALTER TABLE geolocat_mpg ALTER COLUMN type_regulation SET NOT NULL;
