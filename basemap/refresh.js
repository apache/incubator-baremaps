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
import config from "./config.js";

export default {
    "steps": [
        {
            "id": "refresh",
            "needs": [],
            "tasks": [
                "layers/node/refresh.sql",
                "layers/way/refresh.sql",
                "layers/relation/refresh.sql",
                "layers/member/refresh.sql",
                "layers/highway/refresh.sql",
                "layers/landuse/refresh.sql",
                "layers/leisure/refresh.sql",
                "layers/natural/refresh.sql",
                // "layers/ocean/refresh.sql",
                "layers/point/refresh.sql",
                "layers/railway/refresh.sql",
                "layers/route/refresh.sql",
                "layers/waterway/refresh.sql",
            ].map(file => {
                return {
                    "type": "ExecuteSql",
                    "file": file,
                    "database": config.database,
                }
            })
        },
    ]
}
