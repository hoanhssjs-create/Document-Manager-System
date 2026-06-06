USE DocumentManagerDB;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Documents_UserID' AND object_id = OBJECT_ID('Documents'))
    CREATE INDEX IX_Documents_UserID ON Documents(UserID);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Documents_FolderID' AND object_id = OBJECT_ID('Documents'))
    CREATE INDEX IX_Documents_FolderID ON Documents(FolderID);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Documents_FileType' AND object_id = OBJECT_ID('Documents'))
    CREATE INDEX IX_Documents_FileType ON Documents(FileType);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Documents_CreatedDate' AND object_id = OBJECT_ID('Documents'))
    CREATE INDEX IX_Documents_CreatedDate ON Documents(CreatedDate DESC);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_DocumentTags_TagID' AND object_id = OBJECT_ID('DocumentTags'))
    CREATE INDEX IX_DocumentTags_TagID ON DocumentTags(TagID);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_ActivityLogs_User_LogTime' AND object_id = OBJECT_ID('ActivityLogs'))
    CREATE INDEX IX_ActivityLogs_User_LogTime ON ActivityLogs(UserID, LogTime DESC);
GO
