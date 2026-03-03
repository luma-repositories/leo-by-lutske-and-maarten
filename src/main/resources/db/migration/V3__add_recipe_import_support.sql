ALTER TABLE recipes ALTER COLUMN legacy_id DROP NOT NULL;

ALTER TABLE recipes ADD COLUMN description TEXT;
ALTER TABLE recipes ADD COLUMN servings INTEGER;
ALTER TABLE recipes ADD COLUMN source VARCHAR(255);
ALTER TABLE recipes ADD COLUMN tags TEXT;
ALTER TABLE recipes ADD COLUMN ocr_raw_text TEXT;
ALTER TABLE recipes ADD COLUMN import_notes TEXT;

UPDATE recipes
SET source = 'Legacy website import'
WHERE source IS NULL;

ALTER TABLE categories ALTER COLUMN id RESTART WITH 10000;
ALTER TABLE recipes ALTER COLUMN id RESTART WITH 10000;

INSERT INTO categories (slug, name, display_order)
SELECT 'imported', 'Imported', 1000
WHERE NOT EXISTS (
    SELECT 1
    FROM categories
    WHERE slug = 'imported'
);
