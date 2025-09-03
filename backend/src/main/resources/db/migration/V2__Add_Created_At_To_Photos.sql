-- Migration: V2__Add_Created_At_To_Photos
-- Description: Add created_at column to existing photos table if missing
-- This migration handles the case where the table was created before the created_at field was added

-- Check if the column already exists to avoid errors
-- DB2 doesn't support IF NOT EXISTS for ALTER TABLE, so we use a different approach

-- Add created_at column to photos table with a default value
-- Using CURRENT TIMESTAMP for DB2 compatibility
ALTER TABLE photos ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT TIMESTAMP;

-- Update existing records to have a default timestamp
-- This ensures all existing photos have a valid timestamp
UPDATE photos SET created_at = CURRENT TIMESTAMP WHERE created_at IS NULL;

-- Make the column NOT NULL after updating existing records
-- This ensures data integrity going forward
ALTER TABLE photos ALTER COLUMN created_at SET NOT NULL;

-- Create index on created_at for better performance on sorting operations
-- This will improve the performance of ORDER BY created_at queries
CREATE INDEX idx_photos_created_at ON photos(created_at);

-- Note: This migration is designed to be idempotent
-- If the column already exists, the ALTER TABLE will fail gracefully in most DB2 versions
-- The UPDATE and CREATE INDEX statements will handle existing data appropriately
