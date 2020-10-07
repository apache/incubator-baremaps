# Baremaps Docker Image

This image enables to play with Baremaps without having to install java locally.

```bash
docker build -t baremaps/baremaps .
```

This image is published on docker hub with the fol:

```bash
docker push baremaps/baremaps:latest
```

It can then be run as follow:

```bash
docker run --rm --net host --volume "$(pwd)":/data baremaps/baremaps \
  baremaps import \
  'examples/openstreetmap/liechtenstein-latest.osm.pbf' \
  'jdbc:postgresql://localhost:5432/baremaps?allowMultiQueries=true&user=baremaps&password=baremaps'
```

Here, the --volume parameter mounts the current directory in the docker image, enabling to process data stored on the host. Similarly, the --net parameter is used to share the host’s networking namespace with the container.