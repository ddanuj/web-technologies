<html>
    <head>
        <style>
            body > div {
                margin: 0 auto;
            }
            #input_div {
                width: 30%;
                margin: 10% auto;
                padding-top: 0.4%;
                padding-left: 0.8%;
                padding-right: 0.8%;
                padding-bottom: 5%;
                background-color: rgb(246, 246, 246);
                border: 2px solid rgb(240, 240, 240);
            }
            #input_div_title {
                text-align: center;
                font-size: 2em;
            }
            #input_div_input {
                display: flex;
            }
            #input_div_input_title {
                display: inline-block;
            }
            #input_div_input_box {
                display: inline-block;
                line-height: 2em;
            }
            hr {
                color: rgb(215, 215, 215);
            }
        </style>
    </head>
    <body>
        <div id="input_div">
            <div id="input_div_title">
                <i>Stock Search</i>
            </div>
            <div id="input_div_input">
                <div id="input_div_input_title">
                    Enter Stock Ticker Symbol:*&nbsp;
                </div>
                <div id="input_div_input_box">
                    <input type="text"><br>
                    <input type="button" value="Search">
                    <input type="button" value="Clear">
                </div>
            </div> 
            * - <i>Mandatory fields.</i>
        </div>
        <div id="stock_chart_div"></div>
        <div id="stock_news_div">
            <?php phpinfo() ?>
        </div>
    </body>
</html>