-- Add import_metadata column to store AI extraction metadata (provider, model, warnings)
-- Uses TEXT type for H2 compatibility (PostgreSQL would prefer JSONB, but TEXT works for both)
ALTER TABLE recipe ADD COLUMN import_metadata TEXT;
