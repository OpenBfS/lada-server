#!/bin/sh -e
# SYNOPSIS
# ./setup-db.sh [-cn] [-g N] [ROLE_NAME] [ROLE_PW] [DB_NAME]
#   -c         clean - drop an existing database
#   -n         no data - do not import example data
#   -g         generate N samples, each with N measms, each with either
#              N meas_vals or the maximum possible number of meas_vals, which
#              is contrained by the number of available measds.
#   ROLE_NAME  name of db user (default = lada)
#   ROLE_PW    login password  (default = ROLE_NAME)
#   DB_NAME    name of the databaes (default = ROLE_NAME)
#
# There will be used a remote database server if there exists the
# enviroment variable DB_SRV and optional DB_PORT

DIR=$(readlink -f $(dirname $0))

while getopts "cng:" opt; do
    case "$opt" in
        c)
            DROP_DB="true"
            ;;
        n)
            NO_DATA="true"
            ;;
        g)
            NO_DATA="true"
            N_SAMPLES="$OPTARG"
            GENERATE="true"
            ;;
    esac
done

shift $((OPTIND-1))

ROLE_NAME=${1:-lada}
echo "ROLE_NAME = $ROLE_NAME"
ROLE_PW=${2:-$ROLE_NAME}
echo "ROLE_PW = $ROLE_PW"
DB_NAME=${3:-$ROLE_NAME}
echo "DB_NAME = $DB_NAME"

# Stop on error any execution of SQL via psql
DB_CONNECT_STRING="-v ON_ERROR_STOP=on "

# if variable DB_SRV and otional DB_PORT is set a remote database connection will be used
if [ -n "$DB_SRV" ] ; then DB_CONNECT_STRING="$DB_CONNECT_STRING -h $DB_SRV" ; fi
if [ -n "$DB_SRV" -a -n "$DB_PORT"  ] ; then
  DB_CONNECT_STRING="$DB_CONNECT_STRING -p $DB_PORT"
fi
DB_CONNECT_STRING="$DB_CONNECT_STRING -U postgres"
echo "DB_CONNECT_STRING = $DB_CONNECT_STRING"

if [ `psql $DB_CONNECT_STRING -t --quiet --command "SELECT count(*) FROM pg_catalog.pg_user WHERE usename = '$ROLE_NAME'"` -eq 0 ] ; then
  echo create user $ROLE_NAME
  psql $DB_CONNECT_STRING --command "CREATE USER $ROLE_NAME PASSWORD '$ROLE_PW';"
fi

if [ "$DROP_DB" = "true" ] && psql $DB_CONNECT_STRING -l | grep -q "^ $DB_NAME " ; then
  echo drop db $DB_NAME
  psql $DB_CONNECT_STRING --command "DROP DATABASE $DB_NAME"
fi

echo create db $DB_NAME
psql $DB_CONNECT_STRING --command \
     "CREATE DATABASE $DB_NAME OWNER = $ROLE_NAME ENCODING = 'UTF8'"

echo create postgis extension
psql $DB_CONNECT_STRING -d $DB_NAME  --command  \
     "CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public"

echo create tablefunc extension
psql $DB_CONNECT_STRING -d $DB_NAME  --command  \
     "CREATE EXTENSION IF NOT EXISTS tablefunc WITH SCHEMA public"

echo create version table
psql $DB_CONNECT_STRING -d $DB_NAME \
    -c "SET role $ROLE_NAME;" \
    -f $DIR/updates/0000/01.add_schema_version.sql
for d in "$DIR"/updates/* ; do
  new_ver=$( basename $d )
done
psql $DB_CONNECT_STRING -d $DB_NAME --command \
     "INSERT INTO lada_schema_version(version) VALUES ($new_ver)"

echo create master schema
psql -q $DB_CONNECT_STRING -d $DB_NAME \
    -c "SET role $ROLE_NAME;" \
    -f $DIR/master_schema.sql #master_schema.sql

echo create lada schema
psql -q $DB_CONNECT_STRING -d $DB_NAME \
    -c "SET role $ROLE_NAME;" \
    -f $DIR/lada_schema.sql

echo create audit-trail table/trigger/views
for file in \
    audit_master.sql \
    audit_lada.sql \
    lada_views.sql
do
    psql -q $DB_CONNECT_STRING -d $DB_NAME \
        -c "SET role $ROLE_NAME;" \
        -f $DIR/$file
done

echo set grants
psql $DB_CONNECT_STRING -d $DB_NAME \
 --command "GRANT ALL ON ALL TABLES IN SCHEMA lada, master TO $ROLE_NAME;"\
 --command "GRANT ALL ON ALL SEQUENCES IN SCHEMA lada, master TO $ROLE_NAME;"

echo download german administrative borders
cd /tmp
SRC_URI=https://daten.gdz.bkg.bund.de/produkte/vg/vg250_ebenen_0101/aktuell/vg250_01-01.utm32s.shape.ebenen.zip
BASE_NAME=vg250_01-01.utm32s.shape.ebenen
SHAPE_DIR=${BASE_NAME}/vg250_ebenen_0101
if [ ! -f ${BASE_NAME}.zip ]; then
    curl -sfO ${SRC_URI}
fi
unzip -u ${BASE_NAME}.zip "*VG250_*"

echo create schema geo
psql $DB_CONNECT_STRING -d $DB_NAME --command \
     "CREATE SCHEMA geo AUTHORIZATION $ROLE_NAME"
# Only create the tables without importing the data
for file_table in "GEM gem" "KRS kr" "RBZ rb" "LAN bl"
do
    FILE=$(echo $file_table | awk '{print $1}')
    TABLE=$(echo $file_table | awk '{print $2}')
    shp2pgsql -p -s 25832:4326 \
        ${SHAPE_DIR}/VG250_${FILE} \
        geo.vg250_${TABLE} |\
        sed "1iSET role $ROLE_NAME;" |\
        psql -q $DB_CONNECT_STRING -d $DB_NAME
done

echo create main border view
psql -q $DB_CONNECT_STRING -d $DB_NAME \
    -c "SET role $ROLE_NAME;" \
    -f $DIR/master_admin_border_view.sql

echo create german views
psql -q $DB_CONNECT_STRING -d $DB_NAME \
    -c "SET role $ROLE_NAME;" \
    -c "CREATE SCHEMA land AUTHORIZATION $ROLE_NAME" \
    -c "CREATE SCHEMA stamm AUTHORIZATION $ROLE_NAME" \
    -f $DIR/en_dm_german_views.sql

if [ "$NO_DATA" != "true" ]; then
    echo import german administrative borders
    for file_table in "GEM gem" "KRS kr" "RBZ rb" "LAN bl"
    do
        FILE=$(echo $file_table | awk '{print $1}')
        TABLE=$(echo $file_table | awk '{print $2}')
        shp2pgsql -a -s 25832:4326 \
            ${SHAPE_DIR}/VG250_${FILE} \
            geo.vg250_${TABLE} | psql -q $DB_CONNECT_STRING -d $DB_NAME
    done
    echo refresh main border view
    psql $DB_CONNECT_STRING -d $DB_NAME --command \
         "REFRESH MATERIALIZED VIEW master.admin_border_view"

    echo "load data:"
    for file in "$DIR"/data/master/[0-9]*.sql "$DIR"/data/lada/[0-9]*.sql;
    do
        # If file with the same name prefixed "private_" exists, take that
        private=$(dirname $file)/private_$(basename $file)
        [ -f ${private} ] && file=${private}
        echo "  ${file%.sql}"
        psql -q $DB_CONNECT_STRING -d $DB_NAME -f $file
    done
fi

if [ "$GENERATE" = "true" ]; then
    echo "load master data:"
    for file in "$DIR"/data/master/[0-9]*.sql;
    do
        psql -q $DB_CONNECT_STRING -d $DB_NAME -f $file
    done

    echo "generating data ..."
    echo "\set n $N_SAMPLES
          WITH samples AS (INSERT INTO lada.sample (meas_facil_id, appr_lab_id)
              SELECT '06010', '06010' FROM generate_series(1, :n)
              RETURNING id),
          measms AS (INSERT INTO lada.measm (sample_id, mmt_id)
              SELECT id, 'AB' FROM samples, generate_series(1, :n)
              RETURNING id)
          INSERT INTO lada.meas_val (measm_id, measd_id, meas_unit_id)
              SELECT measms.id, measd.id, 0
              FROM measms,
                  (SELECT id FROM master.measd FETCH NEXT :n ROWS ONLY) AS measd" | \
        psql $DB_CONNECT_STRING -d $DB_NAME
fi
