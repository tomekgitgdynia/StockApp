package com.tomk.android.stockapp.mainActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.tomk.android.stockapp.R;
import com.tomk.android.stockapp.Util;
import com.tomk.android.stockapp.models.TimeSeriesItem;

import java.util.ListIterator;


public class StockDetailsActivity extends AppCompatActivity {

    private static final String STOCK_SYMBOL = "stockSymbol";
    private static final String STOCK_NAME = "stockName";
    private GraphChart graphChart;
    public static int graphDisplayType = GraphChartView.FILL_GRAPH;
    private TextView minLabel;
    private TextView minValue;

    private TextView maxLabel;
    private TextView maxValue;

    private TextView dateLBL;
    private TextView dateTV;

    private TextView stockSymbolAndName;

    double totalMax = Double.MIN_VALUE;
    double totalMin = Double.MAX_VALUE;
    double totalVolume = Double.MIN_VALUE;
    String date = "";
    double startingOpen = 0.0;
    double endingClose = 0.0;

    public static Intent newIntent(Context packageContext, String stockSymbol) {
        Intent intent = new Intent(packageContext, StockDetailsActivity.class);
        intent.putExtra(STOCK_SYMBOL, stockSymbol);
        return intent;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(StockDetailsActivity.this, MainStocksActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        String symbolName = MainStocksActivity.stocksResponse.getMetaData().getName() +
                " (" + MainStocksActivity.stocksResponse.getMetaData().getSymbol() + ")";
        stockSymbolAndName = findViewById(R.id.symbolName);
        stockSymbolAndName.setText(symbolName);

//        stockSymbolValue = findViewById(R.id.stockSymbolValue);
//        stockSymbolValue.setText(MainStocksActivity.stocksResponse.getMetaData().getSymbol());
//
//        stockNameLabel = findViewById(R.id.stockNameLabel);
//        stockNameLabel.setText("Stock name: ");
//
//        stockSymbolValue = findViewById(R.id.stockNameValue);
//        stockSymbolValue.setText(MainStocksActivity.stocksResponse.getMetaData().getName());

        //================
        graphChart = (GraphChart) findViewById(R.id.Graph);
        graphChart.init(MainStocksActivity.stocksResponse);

        calculateData();

        minLabel = findViewById(R.id.minLBL);
        minLabel.setText("Minimum value: ");

        minValue = findViewById(R.id.minValueTV);
        minValue.setText(Double.toString(totalMin));

        maxLabel = findViewById(R.id.maxLBL);
        maxLabel.setText("Maximum value: ");

        maxValue = findViewById(R.id.maxTV);
        maxValue.setText(Double.toString(totalMax));

        dateLBL = findViewById(R.id.dateLBL);
        dateLBL.setText("Last trade: ");

        dateTV = findViewById(R.id.dateTV);
        dateTV.setText(Util.dateToString(MainStocksActivity.stocksResponse.getMetaData().getLastRefreshedDate()));
    }

    private void calculateData()
    {
        int cnt = 0;
        ListIterator<TimeSeriesItem> iterator = MainStocksActivity.stocksResponse.getTimeSeriesItems().listIterator();
        while (iterator.hasNext()) {

            TimeSeriesItem item = iterator.next();
            if(cnt == 0) startingOpen = item.getOpen();
            if(item.getHigh() > totalMax) totalMax = item.getHigh();
            if(item.getLow() < totalMin) totalMin = item.getLow();
            totalVolume = totalVolume + item.getVolume();

            cnt++;
        }
        int lastIndex = MainStocksActivity.stocksResponse.getTimeSeriesItems().size();
        endingClose = MainStocksActivity.stocksResponse.getTimeSeriesItems().get(lastIndex - 1).getClose();
    }

}
