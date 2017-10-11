const express = require('express');
const router = express.Router();

// Require our controllers
const searchController = require('../controllers/searchController');

router.post('/encounters', searchController.encounters);

router.post('/diagnosis', searchController.diagnosis)

router.get('/analytics', searchController.analytics);

router.get('/analytics-details', searchController.analyticsDetails);

module.exports = router;