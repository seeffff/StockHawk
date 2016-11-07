package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private LineSet dataSet = new LineSet();
    @BindView(R.id.linechart)
    LineChartView lineChart;
    @BindView(R.id.detail_symbol)
    TextView detailSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        ButterKnife.bind(this);

        Intent i = getIntent();
        String symbol = i.getStringExtra("Symbol");
        Bundle args = new Bundle();
        args.putString("Symbol", symbol);

        detailSymbol.setText(symbol + " " + getResources().getString(R.string.detail_text));
        getLoaderManager().initLoader(0, args, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{bundle.getString("Symbol")},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;
        ArrayList<Float> floats = new ArrayList<>();

        for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            floats.add(Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE))));
        }

        ArrayList<Integer> ints = convertToInt(floats);

        for (int i = 0; i < floats.size(); i++){
            float price = floats.get(i);
            dataSet.addPoint("", price);
        }

        dataSet.setColor(getResources().getColor(R.color.material_blue_500))
                .setDotsStrokeThickness(Tools.fromDpToPx(2))
                .setDotsStrokeColor(getResources().getColor(R.color.material_blue_600))
                .setDotsColor(getResources().getColor(R.color.material_blue_600));
        lineChart.addData(dataSet);
        lineChart.setBorderSpacing(1)
                .setAxisBorderValues(getMin(ints), getMax(ints), getStep(getMin(ints), getMax(ints)))
                .setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsColor(getResources().getColor(R.color.white));
        lineChart.show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public ArrayList<Integer> convertToInt(ArrayList<Float> floats){
        ArrayList<Integer> ints = new ArrayList<>();
        for(int i = 0; i < floats.size(); i++){
            ints.add(i, Math.round(floats.get(i)));
        }
        return ints;
    }

    public int getMax(ArrayList<Integer> ints){
        int max = 0;
        for(int i = 0; i < ints.size(); i++) {
            int number = ints.get(i);
            if(number > max) max = number;
        }
        return max + 5;
    }

    public int getMin(ArrayList<Integer> ints){
        int min = 999999;
        for(int i = 0; i < ints.size(); i++){
            int number = ints.get(i);
            if(number < min) min = number;
        }
        return min - 5;
    }

    public int getStep(int min, int max){
        int distance = max - min;
        int step;
        ArrayList<Integer> ints = new ArrayList<>();

        for(int i = 1; i < distance; i++){
            if(distance % i == 0){
                ints.add(distance/i);
            }
        }

        if(ints.size() > 1){
            if(ints.size() % 2 == 0){
                step = ints.get((ints.size()/2) + 1);
            }else{
                step = ints.get(ints.size()/2);
            }
        }else{
            step = 1;
        }

        return step;
    }
    
}
