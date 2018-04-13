// I require sleep
const sleep = require('../utils/sleep');
const N1qlQuery = require('../utils/n1ql');
const express = require('express');
const router = express.Router();

let sse = {};

router.get('/:id', function(req, res, next) {
  sse[req.params.id] = res.sse;
});

router.post('/:id', function(req, res, next) {
  res.send('');

  if (sse[req.params.id] === undefined) return; // Not listening yet

  sse[req.params.id](`event: update\ndata: { "values": [${JSON.stringify(req.body)}]}\n\n`);
});

module.exports = router;