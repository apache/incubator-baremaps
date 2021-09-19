# Baremaps

![Java CI](https://github.com/baremaps/baremaps/workflows/Java%20CI/badge.svg)

![example workflow](https://github.com/baremaps/baremaps/actions/workflows/build.yml/badge.svg)
![example workflow](https://github.com/baremaps/baremaps/actions/workflows/release.yml/badge.svg)
![example workflow](https://github.com/baremaps/baremaps/actions/workflows/analyze.yml/badge.svg)

[![Total alerts](https://img.shields.io/lgtm/alerts/g/baremaps/baremaps.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/baremaps/baremaps/alerts/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/baremaps/baremaps.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/baremaps/baremaps/context:java)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=baremaps_baremaps&metric=alert_status)](https://sonarcloud.io/dashboard?id=baremaps_baremaps)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=baremaps_baremaps&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=baremaps_baremaps)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=baremaps_baremaps&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=baremaps_baremaps)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=baremaps_baremaps&metric=security_rating)](https://sonarcloud.io/dashboard?id=baremaps_baremaps)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=baremaps_baremaps&metric=coverage)](https://sonarcloud.io/dashboard?id=baremaps_baremaps)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=baremaps_baremaps&metric=ncloc)](https://sonarcloud.io/dashboard?id=baremaps_baremaps)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=baremaps_baremaps&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=baremaps_baremaps)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=baremaps_baremaps&metric=bugs)](https://sonarcloud.io/dashboard?id=baremaps_baremaps)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=baremaps_baremaps&metric=code_smells)](https://sonarcloud.io/dashboard?id=baremaps_baremaps)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=baremaps_baremaps&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=baremaps_baremaps)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=baremaps_baremaps&metric=sqale_index)](https://sonarcloud.io/dashboard?id=baremaps_baremaps)

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

## Start Hacking

* To contribute to this repo see [CONTRIBUTING](CONTRIBUTING.md) and [CODE_OF_CONDUCT](CODE_OF_CONDUCT.md).
* To build baremaps. You'll need `maven` and `Java 11`. 
* `cd baremaps-cli && mvn package -P maputnik -B`. This creates a `.zip` in the `target` folder.
* alternatively you can build a docker image with `mvn jib:dockerBuild -DskipTests`.

To help you starts without worring to much a "starter" [openstreetmap](https://github.com/baremaps/openstreetmap-vecto) project is avaiblable


