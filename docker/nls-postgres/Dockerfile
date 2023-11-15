FROM ndwnls.azurecr.io/nls-postgres-it-container:latest
COPY 00_init.sql /docker-entrypoint-initdb.d/00_init.sql
COPY sql/*.sql /docker-entrypoint-initdb.d/
COPY postgresql.conf /conf/postgresql.conf
COPY copy_postgresql_conf.sh /docker-entrypoint-initdb.d/copy_postgresql_conf.sh
