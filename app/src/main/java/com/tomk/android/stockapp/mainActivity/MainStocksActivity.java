package com.tomk.android.stockapp.mainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tomk.android.stockapp.ListOfStocksDbAsyncLoader;
import com.tomk.android.stockapp.R;
import com.tomk.android.stockapp.StockDbAdapter;
import com.tomk.android.stockapp.StockDbAsyncLoader;
import com.tomk.android.stockapp.StockListItem;
import com.tomk.android.stockapp.WebAccess.GetStockIntentService;
import com.tomk.android.stockapp.models.Repository.DataRepository;
import com.tomk.android.stockapp.models.StockResponse;

import java.util.ArrayList;


public class MainStocksActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        android.support.v4.app.LoaderManager.LoaderCallbacks {


    public static final String T_STANDARD = "standard";
    public static final String T_EMA = "EMA";
    public static final String T_WMA = "WMA";
    public static final String T_DEMA = "DEMA";
    public static final String T_TRIMA = "TRIMA";
    public static final String T_KAMA = "KAMA";
    public static final String T_MAMA = "MAMA";

    public static final String TIME_SERIES_INTRADAY = "TIME_SERIES_INTRADAY";
    public static final String TIME_SERIES_DAILY = "TIME_SERIES_DAILY";
    public static final String TIME_SERIES_DAILY_ADJUSTED = "TIME_SERIES_DAILY_ADJUSTED";

    public static final String STOCK_NOT_FOUND = "Stock Not Found";
    public static final String HIGH_USAGE = "High usage";
    public static final String ERROR_CONNECTING = "Error Connecting";
    public static final String NO_WIFI = "No WiFi";
    public static final String NO_ERRORS = "No Errors";

    public static final String STOCK_SYMBOL = "stockSymbol";
    public static final String STOCK_NAME = "stockName";
    public static final String INTERVAL = "interval";
    public static final String TIME_PERIOD = "timePeriod";
    public static final String SERIES_TYPE = "seriesType";
    public static final String API_KEY_OBTAINED = "apiKeyObtained";

    private static final String TAG = "MainStocksActivity";
    private static final String DEFAULT_STOCK = "IBM";

    private ArrayList<String> listOfRequestedGraphTypes = new ArrayList<>();

    private static final int STOCK_LOADER_ID = 1;
    private static final int LIST_LOADER_ID = 2;
    private StockReceiver stockReceiver = new StockReceiver();
    private StockDbAsyncLoader stockDbAsyncLoader;
    private ListOfStocksDbAsyncLoader listOfStocksDbAsyncLoader;

    public boolean blocking = false;
    public String returnedStockSymbol = " ";

    private StockDbAdapter stockDbAdapter;

    public static StockResponse stocksResponse;
    public static ArrayList<StockListItem> listResponse;


    private RecyclerView recyclerView;
    private StockListRVadapter stockListRVadapter;

    private static int oldConfigInt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_stocks);

        listOfRequestedGraphTypes.add(T_STANDARD);
//        listOfRequestedGraphTypes.add(T_EMA);
//        listOfRequestedGraphTypes.add(T_WMA);
//        listOfRequestedGraphTypes.add(T_DEMA);

        if ((oldConfigInt & ActivityInfo.CONFIG_ORIENTATION) == ActivityInfo.CONFIG_ORIENTATION) {
            // Orientation changed
            if (savedInstanceState != null) {
                // Orientation changed and
                // Data in saved instance state
            } else {
                // Orientation changed. No data in saved instance state
                // If no rotation, and/or no data, that means we just started/restarted
            }
        } else {
            if (savedInstanceState != null) {
                // Orientation not changed. First run or restarted by system. Data in saved instance state
            } else {
                // If no rotation, and/or no data, that means we just started/restarted
                // Orientation not changed. First run or restarted by system. No data in saved instance state
            }
        }

        stockDbAdapter = new StockDbAdapter(this.getApplicationContext());
        stockDbAdapter.open();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


//        this.onItemSelected(DEFAULT_STOCK);


        // Attempt is made to load data from DB regardless of any possible orientation changes
        // or restarts, etc.


        stockListRVadapter = new StockListRVadapter(listResponse);

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setAdapter(stockListRVadapter);
        RecyclerView.LayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setHasFixedSize(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        loadDataFromDB();

    }

    private void loadDataFromDB() {

        // Initiate the load of the data from DB to draw an individual single stock graph
//        getLoaderManager().destroyLoader(STOCK_LOADER_ID);
//        stockDbAsyncLoader = (StockDbAsyncLoader) getSupportLoaderManager().initLoader(STOCK_LOADER_ID, null, MainStocksActivity.this);
//        stockDbAsyncLoader.forceLoad();

        // Initiate the load of list of stocks from the DB
        getLoaderManager().destroyLoader(LIST_LOADER_ID);
        listOfStocksDbAsyncLoader = (ListOfStocksDbAsyncLoader) getSupportLoaderManager().initLoader(LIST_LOADER_ID, null, MainStocksActivity.this);
        listOfStocksDbAsyncLoader.forceLoad();
    }

    public void onItemSelected(String stockSymbol, String stockName) {

        if (blocking == false) {
            blocking = true;
            Intent getSchoolListIntServ = new Intent(getApplicationContext(), GetStockIntentService.class);
            getSchoolListIntServ.putExtra(STOCK_SYMBOL, stockSymbol);
            getSchoolListIntServ.putExtra(STOCK_NAME, stockName);
            getSchoolListIntServ.putExtra(INTERVAL, "1min");
            getSchoolListIntServ.putExtra(TIME_PERIOD, "10");
            getSchoolListIntServ.putExtra(SERIES_TYPE, "open");
            getSchoolListIntServ.putExtra(API_KEY_OBTAINED, "UKY832CIXXPKWVJV");
            getSchoolListIntServ.putStringArrayListExtra("typeList", listOfRequestedGraphTypes);
            startService(getSchoolListIntServ);
        } else {
            Log.d(TAG, " >>>>>>>>>>>>>>>>> BLOCKING !!! <<<<<<<<<<<<<< ");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(stockReceiver, new IntentFilter(GetStockIntentService.STOCK_DATA_ACTION));
        blocking = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(stockReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_stocks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public android.support.v4.content.AsyncTaskLoader onCreateLoader(int id, Bundle args) {

        if (id == STOCK_LOADER_ID) {
            stockDbAsyncLoader = new StockDbAsyncLoader(this.getApplicationContext(), this.stockDbAdapter);
            return stockDbAsyncLoader;
        } else if (id == LIST_LOADER_ID) {
            listOfStocksDbAsyncLoader = new ListOfStocksDbAsyncLoader(this.getApplicationContext(), this.stockDbAdapter);
            return listOfStocksDbAsyncLoader;
        }

        return null;
    }


    // Data is loaded from database
    @Override
    public void onLoadFinished(android.support.v4.content.Loader loader, Object data) {

        stocksResponse = null;
        listResponse = null;

        //=========================================== Stock Graph ==========================================
        if (loader.getId() == STOCK_LOADER_ID) {
            stocksResponse = (StockResponse) data;
            if (stocksResponse != null && stocksResponse.getMetaData() != null) {

                Intent intent = StockDetailsActivity.newIntent(this, stocksResponse.getMetaData().getSymbol());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("min", stocksResponse.getMinLow());
                intent.putExtra("max", stocksResponse.getMaxHigh());
                startActivity(intent);
                finish();

                // Save the stock symbol so next time app is displayed it will display the last stock used instead of
                // a default stock or anything else
                Log.d(TAG, "Saving to SharedPreferences");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainStocksActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("stockNameKey", stocksResponse.getMetaData().getSymbol());
                editor.apply();
            } else {
                // There was no data in the DB, at this point query the web with current stock symbol
//                onItemSelected(MainStocksActivity.this.returnedStockSymbol);

            }
            // ========================================== List of Stocks ===========================================
        } else if (loader.getId() == LIST_LOADER_ID) {

            if(data != null || listResponse.size() > 0 )
            {
                listResponse = (ArrayList<StockListItem>) data;
                if(stockListRVadapter != null)
                {
                    stockListRVadapter.setItems(listResponse);
                    Log.d(TAG, "-------->>>>>>>>>>>> stockListRVadapter OK ");
                } else
                {
                    Log.d(TAG, "-------->>>>>>>>>>>> stockListRVadapter NOT ");
                }
            } else
            {
                Log.d(TAG, "-------->>>>>>>>>>>> No stocks list in db.  Loading from defaults ");
                DataRepository dbRep = new DataRepository();
            }

        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {


        super.onSaveInstanceState(outState);
        Log.d(TAG, "Saving data - instance state ");
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader loader) {

    }


    public class StockReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(GetStockIntentService.STOCK_DATA_ACTION)) {
                String itemCount = intent.getExtras().getString(GetStockIntentService.NUMBER_OF_ITEMS);
                String resultString = intent.getStringExtra(GetStockIntentService.RESULT_STRING);

                if (Integer.valueOf(itemCount) > 0 && resultString.equals(MainStocksActivity.NO_ERRORS)) {

                    getLoaderManager().destroyLoader(STOCK_LOADER_ID);
                    stockDbAsyncLoader = (StockDbAsyncLoader) getSupportLoaderManager().initLoader(STOCK_LOADER_ID, null, MainStocksActivity.this);
                    stockDbAsyncLoader.forceLoad();

//                    getLoaderManager().destroyLoader(LIST_LOADER_ID);
//                    listOfStocksDbAsyncLoader = (ListOfStocksDbAsyncLoader) getSupportLoaderManager().initLoader(LIST_LOADER_ID, null, MainStocksActivity.this);
//                    listOfStocksDbAsyncLoader.forceLoad();
                    Log.i(TAG, " >>>>>>>>>>>>>>>>> Received intent service data " + itemCount);

//                    MainStocksActivity.this.returnedStockSymbol = intent.getStringExtra("stock_symbol");
//                    returnedStockSymbolTextView.setText(MainStocksActivity.this.returnedStockSymbol);

                } else {

                    Toast errors = Toast.makeText(getApplicationContext(), resultString, Toast.LENGTH_LONG);
                    errors.show();
                }
            }
            blocking = false;
        }
    }


    // Adapter for the recycler view that displays the list items in the main activity window
    class StockListRVadapter extends RecyclerView.Adapter<StockListItemVH> {
        ArrayList<StockListItem> stockList;
        private StockListItemVH viewHolder;

        public StockListRVadapter(ArrayList<StockListItem> list) {

            this.stockList = list;
        }

        @Override
        public StockListItemVH onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stock, parent, false);
            return new StockListItemVH(view);

        }

        @Override
        public void onBindViewHolder(StockListItemVH holder, int position) {

            viewHolder = holder;
            String firstLetter = this.stockList.get(position).getStockName().substring(0, 1);
            viewHolder.ballTV.setText(firstLetter);
            viewHolder.ballTV.setBackgroundResource(R.drawable.list_icon);
            viewHolder.stockNameTV.setText(this.stockList.get(position).getStockName());
            viewHolder.stockSymbol = this.stockList.get(position).getStockSymbol();
            viewHolder.stockName = this.stockList.get(position).getStockName();
        }

        @Override
        public int getItemCount() {
            if(this.stockList != null)
            {
                return this.stockList.size();
            }
            return 0;
        }

        public void setItems(ArrayList<StockListItem> list) {
            this.stockList = list;
            notifyDataSetChanged();
        }

        public StockListItemVH getViewHolder() {
            return viewHolder;
        }
    }

    /**
     *
     *
     */
    private class StockListItemVH extends RecyclerView.ViewHolder {

        public Button ballTV;
        public TextView stockNameTV;
        public String stockSymbol;
        public String stockName;

        public StockListItemVH(View itemView) {
            super(itemView);

            stockNameTV = itemView.findViewById(R.id.stock_list_textview);
            stockNameTV.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    onItemSelected(stockSymbol, stockName);
                }
            });

            ballTV = itemView.findViewById(R.id.ball_textview);
            ballTV.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    onItemSelected(stockSymbol, stockName);
                }
            });
        }
    }

}

