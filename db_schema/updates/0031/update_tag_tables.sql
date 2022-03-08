-- Create tag typ table
CREATE TABLE stamm.tag_typ (id text PRIMARY KEY, tagtyp TEXT);
INSERT INTO stamm.tag_typ VALUES('global', 'Global');
INSERT INTO stamm.tag_typ VALUES('netzbetreiber', 'Netzbetreiber');
INSERT INTO stamm.tag_typ VALUES('mst', 'Messstelle');
INSERT INTO stamm.tag_typ VALUES('auto', 'Auto');

-- Update tag table
ALTER TABLE stamm.tag ADD COLUMN netzbetreiber varchar(2) REFERENCES stamm.netz_betreiber;
ALTER TABLE stamm.tag ADD COLUMN user_id INTEGER REFERENCES stamm.lada_user;
ALTER TABLE stamm.tag ADD COLUMN typ TEXT REFERENCES stamm.tag_typ;
ALTER TABLE stamm.tag ADD COLUMN gueltig_bis TIMESTAMP without time zone;
ALTER TABLE stamm.tag ADD COLUMN generated_at TIMESTAMP without time zone NOT NULL DEFAULT (now() AT TIME ZONE 'utc');

-- Set tag_typ for existings tags:
-- If "generated" is true -> type is 'auto'
-- If no mst_id is set -> type is 'global'
-- If mst_id is set -> type is 'mst'
UPDATE stamm.tag SET typ = 'global' WHERE mst_id IS NULL;
UPDATE stamm.tag SET typ = 'mst' WHERE mst_id IS NOT NULL;
UPDATE stamm.tag set typ = 'auto' WHERE generated IS TRUE;

-- Insert default values for "gueltig_bis"
UPDATE stamm.tag SET gueltig_bis = ((now() AT TIME ZONE 'utc') + interval '365' day) WHERE typ = 'mst';
UPDATE stamm.tag SET gueltig_bis = ((now() AT TIME ZONE 'utc') + interval '548' day) WHERE typ = 'auto';

-- Add constraint for tag.typ
ALTER TABLE stamm.tag ALTER COLUMN typ SET NOT NULL;
