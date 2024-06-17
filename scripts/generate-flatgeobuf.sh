#!/bin/bash

flatc --java --gen-all \
  --java-package-prefix org.apache.baremaps.flatgeobuf.generated \
  -o baremaps-flatgeobuf/src/main/java \
  baremaps-flatgeobuf/src/main/resources/fbs/*.fbs
