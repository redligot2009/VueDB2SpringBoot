# Flyway Database Migration Setup

## Overview

This project now uses a **hybrid approach** for database schema management:

- **Local Development**: Hibernate auto-creates tables (`ddl-auto=update`)
- **Production**: Flyway runs controlled migrations (`ddl-auto=validate`)

## Current Status

✅ **Flyway is now properly configured for production deployment**
- Environment variables control Flyway and JPA behavior in `application.properties`
- `docker-compose.prod.yml` sets `SPRING_PROFILES_ACTIVE=production` and Flyway environment variables
- GitHub Actions deployment workflow adds production configuration to `.env` file
- Deployment workflow monitors Flyway migration execution and verifies database schema
- Flyway will automatically run migrations on production startup

## Profiles

### 1. Local Development Profile (`db2`)
- **File**: `application.properties` (default)
- **Settings**:
  ```properties
  spring.flyway.enabled=false
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.show-sql=true
  ```
- **Behavior**: Hibernate automatically creates/updates tables based on entity classes

### 2. Production Profile (`production`)
- **File**: `docker-compose.prod.yml` and `.env` file
- **Settings**:
  ```yaml
  environment:
    - SPRING_PROFILES_ACTIVE=production
    - FLYWAY_ENABLED=true
    - FLYWAY_LOCATIONS=classpath:db/migration
    - FLYWAY_BASELINE_ON_MIGRATE=true
    - FLYWAY_VALIDATE_ON_MIGRATE=true
    - JPA_DDL_AUTO=validate
    - JPA_SHOW_SQL=false
  ```
- **Behavior**: Flyway runs migrations, Hibernate only validates existing schema

## How to Use

### Local Development
```bash
# Default behavior - no special configuration needed
docker compose up --build
```

### Production Deployment
```bash
# The GitHub Actions workflow automatically:
# 1. Sets SPRING_PROFILES_ACTIVE=production in docker-compose.prod.yml
# 2. Waits for DB2 to be fully ready
# 3. Starts the backend with production profile
# 4. Flyway automatically runs migrations on startup
# 5. Hibernate validates the schema (ddl-auto=validate)

# Manual deployment (if needed):
export SPRING_PROFILES_ACTIVE=production
docker compose -f docker-compose.prod.yml up -d
```

## Migration Files

### Location
```
backend/src/main/resources/db/migration/
```

### Naming Convention
- `V1__Initial_Schema.sql` - Version 1, Initial Schema
- `V2__Add_New_Feature.sql` - Version 2, Add New Feature
- `V3__Fix_Bug.sql` - Version 3, Fix Bug

### Current Migration
- **V1__Initial_Schema.sql**: Creates users, galleries, and photos tables with proper indexes

## Benefits

1. **Development Speed**: No need to write migrations during development
2. **Production Safety**: Controlled, versioned database changes
3. **Team Collaboration**: Everyone gets the same database schema
4. **Rollback Capability**: Can revert to previous database versions

## When to Add New Migrations

Add a new migration file when you need to:
- Add new tables
- Modify existing table structure
- Add indexes or constraints
- Transform existing data
- Make any schema change that needs to be tracked

## Example: Adding a New Field

1. **Development**: Update your entity class, Hibernate will add the column
2. **Production**: Create a migration file like `V2__Add_New_Field.sql`
3. **Deploy**: Flyway will run the migration automatically

## Migration Best Practices

1. **Never modify existing migrations** - create new ones instead
2. **Test migrations locally** before deploying
3. **Keep migrations small and focused**
4. **Use descriptive names** for migration files
5. **Include rollback scripts** for complex changes (optional)

## Troubleshooting

### Migration Fails
- Check the migration file syntax
- Ensure the database is in the expected state
- Look at Flyway logs for specific error messages

### Schema Mismatch
- Verify the production profile is active
- Check that all migrations have run successfully
- Ensure entity classes match the database schema
