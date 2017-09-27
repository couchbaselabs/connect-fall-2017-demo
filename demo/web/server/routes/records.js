const express = require('express');
const router = express.Router();

// Require our controllers
const encounterController = require('../controllers/encounterController'); 

/// Encounter Routes ///

/* GET encounter record given a encounter id */
router.get('/encounter/:id', encounterController.encounter);  

/* POST to retrieve a set of encounterss given a list of IDs */
router.post('/encounter/list', encounterController.list);

module.exports = router;