#!/bin/bash

# Configuration for Firebird DB
DB_USER="SYSDBA"
DB_PASS="masterkey"
DB_NAME="OPROS.FDB"
DB_HOST="localhost"

# Absolute path to the database file
# You may want to change this to your actual project path
DB_PATH="$(pwd)/$DB_NAME"
DB_CONNECTION="$DB_HOST:$DB_PATH"

# Paths to directories
MIGRATIONS_DIR="$(pwd)/migrations"
DATA_DIR="$(pwd)/data"

# Firebird ISQL tool (assumes it's in PATH)
ISQL_BIN="isql"
