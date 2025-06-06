ALTER TABLE master.meas_unit
    ADD UNIQUE(name),
    ADD UNIQUE(unit_symbol);
