-- Based on https://github.com/xdimedrolx/audit-trigger/
--
-- which is licensed by "The PostgreSQL License", effectively equivalent to the BSD
-- license.

SET search_path TO lada;
CREATE TABLE audit_trail(
      id bigserial primary key,
      table_name varchar(50) not null,
      tstamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
      action varchar(1) NOT NULL CHECK (action IN ('I','D','U', 'T')),
      object_id integer not null,
      row_data JSONB,
      changed_fields JSONB
);

CREATE OR REPLACE FUNCTION jsonb_delete_left(a jsonb, b jsonb)
  RETURNS jsonb AS
  $BODY$
       SELECT COALESCE(
              (
              SELECT ('{' || string_agg(to_json(key) || ':' || value, ',') || '}')
              FROM jsonb_each(a)
              WHERE NOT ('{' || to_json(key) || ':' || value || '}')::jsonb <@ b
              )
       , '{}')::jsonb;
       $BODY$
LANGUAGE sql IMMUTABLE STRICT;
COMMENT ON FUNCTION jsonb_delete_left(jsonb, jsonb) IS 'delete matching pairs in second argument from first argument';
DROP OPERATOR IF EXISTS - (jsonb, jsonb);
CREATE OPERATOR - ( PROCEDURE = jsonb_delete_left, LEFTARG = jsonb, RIGHTARG = jsonb);
COMMENT ON OPERATOR - (jsonb, jsonb) IS 'delete matching pairs from left operand';

CREATE OR REPLACE FUNCTION if_modified_func() RETURNS TRIGGER AS $body$
DECLARE
    audit_row lada.audit_trail;
    include_values boolean;
    log_diffs boolean;
    h_old jsonb;
    h_new jsonb;
    excluded_cols text[] = ARRAY[]::text[];
    item_id integer;
BEGIN
    IF TG_WHEN <> 'AFTER' THEN
        RAISE EXCEPTION 'lada.if_modified_func() may only run as an AFTER trigger';
    END IF;

    IF (TG_OP = 'DELETE') THEN
        item_id = OLD.id;
    ELSE
        item_id = NEW.id;
    END IF;

    audit_row = ROW(
        nextval('audit_trail_id_seq'),   -- id
        TG_TABLE_NAME::varchar,               -- table_name
        current_timestamp AT TIME ZONE 'utc', -- tstamp
        substring(TG_OP,1,1),                 -- action
        item_id,                              -- object_id
        NULL, NULL                            -- row_data, changed_fields
        );

    IF TG_ARGV[1] IS NOT NULL THEN
        excluded_cols = TG_ARGV[1]::text[];
    END IF;

    IF (TG_OP = 'UPDATE' AND TG_LEVEL = 'ROW') THEN
        audit_row.row_data = row_to_json(OLD)::JSONB;
        audit_row.changed_fields = row_to_json(NEW)::JSONB - audit_row.row_data - excluded_cols;
        IF audit_row.changed_fields = '{}'::jsonb THEN
            -- All changed fields are ignored. Skip this update.
            RETURN NULL;
        END IF;
    ELSIF (TG_OP = 'INSERT' AND TG_LEVEL = 'ROW') THEN
        audit_row.row_data = row_to_json(NEW)::JSONB;
        audit_row.changed_fields = jsonb_strip_nulls(row_to_json(NEW)::JSONB - excluded_cols);
    ELSIF (TG_OP = 'DELETE' AND TG_LEVEL = 'ROW') THEN
        audit_row.row_data = row_to_json(OLD)::JSONB;
        audit_row.changed_fields = jsonb_strip_nulls(row_to_json(OLD)::JSONB - excluded_cols);
    ELSE
        RAISE EXCEPTION '[lada.if_modified_func] - Trigger func added as trigger for unhandled case: %, %',TG_OP, TG_LEVEL;
        RETURN NULL;
    END IF;
    INSERT INTO lada.audit_trail VALUES (audit_row.*);
    RETURN NULL;
END;
$body$
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = lada, public;

CREATE INDEX sample_id_ndx ON audit_trail(cast("row_data"->>'sample_id' AS int));
CREATE INDEX measm_id_ndx ON audit_trail(cast("row_data"->>'measm_id' AS int));

-- View for sample audit trail
CREATE OR REPLACE VIEW lada.audit_trail_sample_view AS
SELECT
    lada_audit.id,
    lada_audit.table_name,
    lada_audit.action,
    lada_audit.object_id,
    lada_audit.tstamp,
    cast(row_data ->> 'measm_id' AS integer) AS measm_id,
    coalesce(cast(row_data ->> 'sample_id' AS integer),
        (SELECT sample_id FROM lada.measm WHERE id = cast(
            row_data ->> 'measm_id' AS integer))) AS sample_id,
    lada_audit.row_data,
    lada_audit.changed_fields,
    null as site_id
FROM lada.audit_trail as lada_audit
UNION
SELECT master_audit.id,
    master_audit.table_name,
    master_audit.action,
    master_audit.object_id,
    master_audit.tstamp,
    null as messungs_id,
    null as probe_id,
    master_audit.row_data,
    master_audit.changed_fields,
    cast(row_data ->> 'id' AS integer) AS site_id
FROM master.audit_trail as master_audit;


-- View for measm audit trail
CREATE OR REPLACE VIEW audit_trail_measm_view AS
SELECT audit_trail.id,
    audit_trail.table_name,
    audit_trail.tstamp,
    audit_trail.action,
    audit_trail.object_id,
    audit_trail.row_data,
    audit_trail.changed_fields,
    cast(row_data ->> 'measm_id' AS int) AS measm_id
FROM audit_trail;


SELECT master.audit_table('sample', true, false, '{id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('measm', true, false, '{id, sample_id, tree_mod, last_mod, status}'::text[]);
SELECT master.audit_table('meas_val', true, false, '{id, measm_id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('comm_sample', true, false, '{id, sample_id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('comm_measm', true, false, '{id, measm_id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('sample_specif_meas_val', true, false, '{id, sample_id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('geolocat', true, false, '{id, sample_id, tree_mod, last_mod}'::text[]);

SET search_path TO public;
