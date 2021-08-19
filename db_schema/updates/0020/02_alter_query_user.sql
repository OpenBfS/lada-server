ALTER TABLE stamm.query_user
    ALTER COLUMN user_id SET NOT NULL,
    ALTER COLUMN base_query SET NOT NULL,
    ALTER COLUMN description SET NOT NULL;