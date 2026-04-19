CREATE TABLE tasks (
                       id          BIGSERIAL    PRIMARY KEY,
                       title       VARCHAR(200) NOT NULL,
                       description VARCHAR(1000),
                       status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
                       due_date    DATE,
                       user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);