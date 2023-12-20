#!/bin/sh
#
#  show-tests.sh
#

set -e

tests=`find data -mindepth 1 -maxdepth 1 -type d | sed -s 's/data\///' | sort`

for t in $tests; do
    cat_no=${t%%-*}
    cat_name_test_id=${t#*-}
    cat_name=${cat_name_test_id%/*}
    test_id=${t#*/}
    if [ -f data/$t/description.txt ]; then
        description=`cat data/$t/description.txt`
    else
        description="NO DESCRIPTION"
    fi
    if [ -f data/$t/result ]; then
        result=`cat data/$t/result`
    else
        result="UNKNOWN"
    fi
    echo "$cat_no $cat_name\t$test_id\t$result\t$description"
done

