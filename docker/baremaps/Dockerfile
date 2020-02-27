FROM openjdk:8-jre-alpine

COPY baremaps-cli/target/baremaps-cli-0.1-SNAPSHOT.zip .
RUN unzip baremaps-cli-0.1-SNAPSHOT.zip
RUN rm baremaps-cli-0.1-SNAPSHOT.zip
WORKDIR /data
ENV PATH="/baremaps-cli-0.1-SNAPSHOT/bin/:${PATH}"