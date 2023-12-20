#!/bin/sh
#
#  create-wkt.sh
#
#  This script will generate possible nodes.wkt and ways.wkt files
#  from the data.osm file. YOU HAVE TO CHECK THE OUTPUT, BECAUSE
#  THIS SCRIPT MIGHT NOT ALWAYS DO THE RIGHT THING!
#

for t in data/?/???; do
    xsltproc bin/osm2nodes_wkt.xsl $t/data.osm >$t/nodes.wkt

    if [ ! -s $t/nodes.wkt ]; then
        rm $t/nodes.wkt
    fi

    xsltproc bin/osm2ways_wkt.xsl $t/data.osm | \
        # remove duplicate coordinates
        sed -re 's/([0-9.]+ [0-9.]+),\1/\1/' | \
        # remove linestrings with single coordinate
        egrep -v 'LINESTRING\([0-9.]+ [0-9.]+\)' \
        >$t/ways.wkt

    if [ ! -s $t/ways.wkt ]; then
        rm $t/ways.wkt
    fi
done

