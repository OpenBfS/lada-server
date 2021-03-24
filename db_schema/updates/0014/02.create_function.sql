SET search_path TO land;

CREATE OR REPLACE FUNCTION if_modified_func() RETURNS TRIGGER AS $body$
DECLARE
    audit_row land.audit_trail;
    include_values boolean;
    log_diffs boolean;
    h_old jsonb;
    h_new jsonb;
    excluded_cols text[] = ARRAY[]::text[];
    item_id integer;
BEGIN
    IF TG_WHEN <> 'AFTER' THEN
        RAISE EXCEPTION 'land.if_modified_func() may only run as an AFTER trigger';
    END IF;

    -- Do nothing on delete.
    IF (TG_OP = 'DELETE') THEN
        item_id = OLD.id;
    ELSE
        item_id = NEW.id;
    END IF;

    audit_row = ROW(
        nextval('land.audit_trail_id_seq'), -- id
        TG_TABLE_NAME::varchar,             -- table_name
        current_timestamp,                  -- tstamp
        substring(TG_OP,1,1),               -- action
        item_id,                             -- object_id
        NULL, NULL                          -- row_data, changed_fields
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
        RAISE EXCEPTION '[land.if_modified_func] - Trigger func added as trigger for unhandled case: %, %',TG_OP, TG_LEVEL;
        RETURN NULL;
    END IF;
    INSERT INTO land.audit_trail VALUES (audit_row.*);
    RETURN NULL;
END;
$body$
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = land, public;

SELECT stamm.audit_table('probe', true, false, '{id, tree_modified, letzte_aenderung}'::text[]);
SELECT stamm.audit_table('messung', true, false, '{id, probe_id, tree_modified, letzte_aenderung, status}'::text[]);
SELECT stamm.audit_table('messwert', true, false, '{id, messungs_id, tree_modified, letzte_aenderung}'::text[]);
SELECT stamm.audit_table('kommentar_p', true, false, '{id, probe_id, tree_modified, letzte_aenderung}'::text[]);
SELECT stamm.audit_table('kommentar_m', true, false, '{id, messungs_id, tree_modified, letzte_aenderung}'::text[]);
SELECT stamm.audit_table('zusatz_wert', true, false, '{id, probe_id, tree_modified, letzte_aenderung}'::text[]);
SELECT stamm.audit_table('ortszuordnung', true, false, '{id, probe_id, tree_modified, letzte_aenderung}'::text[]);

SET search_path TO public;