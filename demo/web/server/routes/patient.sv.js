var express = require('express');
var router = express.Router();

module.exports = function(couched) {
  let route = router.get('/:id', async function(req, res, next) {
    let query = req.app.locals.couchbase.N1qlQuery.fromString(
      "SELECT * FROM health WHERE META().id = 'patient::" + req.params.id + "';"
    );
    
    couched.bucket.query(query, function(err, rows) {
      if (err) {
        console.log(err);
        res.status(500).send({ error: err });
        return;
      }

      res.json(rows[0]);      
    });
  });

  return route;
}
