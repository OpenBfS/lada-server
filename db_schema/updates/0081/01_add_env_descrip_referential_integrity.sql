ALTER TABLE master.env_descrip_env_medium_mp
    ADD FOREIGN KEY (s00) REFERENCES master.env_descrip,
    ADD FOREIGN KEY (s01) REFERENCES master.env_descrip,
    ADD FOREIGN KEY (s02) REFERENCES master.env_descrip,
    ADD FOREIGN KEY (s03) REFERENCES master.env_descrip,
    ADD FOREIGN KEY (s04) REFERENCES master.env_descrip,
    ADD FOREIGN KEY (s05) REFERENCES master.env_descrip,
    ADD FOREIGN KEY (s06) REFERENCES master.env_descrip,
    ADD FOREIGN KEY (s07) REFERENCES master.env_descrip,
    ADD FOREIGN KEY (s08) REFERENCES master.env_descrip,
    ADD FOREIGN KEY (s09) REFERENCES master.env_descrip,
    ADD FOREIGN KEY (s10) REFERENCES master.env_descrip,
    ADD FOREIGN KEY (s11) REFERENCES master.env_descrip;
