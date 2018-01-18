package edu.usc.cs_server.stockinfo.util.webinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;

/**
 * Created by Anuj Doiphode on 19-11-2017.
 */

public class HistoricalWebInterface {
    Context context;
    JSONArray historicalData;
    String ticker;

    public HistoricalWebInterface(Context c, JSONArray j, String t) {
        this.context = c;
        this.historicalData = j;
        this.ticker = t;
    }

    @JavascriptInterface
    public String getHistoricalData() {
        return this.historicalData.toString();
    }

    @JavascriptInterface
    public String getTicker() {
        return this.ticker;
    }
}
