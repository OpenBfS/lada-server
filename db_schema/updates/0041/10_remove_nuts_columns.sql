SET search_path TO master;

ALTER TABLE admin_unit DROP COLUMN nuts;
ALTER TABLE site DROP COLUMN nuts_code;
