-- Create tag typ table
CREATE TABLE stamm.tag_typ (id text PRIMARY KEY, tagtyp TEXT);
INSERT INTO stamm.tag_typ VALUES('global', 'Global');
INSERT INTO stamm.tag_typ VALUES('netz', 'Netzbetreiber');
INSERT INTO stamm.tag_typ VALUES('mst', 'Messstelle');

-- Update tag table
ALTER TABLE stamm.tag ADD COLUMN netzbetreiber_id varchar(2) REFERENCES stamm.netz_betreiber;
ALTER TABLE stamm.tag ADD COLUMN user_id INTEGER REFERENCES stamm.lada_user;
ALTER TABLE stamm.tag ADD COLUMN tag_typ TEXT REFERENCES stamm.tag_typ;
ALTER TABLE stamm.tag ADD COLUMN gueltig_bis TIMESTAMP without time zone;
ALTER TABLE stamm.tag ADD COLUMN created_at TIMESTAMP without time zone NOT NULL DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE IF EXISTS stamm.tag RENAME generated TO auto_tag;
ALTER TABLE stamm.tag DROP CONSTRAINT tag_tag_mst_id_key;
ALTER TABLE stamm.tag ADD CONSTRAINT tag_tag_netzbetreiber_id_mst_id_key UNIQUE (tag, netzbetreiber_id, mst_id);

-- Set tag_typ for existings tags:
UPDATE stamm.tag SET tag_typ = 'global' WHERE mst_id IS NULL;
UPDATE stamm.tag SET tag_typ = 'mst' WHERE mst_id IS NOT NULL;
WITH x
AS (
  SELECT DISTINCT tag.id,
    tag.tag,
    mess_stelle.netzbetreiber_id
  FROM stamm.tag
  JOIN land.tagzuordnung ON tag.id = tagzuordnung.tag_id
  JOIN land.probe ON tagzuordnung.probe_id = probe.id
  JOIN stamm.mess_stelle ON probe.mst_id = mess_stelle.id
  WHERE tag.tag LIKE 'PEP%'
  )
UPDATE stamm.tag
SET tag_typ = 'netz',
  netzbetreiber_id = x.netzbetreiber_id
FROM x
WHERE tag.id = x.id;

-- Insert default values for "gueltig_bis"
UPDATE stamm.tag SET
    gueltig_bis = ((current_timestamp AT TIME ZONE 'utc') + interval '365' day)
    WHERE tag_typ = 'mst';
UPDATE stamm.tag SET
    gueltig_bis = ((current_timestamp AT TIME ZONE 'utc') + interval '548' day)
    WHERE auto_tag;

-- Add constraint for tag.typ
ALTER TABLE stamm.tag ALTER COLUMN tag_typ SET NOT NULL;
