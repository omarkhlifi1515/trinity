"""
Migration helper: copy data from a local SQLite database to a Postgres database.

Usage:
  python migrate_sqlite_to_postgres.py --sqlite "G:/My Drive/Project/hr_data.db" --pg "postgres://user:pass@host:5432/dbname"

Notes:
- This script performs a best-effort table-by-table copy. It will create tables in Postgres matching SQLite column names and types using SQLAlchemy reflection.
- Complex types, constraints, and custom SQLite constructs may need manual review.
- Always backup both databases before running.
"""

import argparse
import sys
import sqlalchemy
from sqlalchemy import create_engine, MetaData, Table
from sqlalchemy.schema import CreateTable
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import sessionmaker


def copy_tables(sqlite_url, pg_url, include_tables=None, exclude_tables=None):
    # Engines
    engine_sqlite = create_engine(sqlite_url)
    engine_pg = create_engine(pg_url)

    meta_sqlite = MetaData()
    meta_pg = MetaData()

    # reflect sqlite
    meta_sqlite.reflect(bind=engine_sqlite)

    tables = list(meta_sqlite.tables.keys())
    if include_tables:
        tables = [t for t in tables if t in include_tables]
    if exclude_tables:
        tables = [t for t in tables if t not in exclude_tables]

    if not tables:
        print("No tables found to migrate.")
        return

    print(f"Found tables to migrate: {tables}")

    # create tables on Postgres
    for tname in tables:
        t = meta_sqlite.tables[tname]
        # create a new Table object for PG with the same columns
        cols = []
        for col in t.columns:
            # create column with same name and a generic type mapping
            ctype = map_sqlite_type_to_sqlalchemy(col.type)
            cols.append(sqlalchemy.Column(col.name, ctype, primary_key=col.primary_key))
        pg_table = Table(tname, meta_pg, *cols)
        try:
            pg_table.create(bind=engine_pg, checkfirst=True)
            print(f"Created table {tname} in Postgres (if not exists)")
        except Exception as e:
            print(f"Warning: could not create table {tname}: {e}")

    # Copy data
    src_conn = engine_sqlite.connect()
    dest_conn = engine_pg.connect()

    for tname in tables:
        print(f"Copying data for table: {tname}")
        t = meta_sqlite.tables[tname]
        rows = src_conn.execute(t.select()).fetchall()
        if not rows:
            print(f"  No rows to copy for {tname}")
            continue
        # Build insert statement for postgres table
        pg_table = Table(tname, meta_pg, autoload_with=engine_pg)
        trans = dest_conn.begin()
        try:
            for row in rows:
                data = dict(row)
                dest_conn.execute(pg_table.insert().values(**data))
            trans.commit()
            print(f"  Inserted {len(rows)} rows into {tname}")
        except Exception as e:
            trans.rollback()
            print(f"  Error inserting rows into {tname}: {e}")

    src_conn.close()
    dest_conn.close()


def map_sqlite_type_to_sqlalchemy(sqlite_type):
    # sqlite_type may be a Dialect-specific type or a string; do a best-effort mapping
    try:
        typename = str(sqlite_type).lower()
    except Exception:
        typename = ''
    if 'int' in typename:
        return sqlalchemy.Integer
    if 'char' in typename or 'text' in typename or 'varchar' in typename:
        return sqlalchemy.Text
    if 'bool' in typename:
        return sqlalchemy.Boolean
    if 'real' in typename or 'floa' in typename or 'doub' in typename:
        return sqlalchemy.Float
    # fallback
    return sqlalchemy.Text


if __name__ == '__main__':
    p = argparse.ArgumentParser(description='Migrate SQLite to Postgres (best-effort)')
    p.add_argument('--sqlite', required=True, help='Path to sqlite file, e.g. sqlite:///C:/path/hr_data.db or G:/...')
    p.add_argument('--pg', required=True, help='Postgres URL, e.g. postgres://user:pass@host:5432/dbname')
    p.add_argument('--include', nargs='*', help='Optional list of tables to include')
    p.add_argument('--exclude', nargs='*', help='Optional list of tables to exclude')

    args = p.parse_args()

    # normalize sqlite url
    sqlite_arg = args.sqlite
    if sqlite_arg.startswith('sqlite:///') or sqlite_arg.startswith('sqlite://'):
        sqlite_url = sqlite_arg
    else:
        # treat as file path
        sqlite_url = f"sqlite:///{sqlite_arg.replace('\\', '/')}"

    try:
        copy_tables(sqlite_url, args.pg, include_tables=args.include, exclude_tables=args.exclude)
    except SQLAlchemyError as e:
        print('Migration failed:', e)
        sys.exit(1)
    except Exception as e:
        print('Unexpected error:', e)
        sys.exit(1)
    print('Migration finished.')
