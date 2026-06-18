# Document Manager System

Desktop application for managing student documents with Java 17, JavaFX, Maven, JDBC, SQL Server, and MVC-style packages.

## Run

1. Install Java 17+ and Maven.
2. Create SQL Server database using `src/main/java/com/documentmanager/database/schema.sql`.
3. Update database settings using environment variables if needed:
   - `DMS_DB_URL`
   - `DMS_DB_USER`
   - `DMS_DB_PASSWORD`
4. Run:

```bash
mvn javafx:run
```

Default database URL:

```text
jdbc:sqlserver://localhost:1433;databaseName=DocumentManagerDB;encrypt=true;trustServerCertificate=true
```
