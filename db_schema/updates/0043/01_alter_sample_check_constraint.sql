ALTER TABLE lada.sample DROP CONSTRAINT sample_env_descrip_display_check;
ALTER TABLE lada.sample
    ADD CONSTRAINT sample_env_descrip_display_check CHECK 
        (env_descrip_display ~ '^D:( [0-9][0-9]){12}$');

ALTER TABLE lada.mpg DROP CONSTRAINT mpg_env_descrip_display_check;
ALTER TABLE lada.mpg
    ADD CONSTRAINT mpg_env_descrip_display_check CHECK 
        (env_descrip_display ~ '^D:( [0-9][0-9]){12}$');
