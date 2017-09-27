var N1qlQuery = require('../utils/n1ql');

exports.patient = function(req, res, next) {
  let n1ql = "WHERE resourceType = 'Patient' AND META().id = '" + req.params.id + "';";
  
  N1qlQuery(n1ql, req, res, next);
};

exports.cohort = function(req, res, next) {
  let n1ql = "patient WHERE resourceType = 'Patient' AND META().id IN " + JSON.stringify(req.body.ids) + ";";
  
  N1qlQuery(n1ql, req, res, next);
}

exports.encounters = function(req, res, next) {
  let n1ql = "encounter WHERE resourceType = 'Encounter' AND META().id LIKE 'encounter::" + req.params.id + "%';";
  
  N1qlQuery(n1ql, req, res, next);
}

exports.location = function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let bucket = req.app.locals.bucket;
  
  let query = `SELECT address.text FROM ${bucket._name} WHERE resourceType = 'Patient' AND META().id = '${req.params.id}';`;
  
  query = couchbase.N1qlQuery.fromString(query);
  
  bucket.query(query, function(err, rows) {
    if (err) {
      console.log(err);
      res.status(500).send({ error: err });
      return;
    }
    
    let address = rows[0].text.replace(/ /g, '+').replace(/,/g, '%2C');
    
    let query = `SELECT RAW CURL("https://maps.googleapis.com/maps/api/geocode/json", {"data":"address=${address}" , "request":"GET"} );`
    
    query = couchbase.N1qlQuery.fromString(query);
    
    bucket.query(query, function(err, rows) {
      if (err) {
        console.log(err);
        res.status(500).send({ error: err });
        return;
      }
      
      res.json(rows[0].results[0].geometry.location);
    });
  });
}

exports.cohortLocations = function(req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let bucket = req.app.locals.bucket;
  
  let query = `SELECT address.text FROM ${bucket._name} WHERE resourceType = 'Patient' AND META().id IN ` + JSON.stringify(req.body.ids) + ";";
  
  query = couchbase.N1qlQuery.fromString(query);
  
  bucket.query(query, function(err, rows) {
    if (err) {
      console.log(err);
      res.status(500).send({ error: err });
      return;
    }
    
    for (row in rows) {
      let query = `SELECT CURL("https://maps.googleapis.com/maps/api/geocode/json", {"data":"address=${row.text}" , "request":"GET"} );`
      
      bucket.query(query, function(err, rows) {
        if (err) {
          console.log(err);
          res.status(500).send({ error: err });
          return;
        }
        
        res.json(row[0].geometry.location);
      });
    }
  });
}