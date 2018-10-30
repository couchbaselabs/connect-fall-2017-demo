// I require sleep
const sleep = require('../utils/sleep');
const express = require('express');
const router = express.Router();

router.get('/:id', async function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let bucket = req.app.locals.bucket;
  let queryPromise = Promise.promisify(bucket.query, { context: bucket });
  let n1ql = `SELECT valueQuantity.\`value\`, issued as recordedAt FROM ${bucket._name} WHERE subject.reference = 'urn:uuid:${req.params.id}' ORDER BY recordedAt DESC LIMIT 12;`;

  let query = couchbase.N1qlQuery.fromString(n1ql);
  
  while (true) {
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
    .finally(() => sleep(2000));      
  }
});

module.exports = router;