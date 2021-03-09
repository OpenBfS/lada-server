CREATE OR REPLACE FUNCTION stamm.check_sql(stmt text)
    RETURNS boolean
    LANGUAGE 'plpgsql'
    VOLATILE PARALLEL UNSAFE
AS $BODY$
  BEGIN
    EXECUTE stmt;
    RETURN true;
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE USING MESSAGE = SQLERRM, ERRCODE = SQLSTATE;
      RETURN false;
  END;
$BODY$;

ALTER TABLE stamm.base_query ADD CHECK(stamm.check_sql(sql));
