package com.tomk.android.stockapp.mainActivity;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;

import com.tomk.android.stockapp.R;
import com.tomk.android.stockapp.models.ChartItem;
import com.tomk.android.stockapp.models.StockResponse;
import com.tomk.android.stockapp.models.TimeSeriesItem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.tomk.android.stockapp.R.styleable.GraphChart_boxColor;


/**
 * Created by Tom Kowszun on 6/1/2018.
 */
public class GraphChart extends ViewGroup {

    public static double MaxValueDisplay = Double.MIN_VALUE;
    public static double MinValueDisplay = Double.MAX_VALUE;
    public static double MaxValueReal = Double.MIN_VALUE;
    public static int MaxDigitsAllowed = 6;
    public static int VertNumberOfTicks = 3;
    public static int boxLineThicknessStat = 2;

    private List<ChartItem> graphItems = new ArrayList<ChartItem>();
    private ArrayList<ArrayList<ChartItem>> indicatorsChartItems =  new ArrayList<>();
    private RectF graphChartBounds = new RectF();
    private RectF componentBounds = new RectF();

//    private Paint graphBackPaint;
    private Paint graphComponentBackPaint;
    private Paint boxPaint;
    private Paint tickLinePaint;
    private Paint textPaint;
    private Paint horizontalGridPaint;
    private int graphColor;
    private float labelTextSize;

    private int indicator1Color;
    private int indicator2Color;
    private int indicator3Color;

    private GraphChartView graphChartView;
    private AppCompatTextView graphWait;
    private int minimumWidth = 100;
    private int tickYaxisOffset = 0;
    private int textYaxisOffset = 20;

    private int approximateCharacterWidth = 13;
    float leftMargin = 10;
    float rightMargin = 20;
    float bottomMargin = 10;
    float topMargin = 0;
    float barWidth = 10;
    float minMarginSize = 20;
    int numLeftMinorTicks = 20;
    int numBottomTicks = 4;

    int tickThickness = 1;
    int boxLineThickness = boxLineThicknessStat;
    int ticLength = 10;
    int bottomTicHorizStartOffset = 0;
    int bottomTextVertOffset = ticLength + 5;
    private int lineOffset = 0; // affects location of Y axis and start of the X axis


    // Data used to plot graph
    public static final int USE_OPEN = 1;
    public static final int USE_CLOSE = 2;
    public static final int USE_HIGH = 3;
    public static final int USE_LOW = 4;
    public static final int USE_VOLUME = 5;
    public static int graphDataUsed = GraphChart.USE_OPEN;

    private static final String TAG = "GraphChart";

    private Context context;


    /**
     * Custom component that shows
     * a chart graph.
     */
    public GraphChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        textPaint = new Paint();
        int spSize = 9;
        float scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spSize, context.getResources().getDisplayMetrics());
        textPaint.setTextSize(scaledSizeInPixels);


//        TypedArray styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.MyTextViewStyleable);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.GraphChart);
        try {

            leftMargin = attributes.getDimension(R.styleable.GraphChart_leftMargin, 20);
            topMargin = attributes.getDimension(R.styleable.GraphChart_topMargin, 20);
            rightMargin = attributes.getDimension(R.styleable.GraphChart_rightMargin, 20);
            bottomMargin = attributes.getDimension(R.styleable.GraphChart_bottomMargin, 50);
            barWidth = attributes.getDimension(R.styleable.GraphChart_barWidth, 2);
            labelTextSize = attributes.getDimension(R.styleable.GraphChart_textSize, 20f);

            graphComponentBackPaint = new Paint();
            graphComponentBackPaint.setColor(attributes.getColor(R.styleable.GraphChart_graphComponentBackColor, Color.LTGRAY));
            graphComponentBackPaint.setStyle(Paint.Style.FILL);

            graphColor = attributes.getColor(R.styleable.GraphChart_graphColor, Color.BLACK);

//            holder.text.setTextColor(Color.argb(0,200,0,0));
            indicator1Color = Color.argb(20,200,50,50);
            indicator2Color = Color.argb(50,50,200,50);
            indicator3Color = Color.argb(50,50,50,200);

//            graphBackPaint = new Paint();
//            graphBackPaint.setColor(attributes.getColor(R.styleable.GraphChart_graphBackColor, Color.WHITE));
//            graphBackPaint.setStyle(Paint.Style.FILL);

            boxPaint = new Paint();
            int boxColor = attributes.getColor(R.styleable.GraphChart_boxColor, Color.RED);
            boxPaint.setColor(boxColor);
            boxPaint.setStrokeWidth(boxLineThickness);
            boxPaint.setStyle(Paint.Style.STROKE);

            tickLinePaint = new Paint();
            tickLinePaint.setColor(attributes.getColor(GraphChart_boxColor, Color.GRAY));
            tickLinePaint.setStyle(Paint.Style.STROKE);

            horizontalGridPaint = new Paint();

            horizontalGridPaint.setColor(attributes.getColor(GraphChart_boxColor, Color.LTGRAY));
            horizontalGridPaint.setStyle(Paint.Style.STROKE);
//            horizontalGridPaint.setARGB(255, 0, 0, 0);
            horizontalGridPaint.setPathEffect(new DashPathEffect(new float[]{5, 2}, 0));

            textPaint = new Paint();
            textPaint.setColor(attributes.getColor(R.styleable.GraphChart_textColor, Color.BLACK));
            textPaint.setTextSize(labelTextSize);
            textPaint.setStyle(Paint.Style.FILL);
//

        } finally {
            attributes.recycle();
        }

    }

    public void init(StockResponse stockResponse) {


        if (stockResponse != null) {
            indicatorsChartItems = new ArrayList<>();
            graphItems = GraphChart.convertGraphData(stockResponse, indicatorsChartItems);
        }

        graphChartView = new GraphChartView(getContext(), graphItems, indicatorsChartItems);
        addView(graphChartView);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Do nothing. Do not call the superclass method--that would start a layout pass
        // on this view's children. GraphChart lays out its children in onSizeChanged().
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (graphItems != null && graphItems.size() > 0) {

            // draw entire component background
            canvas.drawRect(graphChartBounds, graphComponentBackPaint);

            // draw the graph background only
//            canvas.drawRect(graphChartBounds, boxPaint);


            // draw the bottom tic marks with numbers
            paintBottomNumbersAndTics(canvas, numBottomTicks);

            // draw the left side tic marks and major tic marks with numbers
            paintLeftNumbersAndTics(canvas, GraphChart.VertNumberOfTicks);

            // draw the graph box
            // that forms the border of the graph
            canvas.drawRect(graphChartBounds, boxPaint);
        }

    }


    private void paintLeftNumbersAndTics(Canvas canvas, int numOfTics) {

        double minDisp = MinValueDisplay;
        double maxDisp = MaxValueDisplay;

        double singleGraphStep = (graphChartBounds.height() - boxLineThickness) / (numOfTics - 1);
        double singleValueStep = (maxDisp - minDisp) / (numOfTics - 1);

        DecimalFormat df = new DecimalFormat("###.##");
//        Log.d(TAG, "================== singleValueStep = " + df.format(singleValueStep) + " unformatted " + singleValueStep + " minimum " + minDisp + " singleGraphStep is " + singleGraphStep + " maximum " + maxDisp + " num of ticks " + numOfTics);
//        Log.d(TAG, " \n -------------------------- singleGraphStep is " + singleGraphStep + " graphChartBounds.bottom " + graphChartBounds.bottom);

        double currentDisplayValue = maxDisp;
        double currentGraphHeight = graphChartBounds.height();
        Rect bounds = new Rect();


        // path
        Path polyPath = new Path();
        float pathX = (graphChartBounds.left - tickYaxisOffset - lineOffset + boxLineThickness / 2);
        float pathY = graphChartBounds.bottom - (float) (currentGraphHeight) + (boxLineThickness / 2);
        polyPath.moveTo(pathX, pathY);

        for (int i = 0; i < numOfTics; i++) {
            float yPosition = graphChartBounds.bottom - (float) (currentGraphHeight) + (boxLineThickness / 2);
            double displayValue = currentDisplayValue;

            // draw horizontal grid line
            pathY = yPosition;
            if (i != 0 && i != numOfTics - 1) {
                // draw a horizontal grid line
                pathX = graphChartBounds.right + tickYaxisOffset + lineOffset - boxLineThickness / 2;
                polyPath.lineTo(pathX, pathY);
                canvas.drawPath(polyPath, horizontalGridPaint);
            }

            // draw a single small tick
            // lineOffset is used so ticks can follow to the left with the Y axis and start of X axis
            canvas.drawLine((graphChartBounds.left - tickYaxisOffset - lineOffset - boxLineThickness / 2), (float) yPosition, graphChartBounds.left - ticLength - tickYaxisOffset - lineOffset - boxLineThickness / 2, (float) yPosition, tickLinePaint);

            // draw the left numbers
            textPaint.getTextBounds(df.format(displayValue), 0, df.format(displayValue).length(), bounds);
            canvas.drawText(df.format(displayValue), graphChartBounds.left - textYaxisOffset - lineOffset - bounds.width() - boxLineThickness / 2, (float) (yPosition + (bounds.height() / 2)), textPaint);

            currentGraphHeight = currentGraphHeight - singleGraphStep;
            currentDisplayValue = currentDisplayValue - singleValueStep;
            pathX = (graphChartBounds.left - tickYaxisOffset - lineOffset + boxLineThickness / 2);
            pathY = graphChartBounds.bottom - (float) (currentGraphHeight) + (boxLineThickness / 2);
            polyPath.moveTo(pathX, pathY);
        }
    }

    private void paintBottomNumbersAndTics(Canvas canvas, int numOfTics) {

        if (graphItems != null && graphItems.size() > 0) {
            int numberOfDataPoints = graphItems.size();
//            Log.d(TAG, " \n --------------->>> numberOfDataPoints  = " + numberOfDataPoints);
            leftMargin = String.valueOf(GraphChart.MaxValueDisplay).length() * approximateCharacterWidth;
            if (leftMargin < minMarginSize) leftMargin = minMarginSize;
            double xCoordScale = ((graphChartBounds.width() - boxLineThickness) / (numOfTics - 1));

            int ticStep = numberOfDataPoints / numOfTics;
            int yOrig = boxLineThickness / 2;
            int xOrigin = boxLineThickness / 2;
//            Log.d(TAG, " \n --------------->>> ticStep  = " + ticStep + " numOfTics " + numOfTics);
            Rect bounds = new Rect();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");//("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < numOfTics; i++) {

                // draw the bottom tick
                canvas.drawLine((float) ((graphChartBounds.left + xCoordScale * i) + bottomTicHorizStartOffset + xOrigin), (graphChartBounds.bottom + yOrig), (float) ((graphChartBounds.left + xCoordScale * i) + bottomTicHorizStartOffset + xOrigin), (graphChartBounds.bottom + ticLength + yOrig), tickLinePaint);
                String formattedDate = df.format(graphItems.get((int) (ticStep * i)).date);
                textPaint.getTextBounds(formattedDate, 0, formattedDate.length(), bounds);

                // draw the date or time
                canvas.drawText(formattedDate, (float) ((graphChartBounds.left + xCoordScale * i) + bottomTicHorizStartOffset - (bounds.width() / 2)) + xOrigin, (graphChartBounds.bottom + ticLength + bottomTextVertOffset + yOrig), textPaint);
            }
        }
    }

    /**
     * Do all of the recalculations needed when the data array changes.
     *
     * In this method the existing chart item values are recalculated
     * to reflect changed itemValue
     */
    private void recalculateGraph() {

        MaxValueDisplay = Double.MIN_VALUE;
        MinValueDisplay = Double.MAX_VALUE;
        MaxValueReal = Double.MIN_VALUE;

        if (graphItems == null) return;
        for (ChartItem item : graphItems) {
            if (item.itemValue > MaxValueDisplay) MaxValueDisplay = item.itemValue;
            if (item.itemValue < MinValueDisplay) MinValueDisplay = item.itemValue;
        }

        leftMargin = String.valueOf(GraphChart.MaxValueDisplay).length() * approximateCharacterWidth;
        if (leftMargin < minMarginSize)

            leftMargin = minMarginSize;

        graphChartBounds = new RectF(componentBounds.left + leftMargin, componentBounds.top + topMargin, componentBounds.right - rightMargin, componentBounds.bottom - bottomMargin);

        // Lay out the child view that actually draws the graph.
        graphChartView.layout((int) graphChartBounds.left, (int) graphChartBounds.top, (int) graphChartBounds.right, (int) graphChartBounds.bottom);

        int numberOfDataPoints = graphItems.size();
        double yOrigin = boxLineThickness / 2;
        int xOrigin = boxLineThickness / 2;

        double graphLineScaleFactor = (graphChartBounds.width() - boxLineThickness) / (numberOfDataPoints - 1);
        double valueScaleFactor = (graphChartBounds.height() - boxLineThickness) / (GraphChart.MaxValueDisplay - GraphChart.MinValueDisplay);

//        Log.d(TAG, " graphChartBounds.width() " + graphChartBounds.width() + " graphChartBounds.height() " + graphChartBounds.height());
        int cnt = 0;
        for (ChartItem item : graphItems) {
//            item.color = Color.RED;
            item.left = (float) (xOrigin + (graphLineScaleFactor * cnt));
            item.right = item.left + barWidth;
            item.top = graphChartBounds.height() - (float) ((valueScaleFactor * item.itemValue) - (valueScaleFactor * GraphChart.MinValueDisplay) + yOrigin);

            item.bottom = graphChartBounds.height();
            item.color = graphColor;
            cnt++;
//            Log.d(TAG, "-------------Item update " + " left " + item.left + " right " + item.right + " top " + item.top + " bottom " + item.bottom + " value " + item.itemValue);
        }
        recalculateIndicatorItems(indicatorsChartItems, xOrigin, yOrigin, graphLineScaleFactor, valueScaleFactor);
    }

    private void recalculateIndicatorItems(ArrayList<ArrayList<ChartItem>> indicatorsChartItems,
                                           int xOrigin, double yOrigin, double graphLineScaleFactor,
                                           double valueScaleFactor)
    {
        int indicatorColor = graphColor;
        int indicatorIndex = 0;
        for (ArrayList<ChartItem> indicatorsItem : indicatorsChartItems) {
            if(indicatorIndex == 0)
            {
                indicatorColor = indicator1Color;
            } else if (indicatorIndex == 1)
            {
                indicatorColor = indicator2Color;
            } else if(indicatorIndex == 2)
            {
                indicatorColor = indicator3Color;
            }
            int cnt = 0;
            for (ChartItem item : indicatorsItem) {

                item.color = indicatorColor;
                item.left = (float) (xOrigin + (graphLineScaleFactor * cnt));
                item.right = item.left + barWidth;
                item.top = graphChartBounds.height() - (float) ((valueScaleFactor * item.itemValue) - (valueScaleFactor * GraphChart.MinValueDisplay) + yOrigin);
                item.bottom = graphChartBounds.height();
                cnt++;

            }
            indicatorIndex++;
        }
    }

    //
    // Measurement functions. This example uses a simple heuristic: it assumes that
    // the graph chart should be at least as wide as its label.
    //
    @Override
    protected int getSuggestedMinimumWidth() {
        return minimumWidth * 2;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return minimumWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));
        int minh = (w - minimumWidth) + getPaddingBottom() + getPaddingTop();
        int h = Math.min(MeasureSpec.getSize(heightMeasureSpec), minh);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        float ww = (float) w - xpad;
        float hh = (float) h - ypad;
        componentBounds = new RectF(0.0f, 0.0f, ww, hh);
        componentBounds.offsetTo(getPaddingLeft(), getPaddingTop());

        leftMargin = String.valueOf(GraphChart.MaxValueDisplay).length() * approximateCharacterWidth;
        if (leftMargin < minMarginSize) leftMargin = minMarginSize;

        graphChartBounds = new RectF(componentBounds.left + leftMargin, componentBounds.top + topMargin, componentBounds.right - rightMargin, componentBounds.bottom - bottomMargin);

        // Lay out the child view that actually draws the graph.
        graphChartView.layout((int) graphChartBounds.left, (int) graphChartBounds.top, (int) graphChartBounds.right, (int) graphChartBounds.bottom);
        recalculateGraph();
    }

    public void newStockReceived(StockResponse stockResponse) {

        if (stockResponse != null && this.graphItems != null) {
            this.graphItems.clear();
            indicatorsChartItems.clear();
            this.graphItems = GraphChart.convertGraphData(stockResponse, indicatorsChartItems);
            graphChartView.updateData(this.graphItems, this.indicatorsChartItems);
            recalculateGraph();
            changeGraphType(this.graphChartView.getDiagramType());
            float xpad = (float) (getPaddingLeft() + getPaddingRight());
            float ypad = (float) (getPaddingTop() + getPaddingBottom());

            float ww = (float) this.getWidth() - xpad;
            float hh = (float) this.getHeight() - ypad;

            // Layout the child view that actually draws the graph.
            graphChartView.layout((int) graphChartBounds.left, (int) graphChartBounds.top, (int) graphChartBounds.right, (int) graphChartBounds.bottom);
            this.invalidate();
            graphChartView.invalidate();
        }

    }

    public void changeGraphType(int graphType) {


        graphChartView.setDiagramType(graphType);

        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        float ww = (float) this.getWidth() - xpad;
        float hh = (float) this.getHeight() - ypad;

        // Lay out the child view that actually draws the graph.
        graphChartView.layout((int) graphChartBounds.left, (int) graphChartBounds.top, (int) graphChartBounds.right, (int) graphChartBounds.bottom);
        this.invalidate();
        graphChartView.invalidate();
        this.postInvalidate();

    }

    //
    // Extracts individual data items from the stockResponse and converts them to ChartItems
    // that are then drawn directly in the onDraw() method of the GraphChartView class.
    //
    // TimeSeriesItem from stockResponse is converted into ChartItem
    //
    //
    public static List<ChartItem> convertGraphData(StockResponse stockResponse, ArrayList<ArrayList<ChartItem>> indicatorsChartItems) {
        List<ChartItem> chartItems = null;

        // Process the indicator data
        ArrayList<ArrayList<Double>> indicators = stockResponse.getIndicators();
        ArrayList<Double> indicator_1 = indicators.get(0);
        ArrayList<Double> indicator_2 = indicators.get(1);
        ArrayList<Double> indicator_3 = indicators.get(2);

        List<ChartItem> indChartItems1 = null;
        List<ChartItem> indChartItems2 = null;
        List<ChartItem> indChartItems3 = null;


        int chainCnt = 1;
        MaxValueDisplay = Double.MIN_VALUE;
        MinValueDisplay = Double.MAX_VALUE;
        MaxValueReal = MaxValueDisplay;

        int indIdx = 0;
        if (stockResponse.getTimeSeriesItems() != null && stockResponse.getTimeSeriesItems().size() > 0) {
            chartItems = new ArrayList<>();

            // create indicator item lists for three separate indicators
            indChartItems1 = new ArrayList<>();
            indChartItems2 = new ArrayList<>();
            indChartItems3 = new ArrayList<>();

            Iterator<TimeSeriesItem> timeSeriesIterator = stockResponse.getTimeSeriesItems().iterator();
            while (timeSeriesIterator.hasNext()) {
                TimeSeriesItem timeSeriesItem = timeSeriesIterator.next();
                ChartItem chartItem = new ChartItem();

                ChartItem indItem1 = new ChartItem();
                ChartItem indItem2 = new ChartItem();
                ChartItem indItem3 = new ChartItem();

                // switch statement with int data type
                switch (GraphChart.graphDataUsed) {
                    case StockResponse.GRAPH_LOW:
                        chartItem.itemValue = timeSeriesItem.getLow();
                        break;
                    case StockResponse.GRAPH_HIGH:
                        chartItem.itemValue = timeSeriesItem.getHigh();
                        break;
                    case StockResponse.GRAPH_OPEN:
                        chartItem.itemValue = timeSeriesItem.getOpen();
                        break;
                    case StockResponse.GRAPH_CLOSE:
                        chartItem.itemValue = timeSeriesItem.getClose();
                        break;
                    case StockResponse.GRAPH_VOLUME:
                        chartItem.itemValue = timeSeriesItem.getVolume();
                        break;
                    default:
                        chartItem.itemValue = timeSeriesItem.getLow();
                        break;
                }
                if (chartItem.itemValue > MaxValueDisplay) MaxValueDisplay = chartItem.itemValue;
                if (chartItem.itemValue < MinValueDisplay) MinValueDisplay = chartItem.itemValue;
                chartItem.date = (Date) timeSeriesItem.getDate().clone();
                chartItems.add(chartItem);

                indItem1.itemValue = indicator_1.get(indIdx);
                indItem2.itemValue = indicator_2.get(indIdx);
                indItem3.itemValue = indicator_3.get(indIdx);

                indItem1.date = (Date) timeSeriesItem.getDate().clone();
                indItem2.date = (Date) timeSeriesItem.getDate().clone();
                indItem3.date = (Date) timeSeriesItem.getDate().clone();

                indChartItems1.add(indItem1);
                indChartItems2.add(indItem2);
                indChartItems3.add(indItem3);


//                Log.d(TAG, "\n ********************** chartItem.date is " + chartItem.date.toString() + " chartItem.itemValue is " + chartItem.itemValue);
                indIdx++;
            }

            indicatorsChartItems.add((ArrayList<ChartItem>) indChartItems1);
            indicatorsChartItems.add((ArrayList<ChartItem>) indChartItems2);
            indicatorsChartItems.add((ArrayList<ChartItem>) indChartItems3);

        }

        MaxValueReal = MaxValueDisplay;
        double originalMaxValue = MaxValueDisplay;

        return chartItems;
    }

}
