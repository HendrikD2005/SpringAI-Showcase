-- PG Vector Extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Sample Table
CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1536)
    );

-- Index for the COSINE Distance
CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
