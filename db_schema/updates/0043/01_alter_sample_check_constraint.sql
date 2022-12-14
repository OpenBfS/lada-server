ALTER TABLE lada.sample DROP CONSTRAINT sample_env_descrip_display_check;

ALTER TABLE lada.sample
    ADD CONSTRAINT sample_env_descrip_display_check CHECK 
        (env_descrip_display ~ '^D:( [0-9][0-9]){12}$');
