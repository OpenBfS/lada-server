-- Adds contraints to comm_sample and comm_measm that ensure that comment texts
-- are unique for every connected object

ALTER TABLE lada.comm_sample
    ADD UNIQUE(sample_id, text);

ALTER TABLE lada.comm_measm
    ADD UNIQUE(measm_id, text);