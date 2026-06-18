DROP VIEW stamm.grid_column_values;

ALTER TABLE master.grid_col_conf DROP lada_user_id;

CREATE VIEW stamm.grid_column_values AS SELECT
	c.id,
	q.lada_user_id AS user_id,
	c.grid_col_mp_id AS grid_column,
	c.query_user_id AS query_user,
	c.sort,
	c.sort_index,
	c.filter_val AS filter_value,
	c.is_filter_active AS filter_active,
	c.is_visible AS visible,
	c.col_index AS column_index,
	c.width,
	c.is_filter_negate AS filter_negate,
	c.is_filter_regex AS filter_regex,
	c.is_filter_null AS filter_is_null
FROM master.grid_col_conf c JOIN master.query_user q ON q.id = c.query_user_id;
