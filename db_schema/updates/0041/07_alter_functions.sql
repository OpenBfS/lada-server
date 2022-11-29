SET search_path TO master;

ALTER FUNCTION master.if_modified_func()
    SET search_path=master, public;

CREATE OR REPLACE FUNCTION get_media_from_media_desk(media_desk character varying) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
declare
  result character varying(100);
  d00 smallint;
  d01 smallint;
  d02 smallint;
  d03 smallint;
begin
  if media_desk like 'D: %' then
    d00 := substring(media_desk,4,2);
    d01 := substring(media_desk,7,2);
    d02 := substring(media_desk,10,2);
    d03 := substring(media_desk,13,2);
    if d00 = '00' then
      result := null;
    else
      if d01 = '00' then
        select s00.name into result FROM master.env_descrip s00
        where s00.lev = 0 and s00.lev_val = d00::smallint;
      else
        if d02 = '00' or d00 <> '01' then
          select s01.name into result FROM master.env_descrip s01
          where s01.lev = 1 and s01.lev_val = d01::smallint
            and s01.pred_id =
              (select s00.id FROM master.env_descrip s00
               where s00.lev = 0 and s00.lev_val = d00::smallint);
        else
          if d03 = '00' then
            select s02.name into result FROM master.env_descrip s02
            where s02.lev = 2 and s02.lev_val = d02::smallint
              and s02.pred_id =
                (select s01.id FROM master.env_descrip s01
                 where s01.lev = 1 and s01.lev_val = d01::smallint
                   and s01.pred_id =
                     (select s00.id FROM master.env_descrip s00
                      where s00.lev = 0 and s00.lev_val = d00::smallint));
          else
            select s03.name into result FROM master.env_descrip s03
            where s03.lev = 3 and s03.lev_val = d03::smallint
              and s03.pred_id =
              (select s02.id FROM master.env_descrip s02
              where s02.lev = 2 and s02.lev_val = d02::smallint
                and s02.pred_id =
                  (select s01.id FROM master.env_descrip s01
                  where s01.lev = 1 and s01.lev_val = d01::smallint
                    and s01.pred_id =
                      (select s00.id FROM master.env_descrip s00
                      where s00.lev = 0 and s00.lev_val = d00::smallint)));
          end if;
        end if;
      end if;
    end if;
  else
    result := null;
  end if;
  return (result);
end;
$$;


CREATE OR REPLACE FUNCTION get_desk_description(
	media_desk character varying,
	stufe integer)
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE
    d_xx character varying;
  BEGIN
    IF substr(media_desk, 4+stufe*3, 2) = '00' THEN 
      RETURN NULL; 
    END IF;

    IF stufe = 0 THEN
      SELECT d00.name
      INTO d_xx
      FROM master.env_descrip d00
      WHERE d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT);

    ELSEIF stufe = 1 THEN
      SELECT d01.name
      INTO d_xx
      FROM master.env_descrip d01
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT);

    ELSEIF stufe = 2 THEN
      SELECT d02.name
      INTO d_xx
      FROM master.env_descrip d02
      JOIN master.env_descrip d01 ON d01.id = d02.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d02.lev = 2
        AND d02.lev_val = cast(substr(media_desk, 10, 2) AS SMALLINT);

    ELSEIF stufe = 3 AND substr(media_desk, 4, 2) = '01' THEN
      SELECT d03.name
      INTO d_xx
      FROM master.env_descrip d03
      JOIN master.env_descrip d02 ON d02.id = d03.pred_id
        AND d02.lev = 2
        AND d02.lev_val = cast(substr(media_desk , 10, 2) AS SMALLINT)
      JOIN master.env_descrip d01 ON d01.id = d02.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d03.lev = 3
        AND d03.lev_val = cast(substr(media_desk, 13, 2) AS SMALLINT);

    ELSEIF stufe = 3 AND substr(media_desk, 4, 2) <> '01' OR stufe > 3 THEN
      SELECT dxx.name
      INTO d_xx
      FROM master.env_descrip dxx
      JOIN master.env_descrip d01 ON d01.id = dxx.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE dxx.lev = stufe
        AND dxx.lev_val = cast(substr(media_desk, (stufe * 3 + 4), 2) AS SMALLINT);

    ELSE
      d_xx := NULL;
    END IF;
    return d_xx;
  END;
$BODY$;

CREATE OR REPLACE FUNCTION stamm.get_desk_beschreibung(media_desk character varying, tufe integer)
    RETURNS character varying
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
  BEGIN
    return master.get_desc_description(media_desk, stufe);
  END;
$BODY$;

CREATE OR REPLACE FUNCTION get_desk_imis2_id(
	media_desk character varying,
	stufe integer)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE
    imis2_id INTEGER;
  BEGIN
    IF substr(media_desk, 4+stufe*3, 2) = '00' THEN 
      RETURN NULL; 
    END IF;
    IF stufe = 0 THEN
      SELECT d00.imis2_id
      INTO imis2_id
      FROM master.env_descrip d00
      WHERE d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT);

    ELSEIF stufe = 1 THEN
      SELECT d01.imis2_id
      INTO imis2_id
      FROM master.env_descrip d01
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT);

    ELSEIF stufe = 2 THEN
      SELECT d02.imis2_id
      INTO imis2_id
      FROM master.env_descrip d02
      JOIN master.env_descrip d01 ON d01.id = d02.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d02.lev = 2
        AND d02.lev_val = cast(substr(media_desk, 10, 2) AS SMALLINT);

    ELSEIF stufe = 3 AND substr(media_desk, 4, 2) = '01' THEN
      SELECT d03.imis2_id
      INTO imis2_id
      FROM master.env_descrip d03
      JOIN master.env_descrip d02 ON d02.id = d03.pred_id
        AND d02.lev = 2
        AND d02.lev_val = cast(substr(media_desk , 10, 2) AS SMALLINT)
      JOIN master.env_descrip d01 ON d01.id = d02.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d03.lev = 3
        AND d03.lev_val = cast(substr(media_desk, 13, 2) AS SMALLINT);

    ELSEIF stufe = 3 AND substr(media_desk, 4, 2) <> '01' OR stufe > 3 THEN
      SELECT dxx.imis2_id
      INTO imis2_id
      FROM master.env_descrip dxx
      JOIN master.env_descrip d01 ON d01.id = dxx.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE dxx.lev = stufe
        AND dxx.lev_val = cast(substr(media_desk, (stufe * 3 + 4), 2) AS SMALLINT);

    ELSE
      imis2_id := NULL;
    END IF;
    return imis2_id;
  END;
$BODY$;