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

exports.analytics = async function(req, res, next) {
    let couchbase = req.app.locals.couchbase;
    let cluster = req.app.locals.cluster;
    let CbasQuery = couchbase.CbasQuery;
    var statement = "SELECT year_month, count(p.id) AS patient_count " +
                    "FROM patient p, encounter AS e, condition AS c " +
                    "WHERE p.id = substring_after(e.subject.reference, 'uuid:') " +
                    "AND e.id = substring_after(c.context.reference, 'uuid:') " +
                    "AND GET_DATE_FROM_DATETIME(DATETIME(e.period.`start`)) > DATE('2007-10-01') " +
                    "AND p.gender = 'male' " +
                    "AND c.code.text = 'Mongoitis' " +
                    "AND GET_YEAR(DATETIME(e.period.`start`)) - GET_YEAR(DATE(p.birthDate)) BETWEEN 20 AND 80 " +
                    "GROUP BY SUBSTRING(e.period.`start`, 1, 7) AS year_month";
    var query = CbasQuery.fromString(statement);
    cluster.query(query, (error, result) => {
        if(error) {
            return res.status(500).send({ code: error.code, message: error.message });
        }
        res.send(result);
    });
}
