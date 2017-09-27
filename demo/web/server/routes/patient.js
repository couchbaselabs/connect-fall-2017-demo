const express = require('express');
const router = express.Router();

// Require our controllers
const patientController = require('../controllers/patientController'); 

/// Patient Routes ///

/* GET patient personal record given a patient id */
router.get('/:id', patientController.patient);  

/* POST to retrieve a group of patients given a list of IDs */
router.post('/cohort', patientController.cohort);

/* GET patient encounters given a patient id */
router.get('/:id/encounters', patientController.encounters);

/* */
router.get('/:id/location', patientController.location);

router.post('/cohort/locations', patientController.cohortLocations);

module.exports = router;