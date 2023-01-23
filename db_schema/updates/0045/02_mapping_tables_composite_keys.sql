-- Update mapping tables to use composite primary keys instead of id columns

ALTER TABLE master.query_meas_facil_mp
    DROP CONSTRAINT query_meas_facil_mp_pkey,
    ADD PRIMARY KEY(query_id, meas_facil_id),
    DROP COLUMN id;

ALTER TABLE master.rei_ag_gr_mp
    DROP CONSTRAINT rei_ag_gr_mp_pkey,
    ADD PRIMARY KEY(rei_ag_gr_id, rei_ag_id),
    DROP COLUMN id;