<html>
<head>
  <link href='https://api.mapbox.com/mapbox-gl-js/v0.44.0/mapbox-gl.css' rel='stylesheet'/>
  <script src='https://api.mapbox.com/mapbox-gl-js/v0.44.0/mapbox-gl.js'></script>
  <style>

    body {
      font-family: Helvetica, sans-serif;
      margin: 0;
    }

    #map {
      width: 100%;
      height: 100%;
    }

    h1 {
      font-family: 'Roboto', sans-serif;
      margin: 0;
      padding: 0;
    }

    pre {
      font-family: 'Roboto Mono', monospace;
    }

    a, a:hover, a:visited {
      color: rgb(229, 235, 247);
    }

  </style>
  <title>Baremaps</title>
</head>
<body>
<div id="map"></div>
<script>

  // Initialize the map
  let map = new mapboxgl.Map({
    container: 'map',
    style: '/style.json',
    center: [${center.lon?string["0.######"]}, ${center.lat?string["0.######"]}],
    zoom: ${center.zoom?string["0.####"]},
    minZoom: ${bounds.minZoom},
    maxZoom: ${bounds.maxZoom + 4}
  });

  // Assume that the tiles are hosted at the origin
  map.on('sourcedata', () => {
    map.getSource("baremaps").tiles[0] = window.location.origin + "/tiles/{z}/{x}/{y}.pbf";
  })

  // Recenter the map according to the location saved in the url
  if (location.hash) {
    let arr = location.hash.substr(1).split("/");
    let zoom = parseFloat(arr[0]);
    let lng = parseFloat(arr[1]);
    let lat = parseFloat(arr[2]);
    let bearing = parseFloat(arr[3]);
    let pitch = parseFloat(arr[4]);
    map.setZoom(zoom);
    map.setCenter([lng, lat]);
    map.setBearing(bearing);
    map.setPitch(pitch);
  }

  // Changes the hash of the url when the location changes
  map.on('moveend', ev => {
    location.hash = "#"
        + map.getZoom().toFixed(4) + "/"
        + map.getCenter().lng.toFixed(6) + "/"
        + map.getCenter().lat.toFixed(6) + "/"
        + map.getBearing().toFixed(6) + "/"
        + map.getPitch().toFixed(6);
  });

  map.addControl(new mapboxgl.NavigationControl());

</script>
</body>
</html>