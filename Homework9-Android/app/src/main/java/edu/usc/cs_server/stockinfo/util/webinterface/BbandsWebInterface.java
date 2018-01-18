package edu.usc.cs_server.stockinfo.util.webinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;

/**
 * Created by Anuj Doiphode on 23-11-2017.
 */

public class BbandsWebInterface {
    Context context;
    JSONArray bbandsLower;
    JSONArray bbandsMiddle;
    JSONArray bbandsUpper;
    String ticker;
    String title;
    String activeChart;

    public BbandsWebInterface(Context context, JSONArray bbandsUpper, JSONArray bbandsMiddle, JSONArray bbandsLower, String ticker, String title, String activeChart) {
        this.context = context;
        this.bbandsUpper = bbandsUpper;
        this.bbandsMiddle = bbandsMiddle;
        this.bbandsLower = bbandsLower;
        this.ticker = ticker;
        this.title = title;
        this.activeChart = activeChart;
    }

    @JavascriptInterface
    public String getBbandsLower() {
        return bbandsLower.toString();
    }

    @JavascriptInterface
    public String getBbandsMiddle() {
        return bbandsMiddle.toString();
    }

    @JavascriptInterface
    public String getBbandsUpper() {
        return bbandsUpper.toString();
    }

    @JavascriptInterface
    public String getTicker() {
        return this.ticker;
    }

    @JavascriptInterface
    public String getTitle() {
        return this.title;
    }

    @JavascriptInterface
    public String getActiveChart() {
        return activeChart;
    }
}
