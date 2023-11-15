--
-- PostgreSQL database dump
--

-- Dumped from database version 10.17 (Debian 10.17-1.pgdg90+1)
-- Dumped by pg_dump version 11.13 (Ubuntu 11.13-1.pgdg20.04+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner:
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';

--
-- PostgreSQL database dump complete
--



DO
$do$
    BEGIN
        IF NOT EXISTS(
                SELECT
                FROM pg_catalog.pg_roles -- SELECT list can be empty for this
                WHERE rolname = 'nls_read_only')
        THEN
            CREATE ROLE nls_read_only NOLOGIN NOSUPERUSER NOINHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;
        END IF;
    END
$do$;

DO
$do$
    BEGIN
        IF NOT EXISTS(
                SELECT
                FROM pg_catalog.pg_roles -- SELECT list can be empty for this
                WHERE rolname = 'nls')
        THEN
            CREATE ROLE nls NOLOGIN NOSUPERUSER NOINHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;
        END IF;
    END
$do$;

DO
$do$
    BEGIN
        IF NOT EXISTS(
                SELECT
                FROM pg_catalog.pg_roles -- SELECT list can be empty for this
                WHERE rolname = 'shivi_read_only')
        THEN
            CREATE ROLE shivi_read_only NOLOGIN NOSUPERUSER NOINHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;
        END IF;
    END
$do$;

CREATE OR REPLACE FUNCTION public.grant_nls_read_only_on_all()
    RETURNS VOID
AS
$do$
DECLARE
    the_database TEXT := CURRENT_DATABASE();
    the_schema   text;
BEGIN
    EXECUTE FORMAT('GRANT ALL PRIVILEGES ON DATABASE "%s" TO nls_read_only', the_database);
    FOR the_schema IN SELECT nspname FROM pg_namespace WHERE nspowner > 10 OR nspname = 'public'
        LOOP
            -- By default, all users can create new tables. Revoke it using the 'public' keyword.
            EXECUTE format($$ REVOKE CREATE ON SCHEMA %I FROM PUBLIC $$, the_schema);

            -- Grant create to nls and nls_administrator
            EXECUTE format($$ GRANT CREATE ON SCHEMA %I TO nls $$, the_schema);

            -- Grant read only
            EXECUTE format($$ GRANT USAGE ON SCHEMA %I TO nls_read_only $$, the_schema);
            EXECUTE format($$ GRANT SELECT ON ALL TABLES IN SCHEMA %I TO nls_read_only $$, the_schema);
            EXECUTE format($$ ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT SELECT ON TABLES TO nls_read_only $$,
                           the_schema);
        END LOOP;
END;
$do$ LANGUAGE plpgsql;