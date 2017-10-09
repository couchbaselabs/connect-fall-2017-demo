const express = require('express');
const router = express.Router();

const modelController = require('../controllers/modelController'); 

router.get('/model/patient', modelController.patient)
  
module.exports = router;