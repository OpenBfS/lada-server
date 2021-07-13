ALTER TABLE stamm.query_user
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE stamm.query_user
    ALTER COLUMN base_query SET NOT NULL;

ALTER TABLE stamm.query_user
    ALTER COLUMN description SET NOT NULL;