CREATE OR REPLACE VIEW view_all_users AS
SELECT
    id,
    first_name,
    last_name,
    email,
    phone,
    created_at
FROM users
ORDER BY id;


CREATE OR REPLACE VIEW view_user_details AS
SELECT
    id,
    first_name,
    last_name,
    email,
    phone,
    created_at
FROM users;
