#!/bin/bash

# Load configuration
source ./config.sh

echo "Checking for migrations..."

# Get current version from DB
CURRENT_VERSION=$($ISQL_BIN -u $DB_USER -p $DB_PASS -q -t "$DB_CONNECTION" <<EOF
SET HEADING OFF;
SELECT MAX(VERSION) FROM SCHEMA_VERSION;
EXIT;
EOF
)

# Trim whitespace
CURRENT_VERSION=$(echo $CURRENT_VERSION | xargs)

if [ -z "$CURRENT_VERSION" ] || [ "$CURRENT_VERSION" == "null" ]; then
    CURRENT_VERSION=0
fi

echo "Current schema version: $CURRENT_VERSION"

# Find and apply new migrations
# Expects format V<number>__<description>.sql
NEW_MIGRATIONS_FOUND=false

for f in "$MIGRATIONS_DIR"/V*__*.sql; do
    # Extract version number from filename
    filename=$(basename "$f")
    version=$(echo $filename | sed -E 's/V([0-9]+)__.*/\1/')
    
    if [ "$version" -gt "$CURRENT_VERSION" ]; then
        echo "Applying migration $filename (Version $version)..."
        
        $ISQL_BIN -u $DB_USER -p $DB_PASS -i "$f" "$DB_CONNECTION"
        
        if [ $? -eq 0 ]; then
            # Update SCHEMA_VERSION table
            description=$(echo $filename | sed -E 's/V[0-9]+__(.*)\.sql/\1/' | tr '_' ' ')
            $ISQL_BIN -u $DB_USER -p $DB_PASS "$DB_CONNECTION" <<EOF
INSERT INTO SCHEMA_VERSION (VERSION, DESCRIPTION) VALUES ($version, '$description');
COMMIT;
EXIT;
EOF
            echo "Successfully applied version $version."
            NEW_MIGRATIONS_FOUND=true
        else
            echo "ERROR: Failed to apply migration $filename. Stopping."
            exit 1
        fi
    fi
done

if [ "$NEW_MIGRATIONS_FOUND" = false ]; then
    echo "Database is already up to date."
fi
