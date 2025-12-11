-- ============================================
-- FIX: DROP AND RECREATE chat_messages TABLE
-- ============================================

-- Drop existing table if exists
IF OBJECT_ID('chat_messages', 'U') IS NOT NULL
BEGIN
    DROP TABLE chat_messages;
    PRINT 'Tabla chat_messages eliminada'
END
GO

-- Recreate table with correct schema
CREATE TABLE chat_messages (
    id NVARCHAR(255) NOT NULL PRIMARY KEY,  -- Changed from (36) to (255) for UUID compatibility
    sender_id NVARCHAR(255) NOT NULL,
    receiver_id NVARCHAR(255) NOT NULL,
    encrypted_content NVARCHAR(MAX) NOT NULL,
    timestamp DATETIME2 NOT NULL DEFAULT GETDATE(),
    status NVARCHAR(20) NOT NULL DEFAULT 'SENT',
    
    -- Constraints
    CONSTRAINT CHK_chat_messages_status CHECK (status IN ('SENT', 'DELIVERED', 'READ'))
);
GO

-- Create indexes for optimal performance
CREATE INDEX idx_sender_receiver 
ON chat_messages(sender_id, receiver_id);

CREATE INDEX idx_receiver 
ON chat_messages(receiver_id);

CREATE INDEX idx_timestamp 
ON chat_messages(timestamp DESC);

CREATE INDEX idx_receiver_status 
ON chat_messages(receiver_id, status);
GO

PRINT 'Tabla chat_messages recreada exitosamente';
GO

-- Verify the table structure
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'chat_messages'
ORDER BY ORDINAL_POSITION;
