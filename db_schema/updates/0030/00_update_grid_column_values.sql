UPDATE stamm.grid_column_values
SET filter_active = false
WHERE filter_active IS NULL;

UPDATE stamm.grid_column_values
SET visible = false
WHERE visible IS NULL;

UPDATE stamm.grid_column_values
SET filter_negate = false
WHERE filter_negate IS NULL;

UPDATE stamm.grid_column_values
SET filter_regex = false
WHERE filter_regex IS NULL;

UPDATE stamm.grid_column_values
SET filter_is_null = false
WHERE filter_is_null IS NULL;
