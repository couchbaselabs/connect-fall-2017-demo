# Couchbase Connect Fall 2017 Demo

Background, stage script, and application code for Couchbase Connect SF Fall 2017 demo talk.

## Instructions

### On the machine that will host the web server

Install node.  Note the server requires version 7 or higher.  I recommend using nvm to manage Node versions if you have an existing installation.  (The nvm installation guide can be found [here](https://github.com/creationix/nvm/blob/master/README.md#install-script)

Clone the repo

```
git clone https://github.com/couchbaselabs/connect-fall-2017-demo.git
```

#### Configuring the server

Update the cluster hosts in `app.js` to point to your cluster nodes.

#### Running the server

Build requires a package to compile the native part of the Couchbase Node client.  Install if needed.

```
npm install -g node-gyp
```

You may also need to install the native compilation tools (e.g. g++).  On Redhat

```
yum group install "Development Tools"
```

In the directory demo/web/server install Node packages.

```
npm install
```

Run the server.

```
node ./bin/www
```

Or, for simple crash resilience, run a script.

```
#! /usr/bin/env bash

while true
do
  node ./bin/www
done
```

To prevent some systems from killing the process when you log out, run with nohup, like this.

```
nohup ./server.sh < /dev/null >& server.log &
```

#### Configuring the client

Under `src/config` in the client code, update `serverURI` in the `index.js` file to point to your web server.

#### Building the client

Shift to the directory demo/web/client.  Install the Node packages.

```
npm install
```

Decide if you want to run the production version or development version.  The development version supports hot
reloading, but currently requires running a separate development server.

To use the development server:

```
npm run dev
```

You should find the pages served up on localhost:8080.

To run a production version:

```
npm run build
```

You should find the pages served on localhost:3000

### Couchbase Server

#### Data

The file `augment-data.json` contains records hand written to work with the demo.  To add these to a bucket, use (e.g.)

```
cbimport json -u admin -p password -b health -c couchbase://127.0.0.1:8091 -d file://augment-data.json -g '%id%' -f lines
```

On Macs look for cbimport in `/Applications/Couchbase\ Server.app/Contents/Resources/couchbase-core/bin/`

#### Whitelisting sites for curl

The curl functionality in N1QL requires sites to be white/black listed.  For this application, whitelist the Google
geocoding endpoint by creating

`/opt/couchbase/var/lib/couchbase/n1qlcerts/curl_whitelist.json`

with contents

```
{
  "all_access":false,
  "allowed_urls":["https://maps.googleapis.com/maps/api/geocode/json"]
}
```

#### Indexes

Several queries examine resourceType and id:

```
CREATE INDEX `resource-idx` ON `health`(`resourceType`,`id`);
```

Mapping hospitals queries on location resources and specific type codes:

```
CREATE INDEX `location-idx` ON `health`(type.coding[0].code) WHERE resourceType = 'Location';
```

Monitoring incoming observations from our select patient:

```
CREATE INDEX `observation-idx` ON `health`(subject.reference, issued,  valueQuantity.`value`);
```

Full text search:

The full text search index definition can be found in `demo/models/fts-index.json`.  Load it with something like this.

```
curl -T fts-index.json http://admin:password@localhost:8094/api/index/diagnosis
```

### Installing Couchnode from GitHub

To install from the latest

```
npm install --save git+https://git@github.com/brett19/couchnode.git
```

To pin the installation to a specific commit

```
npm install --save git+https://git@github.com/brett19/couchnode.git#dba79ef33b1f4e7d5e88906538624c65caf3d841
```