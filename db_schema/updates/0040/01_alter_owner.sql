ALTER SCHEMA land OWNER TO lada;
ALTER SCHEMA public OWNER TO lada;
ALTER SCHEMA stamm OWNER TO lada;

SELECT 'ALTER TABLE ' || schemaname || '.' || tablename || ' OWNER TO lada;'
FROM pg_tables
WHERE schemaname IN ('geo', 'land', 'stamm')
  AND tableowner <> 'lada'
ORDER BY
  schemaname,
  tablename
\gexec

SELECT 'ALTER TABLE ' || schemaname || '.' || viewname || ' OWNER TO lada;'
FROM pg_views
WHERE schemaname IN ('geo', 'land', 'stamm')
  AND viewowner <> 'lada'
ORDER BY
  schemaname,
  viewname
\gexec

SELECT 'ALTER TABLE ' || schemaname || '.' || matviewname || ' OWNER TO lada;'
FROM pg_matviews
WHERE schemaname IN ('geo', 'land', 'stamm')
  AND matviewowner <> 'lada'
ORDER BY
  schemaname,
  matviewname
\gexec

SELECT 'ALTER TABLE ' || schemaname || '.' || sequencename || ' OWNER TO lada;'
FROM pg_sequences
WHERE schemaname IN ('geo', 'land', 'stamm')
  AND sequenceowner <> 'lada'
ORDER BY
  schemaname,
  sequencename
\gexec

GRANT ALL ON ALL TABLES IN SCHEMA land TO lada;
GRANT ALL ON ALL SEQUENCES IN SCHEMA land TO lada;
GRANT ALL ON ALL TABLES IN SCHEMA stamm TO lada;
GRANT ALL ON ALL SEQUENCES IN SCHEMA stamm TO lada;

ALTER TABLE public.lada_messwert OWNER TO lada;
ALTER TABLE public.lada_schema_version OWNER TO lada;
--ALTER TABLE stamm.verwaltungsgrenze OWNER TO lada;

ALTER FUNCTION stamm.audit_table(regclass) OWNER TO lada;
ALTER FUNCTION stamm.audit_table(regclass, boolean, boolean) OWNER TO lada;
ALTER FUNCTION stamm.audit_table(regclass, boolean, boolean, text[]) OWNER TO lada;

SELECT 'ALTER FUNCTION ' || pronamespace::regnamespace::text || '.' || proname || ' OWNER TO lada;'
FROM pg_proc
WHERE pronamespace::regnamespace::text IN ('land', 'stamm')
  AND proowner::regrole::text <> 'lada'
\gexec
