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

## Prerequisites

- Docker 18
- Java 8
- Maven 3

## Installation

Clone and build the repository:

```bash
git clone git@github.com:gazetteerio/gazetteer.git
cd gazetteer
mvn clean install
```

Unzip the binary distribution and add the `/bin` folder to your `PATH` variable:


```bash
unzip gazetteer-cli/target/gazetteer-cli-1.0-SNAPSHOT.zip
export PATH=$PATH:/path/to/gazetteer/bin
```

Calling the `gazetteer` command should now result in an output similar to the following:

```bash
Usage: <main class> [COMMAND]
Commands:
  osm
  tiles
  postgis
  serve
```

The `gazetteer` command comes with shorthands to manage a postgis docker container. 
The following commands will pull the image create and start the container:

```bash
gazetteer postgis pull
gazetteer postgis create
gazetteer postgis start
```

As a small country Liechtenstein is suitable for testing and easily fits in a git repository. 
You can now import this data in that postgis container using the following command.

```bash
gazetteer osm import \
  'data/liechtenstein.osm.pbf' \
  'jdbc:postgresql://localhost:5432/gazetteer?allowMultiQueries=true&user=gazetteer&password=gazetteer'
```

To preview this data, you can simply run the embed web server with the following command:

```bash
gazetteer serve \
  'config/config.yaml' \
  'jdbc:postgresql://localhost:5432/gazetteer?allowMultiQueries=true&user=gazetteer&password=gazetteer'
```

Well done, open your [browser](http://localhost:8081/), a map of liechtenstein should appear!

## Importing contours with GDAL

```
gdal_contour -f PostgreSQL -i 10 -a elevation -nln dem_contours liechtenstein-srtm-finished-1arcsec.tif "PG:host=localhost user=gazetteer password=gazetteer dbname=gazetteer"
```