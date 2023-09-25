#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to you under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

DIR="$(dirname "$0")"
cd "$DIR" || exit

echo ""
echo "--------------------------------------------------------------------"
echo "Build the project"
echo "--------------------------------------------------------------------"
echo ""

cd ..
./mvnw spotless:apply clean install -DskipTests
rm -fr baremaps
tar -xvf ./baremaps-cli/target/apache-baremaps-0.7.2-SNAPSHOT-incubating-bin.tar.gz -C target/
mv ./target/apache-baremaps-0.7.2-SNAPSHOT-incubating-bin ./baremaps
export PATH=$PATH:`pwd`/baremaps/bin

echo ""
echo "--------------------------------------------------------------------"
echo "Display the path of the baremaps executable"
echo "--------------------------------------------------------------------"
echo ""

which baremaps

echo ""
echo "--------------------------------------------------------------------"
echo "Display the version of the baremaps executable"
echo "--------------------------------------------------------------------"
echo ""

baremaps --version
