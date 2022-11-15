---
layout: default
title: About
description: An open source toolkit for creating vector tiles from OpenStreetMap and other data sources.
permalink: /about/
---

# About

Baremaps is an [open source](https://github.com/baremaps/baremaps/blob/master/LICENSE) toolkit for creating vector tiles from [OpenStreetMap](https://www.openstreetmap.org) and other data sources.

[![State of the map](/assets/screenshot.jpg)](/assets/demo.html)

It is inspired by [Osmosis](https://github.com/openstreetmap/osmosis), but it comes with additional features, such as the ability to:
-   Process data in parallel with the [Stream API](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html) introduced in Java 8
-   Import data faster with the [COPY API](https://www.postgresql.org/docs/11/sql-copy.html) of Postgresql
-   Create postgis geometries on the fly with [JTS](https://github.com/locationtech/jts)
-   Create and serve customized [Vector Tiles](https://docs.mapbox.com/vector-tiles/specification/)

On the longer run, the aim of the project is to work with a variety of data sources in order to create highly specialized and customized maps.


