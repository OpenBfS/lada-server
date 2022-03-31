WITH x
AS (
  SELECT ortszuordnung.id,
    ortszuordnung.probe_id,
    ortszuordnung.ort_id,
    ortszuordnung.oz_id,
    ort.ort_id,
    ort.oz_id AS ort_oz_id
  FROM land.ortszuordnung
  JOIN stamm.ort ON ort.id = ortszuordnung.ort_id
  WHERE ortszuordnung.oz_id IS DISTINCT FROM ort.oz_id
  )
UPDATE land.ortszuordnung
SET oz_id = x.ort_oz_id
FROM x
WHERE ortszuordnung.id = x.id;