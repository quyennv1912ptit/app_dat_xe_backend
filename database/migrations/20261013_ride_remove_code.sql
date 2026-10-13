-- Apply manually to the existing PostgreSQL schema before starting the application.
-- Run 20261013_rename_ride_schema.sql first if the old tables are still named bookings.
-- Use the same search_path/schema as Hibernate. Stored ride codes are removed;
-- ride IDs, other ride fields, rows and relationships are preserved.
BEGIN;
ALTER TABLE rides DROP COLUMN IF EXISTS code;
COMMIT;
