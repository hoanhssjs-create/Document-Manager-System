CREATE DATABASE DocumentManagerDB;
GO

USE DocumentManagerDB;
GO

CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL,
    PasswordHash NVARCHAR(255) NOT NULL,
    PasswordSalt NVARCHAR(255) NOT NULL,
    FullName NVARCHAR(100) NOT NULL,
    Email NVARCHAR(150) NOT NULL,
    CreatedDate DATETIME2 NOT NULL CONSTRAINT DF_Users_CreatedDate DEFAULT SYSUTCDATETIME(),
    IsActive BIT NOT NULL CONSTRAINT DF_Users_IsActive DEFAULT 1,
    CONSTRAINT UQ_Users_Username UNIQUE (Username),
    CONSTRAINT UQ_Users_Email UNIQUE (Email),
    CONSTRAINT CK_Users_Email CHECK (Email LIKE '%_@_%._%')
);

CREATE TABLE Folders (
    FolderID INT IDENTITY(1,1) PRIMARY KEY,
    FolderName NVARCHAR(100) NOT NULL,
    UserID INT NOT NULL,
    CreatedDate DATETIME2 NOT NULL CONSTRAINT DF_Folders_CreatedDate DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Folders_Users FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT UQ_Folders_User_FolderName UNIQUE (UserID, FolderName)
);

CREATE TABLE Documents (
    DocumentID INT IDENTITY(1,1) PRIMARY KEY,
    Title NVARCHAR(200) NOT NULL,
    Description NVARCHAR(1000) NULL,
    FilePath NVARCHAR(500) NOT NULL,
    FileType NVARCHAR(10) NOT NULL,
    FileSize BIGINT NOT NULL,
    CreatedDate DATETIME2 NOT NULL CONSTRAINT DF_Documents_CreatedDate DEFAULT SYSUTCDATETIME(),
    UpdatedDate DATETIME2 NULL,
    FolderID INT NULL,
    UserID INT NOT NULL,
    CONSTRAINT FK_Documents_Folders FOREIGN KEY (FolderID) REFERENCES Folders(FolderID) ON DELETE SET NULL,
    CONSTRAINT FK_Documents_Users FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT UQ_Documents_User_FilePath UNIQUE (UserID, FilePath),
    CONSTRAINT CK_Documents_FileType CHECK (FileType IN ('PDF', 'DOCX', 'TXT')),
    CONSTRAINT CK_Documents_FileSize CHECK (FileSize > 0)
);

CREATE TABLE Tags (
    TagID INT IDENTITY(1,1) PRIMARY KEY,
    TagName NVARCHAR(50) NOT NULL,
    ColorHex CHAR(7) NOT NULL CONSTRAINT DF_Tags_ColorHex DEFAULT '#3366CC',
    UserID INT NOT NULL,
    CreatedDate DATETIME2 NOT NULL CONSTRAINT DF_Tags_CreatedDate DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Tags_Users FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT UQ_Tags_User_TagName UNIQUE (UserID, TagName)
);

CREATE TABLE DocumentTags (
    DocumentID INT NOT NULL,
    TagID INT NOT NULL,
    AssignedDate DATETIME2 NOT NULL CONSTRAINT DF_DocumentTags_AssignedDate DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_DocumentTags PRIMARY KEY (DocumentID, TagID),
    CONSTRAINT FK_DocumentTags_Documents FOREIGN KEY (DocumentID) REFERENCES Documents(DocumentID) ON DELETE CASCADE,
    CONSTRAINT FK_DocumentTags_Tags FOREIGN KEY (TagID) REFERENCES Tags(TagID) ON DELETE CASCADE
);

CREATE TABLE ActivityLogs (
    LogID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL,
    DocumentID INT NULL,
    Action VARCHAR(20) NOT NULL,
    Description NVARCHAR(500) NULL,
    LogTime DATETIME2 NOT NULL CONSTRAINT DF_ActivityLogs_LogTime DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_ActivityLogs_Users FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT FK_ActivityLogs_Documents FOREIGN KEY (DocumentID) REFERENCES Documents(DocumentID) ON DELETE SET NULL,
    CONSTRAINT CK_ActivityLogs_Action CHECK (Action IN ('LOGIN', 'LOGOUT', 'UPLOAD', 'VIEW', 'EDIT', 'DELETE'))
);

CREATE INDEX IX_Folders_UserID ON Folders(UserID);
CREATE INDEX IX_Documents_UserID ON Documents(UserID);
CREATE INDEX IX_Documents_FolderID ON Documents(FolderID);
CREATE INDEX IX_Documents_Title ON Documents(Title);
CREATE INDEX IX_Documents_FileType ON Documents(FileType);
CREATE INDEX IX_Documents_CreatedDate ON Documents(CreatedDate DESC);
CREATE INDEX IX_Tags_UserID ON Tags(UserID);
CREATE INDEX IX_Tags_TagName ON Tags(TagName);
CREATE INDEX IX_DocumentTags_TagID ON DocumentTags(TagID);
CREATE INDEX IX_ActivityLogs_User_LogTime ON ActivityLogs(UserID, LogTime DESC);
GO
