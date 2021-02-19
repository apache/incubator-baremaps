<html>
<head>
  <link href='https://api.mapbox.com/mapbox-gl-js/v0.44.0/mapbox-gl.css' rel='stylesheet'/>
  <script src='https://api.mapbox.com/mapbox-gl-js/v0.44.0/mapbox-gl.js'></script>
  <style>

    body {
      font-family: Helvetica, sans-serif;
      margin: 0;
    }

    #map1 {
      position: fixed;
      width: 50%;
      height: 100%;
    }

    #map2 {
      position: fixed;
      left: 50%;
      width: 50%;
      height: 100%;
    }

    #link {
      position: fixed;
      top: 10px;
      right: 10px;
      background-color: white;
      padding: 10px;
      border: solid 2px rgba(0, 0, 0, .2);
      border-radius: 6px;
      text-decoration: none;
      color: black;
    }

    h1 {
      margin: 0;
      padding: 0;
    }

  </style>
  <title>Baremaps</title>
</head>
<body>
<div id="map1"></div>
<div id="map2"></div>
<a id="link" href="" target="_blank">OpenStreetMap</a>
<script>

  function sync(map1, map2) {
    let zoom = map1.getZoom();
    let center = map1.getCenter();
    let hash = zoom.toFixed(4) + "/"
        + center.lat.toFixed(6) + "/"
        + center.lng.toFixed(6);
    let link = document.getElementById("link");
    link.href = "https://www.openstreetmap.org/#map=" + hash;
    window.location.hash = hash;
    map2.setZoom(map1.getZoom());
    map2.setCenter(map1.getCenter());
    map2.setBearing(map1.getBearing());
    map2.setPitch(map1.getPitch());
  }

  let map1 = new mapboxgl.Map({
    container: 'map1',
    style: '/style.json',
    center: [${center.lon?string["0.######"]}, ${center.lat?string["0.######"]}],
    zoom: ${center.zoom?string["0.####"]},
    minZoom: ${bounds.minZoom},
    maxZoom: ${bounds.maxZoom + 4}
  });

  let map2 = new mapboxgl.Map({
    container: 'map2',
    interactive: false,
    style: {
      'version': 8,
      'sources': {
        'openstreetmap': {
          'type': 'raster',
          'tiles': [
            'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
          ],
          'tileSize': 256
        }
      },
      'layers': [
        {
          'id': 'openstreetmap',
          'type': 'raster',
          'source': 'openstreetmap',
          'minzoom': 0,
          'maxzoom': 22
        }
      ]
    },
  });

  if (location.hash) {
    let arr = location.hash.substr(1).split("/");
    zoom = parseFloat(arr[0]);
    center = [parseFloat(arr[2]), parseFloat(arr[1])];
    map1.setZoom(zoom);
    map1.setCenter(center);
  }

  sync(map1, map2);

  map1.on('moveend', () => {
    sync(map1, map2);
  });

  map1.addControl(new mapboxgl.NavigationControl());

</script>
</body>
</html>