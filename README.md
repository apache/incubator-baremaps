# Baremaps

![Java CI](https://github.com/baremaps/baremaps/workflows/Java%20CI/badge.svg)

[![Total alerts](https://img.shields.io/lgtm/alerts/g/baremaps/baremaps.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/baremaps/baremaps/alerts/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/baremaps/baremaps.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/baremaps/baremaps/context:java)
[![codecov](https://codecov.io/gh/baremaps/baremaps/branch/master/graph/badge.svg)](https://codecov.io/gh/baremaps/baremaps)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fbaremaps%2Fbaremaps.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fbaremaps%2Fbaremaps?ref=badge_shield)

Baremaps is a toolkit for creating Mapbox vector tiles from [OpenStreetMap](https://www.openstreetmap.org) and other data sources. The project is licensed under [Apache License 2.0](LICENSE) and supported by [Camptocamp](https://www.camptocamp.com/geospatial_solutions).

It is inspired by [Osmosis](https://github.com/openstreetmap/osmosis), but it comes with additional features, such as the ability to:
-   Process data in parallel with the [Stream API](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html) introduced in Java 8
-   Import data faster with the [COPY API](https://www.postgresql.org/docs/11/sql-copy.html) of Postgresql
-   Create postgis geometries on the fly with [JTS](https://github.com/locationtech/jts)
-   Create and serve customized [Mapbox Vector Tiles](https://docs.mapbox.com/vector-tiles/specification/)

On the longer run, the aim of the project is to work with a variety of data sources in order to create highly specialized and customized maps.

## Preview

[![State of the map](https://github.com/baremaps/openstreetmap-vecto/blob/master/screenshot.png)](https://github.com/baremaps/openstreetmap-vecto/)

## Installation

In order to run Baremaps, you first need to install Java 8 or a later version. 
[SDKMAN](https://sdkman.io/) provides a convenient Command Line Interface (CLI) to install and upgrade Java.

To install baremaps, download and unzip the latest [release](https://github.com/baremaps/baremaps/releases). 
Then, add the `/bin` folder to your `PATH` variable:

```
wget https://github.com/baremaps/baremaps/releases/latest/download/baremaps.zip
unzip baremaps.zip
export PATH=$PATH:`pwd`/baremaps/bin
```

Calling the `baremaps` command should now result in an output similar to the following:

```
Usage: baremaps [COMMAND]
A toolkit for producing vector tiles.
Commands:
  import  Import OpenStreetMap data in the Postgresql database.
  update  Update OpenStreetMap data in the Postgresql database.
  export  Export vector tiles from the Postgresql database.
  serve   Serve vector tiles from the the Postgresql database.
```

In order to run Baremaps, you need to setup a [postgis](https://postgis.net/) database.
The following docker image will allow you to jump start this installation:

```
docker run \
  --name baremaps-postgis \
  --publish 5432:5432 \
  -e POSTGRES_DB=baremaps \
  -e POSTGRES_USER=baremaps \
  -e POSTGRES_PASSWORD=baremaps \
  -d baremaps/postgis:latest
```

You can then stop and start the container with the following commands:

```
docker stop baremaps-postgis
docker start baremaps-postgis
```

## Quickstart

Start with the [OpenStreetMap](examples/openstreetmap/README.md) example, which introduces the Baremap toolkit and shows how to produce high resolution vector tiles.

Additional examples illustrate how to import other datasets in postgis to produce different kind of vector tiles.
-   The [NaturalEarth](examples/naturalearth/README.md) example shows how to produce low resolution vector tiles.
-   The [Contour](examples/contour/README.md) example shows how to produce contour lines from a digital elevation model (DEM).
-   The [OSMVecto](https://github.com/baremaps/openstreetmap-vecto/) repository shows how to build a more advanced map.
## Contributing

Contributions are welcome and encouraged. Please,checkout our [code of conduct](CODE_OF_CONDUCT.md) and [contributing guidelines](CONTRIBUTING.md).
