#!/usr/bin/ruby
#
#  create-sql-for-multipolygons.rb REFERENCE-JSON-FILE
#
#  Reads the given reference JSON file and creates the
#  SQL commands to create all multipolygons contained
#  in the file. The SQL commands are written to stdout.
#

require 'json'

open(ARGV[0]) do |file|
    reference_data = JSON.load(file, nil)
    reference_data.each do |test|
        if test['areas']
            test['areas'].each do |k,v|
                v.each do |result|
                    if result['wkt'] != 'INVALID'
                        puts "INSERT INTO multipolygons (test_id, id, from_type, variant, geom) VALUES (#{test['test_id']}, #{result['from_id']}, '#{result['from_type'][0]}', '#{k}', MultiPolygonFromText('#{result['wkt']}', 4326));"
                    end
                end
            end
        end
    end
end

