<html>
<head>
  <!-- mapbox -->
  <link href='https://api.mapbox.com/mapbox-gl-js/v0.44.0/mapbox-gl.css' rel='stylesheet'/>
  <script src='https://api.mapbox.com/mapbox-gl-js/v0.44.0/mapbox-gl.js'></script>

  <!-- openstreetmap -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.4.3/css/ol.css"
        type="text/css">
  <script src="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.4.3/build/ol.js"></script>

  <style>

    body {
      font-family: sans-serif;
    }

    #baremaps {
      position: fixed;
      top: 0;
      bottom: 0;
      left: 0;
      right: 50%;
    }

    #openstreetmap {
      position: fixed;
      top: 0;
      bottom: 0;
      left: 50%;
      right: 0;
    }

    #link {
      display: block;
      position: fixed;
      top: 10px;
      right: 10px;
      border: 1px solid #ddd;;
      border-radius: 4px;
      background-color: #fff;
      box-shadow: 0px 0px 0px 2px rgba(0, 0, 0, 0.1);
      padding: 8px;
      color: black;
      text-decoration: none;
    }

  </style>
  <title>Baremaps</title>
</head>
<body style="margin: 0">
<div id="baremaps"></div>
<div id="openstreetmap"></div>
<button id="link">OpenStreetMap</button>
<script>

  let lon = ${center.lon?string["0.######"]};
  let lat = ${center.lat?string["0.######"]};
  let zoom = ${center.zoom?string["0.####"]};

  // Initialize the baremaps
  let baremaps = new mapboxgl.Map({
    container: 'baremaps',
    style: '/style.json',
    center: [lon, lat],
    zoom: zoom,
    minZoom: ${bounds.minZoom},
    maxZoom: ${bounds.maxZoom}
  });

  baremaps.addControl(new mapboxgl.NavigationControl());

  // Reload the webpage when this connection gets closed by the server
  let source = new EventSource("/changes/");
  source.onmessage = function (event) {
    console.log(event);
    baremaps.setStyle('/style.json');
  };

  let openstreetmap = new ol.Map({
    target: 'openstreetmap',
    layers: [
      new ol.layer.Tile({
        source: new ol.source.OSM()
      })
    ],
    view: new ol.View({
      center: ol.proj.fromLonLat([lon, lat]),
      zoom: zoom + 1
    }),
    interactions: ol.interaction.defaults({
      doubleClickZoom: false,
      dragAndDrop: false,
      dragPan: false,
      keyboardPan: false,
      keyboardZoom: false,
      mouseWheelZoom: false,
      pointer: false,
      select: false
    }),
    controls: ol.control.defaults({
      attribution: false,
      zoom: false,
    }),
  });

  // Recenter the map according to the location saved in the url
  if (location.hash) {
    let hash = location.hash.substr(1).split("/");
    let zoom = parseFloat(hash[0]);
    let lon = parseFloat(hash[2]);
    let lat = parseFloat(hash[1]);
    baremaps.setZoom(zoom);
    baremaps.setCenter([lon, lat]);
    openstreetmap.getView().setZoom(parseFloat(zoom) + 1);
    openstreetmap.getView().setCenter(ol.proj.fromLonLat([lon, lat]));
  }

  // Changes the hash of the url when the location changes
  baremaps.on('moveend', ev => {
    let zoom = baremaps.getZoom().toFixed(4);
    let lon = baremaps.getCenter().lng.toFixed(6);
    let lat = baremaps.getCenter().lat.toFixed(6);
    location.hash = "#" + zoom + "/" + lat + "/" + lon;
    openstreetmap.getView().setZoom(parseFloat(zoom) + 1);
    openstreetmap.getView().setCenter(ol.proj.fromLonLat([lon, lat]));
  });

  // View on OpenStreetMap
  document.getElementById("link").addEventListener('click', (event) => {
    event.stopPropagation();
    let hash = location.hash.substr(1).split("/");
    let zoom = parseFloat(hash[0]) + 1;
    let lon = parseFloat(hash[2]);
    let lat = parseFloat(hash[1]);
    let url = 'https://www.openstreetmap.org/#map=' + zoom + '/' + lat + '/' + lon;
    console.log(url);
    window.open(url);
  });

</script>
</body>
</html>