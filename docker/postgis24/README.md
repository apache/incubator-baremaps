# Postgis docker image

This docker image is aimed at getting started with postgis and baremaps.

```bash
docker build -t baremaps/postgis:2.4 .
docker push baremaps/postgis:2.4
```

```bash
docker run \
  --name baremaps-postgis24 \
  --publish 5432:5432 \
  -e POSTGRES_DB=baremaps \
  -e POSTGRES_USER=baremaps \
  -e POSTGRES_PASSWORD=baremaps \
  -d baremaps/postgis:2.4
```
