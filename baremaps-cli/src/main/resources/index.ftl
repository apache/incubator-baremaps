<html>
<head>
  <link href="https://fonts.googleapis.com/css2?family=Montserrat&family=Roboto&family=Roboto+Mono&display=swap" rel="stylesheet">
  <link href='https://api.mapbox.com/mapbox-gl-js/v0.44.0/mapbox-gl.css' rel='stylesheet'/>
  <script src='https://api.mapbox.com/mapbox-gl-js/v0.44.0/mapbox-gl.js'></script>
  <style>

    #map {
      width: 100%;
      height: 100%;
    }

    #map canvas {
      cursor: crosshair;
    }

    #heading {
      font-family: 'Montserrat', sans-serif;
      color: rgb(255, 255, 255);
      background: rgb(40, 40, 40);
      position: fixed;
      top: 0px;
      left: 0px;
      bottom: 0px;
      padding: 30px;
      width: 450px;
      overflow: auto;
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
<body style="margin: 0">
<div id="map"></div>
<div id="heading">
  <h1>Blueprint</h1>
  <p>
    The blueprint lets you explore and improve the tiles generated with your <a href="/config.yaml" download="config.yaml">configuration</a> file.
    In addition, it can be used to generate a mapbox <a href="/style.json" download="style.json">stylesheet</a>.
  </p>
  <pre id='features'>
Select a feature on the map
to display its metadata.
  </pre>
</div>
<script>

  // Initialize the map
  var map = new mapboxgl.Map({
    container: 'map',
    style: '/style.json',
    center: [${center.lon?string["0.######"]}, ${center.lat?string["0.######"]}],
    zoom: ${center.zoom?string["0.####"]},
    minZoom: ${bounds.minZoom},
    maxZoom: ${bounds.maxZoom}
  });

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

  map.on('click', function (e) {
    var features = map.queryRenderedFeatures(e.point);
    document.getElementById('features').innerHTML = JSON.stringify(features, null, 2);
  });

  // Reload the webpage when this connection gets closed by the server
  var source = new EventSource("/changes/");
  source.onmessage = function(event) {
    console.log(event);
    map.setStyle('/style.json');
  };

</script>
</body>
</html>