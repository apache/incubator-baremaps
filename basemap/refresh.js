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
            "id": "openstreetmap-member",
            "needs": [],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "layers/member/refresh.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "openstreetmap-point",
            "needs": ["openstreetmap-member"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "layers/point/refresh.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "openstreetmap-linestring",
            "needs": ["openstreetmap-member"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "layers/linestring/refresh.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "openstreetmap-polygon",
            "needs": ["openstreetmap-member"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "layers/polygon/refresh.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "openstreetmap-highway",
            "needs": ["openstreetmap-linestring"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "layers/highway/refresh.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "openstreetmap-railway",
            "needs": ["openstreetmap-linestring"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "layers/railway/refresh.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "openstreetmap-route",
            "needs": ["openstreetmap-linestring"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "layers/route/refresh.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "openstreetmap-waterway",
            "needs": ["openstreetmap-linestring"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "layers/waterway/refresh.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "openstreetmap-natural",
            "needs": ["openstreetmap-polygon"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "layers/natural/refresh.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "openstreetmap-landuse",
            "needs": ["openstreetmap-polygon"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "layers/landuse/refresh.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "openstreetmap-leisure",
            "needs": ["openstreetmap-polygon"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "layers/leisure/refresh.sql",
                    "database": config.database,
                },
            ]
        },
    ]
}
