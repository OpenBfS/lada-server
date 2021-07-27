/*
 We have to use SQL because geometry field does not work with @UsingDataSet
 Keep this in sync with dbUnit_ort.json, which is still used
 to verify test results!
*/
INSERT INTO stamm.ort (
    id,
    netzbetreiber_id,
    ort_id,
    langtext,
    staat_id,
    gem_id,
    unscharf,
    nuts_code,
    kda_id,
    koord_x_extern,
    koord_y_extern,
    letzte_aenderung,
    geom,
    ort_typ,
    kurztext,
    berichtstext
) VALUES (
    1000,
    '06',
    'D_ 00191',
    'Langer Text',
    0,
    '11000000',
    TRUE,
    'DE716',
    5,
    '32487017',
    '5519769',
    '2015-03-01 12:00:00',
    'SRID=4326;POINT(49.83021 8.81948)',
    1,
    'kurz',
    'bericht'
);
