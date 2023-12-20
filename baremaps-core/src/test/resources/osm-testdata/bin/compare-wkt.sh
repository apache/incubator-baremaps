#!/bin/sh
#
#  compare-wkt.sh WKT_REF WKT_TEST
#
#  Compares the reference and the test geometries given on the command line
#  in WKT format.
#
#  Outputs a line to STDOUT ending in "OK" or "ERR" to show whether the test
#  succeeded or failed. More detailed information is written to STDERR.
#
#  Returns
#  0 - if the geometries are identical
#  1 - if the geometries are geometrically equivalent
#  2 - if the geometries are different
#  3 - if at least one of the geometries is broken
#
#  Instead of a WKT geometry the string "INVALID" can also be given which only
#  compares equal to another "INVALID".
#

set -e
#set -x

WKT_REF=$1
WKT_TEST=$2

DATABASE=compare-wkt-tmp-$$.db

if [ "$WKT_REF" = "INVALID" -a "$WKT_TEST" = "INVALID" ]; then
    echo "both INVALID => OK"
    exit 0
fi

if [ "$WKT_REF" = "$WKT_TEST" ]; then
    echo "geoms identical => OK"
    exit 0
fi

if [ "$WKT_REF" = "INVALID" ]; then
    echo "should be INVALID => ERR"
    exit 2
fi

if [ "$WKT_TEST" = "INVALID" ]; then
    echo "should not be INVALID => ERR"
    exit 2
fi

echo "Testing reference WKT [$WKT_REF]:" 1>&2
rwkt_ref=`spatialite -batch -bail $DATABASE "SELECT IsValid(GeomFromText('$WKT_REF', 4326));" | tail -1`
if [ "$rwkt_ref" != "1" ]; then
    echo "reference geometry is invalid => ERR"
    echo "  Reference geometry is invalid. result: $rwkt_ref" 1>&2
    rm -f $DATABASE
    exit 3
fi

echo "  Geometry valid" 1>&2

echo "Testing test WKT [$WKT_TEST]:" 1>&2
rwkt_test=`spatialite -batch -bail $DATABASE "SELECT IsValid(GeomFromText('$WKT_TEST', 4326));" | tail -1`
if [ "$rwkt_test" != "1" ]; then
    echo "test geometry is invalid => ERR"
    echo "  Test geometry is invalid. result: $rwkt_test" 1>&2
    rm -f $DATABASE
    exit 3
fi

echo "  Geometry valid" 1>&2

result=`spatialite -batch -bail $DATABASE "SELECT Equals(GeomFromText('$WKT_REF', 4326), GeomFromText('$WKT_TEST', 4326));" | tail -1`
rm -f $DATABASE

if [ "$result" = "1" ]; then
    echo "geoms equal => OK"
    exit 1
fi

if [ "$result" = "0" ]; then
    echo "geoms different => ERR"
    exit 2
fi

echo "unknown failure => ERR"
exit 3

