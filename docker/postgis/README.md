# Postgis docker image

A docker image based on for running postgresql with the postgis and hstore extensions.

```bash
docker build -t baremaps/postgis:latest .
docker push baremaps/postgis:latest
```

## Custom Configuration

```
docker run \
--name postgis \
--publish 5432:5432 \
-e POSTGRES_DB=baremaps \
-e POSTGRES_USER=baremaps \
-e POSTGRES_PASSWORD=baremaps \
-v $(pwd)/postgresql.conf:/etc/postgresql.conf \
-d baremaps/postgis:latest \
postgres -c config_file=/etc/postgresql.conf
```

