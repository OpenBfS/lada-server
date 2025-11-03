DROP VIEW IF EXISTS master.status_kombi_view;

CREATE OR REPLACE VIEW master.status_mp_view AS
 SELECT status_mp.id AS status_mp_id, 
    (status_lev.lev::text || ' - '::text) || status_val.val::text AS status_comb
   FROM master.status_mp
     JOIN master.status_lev ON status_lev.id = status_mp.status_lev_id
     JOIN master.status_val ON status_val.id = status_mp.status_val_id;

GRANT ALL ON TABLE master.admin_border_view TO lada;