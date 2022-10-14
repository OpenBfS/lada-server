ALTER TABLE stamm.gemeindeuntergliederung DROP CONSTRAINT gemeindeuntergliederung_kda_id_fkey;
ALTER TABLE stamm.gemeindeuntergliederung DROP COLUMN kda_id;
ALTER TABLE stamm.gemeindeuntergliederung DROP COLUMN koord_x_extern;
ALTER TABLE stamm.gemeindeuntergliederung DROP COLUMN koord_y_extern;
ALTER TABLE stamm.gemeindeuntergliederung DROP COLUMN geom;
ALTER TABLE stamm.gemeindeuntergliederung DROP COLUMN shape;
