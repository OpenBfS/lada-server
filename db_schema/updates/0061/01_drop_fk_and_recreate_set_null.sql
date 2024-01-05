SET ROLE lada;

ALTER TABLE master.tag DROP CONSTRAINT tag_lada_user_id_fkey;
ALTER TABLE master.tag ADD CONSTRAINT tag_lada_user_id_fkey FOREIGN KEY (lada_user_id) REFERENCES master.lada_user (id) ON DELETE SET NULL;
