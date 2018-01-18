package edu.usc.cs_server.stockinfo.util.webinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Anuj Doiphode on 21-11-2017.
 */

public class PriceVolumeWebInterface {
    Context context;
    JSONArray priceArray;
    JSONArray volumeArray;
    String ticker;
    String chartUrl;

    public PriceVolumeWebInterface(Context context, JSONArray priceArray, JSONArray volumeArray, String ticker) {
        this.context = context;
        this.priceArray = priceArray;
        this.volumeArray = volumeArray;
        this.ticker = ticker;
    }

    @JavascriptInterface
    public String getPriceArray() {
        return this.priceArray.toString();
    }

    @JavascriptInterface
    public String getVolumeArray() {
        return this.volumeArray.toString();
    }

    @JavascriptInterface
    public String getTicker() {
        return this.ticker;
    }
}
