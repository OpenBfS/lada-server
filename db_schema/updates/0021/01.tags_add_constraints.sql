ALTER TABLE stamm.tag
    ALTER COLUMN tag SET NOT NULL,
    ALTER COLUMN generated SET NOT NULL,
    ALTER COLUMN generated SET DEFAULT false;

ALTER TABLE land.tagzuordnung
    ALTER COLUMN tag_id SET NOT NULL,
    ALTER COLUMN datum SET NOT NULL,
    ADD CHECK(probe_id IS NOT NULL OR messung_id IS NOT NULL);
