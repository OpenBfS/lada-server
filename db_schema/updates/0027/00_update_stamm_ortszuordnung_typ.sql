DELETE FROM stamm.ortszuordnung_typ WHERE id = 'Z';

UPDATE stamm.ortszuordnung_typ
SET ortstyp = 'Entnahmort und Ursprungsort / REI-Messpunkt'
WHERE id = 'R';