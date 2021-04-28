UPDATE stamm.base_query
SET sql = replace(sql, ', tp, ', ', tourenplan,')
WHERE sql LIKE '%, tp,%';

UPDATE stamm.grid_column
SET name = 'Tourenplan',
  data_index = ' tourenplan'
WHERE data_index = 'tp';
