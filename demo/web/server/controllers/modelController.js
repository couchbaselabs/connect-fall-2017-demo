const models = require('../models');

exports.patient = function(req, res, next) {
  res.json(models.patient);
};
