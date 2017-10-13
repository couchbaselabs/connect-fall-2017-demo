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
    username: 'q7wC9CzpQM-XP96lU6WjOA',
    password: '_jLq81jpRFaRow2P56jaHw'
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