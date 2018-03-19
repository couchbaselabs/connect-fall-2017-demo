const express = require('express');
const router = express.Router();

// Require our controllers
const patientController = require('../controllers/patientController');
const encounterController = require('../controllers/encounterController'); 

/// Patient Routes ///

/* GET patient personal record given a patient ID */
router.get('/patient/:id', patientController.patient);  

/* GET patient condition records given a patient ID */
router.get('/patient/:id/conditions', patientController.conditions);

/* GET patient encounters given a patient ID */
router.get('/patient/:id/encounters', patientController.encounters);

/* GET patient observation records given a patient ID */
router.get('/patient/:id/conditions', patientController.observations);

/* GET all records referring to patient as subject given a patient ID */
router.get('/patient/:id/references', patientController.references)

/* GET patient location given a patient ID */
router.get('/patient/:id/location', patientController.location);

/* POST to retrieve a group of patients given a list of IDs */
router.post('/patient/cohort', patientController.cohort);

/* POST to retrieve locations for a group of patients given a list of IDs */
router.post('/patient/cohort/locations', patientController.cohortLocations);

/// Encounter Routes ///

/* GET encounter record given a encounter ID */
router.get('/encounter/:id', encounterController.encounter);  

/* POST to retrieve a set of encounterss given a list of IDs */
router.post('/encounter/list', encounterController.list);

module.exports = router;