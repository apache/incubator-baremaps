# Baremaps

![Java CI](https://github.com/baremaps/baremaps/workflows/Java%20CI/badge.svg)

[![Total alerts](https://img.shields.io/lgtm/alerts/g/baremaps/baremaps.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/baremaps/baremaps/alerts/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/baremaps/baremaps.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/baremaps/baremaps/context:java)
[![codecov](https://codecov.io/gh/baremaps/baremaps/branch/master/graph/badge.svg)](https://codecov.io/gh/baremaps/baremaps)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fbaremaps%2Fbaremaps.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fbaremaps%2Fbaremaps?ref=badge_shield)

[Baremaps](https://www.baremaps.com/) is a toolkit for creating custom vector tiles from [OpenStreetMap](https://www.openstreetmap.org) and other data sources with Postgis and Java. The project is licensed under [Apache License 2.0](LICENSE).

[![State of the map](/docs/assets/screenshot.jpg)](https://www.baremaps.com/assets/demo.html)

Baremaps is inspired by [Osmosis](https://github.com/openstreetmap/osmosis), but it comes with additional features, such as the ability to:
-   Process data in parallel with the [Stream API](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html) introduced in Java 8
-   Import data faster with the [COPY API](https://www.postgresql.org/docs/11/sql-copy.html) of Postgresql
-   Create postgis geometries on the fly with [JTS](https://github.com/locationtech/jts)
-   Create and serve customized [Mapbox Vector Tiles](https://docs.mapbox.com/vector-tiles/specification/)

On the longer run, the aim of the project is to work with a variety of data sources in order to create highly specialized and customized maps.
