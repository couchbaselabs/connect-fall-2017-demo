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


