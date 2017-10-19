const express = require('express');
const router = express.Router();

// Require our controllers
const searchController = require('../controllers/searchController');

router.post('/encounters', searchController.encounters);

router.post('/diagnosis', searchController.diagnosis)

router.get('/analytics', searchController.analytics);

router.get('/analytics-details', searchController.analyticsDetails);

router.get('/analytics/social', searchController.analyticsSocial);

router.get('/analytics/social/details', searchController.analyticsSocialDetails);

router.get('/analytics/age', searchController.analyticsByAge);

router.get('/analytics/age/details', searchController.analyticsByAgeDetails);

module.exports = router;