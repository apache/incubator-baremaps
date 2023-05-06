/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
const nics = [
    {url: "https://ftp.afrinic.net/pub/dbase/afrinic.db.gz", filename: "afrinic.db"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.as-block.gz", filename: "apnic.db.as-block.db"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.as-set.gz", filename: "apnic.db.as-set.db"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.domain.gz", filename: "apnic.db.domain"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.filter-set.gz", filename: "apnic.db.filter-set"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.inet-rtr.gz", filename: "apnic.db.inet-rtr"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.inet6num.gz", filename: "apnic.db.inet6num"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.inetnum.gz", filename: "apnic.db.inetnum"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.irt.gz", filename: "apnic.db.irt"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.key-cert.gz", filename: "apnic.db.key-cert"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.limerick.gz", filename: "apnic.db.limerick"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.mntner.gz", filename: "apnic.db.mntner"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.organisation.gz", filename: "apnic.db.organisation"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.peering-set.gz", filename: "apnic.db.peering-set"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.role.gz", filename: "apnic.db.role"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.route-set.gz", filename: "apnic.db.route-set"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.route.gz", filename: "apnic.db.route"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.route6.gz", filename: "apnic.db.route6"},
    {url: "https://ftp.apnic.net/apnic/whois/apnic.db.rtr-set.gz", filename: "apnic.db.rtr-set"},
    {url: "https://ftp.arin.net/pub/rr/arin.db.gz", filename: "arin.db"},
    {url: "https://ftp.lacnic.net/lacnic/dbase/lacnic.db.gz", filename: "lacnic.db"},
    {url: "https://ftp.ripe.net/ripe/dbase/ripe.db.gz", filename: "ripe.db"},
];

// Iterate over nic urls to create a list of downloads and ungzip
const fetchAndUnzipNic = nics.map((nic, index) => ({
    id: `fetch-nic-${nic.filename.replace(".", "-")}`,
    needs: [],
    tasks: [
        {
            type: "DownloadUrl",
            url: nic.url,
            path: `downloads/${nic.filename}.gz`
        },
        {
            type: "UngzipFile",
            file: `downloads/${nic.filename}.gz`,
            directory: "archives"
        }
    ]
}));

// Fetch and unzip Geonames
const fetchAndUnzipGeonames = {
    id: "fetch-geonames-allcountries",
    needs: [],
    tasks: [
        {
            type: "DownloadUrl",
            url: "https://download.geonames.org/export/dump/allCountries.zip",
            path: "downloads/geonames-allcountries.zip",
            force: true
        },
        {
            type: "UnzipFile",
            file: "downloads/geonames-allcountries.zip",
            directory: "archives"
        }
    ]
};

// Create the Geocoder index
const createGeonamesIndex = {
    id: "geocoder-index",
    needs: [fetchAndUnzipGeonames.id],
    tasks: [
        {
            type: "CreateGeonamesIndex",
            dataFile: "archives/allCountries.txt",
            indexDirectory: "geocoder-index"
        }
    ]
};

// Create the iploc database
const createIplocIndex = {
    id: "iploc-index",
    needs: [createGeonamesIndex.id, ...fetchAndUnzipNic.map(e => e.id)],
    tasks: [
        {
            type: "CreateIplocIndex",
            geonamesIndexPath: "geocoder-index",
            nicPaths: nics.map(nic => `archives/${nic.filename}`),
            targetIplocIndexPath: "iploc.db"
        }
    ]
};

export default {"steps": [...fetchAndUnzipNic, fetchAndUnzipGeonames, createGeonamesIndex, createIplocIndex]};
