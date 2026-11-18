CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE requests (
    id UUID PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category_id UUID NOT NULL,
    priority VARCHAR(50) NOT NULL CHECK (priority IN ('BAIXA', 'MEDIA', 'ALTA')),
    status VARCHAR(50) NOT NULL DEFAULT 'ABERTA' CHECK (status IN ('ABERTA', 'EM_ANDAMENTO', 'CONCLUIDA')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_requests_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

CREATE TABLE requests_status_history (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('ABERTA', 'EM_ANDAMENTO', 'CONCLUIDA')),
    changed_by UUID NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_request FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE RESTRICT,
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_requests_status ON requests(status);
CREATE INDEX idx_requests_priority ON requests(priority);
CREATE INDEX idx_requests_category_id ON requests(category_id);
CREATE INDEX idx_history_request_id ON requests_status_history(request_id);
CREATE INDEX idx_history_changed_by ON requests_status_history(changed_by);
CREATE INDEX idx_history_request_changed_at ON requests_status_history(request_id, changed_at);