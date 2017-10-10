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

  var now = new Date();
  var yesterday = new Date(now.getTime() - (24 * 60 * 60 * 1000));
  var last7 = new Date(yesterday.getTime() - (6 * 24 * 60 * 60 * 1000));
  var last30 = new Date(last7.getTime() - (23 * 24 * 60 * 60 * 1000));
  var last60 = new Date(last30.getTime() - (30 * 24 * 60 * 60 * 1000));
  var last90 = new Date(last60.getTime() - (30 * 24 * 60 * 60 * 1000));

  let baseQuery = SearchQuery.queryString(req.body.criteria)
  let conj = [baseQuery];
  if (req.body.filterDiagnosis) {
    conj.push(SearchQuery.term(req.body.filterDiagnosis).field('code.text'))
  }
  if (req.body.filterOnset) {
    if (req.body.filterOnset == 'Last 24 Hours') {
      conj.push(SearchQuery.dateRange()
      .start(ISODateString(yesterday))
      .field('onsetDateTime'))
    } else if (req.body.filterOnset == '1-7 days') {
      conj.push(SearchQuery.dateRange()
      .start(ISODateString(last7))
      .end(ISODateString(yesterday))
      .field('onsetDateTime'))
    } else if (req.body.filterOnset == '8-30 days') {
      conj.push(SearchQuery.dateRange()
      .start(ISODateString(last30))
      .end(ISODateString(last7))
      .field('onsetDateTime'))
    } else if (req.body.filterOnset == '30+ days') {
      conj.push(SearchQuery.dateRange()
      .end(ISODateString(last30))
      .field('onsetDateTime'))
    }
  }

  let query = SearchQuery.new('diagnosis', SearchQuery.conjuncts(conj))
                .highlight(SearchQuery.HighlightStyle.HTML, "note.text")
                .fields('*');

  query.addFacet('diagnosis', SearchFacet.term('code.text', 5));
  query.addFacet('onset', SearchFacet.date('onsetDateTime', 3)
    .addRange('Last 24 Hours', ISODateString(yesterday), null)
    .addRange('1-7 days', ISODateString(last7), ISODateString(yesterday))
    .addRange('8-30 days', ISODateString(last30), ISODateString(last7))
    .addRange('30+ days', null, ISODateString(last30))
  );

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

function ISODateString(d){
 function pad(n){return n<10 ? '0'+n : n;}
 return d.getUTCFullYear()+'-'
      + pad(d.getUTCMonth()+1)+'-'
      + pad(d.getUTCDate())+'T'
      + pad(d.getUTCHours())+':'
      + pad(d.getUTCMinutes())+':'
      + pad(d.getUTCSeconds())+'Z'}

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
