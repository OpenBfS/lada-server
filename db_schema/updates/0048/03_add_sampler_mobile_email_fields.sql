-- Adds phone_mobile and email fields to sampler table

ALTER TABLE master.sampler
    ADD COLUMN phone_mobile character varying(20),
    ADD COLUMN email character varying(254);
