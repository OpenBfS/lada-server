ALTER TABLE lada.sample ALTER ext_id SET DEFAULT
    'ZDB' || lpad(nextval('lada.sample_sample_id_seq')::varchar, 12, '0') || 'Y';
