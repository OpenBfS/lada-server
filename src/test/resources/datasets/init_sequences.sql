SET search_path TO lada, master;

DO $$
DECLARE
    tab varchar;
BEGIN
    FOREACH tab IN ARRAY ARRAY[
            'sample',
            'measm',
            'comm_measm',
            'comm_sample',
            'meas_val',
            'geolocat',
            'status_prot',
            'sample_specif_meas_val',
            'tag_link_sample',
            'tag',
            'site'
        ] LOOP
        EXECUTE format('SELECT setval(pg_get_serial_sequence(''%1$s'', ''id''), '
            '(SELECT max(id) FROM %1$I))', tab);
    END LOOP;
END $$;
