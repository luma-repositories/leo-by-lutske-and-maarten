-- Add source and created_at columns to recipe table for imported recipes
ALTER TABLE recipe ADD COLUMN source VARCHAR(100);
ALTER TABLE recipe ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add a default category for imported recipes
INSERT INTO category (id, name) VALUES (15, 'Geimporteerd');

-- Reset identity sequences to avoid conflicts with seed data
-- (H2 and PostgreSQL both support ALTER TABLE ... ALTER COLUMN ... RESTART WITH)
ALTER TABLE recipe ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE category ALTER COLUMN id RESTART WITH 100;
