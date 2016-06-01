var fs = require('fs');
var express = require('express');
var morgan = require('morgan');

var app = express();

var accessLogStream = fs.createWriteStream(__dirname + '/access.log', {flags: 'w'});

app.use(morgan('combined', {
	immediate: true,
	stream: accessLogStream
}));

app.use(express.static('static'));

app.all('*', function (req, res) {
	res.set(req.headers);
	res.sendStatus(200);
});

fs.writeFileSync("process.pid", process.pid);

app.listen(8080, function() {
	console.log('Started Test Server');
});