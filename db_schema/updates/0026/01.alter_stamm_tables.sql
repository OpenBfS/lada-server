CREATE FUNCTION update_letzte_aenderung() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        NEW.letzte_aenderung = now() AT TIME ZONE 'utc';
        RETURN NEW;
    END;
$$;

ALTER TABLE stamm.auth
    ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.deskriptoren
    ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.mess_stelle
    ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.ortszusatz
    ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.koordinaten_art
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.mess_einheit
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.mass_einheit_umrechnung
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.betriebsart
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.staat
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.netz_betreiber
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.umwelt
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.auth_funktion
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.auth_lst_umw
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.datenbasis
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.deskriptor_umwelt
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.mess_methode
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.messgroesse
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.messgroessen_gruppe
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.mg_grp
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.mmt_messgroesse_grp
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.ortszuordnung_typ
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.pflicht_messgroesse
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.rei_progpunkt
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.rei_progpunkt_gruppe
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.rei_progpunkt_grp_zuord
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.rei_progpunkt_grp_umw_zuord
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.kta
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.kta_gruppe
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.kta_grp_zuord
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.ort_typ
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.proben_zusatz
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.umwelt_zusatz
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.probenart
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.zeitbasis
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.importer_config
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');
ALTER TABLE stamm.tm_fm_umrechnung
	 ADD COLUMN IF NOT EXISTS letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc');