#!/bin/sh
#
#  create-grid.sh TEST-CATEGORY...
#
#  Create "grid" of polygons showing the areas of the tests.
#
#  Call with the numbers of the test categories the grid
#  shall show, for instance:
#
#  bin/create-grid.sh 1 7
#

for t in $*; do
    title=`cat data/$t/description.txt`
    echo "INSERT INTO titles (title, geom) VALUES ('${t}. ${title}', LineFromText('LINESTRING(${t}.0 2.1,${t}.9999 2.1)', 4326));\n"
    for y in `seq 0 9`; do
        for x in `seq 0 9`; do
            if [ -d data/$t/$t$y$x ]; then
                available=1
                if [ -f data/$t/$t$y$x/result ]; then
                    result=`cat data/$t/$t$y$x/result`
                else
                    result=""
                fi
                if [ -f data/$t/$t$y$x/description.txt ]; then
                    description=`cat data/$t/$t$y$x/description.txt`
                else
                    description=""
                fi
            else
                available=0
                result=""
                description=""
            fi
            echo "INSERT INTO grid (test_id, geom, available, result, description) VALUES ($t$y$x, Envelope(LineFromText('LINESTRING(${t}.${x} 1.${y},${t}.${x}9999 1.${y}9999)', 4326)), ${available}, '${result}', '${description}');"
        done
        echo
    done
done

