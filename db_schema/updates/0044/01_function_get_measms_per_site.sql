--
-- Name: get_measms_per_site(site_id int); Type: FUNCTION; Schema: lada; Owner: -
--

CREATE OR REPLACE FUNCTION lada.get_measms_per_site (site_id int)
RETURNS int
LANGUAGE plpgsql
AS
$$
DECLARE measms_per_site int;
BEGIN
 
    EXECUTE 
    '
    SELECT COUNT(DISTINCT sa.id)
    FROM master.site s
    INNER JOIN lada.geolocat g ON s.id=g.site_id
    INNER JOIN lada.sample sa ON g.sample_id=sa.id
    INNER JOIN lada.measm m ON m.sample_id=sa.id
    INNER JOIN lada.status_prot sp ON m.status=sp.id
    WHERE s.id=' || site_id || ' and sp.status_comb IN (2,6,10)
    ' INTO measms_per_site;
    
    RETURN measms_per_site;
    
END;
$$;