package edu.usc.cs_server.stockinfo.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import edu.usc.cs_server.stockinfo.R;
import edu.usc.cs_server.stockinfo.model.StockNewsDataModel;
import edu.usc.cs_server.stockinfo.util.SingletonVolleyQueue;
import edu.usc.cs_server.stockinfo.util.adapter.StockNewsListAdapter;

/**
 * Created by Anuj Doiphode on 17-11-2017.
 */

public class StockNewsFragment extends Fragment {

    private ArrayList<StockNewsDataModel> newsList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_news, container, false);

        //Get Stock Info data and create price table
        String ticker = this.getActivity().getIntent().getStringExtra(getString(R.string.stock_ticker));
        ListView stockNewsListView = view.findViewById(R.id.stock_news_list_view);
        displayNews(view, ticker, stockNewsListView);
        return view;
    }

    private void displayNews(final View view, final String ticker, final ListView stockNewsListView) {
        final Context thisContext = StockNewsFragment.this.getContext();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                getString(R.string.aws_common) + ticker + getString(R.string.news_url), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        newsList = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject news = response.getJSONObject(i);
                                //Convert Date
                                String orgDate = news.getString(getString(R.string.seeking_alpha_pub_date));
                                DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
                                dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                                Date orgDateEST = dateFormat.parse(orgDate);
                                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                                String orgDatePDT = dateFormat.format(orgDateEST);
                                newsList.add(new StockNewsDataModel(
                                        news.getString(getString(R.string.seeking_alpha_title)),
                                        news.getString(getString(R.string.seeking_alpha_link)),
                                        orgDatePDT.substring(0, orgDatePDT.length() - 5) + "PDT",
                                        news.getString(getString(R.string.seeking_alpha_author))
                                ));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        view.findViewById(R.id.stock_news_progress_bar).setVisibility(View.GONE);
                        stockNewsListView.setAdapter(new StockNewsListAdapter(newsList, thisContext));
                        stockNewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                StockNewsDataModel stockNewsDataModel = newsList.get(i);
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(stockNewsDataModel.getLink())));
                            }
                        });
                        view.findViewById(R.id.stock_news_list_view).setVisibility(View.VISIBLE);
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
