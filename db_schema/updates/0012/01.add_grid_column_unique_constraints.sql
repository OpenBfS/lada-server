ALTER TABLE stamm.grid_column
    ADD UNIQUE(base_query, name),
    ADD UNIQUE(base_query, data_index),
    ADD UNIQUE(base_query, position);
