---
layout: default
title: Installation
permalink: /installation/
---

# Installation

In order to run Baremaps, you first need to install Java 8 or a later version. 
[SDKMAN](https://sdkman.io/) provides a convenient Command Line Interface (CLI) to install and upgrade Java.

To install baremaps, download and unzip the latest [release](https://github.com/baremaps/baremaps/releases/latest). 
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
  --name baremaps \
  --publish 5432:5432 \
  -e POSTGRES_DB=baremaps \
  -e POSTGRES_USER=baremaps \
  -e POSTGRES_PASSWORD=baremaps \
  -d postgis/postgis:latest
```

You can then stop and start the container with the following commands:

```
docker stop baremaps
docker start baremaps
```

From there, the [OpenStreetMap](/examples/openstreetmap/) example is a good introduction to Baremaps, it shows how to produce high resolution vector tiles.
