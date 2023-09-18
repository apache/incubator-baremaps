/**
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to you under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **/
export default {
    "host": "http://demo.baremaps.com:8888",
    "database": {
        "jdbcUrl": "jdbc:postgresql://localhost:5432/daylight?&user=daylight&password=daylight",
        "maximumPoolSize": 2,
    },
    "osmPbfUrl": "https://download.geofabrik.de/europe/switzerland-latest.osm.pbf",
    "center": [6.6323, 46.5197],
    "bounds": [6.02260949059, 45.7769477403, 10.4427014502, 47.8308275417],
    "zoom": 14,
}
