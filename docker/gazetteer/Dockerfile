FROM openjdk:8-jre-alpine

COPY gazetteer-cli/target/gazetteer-cli-1.0-SNAPSHOT.zip .
RUN unzip gazetteer-cli-1.0-SNAPSHOT.zip
RUN rm gazetteer-cli-1.0-SNAPSHOT.zip
WORKDIR /data
ENV PATH="/gazetteer-cli-1.0-SNAPSHOT/bin/:${PATH}"
