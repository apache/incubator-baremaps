#!/bin/sh
#
#  show-tests.sh
#

set -e

tests=`find data -mindepth 2 -maxdepth 2 -type d | sed -s 's/data\///' | sort`

for t in $tests; do
    cat_no=${t%%/*}
    test_id=${t#*/}
    if [ -f data/$t/result ]; then
        result=`cat data/$t/result`
    else
        result="UNKNOWN RESULT"
    fi
    printf "%1s %3s %-7s %s\n" $cat_no $test_id $result
done

