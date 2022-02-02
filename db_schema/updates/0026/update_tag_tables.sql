-- Create tag typ table
CREATE TABLE stamm.tag_typ (id SERIAL PRIMARY KEY, tagtyp TEXT);
INSERT INTO stamm.tag_typ VALUES(1, 'Global');
INSERT INTO stamm.tag_typ VALUES(2, 'Netzbetreiber');
INSERT INTO stamm.tag_typ VALUES(3, 'Messstelle');
INSERT INTO stamm.tag_typ VALUES(4, 'Auto');

-- Update tag table
ALTER TABLE stamm.tag ADD COLUMN netzbetreiber varchar(2) REFERENCES stamm.netz_betreiber;
ALTER TABLE stamm.tag ADD COLUMN user_id INTEGER REFERENCES stamm.lada_user;
ALTER TABLE stamm.tag ADD COLUMN typ INTEGER REFERENCES stamm.tag_typ;
ALTER TABLE stamm.tag ADD COLUMN gueltig_bis TIMESTAMP;
ALTER TABLE stamm.tag ADD COLUMN generated_at TIMESTAMP NOT NULL DEFAULT NOW();