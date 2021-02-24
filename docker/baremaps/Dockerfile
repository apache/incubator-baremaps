FROM adoptopenjdk/openjdk11:alpine

# Install baremaps
RUN wget https://github.com/baremaps/baremaps/releases/latest/download/baremaps.zip
RUN unzip baremaps.zip
RUN rm baremaps.zip
ENV PATH="/baremaps/bin/:${PATH}"

WORKDIR /home