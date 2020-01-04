# Gazetteer

[![CircleCI](https://circleci.com/gh/gazetteerio/gazetteer.svg?style=svg)](https://circleci.com/gh/gazetteerio/gazetteer)
[![Codacy](https://api.codacy.com/project/badge/Grade/9bb5efb0bea54a868cc70b0d9e564767)](https://app.codacy.com/app/bchapuis/gazetteer?utm_source=github.com&utm_medium=referral&utm_content=bchapuis/gazetteer&utm_campaign=Badge_Grade_Dashboard)
[![codecov](https://codecov.io/gh/gazetteerio/gazetteer/branch/master/graph/badge.svg)](https://codecov.io/gh/gazetteerio/gazetteer)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fgazetteerio%2Fgazetteer.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fgazetteerio%2Fgazetteer?ref=badge_shield)

Gazetteer is an open source pipeline for producing Mapbox vector tiles from [OpenStreetMap](https://www.openstreetmap.org) with Postgis and Java.

It is inspired by [Osmosis](https://github.com/openstreetmap/osmosis), but it comes with additional features, such as the ability to:
-   Process data in parallel with the [Stream API](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html) of Java
-   Import data faster with the [COPY API](https://www.postgresql.org/docs/11/sql-copy.html) of Postgresql
-   Create postgis geometries on the fly with [JTS](https://github.com/locationtech/jts)
-   Create and serve customized [Mapbox Vector Tiles](https://docs.mapbox.com/vector-tiles/specification/)

On the longer run, the aim of the project is to work with a variety of data sources in order to create highly specialized and customized maps.

## State of the map

[![State of the map](https://github.com/gazetteerio/gazetteer/raw/master/screenshots/2019-12-27.png)](https://www.gazetteer.io/)

## Prerequisites

-   Docker 18
-   Java 8
-   Maven 3

## Quick Start

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
The following commands will pull the docker image, create and start the container:

```bash
gazetteer postgis pull
gazetteer postgis create
gazetteer postgis start
```

As a small country, Liechtenstein is suitable for testing and fits in this git repository. 
You can now import this data in that postgis container using the following command.

```bash
gazetteer import \
  'data/liechtenstein-latest.osm.pbf' \
  'jdbc:postgresql://localhost:5432/gazetteer?allowMultiQueries=true&user=gazetteer&password=gazetteer'
```

To preview this data, you can simply run the embed web server with the following command:

```bash
gazetteer serve \
  'config/config.yaml' \
  'jdbc:postgresql://localhost:5432/gazetteer?allowMultiQueries=true&user=gazetteer&password=gazetteer' \
  --port 9000
```

Well done, the test server should have started and a map of liechtenstein should appear in your browser ([http://localhost:9000/](http://localhost:8082/))!

Vector tiles are rarely served dynamically in production. The following command produces a directory that contains precomputed tiles that can be deployed on a CDN:

```bash
gazetteer tiles \
  'config/config.yaml' \
  'jdbc:postgresql://localhost:5432/gazetteer?allowMultiQueries=true&user=gazetteer&password=gazetteer' \
  'tiles/' \
  --minZoom 14 \
  --maxZoom 14
```

## Limitations

Gazetteer is a work in progress and is not production ready, i.e., it comes with a lot of limitations. 
Additional work is needed to: 
-   Configure the map and its style at all zoom levels
-   Improve the creation of geometries for OSM relations with JTS
-   Apply OSM diffs on existing postgresql databases
-   Optimize the SQL queries used to create Mapbox Vector Tiles
-   Add additional datasets
-   Stabilize and document the codebase


## Contributing

Being a side project, gazetteer does not have clear contribution guidelines yet.
As the development work happens on github, feel free to report an issue, suggest a feature, or make a pull request.
Generally speaking, as a contributor, you should:
-   be nice, inclusive and constructive when interacting with others;
-   agree with the terms of the Apache Software License;
-   try to follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html);
-   try to be concise and relevant in [commit messages](https://chris.beams.io/posts/git-commit/);
-   agree to rewrite portions of your code to make it fit better into the upstream sources.
