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

const geonamesUrl =
    "https://download.geonames.org/export/dump/allCountries.zip";

// Iterate over nic urls to create a list of downloads and ungzip
const fetchAndUnzipNic = nics.map((nic,index) =>
    ({id: `fetch-nic-${index}`, needs: [], tasks: [
        {type: "DownloadUrl", url: nic.url, path: `downloads/${nic.filename}.gz`},
        {type: "UngzipFile", file: `downloads/${nic.filename}.gz`, directory: "archives"}
    ]}));

// Fetch and unzip Geonames
const FetchAndUnzipGeonames = {id: "fetch-geonames-allcountries", needs: [], tasks: [
    {type: "DownloadUrl", url: geonamesUrl, path: "downloads/geonames-allcountries.zip", force: true},
    {type: "UnzipFile", file: "downloads/geonames-allcountries.zip", directory: "archives"}
]};

// Create the Geocoder index
const createGeonamesIndex = {id: "geocoder-index", needs: [FetchAndUnzipGeonames.id], tasks: [
    {type: "CreateGeonamesIndex", geonamesDumpPath: "archives/allCountries.txt", targetGeonamesIndexPath: "geocoder-index"}
]};

// Create the iploc database
const createIplocIndex = {id: "iploc-index", needs: fetchAndUnzipNic.map(e => e.id).concat([createGeonamesIndex.id]), tasks: [
    {type: "CreateIplocIndex", geonamesIndexPath: "geocoder-index", iplocNicPath: fetchAndUnzipNic.map(nicStep => nicStep.tasks[1].file.replaceAll(".db.gz", ".txt")), targetIplocIndexPath: "iploc.db"}
]};

export default {"steps": fetchAndUnzipNic.concat([FetchAndUnzipGeonames, createGeonamesIndex, createIplocIndex])};