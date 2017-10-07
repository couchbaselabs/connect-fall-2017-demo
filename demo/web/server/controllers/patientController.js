var N1qlQuery = require('../utils/n1ql');
var Promise = require('bluebird');

exports.patient = function(req, res, next) {
  let n1ql = `WHERE resourceType = 'Patient' AND id = '${req.params.id}';`;
  
  N1qlQuery(n1ql, req, res, next);
};

exports.cohort = function(req, res, next) {
  let n1ql = `patient WHERE resourceType = 'Patient' AND id IN ${JSON.stringify(req.body)};`;
  
  N1qlQuery(n1ql, req, res, next);
}

exports.encounters = function(req, res, next) {
  let n1ql = "encounter WHERE resourceType = 'Encounter' AND META().id LIKE 'encounter::" + req.params.id + "%';";
  
  N1qlQuery(n1ql, req, res, next);
}

exports.location = async function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let bucket = req.app.locals.bucket;
  let queryPromise = Promise.promisify(bucket.query, { context: bucket });

  let query = `SELECT address.text FROM ${bucket._name} WHERE resourceType = 'Patient' AND id = '${req.params.id}';`;
  
  query = couchbase.N1qlQuery.fromString(query);
  
  await queryPromise(query)
  .then(rows => {
    let address = encodeURIComponent(rows[0].text);
    
    let query = `SELECT RAW CURL("https://maps.googleapis.com/maps/api/geocode/json", {"data":["address=${address}", "request":"GET"} );`
    
    query = couchbase.N1qlQuery.fromString(query);
    
    return queryPromise(query);
  })
  .then(rows => {
    res.json(rows[0].results[0].geometry.location);    
  })
  .catch(err => {
    console.log(err);
    res.status(500).send({ error: err });
  });
}

exports.cohortLocations = async function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let bucket = req.app.locals.bucket;
  let queryPromise = Promise.promisify(bucket.query, { context: bucket });
      
  let query = `SELECT address.text FROM ${bucket._name} WHERE resourceType = 'Patient' AND id IN ${JSON.stringify(req.body)};`;
  
  query = couchbase.N1qlQuery.fromString(query);
  
  await queryPromise(query)
  .then(rows => {
    let matched = [];

    for (row of rows) {
      let address = encodeURIComponent(row.text);
      
      // WARNING: This will fail unless the endpoint is white listed for cURL.
      let query = `SELECT RAW CURL("https://maps.googleapis.com/maps/api/geocode/json", {"data":["address=${address}", "key=AIzaSyCkXlW0RHOLmA9VX9v80C_zRN486MTrzgE"], "request":"GET"} );`
      
      query = couchbase.N1qlQuery.fromString(query);
      
      matched.push(
        queryPromise(query)
        .then(rows => {
          if (rows.length < 1) return;
          
          console.dir(rows[0]);

          let patient = { "lat": Math.radians(rows[0].results[0].geometry.location.lat), "lng": Math.radians(rows[0].results[0].geometry.location.lng) };
          let query = `SELECT ${JSON.stringify(patient)} as pat, facility as fac, { "address": h.address.text, "name": h.name } as details, 3959*acos(sin(facility.lat) * sin(${patient.lat}) + cos(facility.lat) * cos(${patient.lat}) * cos(${patient.lng} - facility.lng)) as dist
                       FROM ${bucket._name} h
                       LET facility = { "lat": RADIANS(h.position.latitude), "lng": RADIANS(h.position.longitude) }
                       WHERE resourceType = "Location" AND type.coding[0].code = "HOSP" OR type.coding[0].code = "INFD"
                       ORDER BY dist 
                       LIMIT 1;`;    

          query = couchbase.N1qlQuery.fromString(query);

          return queryPromise(query);
        })
      )
    }

    return Promise.all(matched);
  })
  .then(matched => res.json(matched))
  .catch(err => {
    console.log(err);
    res.status(500).send({ error: err });
  });
}