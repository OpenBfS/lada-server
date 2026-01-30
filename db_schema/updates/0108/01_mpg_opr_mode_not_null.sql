UPDATE lada.mpg SET opr_mode_id = DEFAULT WHERE opr_mode_id IS NULL;
ALTER TABLE lada.mpg ALTER opr_mode_id SET NOT NULL;
