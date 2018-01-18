// create angular app
var valiateStockFormApp = angular.module('valiateStockFormApp', ['ngMaterial', 'ngAnimate'])
    .controller('validateStockFormController', ['$scope', '$http', '$log', '$q', '$location', '$compile', '$filter', '$timeout',
        function ($scope, $http, $log, $q, $location, $compile, $filter, $timeout) {

            var self = this;
            self.states = loadAll;
            self.querySearch = querySearch;
            $scope.showStockInfo = false;
            $scope.stockInfoTableReady = true;
            $scope.stockInfoChartReady = false;
            $scope.tab = 'current_stock';
            $scope.chart = 'price';
            $scope.favorite = 'glyphicon glyphicon-star-empty';
            $scope.favoriteList = new Array();
            $scope.ticker = '';
            $scope.changeColor = "";
            $scope.greenArrow = "http://cs-server.usc.edu:45678/hw/hw6/images/Green_Arrow_Up.png";
            $scope.redArrow = "http://cs-server.usc.edu:45678/hw/hw6/images/Red_Arrow_Down.png";
            $scope.arrow = "";
            $scope.orderByFilter = "default";
            $scope.autoRefreshStatus = false;
            var price_array = [];
            var volume_array = [];
            $scope.node_url = "http://csci571-homework8-stock-info-dev.us-west-1.elasticbeanstalk.com/quote/";
            $scope.borderColor = { "border-color": "#e5e5e5" };

            $scope.sortOptions = {
                "type": "select",
                "name": "sort",
                "value": "Default",
                "values": ["Default", "Symbol", "Price", "Change", "Change Percent", "Volume"]
            };

            $scope.orderOptions = {
                "type": "select",
                "name": "order",
                "value": "Ascending",
                "values": ["Ascending", "Descending"]
            }

            $scope.currentTime = null;
            $scope.getCurrentTime = function () {
                $http.get("http://csci571-homework8-stock-info-dev.us-west-1.elasticbeanstalk.com/" + $scope.ticker + "/time").then(function (response) {
                    if (response.data) {
                        $scope.currentTime = response.data;
                        $scope.stockInfoTableReady = true;
                    } else {
                        alert("Time Error: " + JSON.stringify(response));
                    }
                }, function (response) {
                    alert("Time Error: " + JSON.stringify(response));
                });
            }

            $scope.clearStockForm = function () {
                self.searchText = '';
                self.selectedItem = '';
                $scope.stockForm.$setPristine();
                $scope.stockForm.$setUntouched();
                $scope.stockForm.stockInput.$setPristine();
                $scope.stockForm.stockInput.$setUntouched();
                $scope.showStockInfo = false;
                $scope.stockInfoTableReady = true;
                $scope.stockInfoChartReady = false;
                $scope.borderColor = { "border-color": "#e5e5e5" };
            }

            $scope.autoRefreshTimer;
            $('#auto_refresh').change(function () {
                if ($('#auto_refresh').is(':checked')) {
                    $scope.autoRefreshTimer = setInterval(function () { $scope.updateFavoriteList() }, 5000);
                } else {
                    clearInterval($scope.autoRefreshTimer);
                }
            });

            $scope.updateFavoriteList = function () {
                $scope.favoriteList.forEach(element => {
                    $http.get($scope.node_url + element.symbol + '/price').then(function (response) {
                        var price_object = response.data["Time Series (Daily)"];
                        var last_day = price_object[Object.keys(price_object)[0]];
                        var previous_day = price_object[Object.keys(price_object)[1]];

                        var change_value = (previous_day["4. close"] - last_day["4. close"]).toFixed(2);
                        var change_percent = (change_value / previous_day["4. close"] * 100).toFixed(2);
                        var price = last_day["4. close"];
                        var volume = last_day["5. volume"];

                        element.price = price;
                        element.change = change_value;
                        element.changePercent = change_percent;
                        element.volume = volume;
                    });
                });
            }

            $scope.changedSortValue = function (sortFilter) {
                $scope.orderByFilter = sortFilter.toLowerCase();
                if ($scope.orderOptions.value == 'Descending') {
                    $scope.orderByFilter = '-' + $scope.orderByFilter;
                }
            }

            $scope.changedOrderValue = function (orderFilter) {
                //Does not matter
                if (orderFilter == 'Descending') {
                    $scope.orderByFilter = '-' + $scope.orderByFilter;
                } else {
                    $scope.orderByFilter = $scope.orderByFilter.replace(/-/g, '');
                }
            }

            $scope.removeFromTable = function (index) {
                $scope.favoriteList.splice(index, 1);
            }

            $scope.removeFromArray = function (value) {
                $scope.favoriteList.forEach(element => {
                    if (element.symbol == value) {
                        $scope.favoriteList.splice($scope.favoriteList.indexOf(element), 1);
                    }
                });
            }

            function querySearch(query) {
                var loadRes = loadAll(query);
                var querySearchRes = loadRes.then(function (data) {
                    return data.filter(createFilterFor(query));
                }, function (error) {
                    $log.error(error);
                    return;
                });
                return querySearchRes;
            }

            function loadAll(query) {
                var deferred = $q.defer();
                var http_url = "http://csci571-homework8-stock-info-dev.us-west-1.elasticbeanstalk.com/auto/";
                $http.get(http_url + query)
                    .success(function (response) {
                        deferred.resolve(response);
                    }).error(function (msg, code) {
                        deferred.reject(msg);
                        $log.error(msg, code);
                    });
                return deferred.promise;
            }

            function createFilterFor(query) {
                var lowercaseQuery = angular.lowercase(query);
                return function filterFn(stock) {
                    var lowerSymbol = angular.lowercase(stock.Symbol);
                    var lowerName = angular.lowercase(stock.Name);
                    stock.display = stock.Symbol + " - " + stock.Name + " (" + stock.Exchange + ")";
                    return (lowerSymbol.indexOf(lowercaseQuery) === 0
                        || lowerName.indexOf(lowercaseQuery) === 0);
                };
            }

            $('#auto_complete_input').keyup(function () {
                if ($scope.stockForm.stockInput.$invalid) {
                    $scope.stockForm.stockInput.$setTouched();
                    $scope.borderColor = {
                        "border-color": "#ff0000"
                    }
                } else {
                    $scope.borderColor = {
                        "border-color": "#0000ff"
                    }
                }
            });

            $scope.markStarEmpty = function () {
                var found = false;
                $scope.favoriteList.forEach(element => {
                    if (element.symbol == $scope.ticker) {
                        found = true;
                        $scope.favorite = 'glyphicon glyphicon-star';
                    }
                });
                if (found == false) {
                    $scope.favorite = 'glyphicon glyphicon-star-empty';
                }
            }

            $scope.toggleStockInfo = function () {
                if ($scope.showStockInfo == true) {
                    $scope.showStockInfo = false;
                } else {
                    $scope.showStockInfo = true;
                    $scope.markStarEmpty();
                }
            };

            // function to submit the form after all validation has occurred			
            $scope.submitStockForm = function (query) {
                // check to make sure the form is completely valid
                //alert(query);
                if ($scope.stockForm.$valid) {
                    $scope.showStockInfo = true;
                    $scope.stockInfoTableReady = false;
                    $scope.addProgressBar("stock_info_table_progressbar");
                    $scope.stockInfoChartReady = false;
                    $scope.markStarEmpty();
                    price_array = [];
                    volume_array = [];
                    //var stkInfo = $("#stock_info");
                    //$compile(stkInfo.contents())($scope);

                    var stock_info = query.split(" - ");
                    $scope.ticker = stock_info[0];
                    //alert(ticker);

                    //Call node with this ticker
                    $("#price_chart").html("");
                    $("#historical_price_chart").html("");
                    $("#news").html("");
                    $("#price_chart").html("");
                    $("#sma_chart").html("");
                    $("#ema_chart").html("");
                    $("#stoch_chart").html("");
                    $("#rsi_chart").html("");
                    $("#adx_chart").html("");
                    $("#cci_chart").html("");
                    $("#bbands_chart").html("");
                    $("#macd_chart").html("");

                    $scope.displayChart('price');
                    $scope.displayChart('sma');
                    $scope.displayChart('ema');
                    $scope.displayChart('stoch');
                    $scope.displayChart('rsi');
                    $scope.displayChart('adx');
                    $scope.displayChart('cci');
                    $scope.displayChart('bbands');
                    $scope.displayChart('macd');
                    // if ($scope.chart == 'price') {

                    //     //$scope.addProgressBar("price_chart");

                    // }

                    //Set Favorite class
                    if (!$scope.favoriteList.includes($scope.ticker)) {
                        $scope.favorite = 'glyphicon glyphicon-star-empty';
                    } else {
                        $scope.favorite = 'glyphicon glyphicon-star';
                    }
                }
            };

            $scope.addProgressBar = function (id) {
                //Add progress bar
                $("#" + id).html("");
                var element = angular.element('<div style="margin-top: 5%"><div class="progress">'
                    + '<div class="progress-bar progress-bar-info progress-bar-striped" role="progressbar" aria-valuenow="50"'
                    + 'aria-valuemin="0" aria-valuemax="100" style="width: 50%"><span class="sr-only">50% Complete</span></div></div></div>');
                $("#" + id).append(element);
            }

            $scope.addAlert = function (id) {
                $("#" + id).html("");
                var comp = "";
                var margin = 5;
                switch (id) {
                    case 'stock_info_table_progressbar':
                        comp = "current stock";
                        margin = 10;
                        break;
                    case 'historical_price_chart':
                        comp = "historical charts";
                        break;
                    case 'news':
                        comp = "news feed";
                        break;
                    case 'price_chart':
                        comp = id.slice(0, -6);
                        comp = comp.charAt(0).toUpperCase() + comp.slice(1);
                        break;
                    case 'sma_chart':
                    case 'ema_chart':
                    case 'stoch_chart':
                    case 'rsi_chart':
                    case 'adx_chart':
                    case 'cci_chart':
                    case 'bbands_chart':
                    case 'macd_chart':
                        comp = id.slice(0, -6).toUpperCase();
                        break;
                    default:
                        comp = id;
                        break;
                }
                var element = angular.element('<div class="alert alert-danger" role="alert" style="margin-top: ' + margin + '%">'
                    + 'Error! Failed to get ' + comp + ' data.</div>');
                $("#" + id).append(element);
            }

            $scope.isActive = function (checkTab) {
                return $scope.tab === checkTab;
            };

            $scope.activate = function (activeTab) {
                $scope.tab = activeTab;
            };

            $scope.isActiveChart = function (checkChart) {
                return $scope.chart === checkChart;
            };

            $scope.activateChart = function (activeChart) {
                //alert("Inside activateChart function: " + activeChart);
                $scope.chart = activeChart;
            };

            $scope.markFavorite = function () {
                if ($scope.favorite == 'glyphicon glyphicon-star') {
                    $scope.favorite = 'glyphicon glyphicon-star-empty';
                    $scope.removeFromArray($scope.ticker);
                } else {
                    $scope.favorite = 'glyphicon glyphicon-star';
                    price_array.reverse();
                    volume_array.reverse();
                    var change = (price_array[1][1] - price_array[0][1]).toFixed(2);
                    var changePercent = (change / price_array[1][1] * 100).toFixed(2);
                    var favoriteEntry = {
                        'symbol': $scope.ticker,
                        'price': price_array[0][1],
                        'change': change,
                        'changePercent': changePercent,
                        'volume': volume_array[0][1]
                    }
                    $scope.favoriteList.push(favoriteEntry);
                }
            };

            $scope.displayChart = function (activeChart) {
                //Show the chart only if div is empty
                if ($('#' + activeChart + '_chart').is(':empty')) {
                    $scope.addProgressBar(activeChart + "_chart");
                    var url = '';
                    switch (activeChart) {
                        case 'price':
                            // $scope.addProgressBar("stock_info_table");
                            $http.get($scope.node_url + $scope.ticker + '/' + activeChart).then(function (response) {
                                $("#price_chart").html("");
                                // $("#stock_info_table").html("");
                                var timeStp = "";
                                if (response.data["Time Series (Daily)"]) {
                                    $scope.getCurrentTime();

                                    var price_object = response.data["Time Series (Daily)"];
                                    //alert(JSON.stringify(price_object[Object.keys(price_object)[0]]));
                                    for (var i in price_object) {
                                        price_array.push([Number(Date.parse(i)), Number(price_object[i]["4. close"])]);
                                        volume_array.push([Date.parse(i), parseFloat(price_object[i]["5. volume"])]);
                                    }

                                    var last_day = price_object[Object.keys(price_object)[0]];
                                    var previous_day = price_object[Object.keys(price_object)[1]];

                                    var change_value = (previous_day["4. close"] - last_day["4. close"]).toFixed(2);
                                    var change = (change_value / previous_day["4. close"] * 100).toFixed(2);
                                    if (change < 0) {
                                        $scope.changeColor = 'red';
                                        $scope.arrow = $scope.redArrow;
                                    } else if (change > 0) {
                                        $scope.changeColor = 'green';
                                        $scope.arrow = $scope.greenArrow;
                                    }
                                    $scope.stockPriceInfo = {
                                        "symbol": response.data["Meta Data"]["2. Symbol"],
                                        "last_price": last_day["4. close"],
                                        "change": change_value + " (" + change + ")",
                                        "open": last_day["1. open"],
                                        "close": previous_day["4. close"],
                                        "range": (last_day["4. close"] - last_day["1. open"]).toFixed(2),
                                        "volume": last_day["5. volume"]
                                    };
                                    $scope.stockInfoTableReady = true;

                                    //Display the Price / Volume chart
                                    $scope.stockInfoChartReady = true;
                                    var price_chart = new Highcharts.Chart({
                                        chart: {
                                            renderTo: 'price_chart',
                                            zoomType: 'x'
                                        },
                                        title: {
                                            text: $scope.ticker + ' Stock Price and Volume'
                                        },
                                        subtitle: {
                                            text: '<a href="https://www.alphavantage.co/ style="color: blue">Source: Alpha Vantage</a>'
                                        },
                                        xAxis: [{
                                            type: 'datetime',
                                            labels: {
                                                format: '{value:%m/%d}'
                                            },
                                            tickLength: 0,
                                            tickInterval: 24 * 3600 * 1000 * 7
                                        }],
                                        yAxis: [{
                                            title: {
                                                text: "Stock Price",
                                            },
                                            tickInterval: 30.00
                                        }, {
                                            title: {
                                                text: "Volume"
                                            },
                                            tickInterval: 2000 * 10000,
                                            opposite: true
                                        }],
                                        tooltip: {
                                            formatter: function () {
                                                var toDate = new Date(this.x).getDate();
                                                var toMonth = new Date(this.x).getMonth() + 1;
                                                var symbol = '<span style="color:#0000ff">\u25CF</span>';
                                                return toMonth + "/" + toDate + '<br/>' + symbol
                                                    + this.series.name + " : " + '<b>' + this.y + '</b>';
                                            }
                                        },
                                        plotOptions: {
                                            area: {
                                                lineColor: '#0000ff',
                                                lineWidth: 1,
                                                marker: {
                                                    lineWidth: 1,
                                                    fillColor: '#0000ff',
                                                    lineColor: null
                                                }
                                            }
                                        },
                                        series: [{
                                            name: 'Price',
                                            type: 'area',
                                            yAxis: 0,
                                            data: price_array.slice(0, 130).reverse()
                                        }, {
                                            name: 'Volume',
                                            type: 'column',
                                            yAxis: 1,
                                            data: volume_array.slice(0, 130).reverse(),
                                            pointWidth: 0.9,
                                            color: '#ff0000'
                                        }]
                                    });
                                } else {
                                    //Data Error
                                    $scope.addAlert('price_chart');
                                    $scope.stockInfoTableReady = false;
                                    $scope.addAlert('stock_info_table_progressbar');
                                }
                            }, function (response) {
                                //Http Error
                                $scope.addAlert('price_chart');
                                $scope.stockInfoTableReady = false;
                                $scope.addAlert('stock_info_table_progressbar');
                            });
                            $scope.displayHistoricalPrice();
                            $scope.displayNews();
                            break;
                        case 'sma':
                        case 'ema':
                        case 'rsi':
                        case 'adx':
                        case 'cci':
                            $http.get($scope.node_url + $scope.ticker + '/' + activeChart).then(function (response) {
                                $("#" + activeChart + "_chart").html("");
                                if (response.data["Meta Data"]) {
                                    var data = response.data;
                                    var title = data["Meta Data"]["2: Indicator"];
                                    var data_object = data["Technical Analysis: " + activeChart.toUpperCase()];
                                    var data_array = [];

                                    for (var i in data_object) {
                                        data_array.push([Date.parse(i), parseFloat(data_object[i][activeChart.toUpperCase()])]);
                                    }
                                    var data_six_months = data_array.slice(0, 130);
                                    var line_chart_options = {
                                        chart: {
                                            renderTo: activeChart + '_chart',
                                            zoomType: 'x'
                                        },
                                        title: {
                                            text: title
                                        },
                                        subtitle: {
                                            text: '<a href="https://www.alphavantage.co/ style="color: blue">Source: Alpha Vantage</a>'
                                        },
                                        xAxis: [{
                                            type: 'datetime',
                                            labels: {
                                                format: '{value:%m/%d}'
                                            }
                                        }],
                                        yAxis: [{
                                            title: {
                                                text: activeChart.toUpperCase()
                                            }
                                        }],
                                        series: [{
                                            name: $scope.ticker,
                                            type: 'line',
                                            data: data_six_months.reverse()
                                        }]
                                    };
                                    $scope.stockInfoChartReady = true;
                                    var single_line_chart = new Highcharts.Chart(line_chart_options);
                                } else {
                                    //Data Error
                                    $scope.addAlert(activeChart + '_chart');
                                }
                            }, function (response) {
                                //Http Error
                                $scope.addAlert(activeChart + '_chart');
                            });
                            break;
                        case 'stoch':
                            $http.get($scope.node_url + $scope.ticker + '/' + activeChart).then(function (response) {
                                $("#stoch_chart").html("");
                                if (response.data["Meta Data"]) {
                                    var stoch_data = response.data;
                                    var stoch_title = stoch_data["Meta Data"]["2: Indicator"];
                                    var stoch_object = stoch_data["Technical Analysis: STOCH"];
                                    var stoch_slowd_array = [];
                                    var stoch_slowk_array = [];

                                    for (var i in stoch_object) {
                                        stoch_slowd_array.push([Date.parse(i), parseFloat(stoch_object[i]["SlowD"])]);
                                        stoch_slowk_array.push([Date.parse(i), parseFloat(stoch_object[i]["SlowK"])]);
                                    }
                                    var stoch_slowd_six_months = stoch_slowd_array.slice(0, 130);
                                    var stoch_slowk_six_months = stoch_slowk_array.slice(0, 130);
                                    var stoch_chart_options = {
                                        chart: {
                                            renderTo: 'stoch_chart',
                                            zoomType: 'x'
                                        },
                                        title: {
                                            text: stoch_title + ' Oscillator'
                                        },
                                        subtitle: {
                                            text: '<a href="https://www.alphavantage.co/ style="color: blue">Source: Alpha Vantage</a>'
                                        },
                                        xAxis: [{
                                            type: 'datetime',
                                            labels: {
                                                format: '{value:%m/%d}'
                                            }
                                        }],
                                        yAxis: [{
                                            title: {
                                                text: "STOCH"
                                            }
                                        }],
                                        series: [{
                                            name: $scope.ticker + " SlowD",
                                            type: 'line',
                                            data: stoch_slowd_six_months.reverse()
                                        }, {
                                            name: $scope.ticker + " SlowK",
                                            type: 'line',
                                            data: stoch_slowk_six_months.reverse()
                                        }]
                                    };
                                    $scope.stockInfoChartReady = true;
                                    var stoch_chart = new Highcharts.Chart(stoch_chart_options);
                                } else {
                                    //Data Error
                                    $scope.addAlert('stoch_chart');
                                }
                            }, function (response) {
                                //Http Error
                                $scope.addAlert('stoch_chart');
                            });
                            break;
                        case 'bbands':
                            $http.get($scope.node_url + $scope.ticker + '/' + activeChart).then(function (response) {
                                $("#bbands_chart").html("");
                                if (response.data["Meta Data"]) {
                                    var bbands_data = response.data;
                                    var bbands_title = bbands_data["Meta Data"]["2: Indicator"];
                                    var bbands_object = bbands_data["Technical Analysis: BBANDS"];
                                    var bbands_rub_array = [];
                                    var bbands_rlb_array = [];
                                    var bbands_rmb_array = [];

                                    for (var i in bbands_object) {
                                        bbands_rub_array.push([Date.parse(i), parseFloat(bbands_object[i]["Real Upper Band"])]);
                                        bbands_rlb_array.push([Date.parse(i), parseFloat(bbands_object[i]["Real Lower Band"])]);
                                        bbands_rmb_array.push([Date.parse(i), parseFloat(bbands_object[i]["Real Middle Band"])]);
                                    }
                                    var bbands_rub_six_months = bbands_rub_array.slice(0, 130);
                                    var bbands_rlb_six_months = bbands_rlb_array.slice(0, 130);
                                    var bbands_rmb_six_months = bbands_rmb_array.slice(0, 130);
                                    var bbands_chart_options = {
                                        chart: {
                                            renderTo: 'bbands_chart',
                                            zoomType: 'x'
                                        },
                                        title: {
                                            text: bbands_title
                                        },
                                        subtitle: {
                                            text: '<a href="https://www.alphavantage.co/ style="color: blue">Source: Alpha Vantage</a>'
                                        },
                                        xAxis: [{
                                            type: 'datetime',
                                            labels: {
                                                format: '{value:%m/%d}'
                                            }
                                        }],
                                        yAxis: [{
                                            title: {
                                                text: "BBANDS"
                                            }
                                        }],
                                        series: [{
                                            name: $scope.ticker + " Real Upper Band",
                                            type: 'line',
                                            data: bbands_rub_six_months.reverse()
                                        }, {
                                            name: $scope.ticker + " Real Lower Band",
                                            type: 'line',
                                            data: bbands_rlb_six_months.reverse()
                                        }, {
                                            name: $scope.ticker + " Real Middle Band",
                                            type: 'line',
                                            data: bbands_rmb_six_months.reverse()
                                        }]
                                    };
                                    $scope.stockInfoChartReady = true;
                                    var bbands_chart = new Highcharts.Chart(bbands_chart_options);
                                } else {
                                    //Data Error
                                    $scope.addAlert('bbands_chart');
                                }
                            }, function (response) {
                                //Http Error
                                $scope.addAlert('bbands_chart');
                            });
                            break;
                        case 'macd':
                            $http.get($scope.node_url + $scope.ticker + '/' + activeChart).then(function (response) {
                                $("#macd_chart").html("");
                                if (response.data["Meta Data"]) {
                                    var macd_data = response.data;
                                    var macd_title = macd_data["Meta Data"]["2: Indicator"];
                                    var macd_object = macd_data["Technical Analysis: MACD"];
                                    var macd_signal_array = [];
                                    var macd_hist_array = [];
                                    var macd_array = [];

                                    for (var i in macd_object) {
                                        macd_signal_array.push([Date.parse(i), parseFloat(macd_object[i]["MACD_Signal"])]);
                                        macd_hist_array.push([Date.parse(i), parseFloat(macd_object[i]["MACD_Hist"])]);
                                        macd_array.push([Date.parse(i), parseFloat(macd_object[i]["MACD"])]);
                                    }
                                    var macd_signal_six_months = macd_signal_array.slice(0, 130);
                                    var macd_hist_six_months = macd_hist_array.slice(0, 130);
                                    var macd_six_months = macd_array.slice(0, 130);
                                    var macd_chart_options = {
                                        chart: {
                                            renderTo: 'macd_chart',
                                            zoomType: 'x'
                                        },
                                        title: {
                                            text: macd_title
                                        },
                                        subtitle: {
                                            text: '<a href="https://www.alphavantage.co/ style="color: blue">Source: Alpha Vantage</a>'
                                        },
                                        xAxis: [{
                                            type: 'datetime',
                                            labels: {
                                                format: '{value:%m/%d}'
                                            }
                                        }],
                                        yAxis: [{
                                            title: {
                                                text: "MACD"
                                            }
                                        }],
                                        series: [{
                                            name: $scope.ticker + " MACD",
                                            type: 'line',
                                            data: macd_six_months.reverse()
                                        }, {
                                            name: $scope.ticker + " MACD_Hist",
                                            type: 'line',
                                            data: macd_hist_six_months.reverse()
                                        }, {
                                            name: $scope.ticker + " MACD_Signal",
                                            type: 'line',
                                            data: macd_signal_six_months.reverse()
                                        }]
                                    };
                                    $scope.stockInfoChartReady = true;
                                    var macd_chart = new Highcharts.Chart(macd_chart_options);
                                } else {
                                    //Data Error
                                    $scope.addAlert('macd_chart');
                                }
                            }, function (response) {
                                //Http Error
                                $scope.addAlert('macd_chart');
                            });
                            break;
                        default:
                            break;
                    }
                }
            }

            $scope.displayHistoricalPrice = function () {
                if ($('#historical_price_chart').is(':empty')) {
                    $scope.addProgressBar("historical_price_chart");
                    $http.get("http://csci571-homework8-stock-info-dev.us-west-1.elasticbeanstalk.com/" + $scope.ticker + '/full').then(function (response) {
                        $('#historical_price_chart').html("");
                        if (response.data) {
                            var data = response.data;
                            Highcharts.stockChart('historical_price_chart', {
                                rangeSelector: {
                                    buttons: [{
                                        type: 'week',
                                        count: 1,
                                        text: '1w'
                                    }, {
                                        type: 'month',
                                        count: 1,
                                        text: '1m'
                                    }, {
                                        type: 'month',
                                        count: 3,
                                        text: '3m'
                                    }, {
                                        type: 'month',
                                        count: 6,
                                        text: '6m'
                                    }, {
                                        type: 'ytd',
                                        text: 'YTD'
                                    }, {
                                        type: 'year',
                                        count: 1,
                                        text: '1y'
                                    }, {
                                        type: 'all',
                                        text: 'all'
                                    }],
                                    selected: 1
                                },
                                title: {
                                    text: $scope.ticker + ' Stock Value'
                                },
                                subtitle: {
                                    text: '<a href="https://www.alphavantage.co/ style="color: blue">Source: Alpha Vantage</a>'
                                },
                                series: [{
                                    name: $scope.ticker + ' Price',
                                    data: data,
                                    type: 'area'
                                }]
                            });
                        } else {
                            //Data Error
                            $scope.addAlert('historical_price_chart');
                        }
                    }, function (response) {
                        //Http Error
                        $scope.addAlert('historical_price_chart');
                    });
                }
            }

            $scope.displayNews = function () {
                //alert("Before Calling node");
                if ($('#news').is(':empty')) {
                    $scope.addProgressBar("news");
                    $http.get("http://csci571-homework8-stock-info-dev.us-west-1.elasticbeanstalk.com/" + $scope.ticker + '/news').then(function (response) {
                        $('#news').html("");
                        if (response.data) {
                            response.data.forEach(function (news) {
                                var element = angular.element(
                                    '<div class="well well-lg"><table><tr><td><a href=' + news['link'] + ' target="_blank"><strong>' + news['title']
                                    + '</strong></a></td></tr><tr><td>&nbsp;</td></tr>'
                                    + '<tr><td><strong>Author: ' + news['sa:author_name'] + '</strong></td></tr>'
                                    + '<tr><td><strong>Date: ' + news['pubDate'] + '</strong></td></tr></table></div>'
                                );
                                $("#news").append(element);
                            }, this);
                        } else {
                            //Data Error
                            $scope.addAlert('news');
                        }
                    }, function (response) {
                        //Http Error
                        $scope.addAlert('news');
                    });
                }
            }

            $scope.exportToFacebook = function () {
                var chartImg = $("#" + $scope.chart + "_chart").highcharts();
                var imgData = {
                    options: chartImg.options,
                    filename: $scope.chart + "_chart",
                    type: 'image/png',
                    async: true
                };
                var exportUrl = "http://export.highcharts.com/"
                $http.post(exportUrl, imgData).then(function (response) {
                    FB.ui({
                        app_id: '185673858673950',
                        method: 'feed',
                        picture: exportUrl + response.data
                    }, (response) => {
                        if (response && !response.error_message) {
                            //succeed
                            alert("Shared Successfully");
                        } else {
                            //fail
                            alert("Sharing Failed.");
                        }
                    });
                });
            };
        }]);