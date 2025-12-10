CREATE OR REPLACE PROCEDURE public.create_user_procedure(IN p_first_name character varying, IN p_last_name character varying, IN p_email character varying, IN p_phone character varying, OUT new_id bigint)
 LANGUAGE plpgsql
AS $procedure$
BEGIN
INSERT INTO users (first_name, last_name, email, phone)
VALUES (p_first_name, p_last_name, p_email, p_phone)
    RETURNING id INTO new_id;
END;
$procedure$
