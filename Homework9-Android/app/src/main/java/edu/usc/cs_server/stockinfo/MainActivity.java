package edu.usc.cs_server.stockinfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import edu.usc.cs_server.stockinfo.model.FavoriteListDataModel;
import edu.usc.cs_server.stockinfo.util.SingletonVolleyQueue;
import edu.usc.cs_server.stockinfo.util.adapter.FavoritesListAdapter;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> suggestionsList;
    private AutoCompleteTextView autoCompleteTextView;
    public static List<FavoriteListDataModel> FAVORITE_LIST;
    private FavoritesListAdapter favoritesListAdapter;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        gson = new Gson();

        //Auto Complete
        autoCompleteTextView = findViewById(R.id.auto_complete);
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do Nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                loadAutoCompleteSuggestions(autoCompleteTextView.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Do Nothing
            }
        });

        //Get Quote
        TextView getQuoteTextView = findViewById(R.id.get_quote_button);
        getQuoteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Input Validation
                if (autoCompleteTextView.getText().toString().trim().length() > 0) {
                    //Switch over to other activity
                    //Get Stock ticker symbol
                    String tickerParts[] = autoCompleteTextView.getText().toString().split(" - ");
                    Intent intent = new Intent(MainActivity.this, StockInfoActivity.class);
                    intent.putExtra(getString(R.string.stock_ticker), tickerParts[0]);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.get_quote_validation_fail_toast,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Clear
        TextView clearTextView = findViewById(R.id.clear_button);
        clearTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoCompleteTextView.setText("");
            }
        });

        //Favorites List
        ListView favoriteListView = findViewById(R.id.favorites_list_view);
        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        FAVORITE_LIST = gson.fromJson(sharedPreferences.getString(getString(R.string.favorite_list_key), ""),
                new TypeToken<List<FavoriteListDataModel>>() {
                }.getType());
        if (null == FAVORITE_LIST) {
            FAVORITE_LIST = new ArrayList<>();
        }
        if (!FAVORITE_LIST.isEmpty()) {
            //Display the list
            favoritesListAdapter = new FavoritesListAdapter(this);
            favoriteListView.setAdapter(favoritesListAdapter);
            registerForContextMenu(favoriteListView);
            favoriteListView.setVisibility(View.VISIBLE);
        } else {
            favoriteListView.setVisibility(View.GONE);
        }

        //Sort By and Order
        final Spinner sortBySpinner = findViewById(R.id.sort_by_spinner);
        ArrayAdapter sortBySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_by_array, android.R.layout.simple_spinner_item);
        sortBySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortBySpinner.setAdapter(sortBySpinnerAdapter);

        final Spinner orderSpinner = findViewById(R.id.order_spinner);
        ArrayAdapter orderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.order_array, android.R.layout.simple_spinner_item);
        orderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(orderSpinnerAdapter);

        final String oldSortBySpinnerValue = sortBySpinner.getSelectedItem().toString();
        sortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String newSortBySpinnerValue = sortBySpinner.getSelectedItem().toString();
                if (oldSortBySpinnerValue != newSortBySpinnerValue) {
                    switch (newSortBySpinnerValue) {
                        case "Symbol":
                            Collections.sort(FAVORITE_LIST, FavoriteListDataModel.tickerComparator);
                            favoritesListAdapter.notifyDataSetChanged();
                            orderSpinner.setSelection(1);
                            break;
                        case "Price":
                            Collections.sort(FAVORITE_LIST, FavoriteListDataModel.priceComparator);
                            favoritesListAdapter.notifyDataSetChanged();
                            orderSpinner.setSelection(1);
                            break;
                        case "Change":
                            Collections.sort(FAVORITE_LIST, FavoriteListDataModel.changeComparator);
                            favoritesListAdapter.notifyDataSetChanged();
                            orderSpinner.setSelection(1);
                            break;
                        case "Change Percent":
                            Collections.sort(FAVORITE_LIST, FavoriteListDataModel.changePercentComparator);
                            favoritesListAdapter.notifyDataSetChanged();
                            orderSpinner.setSelection(1);
                            break;
                        default:
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String orderSpinnerValue = orderSpinner.getSelectedItem().toString();
                String sortBySpinnerValue = sortBySpinner.getSelectedItem().toString();
                Comparator<FavoriteListDataModel> comparatorVal = null;
                switch (sortBySpinnerValue) {
                    case "Symbol":
                        comparatorVal = FavoriteListDataModel.tickerComparator;
                        break;
                    case "Price":
                        comparatorVal = FavoriteListDataModel.priceComparator;
                        break;
                    case "Change":
                        comparatorVal = FavoriteListDataModel.changeComparator;
                        break;
                    case "Change Percent":
                        comparatorVal = FavoriteListDataModel.changePercentComparator;
                        break;
                }
                switch (orderSpinnerValue) {
                    case "Ascending":
                        Collections.sort(FAVORITE_LIST, comparatorVal);
                        favoritesListAdapter.notifyDataSetChanged();
                        break;
                    case "Descending":
                        Collections.sort(FAVORITE_LIST, comparatorVal);
                        Collections.reverse(FAVORITE_LIST);
                        favoritesListAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Refresh button
        Button refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFavoriteList();
            }
        });

        //Auto Refresh switch
        final Switch autoRefreshSwitch = findViewById(R.id.auto_refresh_switch);
        autoRefreshSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (autoRefreshSwitch.isChecked()) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateFavoriteList();
                            handler.postDelayed(this, 5000);
                        }
                    }, 5000);
                }
            }
        });
    }

    private void updateFavoriteList() {
        ProgressBar refreshProgressBar = findViewById(R.id.refresh_progress_bar);
        refreshProgressBar.setVisibility(View.VISIBLE);
        for (final FavoriteListDataModel favoriteStock : FAVORITE_LIST) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    getString(R.string.aws_common)
                            + getString(R.string.stock_info_url)
                            + favoriteStock.getTicker() + getString(R.string.price_volume_function_value),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject priceVolumeFull = response.getJSONObject(
                                        getString(R.string.alpha_vantage_time_series_daily));
                                int cnt = 0;
                                JSONObject lastDay = null, previousDay;
                                Iterator<String> priceVolumeFullItr = priceVolumeFull.keys();
                                while (cnt < 2 && priceVolumeFullItr.hasNext()) {
                                    String date = priceVolumeFullItr.next();
                                    if (cnt == 0) {
                                        //last day
                                        lastDay = priceVolumeFull.getJSONObject(date);
                                        favoriteStock.setPrice(String.format("%.2f", Float.parseFloat(
                                                lastDay.get(getString(R.string.alpha_vantage_close)).toString())));
                                    } else if (cnt == 1) {
                                        //previous day
                                        previousDay = priceVolumeFull.getJSONObject(date);
                                        Float changeValue = Float.parseFloat(previousDay.get(getString(R.string.alpha_vantage_close)).toString())
                                                - Float.parseFloat(lastDay.get(getString(R.string.alpha_vantage_close)).toString());
                                        Float changePercent = changeValue
                                                / Float.parseFloat(previousDay.get(getString(R.string.alpha_vantage_close)).toString()) * 100;
                                        favoriteStock.setChange(String.format("%.2f", changeValue));
                                        favoriteStock.setChangePercent(String.format("%.2f", changePercent));
                                    }
                                    cnt++;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error);
                        }
                    }
            );
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, 1));
            SingletonVolleyQueue.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }
        favoritesListAdapter.notifyDataSetChanged();
        refreshProgressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.favorites_list_view) {
            menu.setHeaderTitle(getString(R.string.choice_header));
            AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
            FavoriteListDataModel favoriteListDataModel = (FavoriteListDataModel)
                    ((ListView) v).getItemAtPosition(adapterContextMenuInfo.position);
            String menuItems[] = getResources().getStringArray(R.array.choice_array);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String menuItems[] = getResources().getStringArray(R.array.choice_array);
        String menuItemName = menuItems[menuItemIndex];
        FavoriteListDataModel favoriteListDataModel = FAVORITE_LIST.get(adapterContextMenuInfo.position);

        if (menuItemName.equalsIgnoreCase("Yes")) {
            FAVORITE_LIST.remove(favoriteListDataModel);
            favoritesListAdapter.notifyDataSetChanged();
            // Commit the list to Shared Preferences
            sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.favorite_list_key), gson.toJson(FAVORITE_LIST));
            editor.commit();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadAutoCompleteSuggestions(final String text) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, getString(R.string.aws_common) + getString(R.string.auto_complete_url) + text,
                        null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        suggestionsList = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            suggestionsList.add(
                                    response.optJSONObject(i).optString("Symbol") + " - "
                                            + response.optJSONObject(i).optString("Name") + " ("
                                            + response.optJSONObject(i).optString("Exchange") + ")"
                            );
                        }
                        autoCompleteTextView.setThreshold(1);
                        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                suggestionsList.toArray(new String[suggestionsList.size()]));
                        autoCompleteTextView.setAdapter(stringArrayAdapter);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());
                    }
                });
        SingletonVolleyQueue.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
}
