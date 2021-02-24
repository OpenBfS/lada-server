CREATE OR REPLACE VIEW land.rueckfrage_messung
 AS
 SELECT DISTINCT status_protokoll.messungs_id
   FROM land.status_protokoll
  WHERE (status_protokoll.status_kombi = ANY (ARRAY[9, 13]));
ALTER TABLE land.rueckfrage_messung
    OWNER TO postgres;
GRANT SELECT ON TABLE land.rueckfrage_messung TO lada;
