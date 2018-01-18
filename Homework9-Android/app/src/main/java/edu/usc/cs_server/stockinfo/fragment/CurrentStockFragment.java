package edu.usc.cs_server.stockinfo.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.usc.cs_server.stockinfo.MainActivity;
import edu.usc.cs_server.stockinfo.R;
import edu.usc.cs_server.stockinfo.StockInfoActivity;
import edu.usc.cs_server.stockinfo.model.CurrentStockDataModel;
import edu.usc.cs_server.stockinfo.model.FavoriteListDataModel;
import edu.usc.cs_server.stockinfo.util.UtilMethods;
import edu.usc.cs_server.stockinfo.util.adapter.CurrentStockListAdapter;
import edu.usc.cs_server.stockinfo.util.SingletonVolleyQueue;
import edu.usc.cs_server.stockinfo.util.adapter.FavoritesListAdapter;
import edu.usc.cs_server.stockinfo.util.webinterface.BbandsWebInterface;
import edu.usc.cs_server.stockinfo.util.webinterface.GenericWebInterface;
import edu.usc.cs_server.stockinfo.util.webinterface.MacdWebInterface;
import edu.usc.cs_server.stockinfo.util.webinterface.PriceVolumeWebInterface;
import edu.usc.cs_server.stockinfo.util.webinterface.StochWebInterface;

import static edu.usc.cs_server.stockinfo.MainActivity.FAVORITE_LIST;

/**
 * Created by Anuj Doiphode on 17-11-2017.
 */

public class CurrentStockFragment extends Fragment {

    private ToggleButton favoriteButton;
    //private JSONObject tableData;
    private ArrayList<CurrentStockDataModel> currentStockInfoList;
    private JSONArray priceArray, volumeArray, dataArray, stochSlowD, stochSlowK, bbandsUpper, bbandsMiddle, bbandsLower, macd, macdHistory, macdSignal;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    static class CurrentStock {
        private String symbol;
        private String lastPrice;
        private String change;
        private String changePercent;
        private String timeStamp;
        private String open;
        private String close;
        private String daysRange;
        private String volume;
        private JSONObject indicatorOptions;

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getLastPrice() {
            return lastPrice;
        }

        public void setLastPrice(String lastPrice) {
            this.lastPrice = lastPrice;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }

        public String getChangePercent() {
            return changePercent;
        }

        public void setChangePercent(String changePercent) {
            this.changePercent = changePercent;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getOpen() {
            return open;
        }

        public void setOpen(String open) {
            this.open = open;
        }

        public String getClose() {
            return close;
        }

        public void setClose(String close) {
            this.close = close;
        }

        public String getDaysRange() {
            return daysRange;
        }

        public void setDaysRange(String daysRange) {
            this.daysRange = daysRange;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public JSONObject getIndicatorOptions() {
            return indicatorOptions;
        }

        public void setIndicatorOptions(JSONObject indicatorOptions) {
            this.indicatorOptions = indicatorOptions;
        }
    }

    private boolean isFavorite(String stockName) {
        for (FavoriteListDataModel favoriteStock : FAVORITE_LIST) {
            if (favoriteStock.getTicker().equalsIgnoreCase(stockName)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_current_stock, container, false);

        gson = new Gson();

        //Set Current Stock Object
        final CurrentStockFragment.CurrentStock currentStock = new CurrentStockFragment.CurrentStock();
        currentStock.setSymbol(this.getActivity().getIntent().getStringExtra(getString(R.string.stock_ticker)));

        //Set Favorite button image
        favoriteButton = view.findViewById(R.id.favorite_button);
        favoriteButton.setChecked(false);

        if (isFavorite(currentStock.getSymbol())) {
            favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(
                    this.getContext().getApplicationContext(), R.drawable.icons8_star_filled_50));
            favoriteButton.setChecked(true);
        } else {
            favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(
                    this.getContext().getApplicationContext(), R.drawable.icons8_star_50));
        }
        favoriteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(
                            CurrentStockFragment.this.getContext().getApplicationContext(),
                            R.drawable.icons8_star_filled_50));
                    //Add current stock object to favorite list
                    if (!isFavorite(currentStock.getSymbol())) {
                        FAVORITE_LIST.add(new FavoriteListDataModel(
                                currentStock.getSymbol(),
                                currentStock.getLastPrice(),
                                currentStock.getChange(),
                                currentStock.getChangePercent()
                        ));
                    }
                } else {
                    favoriteButton.setBackgroundDrawable(ContextCompat.getDrawable(
                            CurrentStockFragment.this.getContext().getApplicationContext(),
                            R.drawable.icons8_star_50));
                    //If Current stock is in favorite list then remove it
                    if (isFavorite(currentStock.getSymbol())) {
                        FAVORITE_LIST.remove(currentStock);
                    }
                }
                // Commit the list to Shared Preferences
                sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.favorite_list_key), gson.toJson(FAVORITE_LIST));
                editor.commit();
            }
        });

        //Get Stock Info data and create price table
        final WebView indicatorsWebView = view.findViewById(R.id.indicators_web_view);
        if (Build.VERSION.SDK_INT >= 21) {
            indicatorsWebView.enableSlowWholeDocumentDraw();
        }
        indicatorsWebView.getSettings().setJavaScriptEnabled(true);
        indicatorsWebView.getSettings().setDomStorageEnabled(true);

        indicatorsWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(message);
                    currentStock.setIndicatorOptions(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                result.confirm();
                return true;
            }
        });

        ListView currentStockListView = view.findViewById(R.id.stock_info_table);
        displayStockInfoTable(currentStock, view, currentStockListView, indicatorsWebView);

        //TODO Also create Price / Volume chart along with table
        //Display Indicators chart
        final Spinner spinner = view.findViewById(R.id.indicators_spinner);
        final TextView change_button = view.findViewById(R.id.indicators_change_button);

        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.indicators_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        final String oldSpinnerValue = spinner.getSelectedItem().toString();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String newSpinnerValue = spinner.getSelectedItem().toString();
                if (newSpinnerValue != oldSpinnerValue) {
                    change_button.setTextColor(Color.parseColor("#000000"));
                } else {
                    if (indicatorsWebView.getVisibility() != View.GONE) {
                        change_button.setTextColor(getResources()
                                .getColor(R.color.com_facebook_button_background_color_disabled));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                change_button.setTextColor(Color.parseColor("#000000"));
            }
        });

        change_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                String newSpinnerValue = spinner.getSelectedItem().toString();
                if (newSpinnerValue != oldSpinnerValue) {
                    displayIndicatorChart(currentStock, view, indicatorsWebView, newSpinnerValue);
                } else if (indicatorsWebView.getVisibility() == View.GONE
                        && newSpinnerValue.equalsIgnoreCase("Price")) {
                    displayIndicatorChart(currentStock, view, indicatorsWebView, "Price");
                }
                change_button.setTextColor(getResources()
                        .getColor(R.color.com_facebook_button_background_color_disabled));
            }
        });

        //FB Share
        final ShareDialog fbShareDialog = ((StockInfoActivity) this.getActivity()).getShareDialog();
        final Context thisContext = CurrentStockFragment.this.getContext();
        Button fbButton = view.findViewById(R.id.fb_button);
        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fbShareDialog.canShow(ShareLinkContent.class)) {
                    if (currentStock.getSymbol() != null) {
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.highcharts_export_url),
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        fbShareDialog.show(new ShareLinkContent.Builder().setContentUrl(
                                                Uri.parse(getString(R.string.highcharts_export_url) + response.toString())).build());
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        System.out.println(error.toString());
                                    }
                                }
                        ) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                try {
                                    params.put("options", currentStock.getIndicatorOptions().get("options").toString());
                                    params.put("filename", currentStock.getIndicatorOptions().getString("filename"));
                                    params.put("type", currentStock.getIndicatorOptions().getString("type"));
                                    params.put("async", currentStock.getIndicatorOptions().getString("async"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return params;
                            }
                        };
                        SingletonVolleyQueue.getInstance(thisContext).addToRequestQueue(stringRequest);
                    }
                }
            }
        });

        return view;
    }

    private void displayStockInfoTable(final CurrentStockFragment.CurrentStock currentStock, final View view, final ListView currentStockListView, final WebView indicatorsWebView) {
        final Context thisContext = CurrentStockFragment.this.getContext();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                thisContext.getString(R.string.aws_common)
                        + thisContext.getString(R.string.stock_info_url)
                        + currentStock.getSymbol() + thisContext.getString(R.string.price_volume_function_value),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject priceVolumeFull = response.getJSONObject(
                                    thisContext.getString(R.string.alpha_vantage_time_series_daily));
                            int cnt = 0;
                            JSONObject lastDay = null, previousDay;
                            priceArray = new JSONArray();
                            volumeArray = new JSONArray();

                            Iterator<String> priceVolumeFullItr = priceVolumeFull.keys();
                            while (cnt < 130 && priceVolumeFullItr.hasNext()) {
                                String date = priceVolumeFullItr.next();
                                if (cnt == 0) {
                                    lastDay = priceVolumeFull.getJSONObject(date);
                                    currentStock.setLastPrice(lastDay.get(thisContext.getString(R.string.alpha_vantage_close)).toString());
                                    currentStock.setOpen(lastDay.get(thisContext.getString(R.string.alpha_vantage_close)).toString());
                                    currentStock.setVolume(lastDay.get(thisContext.getString(R.string.alpha_vantage_volume)).toString());
                                    currentStock.setDaysRange(String.format("%.2f", Float.parseFloat(
                                            lastDay.get(thisContext.getString(R.string.alpha_vantage_open)).toString()))
                                    + " - " + String.format("%.2f", Float.parseFloat(
                                            lastDay.get(thisContext.getString(R.string.alpha_vantage_close)).toString())));
                                } else if (cnt == 1) {
                                    previousDay = priceVolumeFull.getJSONObject(date);
                                    Float changeValue = Float.parseFloat(previousDay.get(thisContext.getString(R.string.alpha_vantage_close)).toString())
                                            - Float.parseFloat(lastDay.get(thisContext.getString(R.string.alpha_vantage_close)).toString());
                                    Float changePercent = changeValue
                                            / Float.parseFloat(previousDay.get(thisContext.getString(R.string.alpha_vantage_close)).toString()) * 100;
                                    currentStock.setClose(previousDay.get(thisContext.getString(R.string.alpha_vantage_close)).toString());
                                    currentStock.setChange(String.format("%.2f", changeValue));
                                    currentStock.setChangePercent(String.format("%.2f", changePercent));
                                }

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                try {
                                    priceArray.put(new JSONObject().put(String.valueOf(simpleDateFormat.parse(date).getTime()), Float.parseFloat(priceVolumeFull.getJSONObject(date)
                                            .get(thisContext.getString(R.string.alpha_vantage_close)).toString())));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    volumeArray.put(new JSONObject().put(String.valueOf(simpleDateFormat.parse(date).getTime()), Float.parseFloat(priceVolumeFull.getJSONObject(date)
                                            .get(thisContext.getString(R.string.alpha_vantage_volume)).toString())));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                cnt++;
                            }

                            //Display the table
                            currentStockInfoList = new ArrayList<>();
                            //Get Current Time
                            getTime(currentStock.getSymbol(), view, currentStockListView);

                            currentStockInfoList.add(new CurrentStockDataModel(
                                    thisContext.getString(R.string.stock_into_table_stock_symbol),
                                    currentStock.getSymbol()
                            ));
                            currentStockInfoList.add(new CurrentStockDataModel(
                                    thisContext.getString(R.string.stock_into_table_last_price),
                                    currentStock.getLastPrice()
                            ));
                            currentStockInfoList.add(new CurrentStockDataModel(
                                    thisContext.getString(R.string.stock_into_table_change),
                                    currentStock.getChange() + " (" + currentStock.getChangePercent() + "%)"
                            ));
                            //TODO TimeStamp
                            currentStockInfoList.add(new CurrentStockDataModel(
                                    thisContext.getString(R.string.stock_into_table_open),
                                    currentStock.getOpen()
                            ));
                            currentStockInfoList.add(new CurrentStockDataModel(
                                    thisContext.getString(R.string.stock_into_table_close),
                                    currentStock.getClose()
                            ));
                            currentStockInfoList.add(new CurrentStockDataModel(
                                    thisContext.getString(R.string.stock_into_table_day_range),
                                    currentStock.getDaysRange()
                            ));
                            currentStockInfoList.add(new CurrentStockDataModel(
                                    thisContext.getString(R.string.stock_into_table_volume),
                                    currentStock.getVolume()
                            ));

                            //displayIndicatorChart(currentStock, view, indicatorsWebView, "Price");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //System.out.println(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, 1));
        SingletonVolleyQueue.getInstance(thisContext).addToRequestQueue(jsonObjectRequest);
    }

    private String getTime(String ticker, final View view, final ListView currentStockListView) {
        final Context thisContext = this.getContext();
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                getString(R.string.aws_common) + ticker + getString(R.string.time_value),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        currentStockInfoList.add(3, new CurrentStockDataModel(
                                getString(R.string.stock_into_table_timestamp),
                                response.toString()
                        ));
                        view.findViewById(R.id.stock_info_progress_bar).setVisibility(View.GONE);
                        currentStockListView.setAdapter(new CurrentStockListAdapter(currentStockInfoList, thisContext));
                        view.findViewById(R.id.stock_info_table).setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());
                    }
                });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, 1));
        SingletonVolleyQueue.getInstance(thisContext).addToRequestQueue(stringRequest);
        return null;
    }

    private void displayIndicatorChart(final CurrentStockFragment.CurrentStock currentStock, final View view, final WebView indicatorsWebView, final String s) {
        final Context thisContext = this.getContext();

        indicatorsWebView.setVisibility(View.GONE);
        view.findViewById(R.id.indicators_progress_bar).setVisibility(View.VISIBLE);
        switch (s) {
            case "Price":
                try {
                    PriceVolumeWebInterface priceVolumeWebInterface = new PriceVolumeWebInterface(thisContext, priceArray, volumeArray, currentStock.getSymbol());
                    indicatorsWebView.addJavascriptInterface(priceVolumeWebInterface, "Android");
                    indicatorsWebView.loadData(UtilMethods.readInputStream(
                            getResources().getAssets().open("price_volume_indicator_chart.html")),
                            "text/html", "UTF-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                        thisContext.getString(R.string.aws_common)
                                + thisContext.getString(R.string.stock_info_url)
                                + currentStock.getSymbol() + "/" + s.toLowerCase(), null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String title = (String) response.getJSONObject(thisContext.getString(R.string.alpha_vantage_meta_data))
                                            .get(thisContext.getString(R.string.alpha_vantage_indicator_name));

                                    JSONObject dataFull = response.getJSONObject(
                                            thisContext.getString(R.string.alpha_vantage_technical_analysis) + " " + s);
                                    int cnt = 0;

                                    if (s.equals("STOCH")) {
                                        stochSlowD = new JSONArray();
                                        stochSlowK = new JSONArray();
                                    } else if (s.equals("BBANDS")) {
                                        bbandsLower = new JSONArray();
                                        bbandsMiddle = new JSONArray();
                                        bbandsUpper = new JSONArray();
                                    } else if (s.equals("MACD")) {
                                        macd = new JSONArray();
                                        macdHistory = new JSONArray();
                                        macdSignal = new JSONArray();
                                    } else {
                                        dataArray = new JSONArray();
                                    }

                                    Iterator<String> dataFullItr = dataFull.keys();
                                    while (cnt < 130 && dataFullItr.hasNext()) {
                                        String date = dataFullItr.next();
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                        try {
                                            if (s.equals("STOCH")) {
                                                stochSlowD.put(new JSONObject().put(String.valueOf(simpleDateFormat.parse(date).getTime()),
                                                        Float.parseFloat(dataFull.getJSONObject(date).get(thisContext.getString(R.string.alpha_vantage_stoch_slowD)).toString())));
                                                stochSlowK.put(new JSONObject().put(String.valueOf(simpleDateFormat.parse(date).getTime()),
                                                        Float.parseFloat(dataFull.getJSONObject(date).get(thisContext.getString(R.string.alpha_vantage_stoch_slowK)).toString())));
                                            } else if (s.equals("BBANDS")) {
                                                bbandsUpper.put(new JSONObject().put(String.valueOf(simpleDateFormat.parse(date).getTime()),
                                                        Float.parseFloat(dataFull.getJSONObject(date).get(thisContext.getString(R.string.alpha_vantage_bbands_real_upper_band)).toString())));
                                                bbandsMiddle.put(new JSONObject().put(String.valueOf(simpleDateFormat.parse(date).getTime()),
                                                        Float.parseFloat(dataFull.getJSONObject(date).get(thisContext.getString(R.string.alpha_vantage_bbands_real_middle_band)).toString())));
                                                bbandsLower.put(new JSONObject().put(String.valueOf(simpleDateFormat.parse(date).getTime()),
                                                        Float.parseFloat(dataFull.getJSONObject(date).get(thisContext.getString(R.string.alpha_vantage_bbands_real_lower_band)).toString())));
                                            } else if (s.equals("MACD")) {
                                                macdSignal.put(new JSONObject().put(String.valueOf(simpleDateFormat.parse(date).getTime()),
                                                        Float.parseFloat(dataFull.getJSONObject(date).get(thisContext.getString(R.string.alpha_vantage_macd_signal)).toString())));
                                                macdHistory.put(new JSONObject().put(String.valueOf(simpleDateFormat.parse(date).getTime()),
                                                        Float.parseFloat(dataFull.getJSONObject(date).get(thisContext.getString(R.string.alpha_vantage_macd_history)).toString())));
                                                macd.put(new JSONObject().put(String.valueOf(simpleDateFormat.parse(date).getTime()),
                                                        Float.parseFloat(dataFull.getJSONObject(date).get(thisContext.getString(R.string.alpha_vantage_macd)).toString())));
                                            } else {
                                                dataArray.put(new JSONObject().put(String.valueOf(simpleDateFormat.parse(date).getTime()),
                                                        Float.parseFloat(dataFull.getJSONObject(date).get(s).toString())));
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        cnt++;
                                    }
                                    if (s.equals("STOCH")) {
                                        indicatorsWebView.addJavascriptInterface(new StochWebInterface(thisContext, stochSlowD, stochSlowK, currentStock.getSymbol(), title, s), "Android");
                                        indicatorsWebView.loadData(UtilMethods.readInputStream(
                                                getResources().getAssets().open("stoch_indicator_chart.html")), "text/html", "UTF-8");
                                    } else if (s.equals("BBANDS")) {
                                        indicatorsWebView.addJavascriptInterface(new BbandsWebInterface(thisContext, bbandsUpper, bbandsMiddle, bbandsLower, currentStock.getSymbol(), title, s), "Android");
                                        indicatorsWebView.loadData(UtilMethods.readInputStream(
                                                getResources().getAssets().open("bbands_indicator_chart.html")), "text/html", "UTF-8");
                                    } else if (s.equals("MACD")) {
                                        indicatorsWebView.addJavascriptInterface(new MacdWebInterface(thisContext, macdSignal, macdHistory, macd, currentStock.getSymbol(), title, s), "Android");
                                        indicatorsWebView.loadData(UtilMethods.readInputStream(
                                                getResources().getAssets().open("macd_indicator_chart.html")), "text/html", "UTF-8");
                                    } else {
                                        indicatorsWebView.addJavascriptInterface(new GenericWebInterface(thisContext, dataArray, currentStock.getSymbol(), title, s), "Android");
                                        indicatorsWebView.loadData(UtilMethods.readInputStream(
                                                getResources().getAssets().open("indicator_chart.html")), "text/html", "UTF-8");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());
                    }
                });
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, 1));
                SingletonVolleyQueue.getInstance(thisContext).addToRequestQueue(jsonObjectRequest);
                break;
        }
        view.findViewById(R.id.indicators_progress_bar).setVisibility(View.GONE);
        indicatorsWebView.setVisibility(View.VISIBLE);
    }
}
