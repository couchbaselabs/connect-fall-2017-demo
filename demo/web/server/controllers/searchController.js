var express = require('express');
var randomHexColor = require('random-hex-color');
var router = express.Router();

// Based on Couchbase colors (TM)
let palette = [ '#0074e0', '#f0ce0f', '#b26cda', '#00b6bd', '#00a1db', '#eb242a', '#fd9d0d' ];

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
    /* select year_month, maritalStatus, count(p.id) as patient_count
from patient p, encounter as e, condition as c
where p.id = substring_after(e.subject.reference, "uuid:")
and   e.id = substring_after(c.context.reference, "uuid:")
and get_date_from_datetime(datetime(e.period.`start`)) > date('2007-10-01')
and p.gender = 'male'
and c.code.text = 'Diabetes'
and get_year(datetime(e.period.`start`)) - get_year(date(p.birthDate)) between 20 and 80
group by substring(e.period.`start`, 1, 7) as year_month, p.maritalStatus.text as maritalStatus */
    var statement = "SELECT year_month, maritalStatus, count(p.id) AS patient_count " +
                    "FROM patient p, encounter AS e, condition AS c " +
                    "WHERE p.id = substring_after(e.subject.reference, 'uuid:') " +
                    "AND e.id = substring_after(c.context.reference, 'uuid:') " +
                    "AND GET_DATE_FROM_DATETIME(DATETIME(e.period.`start`)) > DATE('2007-10-01') ";

    if(req.query.gender != "both") {
                    statement += "AND p.gender = lower('" + req.query.gender + "') ";
    }

    statement +=    "AND c.code.text = '" + req.query.code + "' " +
                    "AND GET_YEAR(DATETIME(e.period.`start`)) - GET_YEAR(DATE(p.birthDate)) BETWEEN " + req.query.min_age + " AND " + req.query.max_age + " " +
                    "GROUP BY SUBSTRING(e.period.`start`, 1, 7) AS year_month, p.maritalStatus.text as maritalStatus";
    var query = CbasQuery.fromString(statement);
    cluster.query(query, (error, result) => {
        if(error) {
            return res.status(500).send({ code: error.code, message: error.message });
        }
        var stats = {};
        var datasets = [];
        for(var i = 0; i < result.length; i++) {
            if(!stats[result[i].maritalStatus]) {
                stats[result[i].maritalStatus] = {};
            }
            stats[result[i].maritalStatus][result[i].year_month] = result[i].patient_count;
        }
        var labels = result.map(item => item.year_month);
        labels = labels.filter((item, index, inputArray) => {
            return inputArray.indexOf(item) == index;
        });
        labels.sort();
        for(var key in stats) {
            if(stats.hasOwnProperty(key)) {
                var l = [];
                var p = [];
                for(var j = 0; j < labels.length; j++) {
                    p.push(stats[key][labels[j]]);
                }
                var hexColor = randomHexColor();
                datasets.push({
                    data: p,
                    label: key,
                    fill: false,
                    backgroundColor: 'rgba(0, 0, 0, 0)',
                    borderColor: hexColor,
                    pointBackgroundColor: hexColor
                });
            }
        }
        res.send({
            labels: labels,
            datasets: datasets
        });
    });
}

exports.analyticsDetails = async function(req, res, next) {
    let couchbase = req.app.locals.couchbase;
    let cluster = req.app.locals.cluster;
    let CbasQuery = couchbase.CbasQuery;
    var statement = "SELECT p.id AS p_id, p.name AS p_name, GET_YEAR(DATETIME(e.period.`start`)) - GET_YEAR(DATE(p.birthDate)) AS p_age, p.address AS p_address, e.period.`start` AS e_date " +
                    "FROM patient p, encounter AS e, condition AS c " +
                    "WHERE p.id = substring_after(e.subject.reference, 'uuid:') " +
                    "AND e.id = substring_after(c.context.reference, 'uuid:') ";

    if (req.query.gender.toLowerCase() != "all") {
                    statement += `AND p.gender = '${req.query.gender.toLowerCase()}' `;
    }

    statement +=    "AND c.code.text = '" + req.query.diagnosis + "' " +
                    "AND GET_YEAR(DATETIME(e.period.`start`)) - GET_YEAR(DATE(p.birthDate)) BETWEEN " + req.query.min_age + " AND " + req.query.max_age + " " +
                    "AND SUBSTRING(e.period.`start`, 1, 7) = '" + req.query.year_month + "' " +
                    "LIMIT 20";

    var query = CbasQuery.fromString(statement);

    cluster.query(query, (error, result) => {
        if(error) {
            return res.status(500).send({ code: error.code, message: error.message });
        }
        res.send(result);
    });
}

exports.analyticsSocial = async function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let cluster = req.app.locals.cluster;
  let CbasQuery = couchbase.CbasQuery;
  
  let query = `SELECT year_month, Facebook, WhatsApp, Snapchat, count(p.id) as patient_count
                 FROM condition_archive c, patient_archive p
                 WHERE substring_after(c.subject.reference, "uuid:") = meta(p).id
                 AND c.code.text = '${req.query.diagnosis}'
                 AND date(c.assertedDate) > date('2007-01-01') `;
  
  if (req.query.gender != 'All') {
    query += `AND p.gender = '${req.query.gender.toLowerCase()}' `;
  }

  query += `AND lower(p.address[0].city) = lower('${req.query.city}')
            GROUP BY
              substring(c.assertedDate, 1, 7) as year_month,
              p.telecom[0].facebook is not unknown as Facebook,
              p.telecom[0].whatsapp is not unknown as WhatsApp,
              p.telecom[0].snapchat is not unknown as Snapchat;`

  query = CbasQuery.fromString(query);

  cluster.query(query, (error, result) => {
    if (error) {
      return res.status(500).send({ code: error.code, message: error.message });
    }

    let stats = { 'None': {}, 'Facebook': {}, 'Snapchat': {}, 'WhatsApp': {}};
    let datasets = [];
    let labels = [];

    for (const record of result) {
      let found = false;

      labels.push(record.year_month);

      for (const key in stats) {
        if (record[key]) {
          stats[key][record.year_month] = record.patient_count;

          found = true;
        }
      }

      if (!found) stats['None'][record.year_month] = record.patient_count;
    }
    
    labels = [...new Set(labels)].sort();

    let knife = 0;

    for (const key in stats) {
      if (stats.hasOwnProperty(key)) {
        var entries = [];

        for (let nn = 0; nn < labels.length; ++nn) {
          entries.push(stats[key][labels[nn]]);
        }

        datasets.push({
            data: entries,
            label: key,
            fill: false,
            backgroundColor: 'rgba(0, 0, 0, 0)',
            borderColor: palette[knife],
            pointBackgroundColor: palette[knife]
        });
      }

      knife = (knife + 1) % palette.length;
    }

    res.send({
        labels: labels,
        datasets: datasets
    });
  });
}