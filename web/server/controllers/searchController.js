var express = require('express');
var randomHexColor = require('random-hex-color');
var router = express.Router();

// Based on Couchbase colors (TM)
const palette = [ '#E72731', '#0074e0', '#f0ce0f', '#b26cda', '#00b6bd', '#00a1db', '#eb242a', '#fd9d0d' ];
const searchKeyAllGenders = 'All Genders';
const searchKeyAllCities = 'All Cities';

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

  let criteria = req.body.criteria.trim() + "~2";
  let baseQuery = SearchQuery.queryString(criteria);
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
                .fields('*').limit(20);

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

function searchAllGenders(param) {
  return param === searchKeyAllGenders;
}

function searchAllCities(param) {
  return param === searchKeyAllCities;
}

exports.analyticsByAge = async function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let cluster = req.app.locals.cluster;
  let CbasQuery = couchbase.CbasQuery;
  let query = `SELECT year_month, age_group, count(p.id) as patient_count
                 FROM condition c, patient p
                 WHERE substring_after(c.subject.reference, "uuid:") /*+ indexnl */ = meta(p).id
                 AND c.code.text = '${req.query.diagnosis}'
                 AND date(c.assertedDate) > date('2007-10-01') `;
  
  if (!searchAllGenders(req.query.gender)) {
    query += `AND p.gender = '${req.query.gender.toLowerCase()}' `;
  }

  if (!searchAllCities(req.query.city)) {
    query += `AND p.address[0].city = '${req.query.city}' `;
  }

  query += `GROUP BY
              substring(c.assertedDate, 0, 7) as year_month, 
              to_bigint((get_year(current_date()) - get_year(date(p.birthDate))) / 30) as age_group
            ORDER BY year_month`

  query = CbasQuery.fromString(query);

  cluster.query(query, (error, result) => {
    if (error) {
      return res.status(500).send({ code: error.code, message: error.message });
    }

    let groups = [0, 1, 2, 3];    
    let stats = {};
    let datasets = [];
    let labels = [];

    groups.forEach(group => stats[group] = {});

    for (const record of result) {
      if (!stats[record.age_group]) stats[record.age_group] = {};

      if (!labels.includes(record.year_month)) {
        labels.push(record.year_month);
        groups.forEach(group => stats[group][record.year_month] = 0);
      }

      stats[record.age_group][record.year_month] += record.patient_count;      
    }
    
    let knife = 0;

    for (const key in stats) {
      if (stats.hasOwnProperty(key)) {
        var entries = [];

        for (let nn = 0; nn < labels.length; ++nn) {
          entries.push(stats[key][labels[nn]]);
        }

        datasets.push({
            data: entries,
            label: `${30*key} - ${30*key + 29}`,
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

exports.analyticsByAgeDetails = async function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let cluster = req.app.locals.cluster;
  let CbasQuery = couchbase.CbasQuery;
  let query = `SELECT p.id AS p_id, p.name AS p_name, p.address AS p_address, GET_YEAR(DATE(c.assertedDate)) - GET_YEAR(DATE(p.birthDate)) AS c_age,
               p.gender AS p_gender, GET_YEAR(current_date()) - GET_YEAR(DATE(p.birthDate)) AS p_age
               FROM condition c, patient p
               WHERE substring_after(c.subject.reference, "uuid:") /*+ indexnl */ = meta(p).id
               AND c.code.text = '${req.query.diagnosis}' `;

  if (!searchAllGenders(req.query.gender)) {
    query += `AND p.gender = '${req.query.gender.toLowerCase()}' `;
  }

  if (!searchAllCities(req.query.city)) {
    query += `AND p.address[0].city = '${req.query.city}' `;
  }

  query +=       `and substring(c.assertedDate, 0, 7) = '${req.query.year_month}'
                  and to_bigint((GET_YEAR(CURRENT_DATE()) - GET_YEAR(DATE(p.birthDate))) / 30) = ${req.query.age_group}
                  LIMIT 20;`;

  query = CbasQuery.fromString(query);

  cluster.query(query, (error, result) => {
    if (error) {
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
                 FROM condition c, patient p
                 WHERE substring_after(c.subject.reference, "uuid:") /*+ indexnl */ = meta(p).id
                 AND c.code.text = '${req.query.diagnosis}'
                 AND date(c.assertedDate) > date('2007-10-01') `;
  
  if (!searchAllGenders(req.query.gender)) {
    query += `AND p.gender = '${req.query.gender.toLowerCase()}' `;
  }

  if (!searchAllCities(req.query.city)) {
    query += `AND p.address[0].city = '${req.query.city}' `;
  }

  query += `GROUP BY
              substring(c.assertedDate, 0, 7) as year_month,
              p.telecom[0].facebook is not unknown as Facebook,
              p.telecom[0].whatsapp is not unknown as WhatsApp,
              p.telecom[0].snapchat is not unknown as Snapchat
            ORDER BY year_month;`

  query = CbasQuery.fromString(query);

  cluster.query(query, (error, result) => {
    if (error) {
      return res.status(500).send({ code: error.code, message: error.message });
    }

    let stats = { 'None': {}, 'Facebook': {}, 'WhatsApp': {}, 'Snapchat': {}};
    let display = { 'None': true }
    let datasets = [];
    let labels = [];

    for (const record of result) {
      let found = false;

      if (!labels.includes(record.year_month)) {
        labels.push(record.year_month);
        
        for (const key in stats) {
          stats[key][record.year_month] = 0;
        }
      }

      if (1 > record.patient_count) continue;

      for (const key in stats) {
        if (record[key]) {
          stats[key][record.year_month] += record.patient_count;

          display[key] = true;

          found = true;
        }
      }

      if (!found) stats['None'][record.year_month] += record.patient_count;
    }
    
    let knife = 0;

    for (const key in display) {
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

exports.analyticsSocialDetails = async function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let cluster = req.app.locals.cluster;
  let CbasQuery = couchbase.CbasQuery;
  let query = `SELECT p.id AS p_id, p.name AS p_name, p.address AS p_address, GET_YEAR(DATE(c.assertedDate)) - GET_YEAR(DATE(p.birthDate)) AS c_age,
               p.gender AS p_gender, GET_YEAR(CURRENT_DATE()) - GET_YEAR(DATE(p.birthDate)) AS p_age
               FROM condition c, patient p
               WHERE substring_after(c.subject.reference, "uuid:") /*+ indexnl */ = meta(p).id
               AND c.code.text = '${req.query.diagnosis}' `;

  if (!searchAllGenders(req.query.gender)) {
    query += `AND p.gender = '${req.query.gender.toLowerCase()}' `;
  }

  if (!searchAllCities(req.query.city)) {
    query += `AND p.address[0].city = '${req.query.city}' `;
  }

  if ('none' === req.query.media) {
    query += 'AND p.telecom[0].facebook is unknown AND p.telecom[0].whatsapp is unknown AND p.telecom[0].snapchat is unknown ';
  } else {
    query += `AND p.telecom[0].${req.query.media} is not unknown `;
  }

  query +=   `AND SUBSTRING(c.assertedDate, 0, 7) = '${req.query.year_month}'
                LIMIT 20;`;

  query = CbasQuery.fromString(query);

  cluster.query(query, (error, result) => {
    if (error) {
      return res.status(500).send({ code: error.code, message: error.message });
    }
    
    res.send(result);
  });
}
