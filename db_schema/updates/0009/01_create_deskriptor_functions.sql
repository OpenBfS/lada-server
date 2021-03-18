CREATE FUNCTION get_media_from_media_desk(media_desk character varying) RETURNS character varying
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
        select s00.beschreibung into result FROM stamm.deskriptoren s00
        where s00.ebene = 0 and s00.sn = d00::smallint;
      else
        if d02 = '00' or d00 <> '01' then
          select s01.beschreibung into result FROM stamm.deskriptoren s01
          where s01.ebene = 1 and s01.sn = d01::smallint
            and s01.vorgaenger =
              (select s00.id FROM stamm.deskriptoren s00
               where s00.ebene = 0 and s00.sn = d00::smallint);
        else
          if d03 = '00' then
            select s02.beschreibung into result FROM stamm.deskriptoren s02
            where s02.ebene = 2 and s02.sn = d02::smallint
              and s02.vorgaenger =
                (select s01.id FROM stamm.deskriptoren s01
                 where s01.ebene = 1 and s01.sn = d01::smallint
                   and s01.vorgaenger =
                     (select s00.id FROM stamm.deskriptoren s00
                      where s00.ebene = 0 and s00.sn = d00::smallint));
          else
            select s03.beschreibung into result FROM stamm.deskriptoren s03
            where s03.ebene = 3 and s03.sn = d03::smallint
              and s03.vorgaenger =
              (select s02.id FROM stamm.deskriptoren s02
              where s02.ebene = 2 and s02.sn = d02::smallint
                and s02.vorgaenger =
                  (select s01.id FROM stamm.deskriptoren s01
                  where s01.ebene = 1 and s01.sn = d01::smallint
                    and s01.vorgaenger =
                      (select s00.id FROM stamm.deskriptoren s00
                      where s00.ebene = 0 and s00.sn = d00::smallint)));
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


CREATE OR REPLACE FUNCTION get_desk_beschreibung(
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
      SELECT d00.beschreibung
      INTO d_xx
      FROM stamm.deskriptoren d00
      WHERE d00.ebene = 0
        AND d00.sn = cast(substr(media_desk, 4, 2) AS SMALLINT);

    ELSEIF stufe = 1 THEN
      SELECT d01.beschreibung
      INTO d_xx
      FROM stamm.deskriptoren d01
      JOIN stamm.deskriptoren d00 ON d00.id = d01.vorgaenger
        AND d00.ebene = 0
        AND d00.sn = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d01.ebene = 1
        AND d01.sn = cast(substr(media_desk, 7, 2) AS SMALLINT);

    ELSEIF stufe = 2 THEN
      SELECT d02.beschreibung
      INTO d_xx
      FROM stamm.deskriptoren d02
      JOIN stamm.deskriptoren d01 ON d01.id = d02.vorgaenger
        AND d01.ebene = 1
        AND d01.sn = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN stamm.deskriptoren d00 ON d00.id = d01.vorgaenger
        AND d00.ebene = 0
        AND d00.sn = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d02.ebene = 2
        AND d02.sn = cast(substr(media_desk, 10, 2) AS SMALLINT);

    ELSEIF stufe = 3 AND substr(media_desk, 4, 2) = '01' THEN
      SELECT d03.beschreibung
      INTO d_xx
      FROM stamm.deskriptoren d03
      JOIN stamm.deskriptoren d02 ON d02.id = d03.vorgaenger
        AND d02.ebene = 2
        AND d02.sn = cast(substr(media_desk , 10, 2) AS SMALLINT)
      JOIN stamm.deskriptoren d01 ON d01.id = d02.vorgaenger
        AND d01.ebene = 1
        AND d01.sn = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN stamm.deskriptoren d00 ON d00.id = d01.vorgaenger
        AND d00.ebene = 0
        AND d00.sn = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d03.ebene = 3
        AND d03.sn = cast(substr(media_desk, 13, 2) AS SMALLINT);

    ELSEIF stufe = 3 AND substr(media_desk, 4, 2) <> '01' OR stufe > 3 THEN
      SELECT dxx.beschreibung
      INTO d_xx
      FROM stamm.deskriptoren dxx
      JOIN stamm.deskriptoren d01 ON d01.id = dxx.vorgaenger
        AND d01.ebene = 1
        AND d01.sn = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN stamm.deskriptoren d00 ON d00.id = d01.vorgaenger
        AND d00.ebene = 0
        AND d00.sn = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE dxx.ebene = stufe
        AND dxx.sn = cast(substr(media_desk, (stufe * 3 + 4), 2) AS SMALLINT);

    ELSE
      d_xx := NULL;
    END IF;
    return d_xx;
  END;
$BODY$;
