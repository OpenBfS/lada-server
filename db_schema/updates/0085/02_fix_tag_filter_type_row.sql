-- Reverts the tag filter type description back to 'tag'
UPDATE master.filter_type SET type = 'tag' WHERE id = 8;