WITH x
AS (
  SELECT ortszuordnung_mp.id,
    ortszuordnung_mp.messprogramm_id,
    ortszuordnung_mp.ort_id,
    ortszuordnung_mp.oz_id,
    ort.ort_id,
    ort.oz_id AS ort_oz_id
  FROM land.ortszuordnung_mp
  JOIN stamm.ort ON ort.id = ortszuordnung_mp.ort_id
  WHERE ortszuordnung_mp.oz_id IS DISTINCT FROM ort.oz_id
  )
UPDATE land.ortszuordnung_mp
SET oz_id = x.ort_oz_id
FROM x
WHERE ortszuordnung_mp.id = x.id;