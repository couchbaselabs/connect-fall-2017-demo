const express = require('express');
const router = express.Router();

// Require our controllers
const searchController = require('../controllers/searchController'); 

router.post('/encounters', searchController.encounters);

router.post('/diagnosis', searchController.diagnosis)
  
module.exports = router;