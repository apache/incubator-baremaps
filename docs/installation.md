---
layout: default
title: Installation
permalink: /installation/
---

# Installation

In order to run Baremaps, you first need to install Java 11 or a later version (only avoid the latest, otherwise the data importations will certainly not succeed).
[SDKMAN](https://sdkman.io/) provides a convenient Command Line Interface (CLI) to install and upgrade Java.

To choose which version of Java you want to install, you can use this command:

```
sdk list java
```

Or if you prefer to opt for Java 11:

```
sdk install java 11.0.2-open
```

Before the next step, you will need [Homebrew](https://brew.sh/) (macOS and Linux), and then wget ([alternative for Windows](https://www.gnu.org/software/wget/)):

```
brew install wget
```


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

Baremaps will not be active when you switch on your computer unless you add it in a permanent location. You can find help about that for example [here](https://wpbeaches.com/how-to-add-to-the-shell-path-in-macos-using-terminal/) ("Adding a permanent location").

Before go further, you must now install [Docker](https://docs.docker.com/get-docker/).

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

Notice that the Baremaps container which is inside Docker must be running when you want to edit maps.

From there, the [OpenStreetMap](/examples/openstreetmap/) example is a good introduction to Baremaps, it shows how to produce high resolution vector tiles.
