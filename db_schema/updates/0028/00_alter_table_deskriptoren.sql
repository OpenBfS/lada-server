CREATE SEQUENCE IF NOT EXISTS stamm.deskriptoren_s_xx_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    OWNED BY stamm.deskriptoren.s_xx;

ALTER SEQUENCE stamm.deskriptoren_s_xx_seq
    OWNER TO postgres;

GRANT USAGE ON SEQUENCE stamm.deskriptoren_s_xx_seq TO lada;

GRANT ALL ON SEQUENCE stamm.deskriptoren_s_xx_seq TO postgres;

ALTER TABLE IF EXISTS stamm.deskriptoren
    ALTER COLUMN s_xx SET DEFAULT nextval('stamm.deskriptoren_id_seq'::regclass);

SELECT pg_catalog.setval('stamm.deskriptoren_s_xx_seq', (SELECT max(s_xx) FROM stamm.deskriptoren), true);
