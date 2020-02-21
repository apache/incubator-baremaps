# Postgis docker image

This docker image is aimed at getting started with postgis and gazetteer.

```bash
docker build -t gazetteerio/postgis:2.4 .
docker push gazetteerio/postgis:2.4
```

```bash
docker run \
  --name gazetteer-postgis24 \
  --publish 5432:5432 \
  -e POSTGRES_DB=gazetteer \
  -e POSTGRES_USER=gazetteer \
  -e POSTGRES_PASSWORD=gazetteer \
  -d gazetteerio/postgis:2.4
```
