-- TODO: Is this really what we want?
DELETE FROM land.ortszuordnung WHERE ortszuordnung_typ = 'Z';

DELETE FROM stamm.ortszuordnung_typ WHERE id = 'Z';

UPDATE stamm.ortszuordnung_typ
SET ortstyp = 'Entnahmort und Ursprungsort / REI-Messpunkt'
WHERE id = 'R';
