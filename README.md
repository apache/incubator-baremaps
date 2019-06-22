# Gazetteer

[![CircleCI](https://circleci.com/gh/gazetteerio/gazetteer.svg?style=svg)](https://circleci.com/gh/gazetteerio/gazetteer)
[![Codacy](https://api.codacy.com/project/badge/Grade/9bb5efb0bea54a868cc70b0d9e564767)](https://app.codacy.com/app/bchapuis/gazetteer?utm_source=github.com&utm_medium=referral&utm_content=bchapuis/gazetteer&utm_campaign=Badge_Grade_Dashboard)
[![codecov](https://codecov.io/gh/gazetteerio/gazetteer/branch/master/graph/badge.svg)](https://codecov.io/gh/gazetteerio/gazetteer)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fgazetteerio%2Fgazetteer.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fgazetteerio%2Fgazetteer?ref=badge_shield)

Gazetteer aims at creating high quality and open source vector tiles for web mapping.
For now, the effort consists into creating a pipeline that imports data from OpenStreetMap into postgresql, which is then used to create vector tiles.
On the longer run, the project will include other data sources and be deployable as cloud native components.


## State of the map

![State of the map](https://github.com/gazetteerio/gazetteer/raw/master/screenshots/1550007544903.png)

## Installation

Install postgresql 11 and postgis 2.5:

```
sudo apt-get install postgresql-11-postgis-2.5
```

Connect to postgresql with the psql shell:

```
sudo -u postgres psql
```

From the psql shell, create the database, the username and the extensions:

```
createuser gazetteer -W
CREATE DATABASE gazetteer;
CREATE USER gazetteer WITH ENCRYPTED PASSWORD 'gazetteer'; 
GRANT ALL PRIVILEGES ON DATABASE gazetteer TO gazetteer;
\c gazetteer 
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS postgis;
\q
```

Clone and build the repository:

```
git clone git@github.com:gazetteerio/gazetteer.git
cd gazetteer
mvn clean install
```

Populate the database with the liechtenstein data:

```
mvn -pl gazetteer-osm exec:java \
  -Dexec.mainClass="io.gazetteer.osm.Importer" \
  -Dexec.args="gazetteer-benchmarks/src/main/resources/liechtenstein.osm.pbf jdbc:postgresql://localhost:5432/gazetteer?allowMultiQueries=true&user=gazetteer&password=gazetteer"
```

Start the tile server:

```
mvn -pl gazetteer-tileserver exec:java \
  -Dexec.mainClass="io.gazetteer.tileserver.TileServer" \
  -Dexec.args="config/config.yaml jdbc:postgresql://localhost:5432/gazetteer?user=gazetteer&password=gazetteer"
```

Well done, open your [browser](http://localhost:8081/), a map of liechtenstein should appear!


