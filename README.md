# Couchbase Connect Fall 2017 Demo

Background, stage script, and application code for Couchbase Connect SF Fall 2017 demo talk.

## Instructions

### On the machine that will host the web server

Install node.  Note the server requires version 7 or higher.  I recommend using nvm to manage Node versions if you have an existing installation.

Clone the repo

`git clone https://github.com/couchbaselabs/connect-fall-2017-demo.git`

#### Running the server

Build requires a package to compile the native part of the Couchbase Node client.  Install if needed.

`npm install -g node-gyp`

You may also need to install the native compilation tools (e.g. g++).  On Redhat

`yum group install "Development Tools"`

In the directory demo/web/server install Node packages.

`npm install`

Run the server.

`node ./bin/www`

#### Building the client

Shift to the directory demo/web/client.  Install the Node packages.

`npm install`

Decide if you want to run the production version or development version.  The development version supports hot
reloading, but currently requires running a separate development server.

To use the development server:

`npm run dev`

You should find the pages served up on localhost:8080.

To run a production version:

`npm run build`

You should find the pages served on localhost:3000

### Couchbase Server

### Data

The file `augment-data.json` contains records hand written to work with the demo.  To add these to a bucket, use (e.g.)

`cbimport json -u admin -p password -b health -c couchbase://127.0.0.1:8091 -d file://augment-data.json -g '%id%' -f lines`

On Macs look for cbimport in `/Applications/Couchbase\ Server.app/Contents/Resources/couchbase-core/bin/`

### Indexes

Several queries examine resourceType and id:

`CREATE INDEX `resource-idx` ON `health`(`resourceType`,`id`);`

Mapping hospitals queries on location resources and specific type codes:

`CREATE INDEX `location-idx` ON `health`(type.coding[0].code) WHERE  resourceType = “Location”;`

Monitoring incoming observations from our select patient:

`CREATE INDEX `observation-idx` ON `health`(subject.reference, issued,  valueQuantity.`value`);`

### Installing Couchnode from GitHub

To install from the latest

`npm install --save git+https://git@github.com/brett19/couchnode.git`

To pin the installation to a specific commit

`npm install --save git+https://git@github.com/brett19/couchnode.git#dba79ef33b1f4e7d5e88906538624c65caf3d841`