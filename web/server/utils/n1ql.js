module.exports = function N1qlQuery(clauses, req, res, next) {
  let couchbase = req.app.locals.couchbase;
  let bucket = req.app.locals.bucket;

  // If retrieving all fields (i.e. the whole document), it would be preferable to use get or getMulti operations.
  // For the sake of code simplicity, we always use N1QL here.
  let fields = '*';

  if (req.query.fields) {
    fields = req.query.fields;
  }

  let query = `SELECT ${fields} FROM ${bucket._name} ${clauses}`;

  query = couchbase.N1qlQuery.fromString(query);
  
  bucket.query(query, function(err, rows) {
    if (err) {
      console.log(err);
      res.status(500).send({ error: err });
      return;
    }

    res.json(rows);  
  });  
}