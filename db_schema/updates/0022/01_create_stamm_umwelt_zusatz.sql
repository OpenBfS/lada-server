CREATE TABLE stamm.umwelt_zusatz (
    id serial PRIMARY KEY,
    pzs_id character varying(3) REFERENCES stamm.proben_zusatz,
    umw_id character varying(3) REFERENCES stamm.umwelt,
    UNIQUE (pzs_id, umw_id)
);

ALTER TABLE stamm.umwelt_zusatz OWNER to postgres;
GRANT INSERT, SELECT, UPDATE, DELETE, REFERENCES ON TABLE stamm.umwelt_zusatz TO lada;
GRANT ALL ON TABLE stamm.umwelt_zusatz TO postgres;
