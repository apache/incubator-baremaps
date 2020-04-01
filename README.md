# Baremaps

![Java CI](https://github.com/baremaps/baremaps/workflows/Java%20CI/badge.svg)

[![Total alerts](https://img.shields.io/lgtm/alerts/g/baremaps/baremaps.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/baremaps/baremaps/alerts/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/baremaps/baremaps.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/baremaps/baremaps/context:java)
[![Codacy](https://api.codacy.com/project/badge/Grade/9bb5efb0bea54a868cc70b0d9e564767)](https://app.codacy.com/app/bchapuis/baremaps?utm_source=github.com&utm_medium=referral&utm_content=bchapuis/baremaps&utm_campaign=Badge_Grade_Dashboard)
[![codecov](https://codecov.io/gh/baremaps/baremaps/branch/master/graph/badge.svg)](https://codecov.io/gh/baremaps/baremaps)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fbaremaps%2Fbaremaps.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fbaremaps%2Fbaremaps?ref=badge_shield)

Baremaps is a vector tile server and pipeline built with Postgis and Java for producing and serving Mapbox vector tiles from [OpenStreetMap](https://www.openstreetmap.org) and other data sources.

It is inspired by [Osmosis](https://github.com/openstreetmap/osmosis), but it comes with additional features, such as the ability to:
-   Process data in parallel with the [Stream API](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html) of Java
-   Import data faster with the [COPY API](https://www.postgresql.org/docs/11/sql-copy.html) of Postgresql
-   Create postgis geometries on the fly with [JTS](https://github.com/locationtech/jts)
-   Create and serve customized [Mapbox Vector Tiles](https://docs.mapbox.com/vector-tiles/specification/)

On the longer run, the aim of the project is to work with a variety of data sources in order to create highly specialized and customized maps.

## State of the map

[![State of the map](https://github.com/baremaps/baremaps/raw/master/screenshots/2019-12-27.png)](https://www.baremaps.com/)

## Quick Start

In order to run Baremaps, you first need to install [Java 8](https://sdkman.io/).

Download and unzip the latest distribution. Add the `/bin` folder to your `PATH` variable:

```bash
wget https://github.com/baremaps/baremaps/releases/latest/download/baremaps.zip
unzip baremaps.zip
export PATH=$PATH:`pwd`/baremaps/bin
```

Calling the `baremaps` command should now result in an output similar to the following:

```bash
Usage: baremaps [COMMAND]
Commands:
  import
  update
  export
  serve
```

In order to run Baremaps, you need to setup a postgis database.
The following docker image will allow you to jump start this installation:

```bash
docker run \
  --name baremaps-postgis \
  --publish 5432:5432 \
  -e POSTGRES_DB=baremaps \
  -e POSTGRES_USER=baremaps \
  -e POSTGRES_PASSWORD=baremaps \
  -d baremaps/postgis:latest
```

You can then start and stop the container with the following commands:

```bash
docker start baremaps-postgis
docker stop baremaps-postgis
```

## Examples

Several examples illustrate how to import datasets in postgis and produce vector tiles.
-   The [OpenStreetMap](examples/openstreetmap/README.md) example shows how to produce high resolution vector tiles.
-   The [NaturalEarth](examples/naturalearth/README.md) example shows how to produce low resolution vector tiles.
-   The [Contour](examples/contour/README.md) example shows how to produce contours from a digital elevation model (DEM).

## Contributing

Contributions are welcome and encouraged. Please,checkout our [code of conduct](CODE_OF_CONDUCT.md) and [contributing guidelines](CONTRIBUTING.md).
