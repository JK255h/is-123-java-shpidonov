#!/bin/bash

# Load configuration
source ./config.sh

echo "Creating database at $DB_CONNECTION..."

# Create database command for ISQL
CREATE_DB_CMD="CREATE DATABASE '$DB_CONNECTION' USER '$DB_USER' PASSWORD '$DB_PASS' DEFAULT CHARACTER SET UTF8;"

# Create the DB file
echo "$CREATE_DB_CMD" | $ISQL_BIN -q

if [ $? -eq 0 ]; then
    echo "Database created successfully."
else
    echo "Failed to create database. It might already exist."
fi

# Apply initial migration V1
echo "Applying initial schema V1..."
$ISQL_BIN -u $DB_USER -p $DB_PASS -i "$MIGRATIONS_DIR/V1__init_schema.sql" "$DB_CONNECTION"

# Apply seed data
echo "Seeding initial data..."
for f in "$DATA_DIR"/*.sql; do
    echo "Running $f..."
    $ISQL_BIN -u $DB_USER -p $DB_PASS -i "$f" "$DB_CONNECTION"
done

echo "Database initialization complete."
