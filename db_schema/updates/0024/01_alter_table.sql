ALTER TABLE stamm.filter_type
    ALTER multiselect SET DEFAULT false,
    ALTER multiselect SET NOT NULL;

ALTER TABLE stamm.staat
    ALTER COLUMN eu SET DEFAULT false,
    ALTER COLUMN eu SET NOT NULL;

ALTER TABLE stamm.netz_betreiber
    ALTER COLUMN is_bmn SET DEFAULT false,
    ALTER COLUMN is_bmn SET NOT NULL,
    ALTER COLUMN aktiv SET DEFAULT false,
    ALTER COLUMN aktiv SET NOT NULL;

ALTER TABLE stamm.messgroesse
    ALTER COLUMN ist_leitnuklid SET DEFAULT false,
    ALTER COLUMN ist_leitnuklid SET NOT NULL;

UPDATE stamm.ort SET unscharf = false WHERE unscharf is NULL;
ALTER TABLE stamm.ort
    ALTER COLUMN unscharf SET DEFAULT false,
    ALTER COLUMN unscharf SET NOT NULL;
