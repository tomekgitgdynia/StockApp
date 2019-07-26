package com.tomk.android.stockapp.mainActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import com.tomk.android.stockapp.models.ChartItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom Kowszun on 6/1/2018.
 */
public class GraphChartView extends View {

    public static int BAR_GRAPH = 1;
    public static int LINE_GRAPH = 2;
    public static int POINT_GRAPH = 3;
    public static int FILL_GRAPH = 4;
    private RectF graphBounds;
    private Paint graphPaint;
    private Paint indicatorPaint;
    private List<ChartItem> chartItems = new ArrayList<ChartItem>();
    private ArrayList<ArrayList<ChartItem>> indicatorsChartItems = new ArrayList<>();
    private int diagramType = FILL_GRAPH;
    private static final String TAG = "GraphChartView";

    private int timeSeriesStrokeWidth = 1;
    private int techIndicatorStrokeWidth = 2;

    /**
     * Construct a GraphChartView
     *
     * @param context
     */
    public GraphChartView(Context context, List<ChartItem> chartItems, ArrayList<ArrayList<ChartItem>> indicatorsChartItems) {
        super(context);
        this.chartItems = chartItems;
        this.indicatorsChartItems = indicatorsChartItems;
        graphPaint = new Paint();
        graphPaint.setAlpha(150);
        graphPaint.setStrokeWidth(timeSeriesStrokeWidth);

        indicatorPaint = new Paint();
        indicatorPaint.setAlpha(10);
        indicatorPaint.setStrokeWidth(techIndicatorStrokeWidth);

    }


    // draw the actual graph from the data points
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawIndicatorItems(canvas);
        drawTimeItems(canvas);
    }

    private void drawIndicatorItems(Canvas canvas) {
        for (ArrayList<ChartItem> indicatorsItems : indicatorsChartItems) {
            for (ChartItem item : indicatorsItems) {
                if (indicatorsItems != null && indicatorsItems.size() > 0) {

                    double oldTop = indicatorsItems.get(0).top;
                    double oldLeft = indicatorsItems.get(0).left;
                    int cnt = 0;
                    for (ChartItem indicatorItem : indicatorsItems) {
                        indicatorPaint.setColor(indicatorItem.color);
                        if (diagramType == LINE_GRAPH || diagramType == FILL_GRAPH) {
                            if (cnt == 0) {
                                canvas.drawPoint((float)indicatorItem.left, (float) indicatorItem.top, indicatorPaint);
                            } else {
                                canvas.drawLine((float)oldLeft, (float)oldTop, (float)indicatorItem.left, (float)indicatorItem.top, indicatorPaint);
                            }
                            oldTop = indicatorItem.top;
                            oldLeft = indicatorItem.left;

                        } else if (diagramType == POINT_GRAPH) {
                            if (cnt == chartItems.size() - 1) {
                                indicatorItem.color = Color.RED;
                            } else if (cnt == 0) {
                                indicatorItem.color = Color.BLUE;
                            } else {
                                indicatorPaint.setColor(indicatorItem.color);
                            }
                            indicatorPaint.setColor(indicatorItem.color);
                            canvas.drawPoint((float)indicatorItem.left, (float)indicatorItem.top, indicatorPaint);
                        }
                        cnt++;
                    }
                }
            }
        }
    }

    private void drawTimeItems(Canvas canvas) {
        if (chartItems != null && chartItems.size() > 0) {
            double oldTop = this.chartItems.get(0).top;
            double oldLeft = this.chartItems.get(0).left;

//            Paint clearPaint = new Paint();
//            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), clearPaint);

            if (diagramType == FILL_GRAPH) {
                drawPoly(canvas, chartItems);

                int cnt = 0;
                for (ChartItem it : chartItems) {
                    graphPaint.setColor(it.color);
                    canvas.drawLine((float)oldLeft, (float)oldTop, (float)it.left, (float)it.top, graphPaint);
                    cnt++;
                    oldTop = it.top;
                    oldLeft = it.left;
                }
            } else {
                int cnt = 0;


                for (ChartItem it : chartItems) {
                    graphPaint.setColor(it.color);
                    if (diagramType == BAR_GRAPH) {
                        canvas.drawRect((float)it.left, (float)it.top, (float)it.right, (float)it.bottom, graphPaint);

                    } else if (diagramType == LINE_GRAPH) {

                        graphPaint.setColor(it.color);
                        if (cnt == 0) {
                            canvas.drawPoint((float)it.left, (float)it.top, graphPaint);
                        } else {
                            canvas.drawLine((float)oldLeft, (float)oldTop, (float)it.left, (float)it.top, graphPaint);
                        }
                        oldTop = it.top;
                        oldLeft = it.left;

                    } else {
                        graphPaint.setColor(it.color);
                        canvas.drawPoint((float)it.left, (float)it.top, graphPaint);
                    }
                    cnt++;
                }
            }
        }
    }

    /**
     * @param canvas The canvas to draw on
     */
    private void drawPoly(Canvas canvas, List<ChartItem> chartItems) {

        if (chartItems.size() < 2) {
            return;
        }

        int lastIndex = chartItems.size() - 1;

        double graphLeft = chartItems.get(0).left;
        double graphBottom = graphBounds.bottom;
        double graphRight = chartItems.get(lastIndex).right;

        // paint
        Paint polyPaint = new Paint();
        polyPaint.setARGB(120, 200, 200, 200);
        polyPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // path
        Path polyPath = new Path();
        polyPath.moveTo((float)graphLeft, (float)graphBottom);
        int i, len;
        len = chartItems.size();
        for (i = 0; i < len; i++) {
            polyPath.lineTo((float)chartItems.get(i).left, (float)chartItems.get(i).top);
        }
        polyPath.lineTo((float)chartItems.get(i - 1).left, (float)graphBottom);

        // draw
        canvas.drawPath(polyPath, polyPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        h = h - GraphChart.boxLineThicknessStat / 2;
        graphBounds = new RectF(0, 0, w, h);
    }

    // private ArrayList<ArrayList<ChartItem>> indicatorsChartItems =  new ArrayList<>();
    public void updateData(List<ChartItem> chartItems, ArrayList<ArrayList<ChartItem>> indicatorsChartItems) {
        this.chartItems = chartItems;
        this.indicatorsChartItems = indicatorsChartItems;
    }

    public void setDiagramType(int diagramType) {
        this.diagramType = diagramType;
    }


    public int getDiagramType() {
        return diagramType;
    }
}