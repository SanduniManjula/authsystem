INSERT INTO permission (name) VALUES
    ('CREATE_USER'),
    ('EDIT_USER'),
    ('UPDATE_USER'),
    ('DELETE_USER'),
    ('LIST_USER'),
    ('CREATE_BRANCH'),
    ('LIST_BRANCH'),
    ('DELETE_BRANCH');

INSERT INTO role (name) VALUES ('ROLE_ADMIN');
INSERT INTO role (name) VALUES ('ROLE_USER');


INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8);
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 5), (2, 7);
