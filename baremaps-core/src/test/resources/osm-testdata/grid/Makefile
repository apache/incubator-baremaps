
# You need osmium tool from https://github.com/osmcode/osmium-tool for this
OSMIUM := ../../osmium-tool/osmium

SQL_FILES := grid.sql data-nodes.sql data-ways.sql data-multipolygons.sql data-labels.sql

.PHONY: grid data all clean show create-wkt

all: grid data

grid: grid.db

grid.sql: data/? bin/create-grid.sh
	bin/create-grid.sh `find data -mindepth 1 -maxdepth 1 -type d | cut -d/ -f2 | cut -d- -f1 | sort` >$@

data-nodes.sql: data/*/*/nodes.wkt
	cat $^ | sed -re "s/^([0-9][0-9][0-9])([0-9][0-9][0-9]) (.*)/INSERT INTO nodes (test_id, id, geom) VALUES (\1, \1\2, PointFromText('\3', 4326));/" >$@

data-ways.sql: data/*/*/ways.wkt
	cat $^ | sed -re "s/^([0-9][0-9][0-9])([0-9][0-9][0-9]) (.*)/INSERT INTO ways (test_id, id, geom) VALUES (\1, \1\2, LineFromText('\3', 4326));/" >$@

data-multipolygons.sql: data/tests.json
	bin/create-sql-for-multipolygons.rb $< >$@

data-labels.sql: data/*/*/labels.wkt
	cat $^ | sed -re "s/^(POINT\([0-9. ]+\)) (.*)/INSERT INTO labels (label, geom) VALUES ('\2', PointFromText('\1', 4326));/" >$@

grid.db: setup.sql $(SQL_FILES)
	rm -f $@
	cat $^ | spatialite -batch -bail $@

data: data/all.osm data/tests.json

data-files.xml: data/*/*/data.osm
	echo '<?xml version="1.0" encoding="utf-8"?><files>' >$@
	find data -name data.osm | sort | sed -e 's/^/<file name="/' -e 's/$$/"\/>/' >>$@
	echo "</files>" >>$@

data/tests.json: data/*/*/test.json
	../bin/concat-test-json.rb $^ >$@

data/all.osm: data/*/*/data.osm data-files.xml
	xsltproc --output $@ bin/concat.xsl data-files.xml

show:
	bin/show-tests.sh

create-wkt:
	bin/create-wkt.sh

clean:
	rm -f grid.db $(SQL_FILES) data-files.xml data/all.osm data/tests.json tests.qgs~

