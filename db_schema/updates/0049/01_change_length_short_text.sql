SET role lada;

DROP VIEW stamm.ort;

ALTER TABLE master.site ALTER COLUMN short_text TYPE VARCHAR(20);

CREATE OR REPLACE VIEW stamm.ort
 AS
 SELECT site.id,
    site.network_id AS netzbetreiber_id,
    site.ext_id AS ort_id,
    site.long_text AS langtext,
    site.state_id AS staat_id,
    site.munic_id AS gem_id,
    site.is_fuzzy AS unscharf,
    site.spat_ref_sys_id AS kda_id,
    site.coord_x_ext AS koord_x_extern,
    site.coord_y_ext AS koord_y_extern,
    site.alt AS hoehe_land,
    site.last_mod AS letzte_aenderung,
    site.geom,
    site.shape,
    site.site_class_id AS ort_typ,
    SUBSTR(site.short_text, 1, 15) AS kurztext,
    site.rei_report_text AS berichtstext,
    site.rei_zone AS zone,
    site.rei_sector AS sektor,
    site.rei_competence AS zustaendigkeit,
    site.rei_opr_mode AS mp_art,
    site.is_rei_active AS aktiv,
    site.rei_nucl_facil_gr_id AS kta_gruppe_id,
    site.poi_id AS oz_id,
    site.height_asl AS hoehe_ueber_nn,
    site.rei_ag_gr_id AS rei_progpunkt_grp_id,
    site.munic_div_id AS gem_unt_id
   FROM master.site;
