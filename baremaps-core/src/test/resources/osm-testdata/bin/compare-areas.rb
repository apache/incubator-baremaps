#!/usr/bin/ruby
#
#  compare-areas.rb REFERENCE-JSON-FILE TEST-JSON-FILE
#
#  Compare the areas created by running the test program in the TEST-JSON-FILE
#  to the areas described in the REFERENCE-JSON-FILE.
#
#  All geometries and tags from the reference data file are checked against
#  geometries in the test data file. Results are printed in short form containing
#  the test id and the word "OK" or "ERR" (in green or red, respectively).
#
#  More detailed (debug) output can be found in the file
#  compare-areas.log.
#

require 'json'
require 'open3'

file = open(ARGV[0])
reference_data = JSON.load(file, nil, {:symbolize_names => true, :create_additions => false})
file.close

file = open(ARGV[1])
test_data = JSON.load(file, nil, {:symbolize_names => true, :create_additions => false})
file.close

LOG = open('compare-areas.log', 'w')
LOG.puts "Running at #{Time.now}"

def print_tags(tags)
    tags.map{ |k,v|
        "#{k}=#{v}"
    }.join(', ')
end

# Compare set of reference and test tags given as hashes
def compare_tags(ref, tst)
    LOG.puts "    Comparing tags..."
    if ref.nil?
        if tst.nil?
            LOG.puts "      No tags => OK"
            return true
        else
            LOG.puts "      Has tags but ref does not => ERR"
            return false
        end
    end

    if tst.nil?
        LOG.puts "      Missing tags (#{ print_tags(ref) }) => ERR"
        return false
    end

    refcpy = ref
    ref.each do |k,v|
        if tst[k] != v
            LOG.puts "      Tag key '#{k}' should be '#{v}' but is '#{tst[k]}' => ERR"
            return false
        end
        tst.delete k
        refcpy.delete k
    end

    if refcpy.size != 0
        LOG.puts "      Missing tags (#{ print_tags(refcpy) }) => ERR"
        return false
    end

    if tst.size != 0
        LOG.puts "      Additional tags (#{ print_tags(tst) }) => ERR"
        return false
    end

    LOG.puts '      Tags match => OK'
    return true
end

def spatial_sql(query)
    $sl_stdin.puts(query)
    res = ''
    err = ''
    timeout = 10
    while true do
        r = IO.select([$sl_stdout ,$sl_stderr], [], [], timeout)
        if r.nil?
            if err != ''
                LOG.puts err
            end
            return res.chomp!
        end
        begin
            if r[0][0] == $sl_stdout
                res += $sl_stdout.read_nonblock(10000)
                timeout = 0.001
            elsif r[0][0] == $sl_stderr
                err += $sl_stderr.read_nonblock(10000)
            end
        rescue IO::WaitReadable, IO::EWOULDBLOCKWaitReadable
            # ignore
        end
    end
end

def compare_wkt(ref, test)
    if ref == 'INVALID' && test == 'INVALID'
       return 0, "both INVALID => OK"
    end
    if ref == test
       return 0, "geoms identical => OK"
    end
    if ref == 'INVALID'
       return 2, "should be INVALID => ERR"
    end
    if test == 'INVALID'
       return 2, "should not be INVALID => ERR"
    end

    LOG.puts "Testing reference WKT [#{ref}]:"
    rwkt_ref = spatial_sql("SELECT IsValid(GeomFromText('#{ref}', 4326));")
    if rwkt_ref != "1"
      LOG.puts "  Reference geometry is invalid. result: #{rwkt_ref}"
      return 3, "reference geometry is invalid => ERR"
    end
    LOG.puts "  Geometry valid"

    LOG.puts "Testing test WKT [#{test}]:"
    rwkt_test = spatial_sql("SELECT IsValid(GeomFromText('#{test}', 4326));")
    if rwkt_test != "1"
      LOG.puts "  Test geometry is invalid. result: #{rwkt_test}"
      return 3, "test geometry is invalid => ERR"
    end
    LOG.puts "  Geometry valid"

    result = spatial_sql("SELECT Equals(GeomFromText('#{ref}', 4326), GeomFromText('#{test}', 4326));")
    return 1, "geoms equal => OK" if result == "1"
    return 2, "geoms different => ERR" if result == "0"
    return 3, "unknown failure => ERR"
end

# Compare reference and test geometries given as WKT
def compare_geom(ref, tst)
    LOG.puts "    Comparing geometries..."
    result, msg = compare_wkt(ref, tst)
    LOG.puts "      #{msg}"
    return result <= 1
end

# Compare all aspects of reference to test area
def compare_area(area, t)
    geom_okay = compare_geom(area[:wkt], t[:wkt])
    tags_okay = compare_tags(area[:tags], t[:tags])
    from_okay = (area[:from_id] == t[:from_id] && area[:from_type] == t[:from_type])

    return geom_okay && tags_okay && from_okay
end


def check_variant(variant, areas, td)
    LOG.puts "  Checking variant #{variant}:"

    used_areas = {}

    td.each_with_index do |t, tindex|
        areas.select{ |area| !used_areas[area] }.each do |area|
            LOG.puts "    Checking test area ##{tindex} against reference area ##{area[:index]}..."
            result = compare_area(area, t)
            if result
                used_areas[area] = true
                break
            end
        end
    end

    if used_areas.size != areas.size
        LOG.puts "    Not all areas created => ERR"
        return false
    end

    if td.size != areas.size
        LOG.puts "    More areas in test data than in reference data => ERR"
        return false
    end

    true
end

result_all = true

$sl_stdin, $sl_stdout, $sl_stderr, $wait_thr = Open3.popen3('spatialite', '-batch', '-bail', ':memory:')

# spatialite will print put some stuff when starting up, ignore it
$sl_stdin.puts("SELECT 'COMPARE_AREAS_START';");
while ($sl_stdout.gets() !~ /COMPARE_AREAS_START/) do
end

begin
    $sl_stderr.read_nonblock(10000);
rescue IO::WaitReadable, IO::EWOULDBLOCKWaitReadable
    # ignore
end

# Get all the test cases from the reference data and check all that
# have area information in turn
reference_data.select{ |ref| ref[:areas] }.each do |ref|

    # Number each area in each variant (for debugging output)
    ref[:areas].each do |variant, areas|
        areas.each_with_index do |area, index|
            area[:index] = index
        end
    end

    LOG.puts "\n============================\nTesting id #{ref[:test_id]}:"
    if ref[:test_id] % 100 == 0
        puts
    end
    if ref[:test_id] % 10 == 0
        puts
    end
    print "#{ref[:test_id]}..."

    # find all test data for this test id
    td = test_data.select do |td|
        td[:test_id] == ref[:test_id]
    end

    if !td
        STDERR.puts "Missing test data for test id #{ref[:id]}\n"
        exit 1
    end

    # check each variant
    result = ref[:areas].map{ |variant, areas|
        variant_result = check_variant(variant, areas, td)
        LOG.puts "  Variant result: #{ variant_result ? 'OK' : 'ERR' }"
        variant_result
    }.include?(true)

    LOG.puts "Final result: #{ result ? 'OK ' : 'ERR' }"
    if ENV['OS']=~/Windows.*/ then
       print "#{ result ? 'OK ' : 'ERR' }  "
    else
       print "\033[1;#{ result ? '32mOK ' : '31mERR' }\033[0m  "
    end
    result_all = result_all && result
end

$sl_stdin.close
$sl_stdout.close
$sl_stderr.close

puts

LOG.close

exit 1 unless result_all
