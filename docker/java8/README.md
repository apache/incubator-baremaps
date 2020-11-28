# Java 8 and maven 3.6 builds

This image enables reproducible builds with Java 8 and maven 3.6. 

It can be built from the current directory:

```bash
docker build -t java8-build-image .
```

It can then be used to build the project from the root directory:

```bash
docker run -it --net host -v ${PWD}:/maven java8-build-image
```
