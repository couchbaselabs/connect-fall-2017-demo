const N1qlQuery = require('../utils/n1ql');
const express = require('express');
const router = express.Router();

let sse = {};

router.get('/:id', async function(req, res, next) {
  sse[req.params.id] = res.sse;

  // Send latest readings.  New readings will be pushed.
  let couchbase = req.app.locals.couchbase;
  let bucket = req.app.locals.bucket;
  let queryPromise = Promise.promisify(bucket.query, { context: bucket });
  let n1ql = `SELECT valueQuantity.\`value\`, issued as recordedAt FROM ${bucket._name} WHERE subject.reference = 'urn:uuid:${req.params.id}' ORDER BY recordedAt DESC LIMIT 12;`;

  let query = couchbase.N1qlQuery.fromString(n1ql);
  
  await queryPromise(query)
  .then(rows => {
    if (rows.length < 1) return;

    res.sse('event: update\ndata: { "values": ');
    res.sse(JSON.stringify(rows.reverse()));      
    res.sse(' }\n\n');
  })
  .catch(err => {
    console.log(err);
  })
});

router.post('/:id', function(req, res, next) {
  res.send('');

  if (sse[req.params.id] === undefined) return; // Not listening yet

  sse[req.params.id](`event: update\ndata: { "values": [${JSON.stringify(req.body)}]}\n\n`);
});

module.exports = router;