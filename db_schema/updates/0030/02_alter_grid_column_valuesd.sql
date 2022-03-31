ALTER TABLE stamm.grid_column_values
    ALTER COLUMN filter_active SET NOT NULL,
    ALTER COLUMN visible SET NOT NULL,
    ALTER COLUMN filter_negate SET NOT NULL,
    ALTER COLUMN filter_regex SET NOT NULL,
    ALTER COLUMN filter_is_null SET NOT NULL;
