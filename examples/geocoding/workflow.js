const geonamesUrl = "https://download.geonames.org/export/dump/allCountries.zip";

// Fetch and unzip Geonames
const FetchAndUnzipGeonames = {id: "fetch-geonames-allcountries", needs: [], tasks: [
    {type: "DownloadUrl", url: geonamesUrl, path: "downloads/geonames-allcountries.zip", force: true},
    {type: "UnzipFile", file: "downloads/geonames-allcountries.zip", directory: "archives"}
]};

// Create the Geocoder index
const createGeonamesIndex = {id: "geocoder-index", needs: [FetchAndUnzipGeonames.id], tasks: [
    {type: "CreateGeonamesIndex", geonamesDumpPath: "archives/allCountries.txt", targetGeonamesIndexPath: "geocoder-index"}
]};

export default {"steps": [FetchAndUnzipGeonames, createGeonamesIndex]};