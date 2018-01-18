package edu.usc.cs_server.stockinfo.util.webinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;

/**
 * Created by Anuj Doiphode on 22-11-2017.
 */

public class GenericWebInterface {
    Context context;
    JSONArray data;
    String ticker;
    String title;
    String activeChart;

    public GenericWebInterface(Context context, JSONArray data, String ticker, String title, String activeChart) {
        this.context = context;
        this.data = data;
        this.ticker = ticker;
        this.title = title;
        this.activeChart = activeChart;
    }

    @JavascriptInterface
    public String getData() {
        return this.data.toString();
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
