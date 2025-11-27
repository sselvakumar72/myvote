-- Table: myvote_app.users

DROP TABLE IF EXISTS myvote_app.users;

CREATE TABLE IF NOT EXISTS myvote_app.users
(
    id bigint NOT NULL DEFAULT nextval('myvote_app.users_id_seq'),
    c_user_name character(48) NOT NULL,
    c_password character(255) NOT NULL,
    c_first_name character(32),
    c_last_name character(32),
    c_email_id character(254),
    t_last_login_at timestamp without time zone,
    c_status character(1) NOT NULL,
    l_created_by bigint NOT NULL,
    t_create_at timestamp without time zone NOT NULL,
    l_last_updated_by bigint,
    t_last_updated_at timestamp without time zone
);

ALTER TABLE IF EXISTS myvote_app.users
    OWNER to myvote_app;

COMMENT ON TABLE myvote_app.users
    IS 'Table to store user details';

CREATE INDEX idx_users_email_id ON myvote_app.users(c_email_id);
CREATE INDEX idx_users_user_name ON myvote_app.users(c_user_name);
