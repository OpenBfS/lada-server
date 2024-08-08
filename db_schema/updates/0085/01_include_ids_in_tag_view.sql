DROP MATERIALIZED VIEW lada.mv_tags_array;
CREATE MATERIALIZED VIEW lada.mv_tags_array AS
 SELECT
    sample.id AS pid,
    measm.id as mid,
    array_agg(tag.name) AS tags,
    array_agg(tag.id) AS tagids
 FROM lada.sample
 INNER JOIN lada.measm ON sample.id = measm.sample_id
 LEFT OUTER JOIN lada.tag_link_measm ON measm.id = tag_link_measm.measm_id
 LEFT OUTER JOIN lada.tag_link_sample ON sample.id = tag_link_sample.sample_id
 JOIN master.tag ON (tag_link_sample.tag_id = tag.id OR tag_link_measm.tag_id = tag.id)
 GROUP BY sample.id, measm.id;