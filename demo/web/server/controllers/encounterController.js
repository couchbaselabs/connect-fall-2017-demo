var N1qlQuery = require('../utils/n1ql');

exports.encounter = function(req, res, next) {
  let n1ql = "encounter WHERE recordType = 'Encounter' AND META().id = '" + req.params.id + "';";
  
  N1qlQuery(n1ql, req, res, next);
};

exports.list = function(req, res, next) {
  let n1ql = "encounter WHERE recordType = 'Encounter' AND META().id IN " + JSON.stringify(req.body.ids) + ";";

  N1qlQuery(n1ql, req, res, next);
}
