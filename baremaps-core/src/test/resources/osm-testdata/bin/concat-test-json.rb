#!/usr/bin/ruby
#
#  concat-test-json.rb JSON-FILE..
#
#  Concatenate given files in JSON format into one big JSON file containing an
#  array of the contents of the input files. The result is written to stdout.
#  This also checks that the test_id in the input files matches the file name.
#

require 'json'

array = ARGV.sort.map do |filename|
    id = filename.sub(%r{/test\.json$}, '').sub(%r{.*/}, '').to_i
    open(filename) do |file|
        data = JSON.load(file)

        if data['test_id'] != id
            STDERR.puts "File #{filename} should contain test with id #{id}, but instead contains test_id: #{data['test_id']}"
            exit 1
        end

        if ! data['description']
            STDERR.puts "WARNING! Missing description for test #{id}."
        end

        data
    end
end

puts JSON.pretty_generate(array)

