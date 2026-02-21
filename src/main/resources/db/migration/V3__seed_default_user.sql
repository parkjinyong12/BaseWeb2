INSERT INTO users (id, email, password, role)
VALUES ('11111111-1111-1111-1111-111111111111', 'user@example.com', '$2a$10$9nPrerIz8svw4mwbGvCL8OfN1qSEfN7PIly4JQFG0DZ6f31dA8l8S', 'ROLE_USER')
ON CONFLICT (email) DO NOTHING;
