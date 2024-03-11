DROP TRIGGER set_site_id_site ON master.site;
CREATE TRIGGER set_site_id_site BEFORE INSERT OR UPDATE ON master.site
    FOR EACH ROW EXECUTE PROCEDURE master.set_site_id();
