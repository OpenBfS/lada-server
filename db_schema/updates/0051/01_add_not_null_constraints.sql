-- Adds not null constraints replacing removed validation rules

ALTER TABLE lada.sample
    ALTER COLUMN opr_mode_id SET NOT NULL,
    ALTER COLUMN regulation_id SET NOT NULL,
    ALTER COLUMN sample_meth_id SET NOT NULL,
    ALTER COLUMN is_test SET NOT NULL;
