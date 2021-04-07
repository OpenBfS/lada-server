CREATE TABLE stamm.tm_fm_umrechnung(
  id serial NOT NULL PRIMARY KEY,	
  meh_id smallint NOT NULL REFERENCES stamm.mess_einheit(id),
  meh_id_nach smallint NOT NULL REFERENCES stamm.mess_einheit(id),
  umw_id character varying(3) NOT NULL REFERENCES stamm.umwelt(id),
  media_desk_pattern character varying(100),	
  faktor numeric(25,12) NOT NULL
);

ALTER TABLE stamm.tm_fm_umrechnung OWNER to postgres;
GRANT INSERT, SELECT, UPDATE, DELETE, REFERENCES ON TABLE stamm.tm_fm_umrechnung TO lada;
GRANT ALL ON TABLE stamm.tm_fm_umrechnung TO postgres;
GRANT SELECT ON TABLE stamm.tm_fm_umrechnung TO readonly;
