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
ALTER TABLE stamm.tag ADD COLUMN gueltig_bis TIMESTAMP;
ALTER TABLE stamm.tag ADD COLUMN generated_at TIMESTAMP NOT NULL DEFAULT NOW();