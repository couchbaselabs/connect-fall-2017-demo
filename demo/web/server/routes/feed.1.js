var express = require('express');
var router = express.Router();

module.exports = function(couched) {
  let route = router.get('/:id', async function(req, res, next) {
    let query = couched.N1qlQuery.fromString(
      "SELECT valueQuantity.`value`, issued as recordedAt FROM health WHERE subject.reference = '" + req.params.id + "' ORDER BY recordedAt;"
    );
    
    while (true) {
      couched.bucket.query(query, function(err, rows) {
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
      });

      await sleep(2000);      
    }
  });
    
  function sleep (ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }
    
  return route;
}