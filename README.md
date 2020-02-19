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
export PATH=$PATH:`pwd`/gazetteer-cli-1.0-SNAPSHOT/bin
```

Calling the `gazetteer` command should now result in an output similar to the following:

```bash
Usage: gazetteer [COMMAND]
Commands:
  import
  update
  tiles
  serve
```

In order to run Gazetteer, you need to setup a postgis database.
The following docker image will allow you to jump start this installation:

```bash
docker run \
  --name gazetteer-postgis \
  --publish 5432:5432 \
  -e POSTGRES_DB=gazetteer \
  -e POSTGRES_USER=gazetteer \
  -e POSTGRES_PASSWORD=gazetteer \
  -d gazetteerio/postgis:1
```

You can then start and stop the container with the following commands:

```bash
docker start gazetteer-postgis
docker stop gazetteer-postgis
```

## Examples

Several examples illustrate how to import datasets in postgis and produce vector tiles.
-   The [OpenStreetMap](examples/openstreetmap/README.md) example shows how to produce high resolution vector tiles.
-   The [NaturalEarth](examples/naturalearth/README.md) example shows how to produce low resolution vector tiles.
-   The [Contour](examples/contour/README.md) example shows how to produce contours from a digital elevation model (DEM).

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
