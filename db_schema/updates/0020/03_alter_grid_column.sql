ALTER TABLE stamm.grid_column
    ALTER COLUMN base_query SET NOT NULL,
    DROP CONSTRAINT grid_column_base_query_fkey,
    ADD CONSTRAINT grid_column_base_query_fkey FOREIGN KEY (base_query)
       REFERENCES stamm.base_query (id) MATCH SIMPLE
       ON UPDATE NO ACTION
       ON DELETE CASCADE;
