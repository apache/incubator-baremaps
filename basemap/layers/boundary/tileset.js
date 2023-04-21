/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
export default {
    id: 'boundary',
    queries: [
        {
            minzoom: 1,
            maxzoom: 6,
            sql:
                "SELECT fid as id, jsonb_build_object('boundary', 'administrative', 'admin_level', '0') as tags, geom FROM globaladm0_z$zoom",
        },
        {
            minzoom: 6,
            maxzoom: 14,
            sql:
                "SELECT fid as id, jsonb_build_object('boundary', 'administrative', 'admin_level', '1') as tags, geom FROM globaladm1_z$zoom",
        },
        {
            minzoom: 14,
            maxzoom: 20,
            sql:
                "SELECT id, tags, geom FROM osm_ways WHERE tags ? 'boundary'",
        },
    ],
}
