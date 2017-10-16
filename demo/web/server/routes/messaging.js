const express = require('express');
const router = express.Router();
const axios = require('axios');

let ua = axios.create({
  method: 'post',
  baseURL: 'https://go.urbanairship.com',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/vnd.urbanairship+json; version=3;'
  },
  auth: {
    username: process.env.UA_APPLICATION_KEY,
    password: process.env.UA_APPLICATION_MASTER_SECRET
  }
})  

router.post('/alert', function(req, res, next) {
  ua.post('/api/push', req.body)
  .then(response => {
    res.send(response);
  })
  .catch(error => {
    console.dir(error);
    res.status(500).send({ error: error });
  });
});

module.exports = router;