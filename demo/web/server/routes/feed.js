// I require sleep
const sleep = require('../utils/sleep');
const N1qlQuery = require('../utils/n1ql');
const express = require('express');
const router = express.Router();

router.get('/:id', async function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let bucket = req.app.locals.bucket;
  let queryPromise = Promise.promisify(bucket.query, { context: bucket });
  let n1ql = "SELECT valueQuantity.`value`, issued as recordedAt FROM " + bucket._name + " WHERE subject.reference = '" + req.params.id + "' ORDER BY recordedAt;";
  let query = couchbase.N1qlQuery.fromString(n1ql);
  
  while (true) {
    await queryPromise(query, function(err, rows) {
      if (err) {
        console.log(err);
        res.status(500).send({ error: err });
        return;
      }
      
      if (rows.length < 1) return;
      
      res.sse('event: update\n');
      res.sse('data: { "values": [');
      res.sse(JSON.stringify(rows[rows.length - 1].value));      
      res.sse('] }\n\n');
    })
    .catch(err => {
      console.log(err);
    })
    .finally(() => sleep(2000));      
  }
});

module.exports = router;