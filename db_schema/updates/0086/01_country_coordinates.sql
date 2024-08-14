--UPDATE master.site SET coord_x_ext="32,5", coord_y_ext="15,6", spat_ref_sys_id=4 WHERE id=684;
--UPDATE master.site SET coord_x_ext="31,6", coord_y_ext="4,85", spat_ref_sys_id=4 WHERE id=685;
--UPDATE master.site SET coord_x_ext="0", coord_y_ext="0", spat_ref_sys_id=4 WHERE id=683;
ALTER TABLE master.site ALTER COLUMN coord_x_ext SET NOT NULL;
ALTER TABLE master.site ALTER COLUMN coord_x_ext SET NOT NULL;
