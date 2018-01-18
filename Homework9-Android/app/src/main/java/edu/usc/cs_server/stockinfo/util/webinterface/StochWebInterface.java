package edu.usc.cs_server.stockinfo.util.webinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;

/**
 * Created by Anuj Doiphode on 22-11-2017.
 */

public class StochWebInterface {
    Context context;
    JSONArray stochSlowD;
    JSONArray stochSlowK;
    String ticker;
    String title;
    String activeChart;

    public StochWebInterface(Context context, JSONArray stochSlowD, JSONArray stochSlowK, String ticker, String title, String activeChart) {
        this.context = context;
        this.stochSlowD = stochSlowD;
        this.stochSlowK = stochSlowK;
        this.ticker = ticker;
        this.title = title;
        this.activeChart = activeChart;
    }

    @JavascriptInterface
    public String getStochSlowD() {
        return stochSlowD.toString();
    }

    @JavascriptInterface
    public String getStochSlowK() {
        return stochSlowK.toString();
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
