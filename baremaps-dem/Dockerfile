FROM eclipse-temurin:21-alpine

# Install gdal-tools using apk
RUN apk add --no-cache gdal-tools java-gdal

# Set the working directory
WORKDIR /app

ENTRYPOINT ["/__cacert_entrypoint.sh"]

CMD ["jshell"]

