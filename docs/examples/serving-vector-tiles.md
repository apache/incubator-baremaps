# Serving Vector Tiles

In this tutorial, we’ll learn how to serve precomputed vector tiles using Python and Nginx.

In production, vector tiles are rarely served dynamically. Why is that so? First, a large blob store is much cheaper than a relational database to operate. Second, content delivery networks (CDNs) greatly improve web performances by caching static content close to the end user. Baremaps has been conceived with these lasting trends in mind. The following command produces a local directory containing precomputed static tiles. 

```
baremaps export \
--database 'jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps' \
--tileset 'tiles.json' \
--repository 'tiles/'
```

These tiles can be served with Apache, Nginx, or Python, but also copied in a blob store behind a content delivery network, such as Cloudflare, Stackpath, or Fastly.

## Serve with Python

Mapbox vector tiles should be served with the following headers.

```
content-encoding: gzip
content-type: application/vnd.mapbox-vector-tile
```
Sample python code to server vector tiles with python built-in webserver

```
#server.py

try:
    from http import server # Python 3
except ImportError:
    import SimpleHTTPServer as server # Python 2

class MyHTTPRequestHandler(server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_my_headers()

        server.SimpleHTTPRequestHandler.end_headers(self)

    def send_my_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("content-encoding", "gzip")
        self.send_header("content-type", "application/vnd.mapbox-vector-tile")


if __name__ == '__main__':
    server.test(HandlerClass=MyHTTPRequestHandler, port=9001)
```
Run the following command in a terminal, where you saved the static tiles directory;

`
$ python server.py
`

and your tiles should be serving;

`
Serving HTTP on 0.0.0.0 port 9001 (http://0.0.0.0:9001/) ...
`

Now you can access the tiles through a url like this, depending upon your directory structure;

http://localhost:9001/tiles/{z}/{x}/{y}.mvt

OR

http://yourip:9001/tiles/{z}/{x}/{y}.mvt

## Serve with Nginx

I asume that you already have nginx installed on your system. By default it is not configured to serve vector tiles and to do that you need to edit the nginx configuration file. Open a terminal and run the following command;

```
# vi /etc/nginx/sites-enabled/default  [On Debian/Ubuntu]
# vi /etc/nginx/nginx.conf             [On CentOS/RHEL]
```

Default server configuration

```
server {
        listen 80 default_server;
        listen [::]:80 default_server;

        root /var/www/html;

        # Add index.php to the list if you are using PHP
        index index.html index.htm index.nginx-debian.html;

        server_name _;

        location / {
            # First attempt to serve request as file, then
            # as directory, then fall back to displaying a 404.
            try_files $uri $uri/ =404;
          }
       }
```

Add the following lines to the configuration file;

```
location ~* \.mvt$ {
    include proxy_params;
    add_header Access-Control-Allow-Origin *;
    add_header Content-Encoding gzip;
    add_header Content-Type application/vnd.mapbox-vector-tile;
  }
```

Your final nginx configuration should look like this;

```
server {
        listen 80 default_server;
        listen [::]:80 default_server;

        root /var/www/html;

        # Add index.php to the list if you are using PHP
        index index.html index.htm index.nginx-debian.html;

        server_name _;

        location / {
            # First attempt to serve request as file, then
            # as directory, then fall back to displaying a 404.
            try_files $uri $uri/ =404;
        }
        
        # Add support for serving pbf files
        location ~* \.mvt$ {
            include proxy_params;
            add_header Access-Control-Allow-Origin *;
            add_header Content-Encoding gzip;
            add_header Content-Type application/vnd.mapbox-vector-tile;
        }
        }
```

Copy the tiles folder into nginx root directory i.e. `/var/www/html`

and you should be now able to access the tiles with a url like this;

http://localhost:80/tiles/{z}/{x}/{y}.mvt

OR

http://yourip:80/tiles/{z}/{x}/{y}.mvt


