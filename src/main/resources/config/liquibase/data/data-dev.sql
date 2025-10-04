INSERT INTO users (id, phone_number, first_name, last_name, password_hash, active, created_at, updated_at)
VALUES (1, '09104916481', 'Zahra', 'Shahrabi', '$2a$10$42WZuM4gg258p/cCGwiPEO.lq8idj3k59I9NnvARiaZltJv8MW9K.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO roles (id, type, description, created_at, updated_at)
VALUES (1, 'USER', 'User Role', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1);

INSERT INTO wallets (id, user_id, balance, version, created_at, updated_at)
VALUES (1, 1, 150000.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO transactions (id, wallet_id, type, amount, status, created_at, updated_at)
VALUES (1, 1, 'TOP_UP', 150000.00, 'SUCCESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP );


INSERT INTO users (id, phone_number, first_name, last_name, password_hash, active, created_at, updated_at)
VALUES (2, '0922508900', 'Fateme', 'Shahrabi', '$2a$10$42WZuM4gg258p/cCGwiPEO.lq8idj3k59I9NnvARiaZltJv8MW9K.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id)
VALUES (2, 1);

INSERT INTO wallets (id, user_id, balance, version, created_at, updated_at)
VALUES (2, 2, 120000.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
