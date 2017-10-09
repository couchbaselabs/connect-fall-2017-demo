var express = require('express');
var router = express.Router();

exports.encounters = async function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let bucket = req.app.locals.bucket;  
  let SearchQuery = couchbase.SearchQuery;
  let SearchFacet = couchbase.SearchFacet;
  
  if (!req.body || !req.body.criteria) {
    res.status(400).send('Empty query');
    return;
  }
  
  let query = SearchQuery.new('encounters', SearchQuery.queryString(req.body.criteria))
                .highlight(SearchQuery.HighlightStyle.HTML, "reason.text")
                .fields('*');
  
  query.addFacet('symptoms', SearchFacet.term('reason.text', 5));
  
  bucket.query(query, function(err, hits, meta) {
    console.dir(err);
    console.dir(hits);
    console.dir(meta);
    let data = { hits: [] };
    
    if (err) {
      res.json(data);
      return;
    }
    
    data.hits = hits;
    
    res.json(data);      
  });
}

exports.diagnosis = async function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let bucket = req.app.locals.bucket;  
  let SearchQuery = couchbase.SearchQuery;
  let SearchFacet = couchbase.SearchFacet;
  
  if (!req.body || !req.body.criteria) {
    res.status(400).send('Empty query');
    return;
  }
  
  let query = SearchQuery.new('diagnosis', SearchQuery.queryString(req.body.criteria))
                .highlight(SearchQuery.HighlightStyle.HTML, "note.text")
                .fields('*');
  
  query.addFacet('diagnosis', SearchFacet.term('code.text', 5));
  
  bucket.query(query, function(err, hits, meta) {
    let data = { hits: [], meta: {} };
    
    if (err) {
      res.json(data);
      return;
    }
    
    data.hits = hits;
    data.meta = meta;
    
    res.json(data);      
  });
}
