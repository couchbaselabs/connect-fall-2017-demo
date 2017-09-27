var express = require('express');
var router = express.Router();

module.exports = function(couched) {
  let route = router.post('/', async function(req, res, next) {
    if (!req.body || !req.body.criteria) {
      res.satus(400).send('Empty query');
      return;
    }

    let SearchQuery = couched.SearchQuery;
    let query = SearchQuery.new('encounters', SearchQuery.term(req.body.criteria)).fields('*');

    couched.bucket.query(query, function(err, hits, meta) {
      let data = { hits: [] };

      if (err) {
        res.json(data);
        return;
      }

      data.hits = hits;
  
      res.json(data);      
    });
  });

  return route;
}
