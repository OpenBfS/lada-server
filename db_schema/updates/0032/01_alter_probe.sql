ALTER TABLE land.probe 
  ADD COLUMN IF NOT EXISTS mitte_sammelzeitraum TIMESTAMP WITHOUT TIME ZONE GENERATED always AS 
    (CASE 
      WHEN probeentnahme_beginn IS NULL THEN NULL 
      WHEN probeentnahme_beginn IS NOT NULL AND probeentnahme_ende IS NULL THEN probeentnahme_beginn 
      ELSE probeentnahme_beginn + (probeentnahme_ende - probeentnahme_beginn) / 2 
    END) STORED;

DROP INDEX IF EXISTS land.idx_land_probe_mitte_sammelzeitraum_idx;
CREATE INDEX idx_land_probe_mitte_sammelzeitraum_idx
    ON land.probe USING btree
    (mitte_sammelzeitraum ASC NULLS LAST)
    TABLESPACE pg_default;
