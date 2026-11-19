INSERT INTO categories (id, name, active, created_at) VALUES
(gen_random_uuid(), 'Desenvolvimento e Bugs', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Infraestrutura e DevOps', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Design e UI/UX', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Suporte e Ferramentas Internas', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Gestão de Projetos e Prazos', true, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;