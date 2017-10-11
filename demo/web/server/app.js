// Use bluebird promises everywhere
global.Promise = require('bluebird')

var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var sse = require('express-server-sent-events');

var couchbase = require('couchbase');
var cluster = new couchbase.Cluster('couchbase://localhost/');
cluster.authenticate("admin", "password");

Math.radians = function (degrees) {
  return degrees * Math.PI / 180
}

Math.degrees = function (radians) {
  return radians * 180 / Math.PI
}

var app = express();

app.locals.couchbase = couchbase;
app.locals.bucket = cluster.openBucket('health');

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use(function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
  next();
});

var feed = require('./routes/feed');
app.use('/feed', sse, feed);
const search = require('./routes/search');
app.use('/search', search);
const records = require('./routes/records');
app.use('/records', records);
const db = require('./routes/db');
app.use('/db', db);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  console.dir(req);
  console.dir(res);
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
