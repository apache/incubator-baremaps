<html>
<head>
  <link href='https://api.mapbox.com/mapbox-gl-js/v0.44.0/mapbox-gl.css' rel='stylesheet'/>
  <script src='https://api.mapbox.com/mapbox-gl-js/v0.44.0/mapbox-gl.js'></script>
  <style>

    #map {
      width: 100%;
      height: 100%;
    }

  </style>
  <title>Baremaps</title>
</head>
<body style="margin: 0">
<div id="map"></div>
<script>

  // Initialize the map
  let map = new mapboxgl.Map({
    container: 'map',
    style: '/style.json',
    center: [${center.lon?string["0.######"]}, ${center.lat?string["0.######"]}],
    zoom: ${center.zoom?string["0.####"]},
    minZoom: ${bounds.minZoom},
    maxZoom: ${bounds.maxZoom}
  });

  map.addControl(new mapboxgl.NavigationControl());

  // Reload the webpage when this connection gets closed by the server
  let source = new EventSource("/changes/");
  source.onmessage = function(event) {
    console.log(event);
    map.setStyle('/style.json');
  };

  // Recenter the map according to the location saved in the url
  if (location.hash) {
    let arr = location.hash.substr(1).split("/");
    let zoom = parseFloat(arr[0]);
    let lon = parseFloat(arr[2]);
    let lat = parseFloat(arr[1]);
    let bearing = parseFloat(arr[3]);
    let pitch = parseFloat(arr[4]);
    map.setZoom(zoom);
    map.setCenter([lon, lat]);
    map.setBearing(bearing);
    map.setPitch(pitch);
  }

  // Changes the hash of the url when the location changes
  map.on('moveend', ev => {
    location.hash = "#"
        + map.getZoom().toFixed(4) + "/"
        + map.getCenter().lat.toFixed(6) + "/"
        + map.getCenter().lng.toFixed(6) + "/"
        + map.getBearing().toFixed(6) + "/"
        + map.getPitch().toFixed(6);
  });

</script>
</body>
</html>