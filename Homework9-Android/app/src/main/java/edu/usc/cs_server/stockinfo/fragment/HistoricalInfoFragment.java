package edu.usc.cs_server.stockinfo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.io.IOException;

import edu.usc.cs_server.stockinfo.R;
import edu.usc.cs_server.stockinfo.util.webinterface.HistoricalWebInterface;
import edu.usc.cs_server.stockinfo.util.SingletonVolleyQueue;
import edu.usc.cs_server.stockinfo.util.UtilMethods;

/**
 * Created by Anuj Doiphode on 17-11-2017.
 */

public class HistoricalInfoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historical_info,container,false);

        //Get Stock Info data and create price table
        String ticker = this.getActivity().getIntent().getStringExtra(getString(R.string.stock_ticker));
        WebView historicalWebView = view.findViewById(R.id.historical_web_view);
        historicalWebView.getSettings().setJavaScriptEnabled(true);
        displayHistoricalInfoTable(ticker, view, historicalWebView);
        return view;
    }

    private void displayHistoricalInfoTable(final String ticker, final View view, final WebView historicalWebView) {
        final Context thisContext = this.getContext();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                getString(R.string.aws_common) + ticker + getString(R.string.historical_info_url), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //System.out.println(response);
                        historicalWebView.addJavascriptInterface(
                                new HistoricalWebInterface(thisContext,response,ticker),"Android");
                        try {
                            historicalWebView.loadData(UtilMethods.readInputStream(
                                    getResources().getAssets().open("historical_chart.html")),
                                    "text/html","UTF-8");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        view.findViewById(R.id.historical_progress_bar).setVisibility(View.GONE);
                        historicalWebView.setVisibility(View.VISIBLE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });
        SingletonVolleyQueue.getInstance(thisContext).addToRequestQueue(jsonArrayRequest);
    }
}
