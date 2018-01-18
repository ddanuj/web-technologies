var express = require('express');
var request = require('request');
var xml2js = require('xml2js');
var momentTz = require('moment-timezone');

var app = express();

var router = express.Router();
router.use(function (req, res, next) {
    //console.log("app started...");
    next();
});

router.get('/', function (req, res) {
    res.json("This is Homework8 sample response.");
});

router.route('/:ticker/time')
    .get(function (req, res) {
        var ticker = req.params.ticker;

        var timeStr = "";
        request("https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=" + ticker + "&interval=1min&apikey=DKHCAAD5PDYS8JWI", function (error, response, body) {
            var data = JSON.parse(body);

            var date = momentTz().tz("America/New_York").format();
            var ts = momentTz().unix();
            // console.log(ts);
            var tZStr = momentTz.tz.zone("America/New_York").abbr(ts);
            // console.log(tZStr);

            var dateParts = date.split("T");
            var ymd = dateParts[0];
            var t24tz = dateParts[1];

            var timeParts = t24tz.split("-");
            var t24 = timeParts[0];
            var t24Parts = t24.split(":");

            var hrs = t24Parts[0];
            console.log(hrs);

            if (hrs > 9 && hrs < 16) {
                timeStr = ymd + " " + t24 + " " + tZStr;
            } else {
                timeStr = data["Meta Data"]["3. Last Refreshed"] + " " + tZStr;
            }
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept");
            res.send(timeStr);
        });
    });

router.route('/auto/:query')
    .get(function (req, res) {
        var query = req.params.query;
        //console.log(query);
        var path = "http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=" + query;

        request(path, function (error, response, body) {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept");
            if (!error && response.statusCode == 200) {
                res.send(body);
            } else {
                return res.send();
            }
        });
    });

router.route('/:ticker/news')
    .get(function (req, res, next) {
        var ticker = req.params.ticker;
        var common_url = "https://seekingalpha.com/api/sa/combined/";
        var url = common_url + ticker + ".xml";
        console.log("URL:" + url);
        request(url, function (error, response, body) {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept");
            if (!error && response.statusCode == 200) {
                console.log("success: " + url);

                var xml_data = "";
                var xmlParser = new xml2js.Parser();
                var clientNewsList = [];
                var isEmpty = function (obj) {
                    for (var key in obj) {
                        if (obj.hasOwnProperty(key)) {
                            return false;
                        }
                    }
                    return true;
                };
                xmlParser.parseString(body, function (err, result) {
                    var newsList = result['rss']['channel'][0]['item'];
                    var discard = "https://seekingalpha.com/symbol";
                    for (var i = 0; i < newsList.length; i++) {
                        //console.log(newsList[i]['title'][0]);
                        var news = {};
                        if (newsList[i]['link'][0].indexOf(discard) == -1) {
                            for (key in newsList[i]) {
                                //console.log(key);
                                if (key == 'title' || key == 'link' || key == 'pubDate' || key == 'sa:author_name') {
                                    //console.log(key + " = " + newsList[i][key]);
                                    news[key] = newsList[i][key][0];
                                    //console.log("*** News1 = ***" + news);
                                }
                            }
                        }
                        if (!isEmpty(news)) {
                            clientNewsList.push(news);
                        }
                    }
                });
                //console.log(clientNewsList);
                res.json(clientNewsList.slice(0, 5));
            } else {
                // console.log("Error: " + error + "\nfor: " + url);
                res.send();
            }
        });
    });

router.route('/:ticker/full')
    .get(function (req, res, next) {
        var ticker = req.params.ticker;
        var url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + ticker + "&outputsize=full&apikey=DKHCAAD5PDYS8JWI";

        request(url, function (error, response, body) {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept");
            if (!error && response.statusCode == 200) {
                console.log("success: " + url);

                var price_arr = new Array();
                //console.log(body);
                var data = JSON.parse(body);
                if (data["Error Message"]) {
                    res.send();
                } else {
                    var price_obj = data["Time Series (Daily)"];
                    //console.log(price_obj);
                    var cnt = 0;
                    for (var i in price_obj) {
                        price_arr.push(new Array(Number(new Date(i)), Number(price_obj[i]["4. close"])));
                        //price_arr.push(new Array(Date.parse(i), Number(price_obj[i]["4. close"])));
                        //cnt = cnt + 86400000;
                    }
                    var result = price_arr.slice(0, 1000);
                    // console.log(result.length);
                    res.json(result.reverse());
                }
            } else {
                // console.log("Error: " + error + "\nfor: " + url);
                res.send();
            }
        });
    });

router.route('/quote/:ticker/:func')
    .get(function (req, res, next) {
        // Get data from Alpha vantage
        var ticker = req.params.ticker;
        var func = req.params.func;
        var apiKey = "DKHCAAD5PDYS8JWI";
        var path = "https://www.alphavantage.co/query?";

        //Resolve function name
        var url = '';
        switch (func) {
            case 'price':
                url = path + "function=TIME_SERIES_DAILY&symbol=" + ticker + "&outputsize=full&apikey=" + apiKey;
                break;
            case 'sma':
            case 'ema':
            case 'rsi':
            case 'adx':
            case 'cci':
                url = path + "function=" + func.toUpperCase() + "&symbol=" + ticker + "&interval=daily&time_period=10&series_type=open&apikey=" + apiKey;
                break;
            case 'stoch':
                url = path + "function=STOCH&symbol=" + ticker + "&interval=daily&apikey=" + apiKey;
                break;
            case 'bbands':
                url = path + "function=BBANDS&symbol=" + ticker + "&interval=daily&time_period=5&series_type=close&nbdevup=3&nbdevdn=3&apikey=" + apiKey;
                break;
            case 'macd':
                url = path + "function=MACD&symbol=" + ticker + "&interval=daily&series_type=open&apikey=" + apiKey;
                break;
            default:
                break;
        }

        //Get data
        request(url, function (error, response, body) {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept");
            if (!error && response.statusCode == 200) {
                console.log("success: " + url);
                res.send(body);
            } else {
                // console.log("Error: " + error + "\nfor: " + url);
                res.send();
            }
        });
    });

router.use(function (err, req, res, next) {
    console.error(err.stack);
    next();
});

function normalizePort(val) {
    var port = parseInt(val, 10);

    if (isNaN(port)) {
        // named pipe
        return val;
    }

    if (port >= 0) {
        // port number
        return port;
    }

    return false;
}

app.use('/', router);
var port = normalizePort(process.env.PORT || '3000');
app.listen(port);
module.exports = app;