package edu.usc.cs_server.stockinfo.util.webinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;

/**
 * Created by Anuj Doiphode on 23-11-2017.
 */

public class MacdWebInterface {
    Context context;
    JSONArray macdSignal;
    JSONArray macdHistory;
    JSONArray macd;
    String ticker;
    String title;
    String activeChart;

    public MacdWebInterface(Context context, JSONArray macdSignal, JSONArray macdHistory, JSONArray macd, String ticker, String title, String activeChart) {
        this.context = context;
        this.macdSignal = macdSignal;
        this.macdHistory = macdHistory;
        this.macd = macd;
        this.ticker = ticker;
        this.title = title;
        this.activeChart = activeChart;
    }

    @JavascriptInterface
    public String getMacdSignal() {
        return macdSignal.toString();
    }

    @JavascriptInterface
    public String getMacdHistory() {
        return macdHistory.toString();
    }

    @JavascriptInterface
    public String getMacd() {
        return macd.toString();
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
